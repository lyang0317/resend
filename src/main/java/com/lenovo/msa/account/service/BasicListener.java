package com.lenovo.msa.account.service;

import com.alibaba.fastjson.JSONObject;
import com.lenovo.msa.account.model.CommonMessageData;
import com.lenovo.msa.account.exception.ConsumeException;
import com.lenovo.msa.account.utils.SpringContextUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
public abstract class BasicListener {

    @Value("${ct.host:https://api154.crowdtwist.com}")
    public String ct_host;

    @Resource(name = "RestTemplate4Mq")
    private RestTemplate restTemplate;

    @Autowired
    private PersistenceHandler persistenceHandler;

    protected void handleMessage(CommonMessageData data, Channel channel, MessageProperties properties) {
        if (data == null || data.getRequest() == null) {
            log.warn("The input data is empty, and the deliver tag is: {}", properties.getDeliveryTag());
            return;
        }

        CommonMessageData.RequestData request = data.getRequest();
        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(request.getBody(), request.getHeader());
            log.info("Start to request CT with Url:{},Header:{} and Body:{}", request.getUrl(), request.getHeader(), request.getBody());
            ResponseEntity<String> resp = restTemplate.exchange(ct_host + request.getUrl(), HttpMethod.resolve(request.getMethod()), httpEntity, String.class);
            log.info("Get a Response from CT with Url:{}, StatusCode:{} and Body:{}", request.getUrl(), resp.getStatusCode(), resp.getBody());
            if (resp.getStatusCode() == HttpStatus.OK) {
                if (data.getResultHandler() != null) {
                    ResultHandler resultHandler = (ResultHandler) SpringContextUtils.getContext().getBean(data.getResultHandler());
                    Type actualTypeParameter = ((ParameterizedType) resultHandler.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
                    Object output = JSONObject.parseObject(resp.getBody(), actualTypeParameter);
                    if (resultHandler != null && resultHandler.shouldHandle(data, output)) {
                        resultHandler.handle(data, output);
                    }
                }
            } else if (resp.getStatusCode().is5xxServerError()) {
                throw new ConsumeException("HTTP Request error: " + resp.getBody());
            } else {
                log.info("Discard message without 5xx server error. resp:[{}]", resp.getBody());
            }
        } catch (Throwable e) {
            if (persistenceHandler.shouldHandle(data, properties)) {
                persistenceHandler.handle(data, properties);
            } else {
                // re-throw exception
                throw e;
            }
        }
    }
}

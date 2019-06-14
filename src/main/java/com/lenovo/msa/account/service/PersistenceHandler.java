package com.lenovo.msa.account.service;


import com.lenovo.msa.account.model.CommonMessageData;
import com.lenovo.msa.account.model.MqConstant;
import com.lenovo.msa.account.model.MqRetryFailed;
import com.lenovo.msa.account.dao.MqRetryFailedDao;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class PersistenceHandler implements ResultHandler<MessageProperties> {

    private static final String var1 = "x-original-exchange";
    private static final String var2 = "x-original-routingKey";
    private static final String var3 = MqConstant.header_latest_message_key;
    private static final String var4 = MqConstant.header_retry_count_key;
    private static final String var5 = "x-exception-stacktrace";

    @Autowired
    private MqRetryFailedDao dao;

    @Override
    public boolean shouldHandle(CommonMessageData input, MessageProperties output) {
        Map<String, Object> headers = output.getHeaders();
        if (headers == null || headers.get(var3) == null || !(Boolean) headers.get(var3)) {
            return false;
        }
        return true;
    }

    @Override
    public void handle(CommonMessageData input, MessageProperties output) {
        MqRetryFailed record = new MqRetryFailed();
        Map<String, Object> headers = output.getHeaders();

        String type = MqRetryFailed.generateType((String) headers.get(var1), (String) headers.get(var2));
        String uniformId = MqRetryFailed.generateUniformId(output.getConsumerQueue(), output.getConsumerTag(), output.getDeliveryTag());

        record.setType(type);
        record.setUniformId(uniformId);
        record.setTimestamp(new Date().getTime());
        String stackTrace = String.valueOf(headers.get(var5));
        record.setStackTrace(stackTrace.replaceAll("\\s+at\\s*?\\S+|\\s+\\.\\.\\.\\s*?\\S+\\s+\\S+",""));

        MqRetryFailed.RequestRecord request = new MqRetryFailed.RequestRecord();
        request.setUrl(input.getRequest().getUrl());
        request.setMethod(input.getRequest().getMethod());
        request.setHeader(input.getRequest().getHeader());
        request.setBody(input.getRequest().getBody());
        record.setRequest(request);

        MqRetryFailed.MQRecord mq = new MqRetryFailed.MQRecord();
        mq.setDeliverTag(output.getDeliveryTag());
        mq.setQueue(output.getConsumerQueue());
        mq.setRouterKey(output.getReceivedRoutingKey());
        mq.setRetryTimes((Integer) headers.get(var4));
        record.setMq(mq);
        dao.createRecord(record);
    }
}

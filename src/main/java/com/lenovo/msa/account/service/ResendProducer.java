package com.lenovo.msa.account.service;

import com.lenovo.liecomm.microservices.common.serialization.JsonConvert;
import com.lenovo.msa.account.model.CommonMessageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.lenovo.msa.account.model.MqConstant.generate_exchange_name;

/**
 * 消息发送端(producer/consumer尽量使用不同connection，虽然内部已实现将发送和接受分离)
 */
@Component
@Slf4j
public class ResendProducer {

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private RabbitTemplate rabbitResendTemplate;

    public <T>void publish(String exchange, String routingKey, T t, ResendProducerHandler<T> rabbitProducerHandler) {
        CommonMessageData messageData = process(t, rabbitProducerHandler);
        if(messageData != null) {
            log.info("This message exchange is [{}], routingKey is [{}], message is [{}]", exchange, routingKey, messageData);
            rabbitResendTemplate.convertAndSend(generate_exchange_name(exchange, env), routingKey, messageData);
        }
    }

    public <T>void publishWithMessage(String exchange, String routingKey, T t, ResendProducerHandler<T> rabbitProducerHandler) {
        CommonMessageData messageData = process(t, rabbitProducerHandler);
        if(messageData != null) {
            String str = JsonConvert.serializeObject(messageData);
            Message message = MessageBuilder.withBody(str.getBytes()).build();
            log.info("This message exchange is [{}],  routingKey is [{}], message is [{}]", exchange, routingKey, message);
            rabbitResendTemplate.convertAndSend(generate_exchange_name(exchange, env), routingKey, message);
        }
    }

    /**
     * @param t
     * @param rabbitProducerHandler
     * @param <T>
     * @return
     */
    private <T> CommonMessageData process(T t,ResendProducerHandler<T> rabbitProducerHandler) {
        if(rabbitProducerHandler != null && !rabbitProducerHandler.validate(t)) {
            return null;
        }
        CommonMessageData messageData = null;
        if(rabbitProducerHandler != null) {
            messageData = rabbitProducerHandler.build(t);
        } else {
            if(t instanceof CommonMessageData) {
                messageData = (CommonMessageData) t;
            }
        }
        return messageData;
    }

}

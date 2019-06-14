package com.lenovo.msa.account.service;

import com.lenovo.msa.account.model.CommonMessageData;
import com.lenovo.msa.account.model.MqConstant;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RabbitListener(queues = MqConstant.account_queue + "#" + "${spring.profiles.active}")
@Component
public class AcctListener extends BasicListener {

    @RabbitHandler
    private void handle(@Payload CommonMessageData data, Channel channel, Message message) {
        log.info("### start to consume message :{}", data);
        handleMessage(data, channel, message.getMessageProperties());
    }
}

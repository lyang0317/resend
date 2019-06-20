package com.lenovo.msa.account.service;

import com.lenovo.msa.account.model.MqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * FileName: AccountResendProducer
 * Author:   lujy7
 * Date:     2019/6/20 18:31
 * Description:
 */
@Component
@Slf4j
public class AccountResendProducer extends ResendProducer {

    public <T>void publish(T t, ResendProducerHandler<T> rabbitProducerHandler) {
        publish(MqConstant.account_exchange,MqConstant.account_router_key, t, rabbitProducerHandler);
    }

}

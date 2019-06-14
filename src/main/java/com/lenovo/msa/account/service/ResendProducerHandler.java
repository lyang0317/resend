package com.lenovo.msa.account.service;

import com.lenovo.msa.account.model.CommonMessageData;

public interface ResendProducerHandler<T> {

    Boolean validate(T t);

    CommonMessageData build(T t);

}

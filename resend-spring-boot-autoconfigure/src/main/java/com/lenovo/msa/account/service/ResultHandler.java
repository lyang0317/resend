package com.lenovo.msa.account.service;

import com.lenovo.msa.account.model.CommonMessageData;

public interface ResultHandler<R> {
    boolean shouldHandle(CommonMessageData input, R output);

    void handle(CommonMessageData input, R output);
}
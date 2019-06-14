package com.lenovo.msa.account.exception;

public class ConsumeException extends RuntimeException {
    public ConsumeException() {
        super();
    }

    public ConsumeException(String message) {
        super(message);
    }

    public ConsumeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsumeException(Throwable cause) {
        super(cause);
    }

    protected ConsumeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

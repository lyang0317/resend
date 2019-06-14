package com.lenovo.msa.account.model;

import lombok.Data;
import org.springframework.http.HttpHeaders;

import java.io.Serializable;

@Data
public class CommonMessageData implements Serializable {

    private String uri;
    private RequestData request;

    private String resultHandler;

    @Data
    public static class RequestData {
        private String url;
        private String method;
        private HttpHeaders header;
        private String body;
    }
}

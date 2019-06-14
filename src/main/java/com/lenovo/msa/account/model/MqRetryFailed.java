package com.lenovo.msa.account.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpHeaders;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "MqRetryFailed")
public class MqRetryFailed {
    private final static String delimiter1 = "@";
    private final static String delimiter2 = "$";
    @Id
    private String id;

    private String uniformId;

    private String type;
    private RequestRecord request;
    private MQRecord mq;
    private String stackTrace;
    private Long timestamp;

    public static String generateType(String exchangeName, String routeKey) {
        return exchangeName + delimiter1 + routeKey;
    }

    public static String generateUniformId(String queueName, String consumerTag, Long deliverTag) {
        return queueName + delimiter1 + consumerTag + delimiter2 + deliverTag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestRecord {
        private String url;
        private String method;
        private HttpHeaders header;
        private String body;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MQRecord {
        private String queue;
        private String routerKey;
        private Long deliverTag;
        private Integer retryTimes;
    }
}

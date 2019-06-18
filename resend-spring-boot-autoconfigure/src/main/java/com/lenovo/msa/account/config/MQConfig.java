package com.lenovo.msa.account.config;

import com.google.common.collect.Maps;
import com.lenovo.msa.account.dao.MqRetryFailedDao;
import com.lenovo.msa.account.service.AcctListener;
import com.lenovo.msa.account.service.PersistenceHandler;
import com.lenovo.msa.account.service.ResendProducer;
import com.lenovo.msa.account.utils.SpringContextUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.Map;

import static com.lenovo.msa.account.model.MqConstant.*;

@Configuration
@Import({MqRetryFailedDao.class, AcctListener.class, PersistenceHandler.class, ResendProducer.class, SpringContextUtils.class})
@Slf4j
public class MQConfig {

    @Value("${broker.host:localhost}")
    private String brokerHost;

    @Value("${broker.port:5672}")
    private String brokerPort;

    @Value("${broker.username:root}")
    private String brokerUserName;

    @Value("${broker.password:123456}")
    private String brokerPassword;

    @Value("${broker.virtualHost:/}")
    private String brokerVirtualHost;

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${broker.consumer.minimum:5}")
    private Integer concurrentConsumers;

    @Value("${broker.consumer.maximum:20}")
    private Integer maxConcurrentConsumers;

    @Value("${broker.retry.max-attempts:3}")
    public Integer retryMaximum;

    @Value("${broker.retry.toggle:true}")
    public Boolean retryToggle;

    @Value("${broker.retry.initial-ttl:1000}")
    public Integer retryInterval;

    @Value("${broker.retry.multiplier:5}")
    public Integer retryMultiplier;

    @Value("${broker.retry.max-ttl:259200000}")
    public Integer retryMaxTtl;

    @Bean
    public ConnectionFactory connectionFactory() throws IOException {
        CachingConnectionFactory cf = new CachingConnectionFactory(brokerHost, Integer.parseInt(brokerPort));
        cf.setUsername(brokerUserName);
        cf.setPassword(brokerPassword);
        cf.setVirtualHost(brokerVirtualHost);
        cf.setPublisherConfirms(true);
        Connection connection = cf.createConnection();
        Channel channel = connection.createChannel(false);
        String exchangeName = generate_exchange_name(account_exchange, env);
        channel.exchangeDeclare(exchangeName, "direct", true, false, null);
        String queueName = generate_exchange_name(account_queue, env);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, account_bind_key);


        channel.exchangeDeclare(generate_exchange_name(account_dead_letter_exchange, env), "direct", true, false, null);
        Map<String, Object> args = Maps.newHashMap();
        args.put("x-dead-letter-exchange", exchangeName);
        channel.queueDeclare(generate_exchange_name(account_dead_letter_queue, env), true, false, false, args);
        channel.queueBind(generate_exchange_name(account_dead_letter_queue, env), generate_exchange_name(account_dead_letter_exchange, env), account_bind_key);
        return cf;
    }

    @Bean("rabbitResendTemplate")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if(!ack) {
                log.error("message send to exchange failed, id is [{}], cause is [{}]", correlationData.getId(), cause);
            }
        });
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }


    @Bean
    public RabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, @Qualifier("mqRetryOperations") RetryOperationsInterceptor retryOperationsInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        if (retryToggle) {
            factory.setAdviceChain(retryOperationsInterceptor);
        }
        return factory;
    }

    public class CustomMessageRecoverer extends RepublishMessageRecoverer {

        public CustomMessageRecoverer(AmqpTemplate errorTemplate, String errorExchange) {
            super(errorTemplate, errorExchange);
        }

        @Override
        protected Map<? extends String, ? extends Object> additionalHeaders(Message message, Throwable cause) {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            Integer count = (Integer) headers.get(header_retry_count_key);
            if (count == null) {
                count = 0;
            }
            headers.put(header_retry_count_key, ++count);
            int expiration = count * retryMultiplier * retryInterval;
            if (count >= retryMaximum || expiration >= retryMaxTtl) {
                headers.put(header_latest_message_key, true);
                expiration = expiration > retryMaxTtl ? retryMaxTtl : expiration;
            }
            message.getMessageProperties().setExpiration(String.valueOf(expiration));
            return headers;
        }
    }

    @Bean
    public CustomMessageRecoverer messageRecoverer(@Qualifier("rabbitResendTemplate") RabbitTemplate rabbitTemplate) {
        CustomMessageRecoverer recoverer = new CustomMessageRecoverer(rabbitTemplate,
                generate_exchange_name(account_dead_letter_exchange, env));
        recoverer.setErrorRoutingKeyPrefix("");
        return recoverer;
    }

    @Bean("mqRetryOperations")
    public RetryOperationsInterceptor statefulRetryOperationsInterceptor(CustomMessageRecoverer recoverer) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(1));
        return RetryInterceptorBuilder.stateless().retryOperations(retryTemplate).recoverer(recoverer).build();
    }
}

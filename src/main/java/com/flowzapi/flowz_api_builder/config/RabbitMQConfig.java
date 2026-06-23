package com.flowzapi.flowz_api_builder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_FLOWS = "flows-queue";
    public static final String FLOWS_EXCHANGE = "flows-exchange";
    public static final String ROUTING_KEY_FLOWS = "flows.execution.run";
    public static final String FLOWS_DLX = "flows-dlx";
    public static final String FLOWS_DLQ = "flows-dlq";
    public static final String ROUTING_KEY_FLOWS_DLQ = "flows.dead.letter";
    public static final String EMAILS_QUEUE = "emails-queue";
    public static final String EMAILS_ROUTING_KEY = "emails.send";
    public static final String EMAILS_EXCHANGE= "emails-exchange";
    public static final String EMAILS_DLQ = "emails-dlq";
    public static final String EMAILS_DLX = "emails-dlx";
    public static final String EMAILS_DLQ_ROUTING = "emails.dead.letter";


    @Bean
    public Queue emailsQueue() {
        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", EMAILS_DLX);
        args.put("x-dead-letter-routing-key", EMAILS_DLQ_ROUTING);
        return new Queue(EMAILS_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange emailsExchange() {
        return new DirectExchange(EMAILS_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingEmailsQueue() {
        return BindingBuilder.bind(emailsQueue())
                .to(emailsExchange())
                .with(EMAILS_ROUTING_KEY);
    }

    @Bean
    public DirectExchange emailsDlx() {
        return new DirectExchange(EMAILS_DLX, true, false);
    }

    @Bean
    public Queue emailsDlq() {
        return new Queue(EMAILS_DLQ, true, false, false);
    }

    @Bean
    public Binding bindingEmailsDLQ() {
        return BindingBuilder.bind(emailsDlq())
                .to(emailsDlx())
                .with(EMAILS_DLQ_ROUTING);
    }


    @Bean
    public Queue flowsQueue() {
        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", FLOWS_DLX);
        args.put("x-dead-letter-routing-key", ROUTING_KEY_FLOWS_DLQ);
        return new Queue(QUEUE_FLOWS, true, false, false, args);
    }

    @Bean
    public DirectExchange flowsExchange() {
        return new DirectExchange(FLOWS_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingFlowsQueue() {
        return BindingBuilder.bind(flowsQueue())
                .to(flowsExchange())
                .with(ROUTING_KEY_FLOWS);
    }

    @Bean
    public DirectExchange flowsDlx() {
        return new DirectExchange(FLOWS_DLX);
    }

    @Bean
    public Queue flowsDlq() {
        return new Queue(FLOWS_DLQ, true, false, false);
    }

    @Bean
    public Binding bindingFlowsDlq() {
        return BindingBuilder.bind(flowsDlq())
                .to(flowsDlx())
                .with(ROUTING_KEY_FLOWS_DLQ);
    }

    @Bean(name = "rabbitContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        configurer.configure(factory, connectionFactory);

        factory.setMessageConverter(jsonMessageConverter);

        factory.setErrorHandler(t -> {
            log.error("RabbitMQ Global Handler caught an exception during message processing", t);

            throw new AmqpRejectAndDontRequeueException("Rejecting message to DLQ due to error", t);
        });

        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

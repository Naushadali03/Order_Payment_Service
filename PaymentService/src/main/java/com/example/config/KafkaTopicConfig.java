package com.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import jakarta.validation.Valid;

@Configuration
public class KafkaTopicConfig {
    @Value("${kafka.topic.payment-events}")
    private String paymentEventsTopic;

    public NewTopic paymentEventsTopic(){
        return TopicBuilder
                .name(paymentEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

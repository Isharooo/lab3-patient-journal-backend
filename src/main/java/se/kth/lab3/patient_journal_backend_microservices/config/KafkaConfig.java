package se.kth.lab3.patient_journal_backend_microservices.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.patient}")
    private String patientTopic;

    @Value("${kafka.topic.journal}")
    private String journalTopic;

    @Bean
    public NewTopic patientEventsTopic() {
        return TopicBuilder.name(patientTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic journalEventsTopic() {
        return TopicBuilder.name(journalTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
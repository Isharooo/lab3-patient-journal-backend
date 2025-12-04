package se.kth.lab3.patient_journal_backend_microservices.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientCommandDTO;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.patient}")
    private String patientTopic;

    @Value("${kafka.topic.journal}")
    private String journalTopic;

    @Value("${kafka.topic.patient-commands}")
    private String patientCommandsTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // ==================== TOPICS ====================

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

    /**
     * Topic för patient-kommandon (CREATE, UPDATE, DELETE via Kafka)
     */
    @Bean
    public NewTopic patientCommandsTopic() {
        return TopicBuilder.name(patientCommandsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // ==================== CONSUMER CONFIG ====================

    @Bean
    public ConsumerFactory<String, PatientCommandDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<PatientCommandDTO> deserializer = new JsonDeserializer<>(PatientCommandDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PatientCommandDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PatientCommandDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
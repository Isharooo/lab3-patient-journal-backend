package se.kth.lab3.patient_journal_backend_microservices.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

/**
 * Konfiguration för Embedded Kafka.
 *
 * Denna klass startar en inbäddad Kafka-broker inuti applikationen.
 * Detta gör att vi kan använda Kafka utan att behöva en extern Kafka-server,
 * vilket löser problemet med att cbhcloud inte stödjer intern TCP-kommunikation
 * mellan tjänster.
 */
@Configuration
@Slf4j
public class EmbeddedKafkaConfig {

    private EmbeddedKafkaBroker embeddedKafkaBroker;

    /**
     * Skapar och startar en embedded Kafka-broker.
     *
     * Brokern körs på localhost:9092 inuti samma JVM som applikationen.
     * Topics skapas automatiskt.
     */
    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        log.info("=== Startar Embedded Kafka Broker ===");

        embeddedKafkaBroker = new EmbeddedKafkaZKBroker(
                1,      // antal brokers
                true,   // kontrollerad shutdown
                3,      // partitioner per topic
                "patient.events", "journal.events", "patient.commands"  // topics
        )
                .kafkaPorts(9092)
                .brokerListProperty("spring.kafka.bootstrap-servers");

        // Starta brokern
        embeddedKafkaBroker.afterPropertiesSet();

        log.info("=== Embedded Kafka Broker startad på: {} ===",
                embeddedKafkaBroker.getBrokersAsString());

        return embeddedKafkaBroker;
    }

    @PreDestroy
    public void shutdown() {
        if (embeddedKafkaBroker != null) {
            log.info("=== Stänger ner Embedded Kafka Broker ===");
            embeddedKafkaBroker.destroy();
        }
    }
}

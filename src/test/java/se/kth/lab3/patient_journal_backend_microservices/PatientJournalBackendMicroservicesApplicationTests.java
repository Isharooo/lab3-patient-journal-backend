package se.kth.lab3.patient_journal_backend_microservices;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.kth.lab3.patient_journal_backend_microservices.dto.JournalEntryDTO;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientCommandDTO;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;

@SpringBootTest(properties = {
        "kafka.topic.patient=test-patient-topic",
        "kafka.topic.journal=test-journal-topic",
        "kafka.topic.patient-commands=test-patient-commands",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.consumer.group-id=test-group"
})
@ActiveProfiles("test")
class PatientJournalBackendMicroservicesApplicationTests {

    // Mocka Kafka så att appen startar utan Kafka-server
    @MockitoBean
    private KafkaTemplate<String, PatientDTO> patientKafkaTemplate;

    @MockitoBean
    private KafkaTemplate<String, JournalEntryDTO> journalKafkaTemplate;

    @MockitoBean
    private KafkaTemplate<String, PatientCommandDTO> commandKafkaTemplate;

    // Mocka JwtDecoder så att SecurityConfig startar utan Keycloak
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
        // Nu bör kontexten ladda utan problem!
    }
}
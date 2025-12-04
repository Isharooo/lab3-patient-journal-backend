package se.kth.lab3.patient_journal_backend_microservices;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.kth.lab3.patient_journal_backend_microservices.dto.JournalEntryDTO;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;

@SpringBootTest(properties = {
        "kafka.topic.patient=test-patient-topic",
        "kafka.topic.journal=test-journal-topic",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@ActiveProfiles("test")
// OBS: Vi har tagit bort @Import(TestSecurityConfig.class) för att slippa krocken!
class PatientJournalBackendMicroservicesApplicationTests {

    // Mocka Kafka så att appen startar utan Kafka-server
    @MockitoBean
    private KafkaTemplate<String, PatientDTO> patientKafkaTemplate;

    @MockitoBean
    private KafkaTemplate<String, JournalEntryDTO> journalKafkaTemplate;

    // Mocka JwtDecoder så att SecurityConfig startar utan Keycloak
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
        // Nu bör kontexten ladda utan problem!
    }
}
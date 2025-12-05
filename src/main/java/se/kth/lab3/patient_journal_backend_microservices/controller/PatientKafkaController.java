package se.kth.lab3.patient_journal_backend_microservices.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientCommandDTO;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;

/**
 * Controller för att hantera patienter via Kafka.
 *
 * Detta uppfyller Lab3-kravet: "göra om minst ett rest api till att bli strömmande via Kafka.
 * Det betyder att den ska anropas via Kafka istället för Rest."
 *
 * SKILLNAD MOT VANLIG REST (/api/patients):
 *
 * REST (synkront):
 *   POST /api/patients → PatientService.createPatient() → Databas → Returnerar skapad patient
 *
 * KAFKA (asynkront):
 *   POST /api/kafka/patients → Skickar till Kafka topic → Returnerar 202 Accepted
 *                                    ↓
 *                          PatientCommandConsumer lyssnar
 *                                    ↓
 *                          Skapar patient i databas
 *
 * Fördelen med Kafka-flödet:
 * - Asynkront: Klienten behöver inte vänta på att operationen slutförs
 * - Skalbart: Flera consumers kan bearbeta meddelanden parallellt
 * - Pålitligt: Meddelanden lagras i Kafka och kan hanteras om consumer är nere
 */
@RestController
@RequestMapping("/api/kafka/patients")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PatientKafkaController {

    private final KafkaTemplate<String, PatientCommandDTO> commandKafkaTemplate;

    private static final String TOPIC = "patient.commands";

    /**
     * Skapar en patient via Kafka (asynkront).
     *
     * Istället för att direkt anropa PatientService, skickas ett kommando
     * till Kafka topic "patient.commands". PatientCommandConsumer lyssnar
     * på detta topic och utför den faktiska skapandet.
     */
    @PostMapping
    public ResponseEntity<String> createPatientViaKafka(@RequestBody PatientDTO patientDTO) {
        log.info("========================================");
        log.info("=== KAFKA PRODUCER: Skickar CREATE ===");
        log.info("=== Patient: {} {} ===", patientDTO.getFirstName(), patientDTO.getLastName());
        log.info("========================================");

        PatientCommandDTO command = new PatientCommandDTO("CREATE", null, patientDTO);

        commandKafkaTemplate.send(TOPIC, command);

        return ResponseEntity.accepted()
                .body("CREATE-kommando skickat till Kafka topic '" + TOPIC + "'. " +
                        "Patienten skapas asynkront av Kafka Consumer. " +
                        "Kolla GET /api/patients för att se resultatet.");
    }

    /**
     * Uppdaterar en patient via Kafka (asynkront).
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePatientViaKafka(
            @PathVariable Long id,
            @RequestBody PatientDTO patientDTO) {

        log.info("========================================");
        log.info("=== KAFKA PRODUCER: Skickar UPDATE ===");
        log.info("=== Patient ID: {} ===", id);
        log.info("========================================");

        PatientCommandDTO command = new PatientCommandDTO("UPDATE", id, patientDTO);

        commandKafkaTemplate.send(TOPIC, id.toString(), command);

        return ResponseEntity.accepted()
                .body("UPDATE-kommando skickat till Kafka topic '" + TOPIC + "'. " +
                        "Patienten uppdateras asynkront av Kafka Consumer.");
    }

    /**
     * Tar bort en patient via Kafka (asynkront).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatientViaKafka(@PathVariable Long id) {
        log.info("========================================");
        log.info("=== KAFKA PRODUCER: Skickar DELETE ===");
        log.info("=== Patient ID: {} ===", id);
        log.info("========================================");

        PatientCommandDTO command = new PatientCommandDTO("DELETE", id, null);

        commandKafkaTemplate.send(TOPIC, id.toString(), command);

        return ResponseEntity.accepted()
                .body("DELETE-kommando skickat till Kafka topic '" + TOPIC + "'. " +
                        "Patienten tas bort asynkront av Kafka Consumer.");
    }
}

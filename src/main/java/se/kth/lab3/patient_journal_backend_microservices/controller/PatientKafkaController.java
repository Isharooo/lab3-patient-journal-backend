package se.kth.lab3.patient_journal_backend_microservices.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientCommandDTO;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;

/**
 * Controller för att skicka patient-kommandon via Kafka.
 *
 * Detta demonstrerar Lab3-kravet: operationer som triggas via Kafka istället för REST.
 */
@RestController
@RequestMapping("/api/kafka/patients")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PatientKafkaController {

    private final KafkaTemplate<String, PatientCommandDTO> commandKafkaTemplate;

    @Value("${kafka.topic.patient-commands}")
    private String patientCommandsTopic;

    /**
     * Skickar ett CREATE-kommando via Kafka för att skapa en patient.
     */
    @PostMapping
    public ResponseEntity<String> createPatientViaKafka(@RequestBody PatientDTO patientDTO) {
        PatientCommandDTO command = new PatientCommandDTO("CREATE", null, patientDTO);

        commandKafkaTemplate.send(patientCommandsTopic, command);
        log.info("=== Kafka: Skickade CREATE-kommando för patient: {} {} ===",
                patientDTO.getFirstName(), patientDTO.getLastName());

        return ResponseEntity.accepted()
                .body("CREATE-kommando skickat till Kafka. Patienten skapas asynkront.");
    }

    /**
     * Skickar ett UPDATE-kommando via Kafka för att uppdatera en patient.
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePatientViaKafka(
            @PathVariable Long id,
            @RequestBody PatientDTO patientDTO) {

        PatientCommandDTO command = new PatientCommandDTO("UPDATE", id, patientDTO);

        commandKafkaTemplate.send(patientCommandsTopic, id.toString(), command);
        log.info("=== Kafka: Skickade UPDATE-kommando för patient ID: {} ===", id);

        return ResponseEntity.accepted()
                .body("UPDATE-kommando skickat till Kafka. Patienten uppdateras asynkront.");
    }

    /**
     * Skickar ett DELETE-kommando via Kafka för att ta bort en patient.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatientViaKafka(@PathVariable Long id) {
        PatientCommandDTO command = new PatientCommandDTO("DELETE", id, null);

        commandKafkaTemplate.send(patientCommandsTopic, id.toString(), command);
        log.info("=== Kafka: Skickade DELETE-kommando för patient ID: {} ===", id);

        return ResponseEntity.accepted()
                .body("DELETE-kommando skickat till Kafka. Patienten tas bort asynkront.");
    }
}

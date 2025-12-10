package se.kth.lab3.patient_journal_backend_microservices.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientCommandDTO;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;

@RestController
@RequestMapping("/api/kafka/patients")
@RequiredArgsConstructor
@Slf4j
// OBS: @CrossOrigin borttagen här
public class PatientKafkaController {

    private final KafkaTemplate<String, PatientCommandDTO> commandKafkaTemplate;

    private static final String TOPIC = "patient.commands";

    @PostMapping
    public ResponseEntity<String> createPatientViaKafka(@RequestBody PatientDTO patientDTO) {
        PatientCommandDTO command = new PatientCommandDTO("CREATE", null, patientDTO);
        commandKafkaTemplate.send(TOPIC, command);
        return ResponseEntity.accepted()
                .body("CREATE-kommando skickat till Kafka topic '" + TOPIC + "'.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePatientViaKafka(
            @PathVariable Long id,
            @RequestBody PatientDTO patientDTO) {
        PatientCommandDTO command = new PatientCommandDTO("UPDATE", id, patientDTO);
        commandKafkaTemplate.send(TOPIC, id.toString(), command);
        return ResponseEntity.accepted()
                .body("UPDATE-kommando skickat till Kafka topic '" + TOPIC + "'.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatientViaKafka(@PathVariable Long id) {
        PatientCommandDTO command = new PatientCommandDTO("DELETE", id, null);
        commandKafkaTemplate.send(TOPIC, id.toString(), command);
        return ResponseEntity.accepted()
                .body("DELETE-kommando skickat till Kafka topic '" + TOPIC + "'.");
    }
}
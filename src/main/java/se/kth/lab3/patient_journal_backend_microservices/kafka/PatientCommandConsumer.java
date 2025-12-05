package se.kth.lab3.patient_journal_backend_microservices.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientCommandDTO;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;
import se.kth.lab3.patient_journal_backend_microservices.entity.Patient;
import se.kth.lab3.patient_journal_backend_microservices.repository.PatientRepository;

/**
 * Kafka Consumer som lyssnar på patient.commands topic.
 *
 * Detta uppfyller Lab3-kravet: "göra om minst ett rest api till att bli strömmande via Kafka.
 * Det betyder att den ska anropas via Kafka istället för Rest."
 *
 * FLÖDET:
 * 1. PatientKafkaController tar emot HTTP-request och skickar kommando till Kafka topic
 * 2. Denna consumer LYSSNAR på Kafka topic "patient.commands"
 * 3. När meddelande kommer → Consumer utför operationen (CREATE/UPDATE/DELETE)
 * 4. Patient skapas/uppdateras/tas bort baserat på Kafka-meddelandet
 *
 * SKILLNAD MOT REST:
 * - REST: Synkront - PatientController → PatientService → Databas → Svar
 * - Kafka: Asynkront - Controller → Kafka Topic → Consumer → Databas (inget direkt svar)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientCommandConsumer {

    private final PatientRepository patientRepository;

    /**
     * Lyssnar på patient.commands topic och utför CRUD-operationer.
     *
     * Detta är "Consumer"-delen av Kafka-flödet.
     * Meddelanden skickas hit från PatientKafkaController via Kafka.
     */
    @KafkaListener(
            topics = "patient.commands",
            groupId = "patient-journal-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePatientCommand(PatientCommandDTO command) {
        log.info("========================================");
        log.info("=== KAFKA CONSUMER: Mottog kommando ===");
        log.info("=== Typ: {} ===", command.getCommandType());
        log.info("========================================");

        try {
            switch (command.getCommandType().toUpperCase()) {
                case "CREATE":
                    handleCreate(command.getPatient());
                    break;
                case "UPDATE":
                    handleUpdate(command.getPatientId(), command.getPatient());
                    break;
                case "DELETE":
                    handleDelete(command.getPatientId());
                    break;
                default:
                    log.warn("Okänd kommandotyp: {}", command.getCommandType());
            }
        } catch (Exception e) {
            log.error("Fel vid hantering av Kafka-kommando: {}", e.getMessage(), e);
        }
    }

    private void handleCreate(PatientDTO patientDTO) {
        if (patientDTO == null) {
            log.error("CREATE: PatientDTO är null");
            return;
        }

        if (patientRepository.existsByPersonalNumber(patientDTO.getPersonalNumber())) {
            log.warn("CREATE: Patient med personnummer {} finns redan", patientDTO.getPersonalNumber());
            return;
        }

        Patient patient = new Patient(
                null,
                patientDTO.getFirstName(),
                patientDTO.getLastName(),
                patientDTO.getPersonalNumber(),
                patientDTO.getDateOfBirth(),
                patientDTO.getEmail(),
                patientDTO.getPhoneNumber(),
                patientDTO.getAddress()
        );

        Patient saved = patientRepository.save(patient);
        log.info("=== KAFKA: Patient skapad via Kafka med ID: {} ===", saved.getId());
    }

    private void handleUpdate(Long patientId, PatientDTO patientDTO) {
        if (patientId == null) {
            log.error("UPDATE: PatientId är null");
            return;
        }

        patientRepository.findById(patientId).ifPresentOrElse(
                patient -> {
                    patient.setFirstName(patientDTO.getFirstName());
                    patient.setLastName(patientDTO.getLastName());
                    patient.setDateOfBirth(patientDTO.getDateOfBirth());
                    patient.setEmail(patientDTO.getEmail());
                    patient.setPhoneNumber(patientDTO.getPhoneNumber());
                    patient.setAddress(patientDTO.getAddress());
                    patientRepository.save(patient);
                    log.info("=== KAFKA: Patient uppdaterad via Kafka med ID: {} ===", patientId);
                },
                () -> log.warn("UPDATE: Patient med ID {} finns inte", patientId)
        );
    }

    private void handleDelete(Long patientId) {
        if (patientId == null) {
            log.error("DELETE: PatientId är null");
            return;
        }

        if (patientRepository.existsById(patientId)) {
            patientRepository.deleteById(patientId);
            log.info("=== KAFKA: Patient borttagen via Kafka med ID: {} ===", patientId);
        } else {
            log.warn("DELETE: Patient med ID {} finns inte", patientId);
        }
    }
}

package se.kth.lab3.patient_journal_backend_microservices.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientCommandDTO;
import se.kth.lab3.patient_journal_backend_microservices.service.PatientService;

/**
 * Kafka Consumer som hanterar kommandon for patienter.
 * Uppfyller kravet for hogre betyg genom att moijliggora asynkron hantering via Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientCommandConsumer {

    // VIKTIGT: Vi använder Service, inte Repository.
    // Detta gor att när en patient skapas här, skickas OCKSa ett event till Search Service.
    private final PatientService patientService;

    @KafkaListener(
            topics = "patient.commands",
            groupId = "patient-journal-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePatientCommand(PatientCommandDTO command) {
        log.info("Mottog Kafka-kommando: {} for patient-ID: {}", command.getCommandType(), command.getPatientId());

        try {
            switch (command.getCommandType().toUpperCase()) {
                case "CREATE":
                    // Anropar service -> Sparar i DB -> Skickar event till Search Service
                    patientService.createPatient(command.getPatient());
                    log.info("KAFKA: Patient skapad via Service (Event skickat).");
                    break;

                case "UPDATE":
                    if (command.getPatientId() != null) {
                        patientService.updatePatient(command.getPatientId(), command.getPatient());
                        log.info("KAFKA: Patient uppdaterad via Service (Event skickat).");
                    }
                    break;

                case "DELETE":
                    if (command.getPatientId() != null) {
                        patientService.deletePatient(command.getPatientId());
                        log.info("KAFKA: Patient borttagen via Service (Tombstone skickad).");
                    }
                    break;

                default:
                    log.warn("Okänd kommandotyp: {}", command.getCommandType());
            }
        } catch (Exception e) {
            log.error("Fel vid hantering av Kafka-kommando: {}", e.getMessage(), e);
        }
    }
}
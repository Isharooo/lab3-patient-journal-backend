package se.kth.lab3.patient_journal_backend_microservices.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientCommandDTO;
import se.kth.lab3.patient_journal_backend_microservices.service.PatientService;

/**
 * Kafka Consumer som lyssnar på patient.commands topic.
 *
 * FLÖDET FÖR HÖGRE BETYG:
 * 1. PatientKafkaController tar emot HTTP-request och skickar kommando till "patient.commands".
 * 2. Denna consumer lyssnar på "patient.commands".
 * 3. Consumer anropar PatientService för att utföra operationen.
 * 4. PatientService sparar till DB *OCH* skickar ett nytt event till "patient.events".
 * 5. Search Service lyssnar på "patient.events" och uppdaterar sitt sökindex.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientCommandConsumer {

    private final PatientService patientService;

    @KafkaListener(
            topics = "patient.commands",
            groupId = "patient-journal-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePatientCommand(PatientCommandDTO command) {
        log.info("Mottog Kafka-kommando: {} för patient-ID: {}", command.getCommandType(), command.getPatientId());

        try {
            switch (command.getCommandType().toUpperCase()) {
                case "CREATE":
                    // Anropar service -> Sparar i DB -> Skickar event till Search Service
                    patientService.createPatient(command.getPatient());
                    log.info("KAFKA: CREATE-kommando utfört via Service.");
                    break;

                case "UPDATE":
                    if (command.getPatientId() != null) {
                        patientService.updatePatient(command.getPatientId(), command.getPatient());
                        log.info("KAFKA: UPDATE-kommando utfört via Service.");
                    } else {
                        log.warn("UPDATE misslyckades: Patient-ID saknas.");
                    }
                    break;

                case "DELETE":
                    if (command.getPatientId() != null) {
                        patientService.deletePatient(command.getPatientId());
                        log.info("KAFKA: DELETE-kommando utfört via Service.");
                    } else {
                        log.warn("DELETE misslyckades: Patient-ID saknas.");
                    }
                    break;

                default:
                    log.warn("Mottog okänd kommandotyp: {}", command.getCommandType());
            }
        } catch (Exception e) {
            log.error("Fel vid hantering av Kafka-kommando: {}", e.getMessage(), e);
        }
    }
}
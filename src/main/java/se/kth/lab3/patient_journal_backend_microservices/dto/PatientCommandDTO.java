package se.kth.lab3.patient_journal_backend_microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO för Kafka-kommandon för patient-operationer.
 *
 * Denna klass representerar meddelanden som skickas via Kafka.
 *
 * CommandType kan vara:
 * - "CREATE": Skapar en ny patient
 * - "UPDATE": Uppdaterar en befintlig patient
 * - "DELETE": Tar bort en patient
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientCommandDTO {

    /**
     * Typ av kommando: CREATE, UPDATE, eller DELETE
     */
    private String commandType;

    /**
     * Patient-ID (används för UPDATE och DELETE)
     */
    private Long patientId;

    /**
     * Patient-data (används för CREATE och UPDATE)
     */
    private PatientDTO patient;
}

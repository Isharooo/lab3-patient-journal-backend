package se.kth.lab3.patient_journal_backend_microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO för att skicka kommandon via Kafka istället för REST.
 * Används för att trigga operationer på patienter via Kafka-meddelanden.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientCommandDTO {

    /**
     * Typ av kommando: CREATE, UPDATE, DELETE
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
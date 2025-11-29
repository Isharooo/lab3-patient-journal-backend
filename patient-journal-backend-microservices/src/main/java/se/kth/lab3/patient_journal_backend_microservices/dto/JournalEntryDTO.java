package se.kth.lab3.patient_journal_backend_microservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryDTO {

    private Long id;

    @NotNull(message = "Patient-ID krävs")
    private Long patientId;

    @NotBlank(message = "Anteckning krävs")
    private String note;

    private LocalDateTime createdAt;

    private String diagnosis;

    private String treatment;
}
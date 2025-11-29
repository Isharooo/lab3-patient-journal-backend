package se.kth.lab3.patient_journal_backend_microservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {

    private Long id;

    @NotBlank(message = "Förnamn krävs")
    private String firstName;

    @NotBlank(message = "Efternamn krävs")
    private String lastName;

    @NotBlank(message = "Personnummer krävs")
    private String personalNumber;

    private LocalDate dateOfBirth;

    @Email(message = "Ogiltig e-postadress")
    private String email;

    private String phoneNumber;

    private String address;
}
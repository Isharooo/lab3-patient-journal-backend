package se.kth.lab3.patient_journal_backend_microservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Förnamn krävs")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Efternamn krävs")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Personnummer krävs")
    @Column(unique = true, nullable = false)
    private String personalNumber;

    private LocalDate dateOfBirth;

    private String email;

    private String phoneNumber;

    private String address;
}



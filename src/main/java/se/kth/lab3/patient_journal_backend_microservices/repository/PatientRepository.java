package se.kth.lab3.patient_journal_backend_microservices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.kth.lab3.patient_journal_backend_microservices.entity.Patient;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPersonalNumber(String personalNumber);

    boolean existsByPersonalNumber(String personalNumber);
}
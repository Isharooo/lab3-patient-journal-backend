package se.kth.lab3.patient_journal_backend_microservices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.kth.lab3.patient_journal_backend_microservices.entity.JournalEntry;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
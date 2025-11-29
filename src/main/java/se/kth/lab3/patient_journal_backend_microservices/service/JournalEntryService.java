package se.kth.lab3.patient_journal_backend_microservices.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.kth.lab3.patient_journal_backend_microservices.dto.JournalEntryDTO;
import se.kth.lab3.patient_journal_backend_microservices.entity.JournalEntry;
import se.kth.lab3.patient_journal_backend_microservices.entity.Patient;
import se.kth.lab3.patient_journal_backend_microservices.repository.JournalEntryRepository;
import se.kth.lab3.patient_journal_backend_microservices.repository.PatientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final PatientRepository patientRepository;
    private final KafkaTemplate<String, JournalEntryDTO> kafkaTemplate;

    @Value("${kafka.topic.journal}")
    private String journalTopic;

    public JournalEntryDTO createJournalEntry(JournalEntryDTO journalEntryDTO) {
        Patient patient = patientRepository.findById(journalEntryDTO.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient med ID " + journalEntryDTO.getPatientId() + " finns inte"));

        JournalEntry journalEntry = convertToEntity(journalEntryDTO, patient);
        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);

        JournalEntryDTO dto = convertToDTO(savedEntry);

        // Send event to Kafka
        kafkaTemplate.send(journalTopic, dto.getPatientId().toString(), dto);
        System.out.println("=== Kafka: Skickade journal-händelse till topic: " + journalTopic);

        return dto;
    }

    public JournalEntryDTO getJournalEntryById(Long id) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journalanteckning med ID " + id + " finns inte"));
        return convertToDTO(entry);
    }

    public List<JournalEntryDTO> getAllJournalEntries() {
        return journalEntryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JournalEntryDTO> getJournalEntriesByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient med ID " + patientId + " finns inte");
        }
        return journalEntryRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public JournalEntryDTO updateJournalEntry(Long id, JournalEntryDTO journalEntryDTO) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journalanteckning med ID " + id + " finns inte"));

        entry.setNote(journalEntryDTO.getNote());
        entry.setDiagnosis(journalEntryDTO.getDiagnosis());
        entry.setTreatment(journalEntryDTO.getTreatment());

        JournalEntry updatedEntry = journalEntryRepository.save(entry);
        JournalEntryDTO dto = convertToDTO(updatedEntry);

        // Send update event to Kafka
        kafkaTemplate.send(journalTopic, dto.getPatientId().toString(), dto);
        System.out.println("=== Kafka: Skickade journal-uppdaterings-händelse till topic: " + journalTopic);

        return dto;
    }

    public void deleteJournalEntry(Long id) {
        if (!journalEntryRepository.existsById(id)) {
            throw new RuntimeException("Journalanteckning med ID " + id + " finns inte");
        }
        journalEntryRepository.deleteById(id);
    }

    private JournalEntryDTO convertToDTO(JournalEntry entry) {
        return new JournalEntryDTO(
                entry.getId(),
                entry.getPatient().getId(),
                entry.getNote(),
                entry.getCreatedAt(),
                entry.getDiagnosis(),
                entry.getTreatment()
        );
    }

    private JournalEntry convertToEntity(JournalEntryDTO dto, Patient patient) {
        return new JournalEntry(
                null,
                patient,
                dto.getNote(),
                null,
                dto.getDiagnosis(),
                dto.getTreatment()
        );
    }
}
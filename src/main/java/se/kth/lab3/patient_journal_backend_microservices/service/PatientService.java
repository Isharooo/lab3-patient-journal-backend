package se.kth.lab3.patient_journal_backend_microservices.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;
import se.kth.lab3.patient_journal_backend_microservices.entity.Patient;
import se.kth.lab3.patient_journal_backend_microservices.repository.PatientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final KafkaTemplate<String, PatientDTO> kafkaTemplate;

    @Value("${kafka.topic.patient}")
    private String patientTopic;

    public PatientService(
            PatientRepository patientRepository,
            @Qualifier("patientKafkaTemplate") KafkaTemplate<String, PatientDTO> kafkaTemplate) {
        this.patientRepository = patientRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public PatientDTO createPatient(PatientDTO patientDTO) {
        if (patientRepository.existsByPersonalNumber(patientDTO.getPersonalNumber())) {
            throw new RuntimeException("Patient med personnummer " + patientDTO.getPersonalNumber() + " finns redan");
        }

        Patient patient = convertToEntity(patientDTO);
        Patient savedPatient = patientRepository.save(patient);
        PatientDTO dto = convertToDTO(savedPatient);

        // Send event to Kafka
        try {
            kafkaTemplate.send(patientTopic, dto.getId().toString(), dto);
            System.out.println("=== Kafka: Skickade patient-händelse till topic: " + patientTopic);
        } catch (Exception e) {
            System.err.println("=== Kafka: Kunde inte skicka patient-händelse: " + e.getMessage());
        }

        return dto;
    }

    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient med ID " + id + " finns inte"));
        return convertToDTO(patient);
    }

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient med ID " + id + " finns inte"));

        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setEmail(patientDTO.getEmail());
        patient.setPhoneNumber(patientDTO.getPhoneNumber());
        patient.setAddress(patientDTO.getAddress());

        Patient updatedPatient = patientRepository.save(patient);
        PatientDTO dto = convertToDTO(updatedPatient);

        // Send update event to Kafka
        try {
            kafkaTemplate.send(patientTopic, dto.getId().toString(), dto);
            System.out.println("=== Kafka: Skickade patient-uppdaterings-händelse till topic: " + patientTopic);
        } catch (Exception e) {
            System.err.println("=== Kafka: Kunde inte skicka patient-uppdaterings-händelse: " + e.getMessage());
        }

        return dto;
    }

    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Patient med ID " + id + " finns inte");
        }
        patientRepository.deleteById(id);
    }

    private PatientDTO convertToDTO(Patient patient) {
        return new PatientDTO(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPersonalNumber(),
                patient.getDateOfBirth(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getAddress()
        );
    }

    private Patient convertToEntity(PatientDTO dto) {
        return new Patient(
                null,
                dto.getFirstName(),
                dto.getLastName(),
                dto.getPersonalNumber(),
                dto.getDateOfBirth(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getAddress()
        );
    }
}

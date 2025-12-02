package se.kth.lab3.patient_journal_backend_microservices.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import se.kth.lab3.patient_journal_backend_microservices.config.TestSecurityConfig;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;
import se.kth.lab3.patient_journal_backend_microservices.entity.Patient;
import se.kth.lab3.patient_journal_backend_microservices.repository.PatientRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private KafkaTemplate<String, PatientDTO> kafkaTemplate;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private PatientDTO testPatientDTO;

    @BeforeEach
    void setUp() {
        // Sätt Kafka topic för tester
        ReflectionTestUtils.setField(patientService, "patientTopic", "test-patient-events");

        testPatient = new Patient(
                1L,
                "Anna",
                "Andersson",
                "19900101-1234",
                LocalDate.of(1990, 1, 1),
                "anna@example.com",
                "0701234567",
                "Testgatan 1"
        );

        testPatientDTO = new PatientDTO(
                1L,
                "Anna",
                "Andersson",
                "19900101-1234",
                LocalDate.of(1990, 1, 1),
                "anna@example.com",
                "0701234567",
                "Testgatan 1"
        );
    }

    @Test
    void testCreatePatient_Success() {
        // Arrange
        when(patientRepository.existsByPersonalNumber(anyString())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        when(kafkaTemplate.send(eq("test-patient-events"), eq("1"), any(PatientDTO.class)))
                .thenReturn(null);

        // Act
        PatientDTO result = patientService.createPatient(testPatientDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Anna", result.getFirstName());
        assertEquals("Andersson", result.getLastName());
        assertEquals("19900101-1234", result.getPersonalNumber());

        verify(patientRepository).existsByPersonalNumber("19900101-1234");
        verify(patientRepository).save(any(Patient.class));
        verify(kafkaTemplate).send(eq("test-patient-events"), eq("1"), any(PatientDTO.class));
    }

    @Test
    void testCreatePatient_DuplicatePersonalNumber_ThrowsException() {
        // Arrange
        when(patientRepository.existsByPersonalNumber(anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.createPatient(testPatientDTO);
        });

        assertTrue(exception.getMessage().contains("finns redan"));
        verify(patientRepository, never()).save(any(Patient.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(PatientDTO.class));
    }

    @Test
    void testGetPatientById_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act
        PatientDTO result = patientService.getPatientById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Anna", result.getFirstName());
        verify(patientRepository).findById(1L);
    }

    @Test
    void testGetPatientById_NotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.getPatientById(999L);
        });

        assertTrue(exception.getMessage().contains("finns inte"));
    }

    @Test
    void testGetAllPatients_Success() {
        // Arrange
        Patient patient2 = new Patient(2L, "Erik", "Eriksson", "19850505-5678",
                LocalDate.of(1985, 5, 5), "erik@example.com", "0709876543", "Eriksgatan 2");

        when(patientRepository.findAll()).thenReturn(Arrays.asList(testPatient, patient2));

        // Act
        List<PatientDTO> results = patientService.getAllPatients();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Anna", results.get(0).getFirstName());
        assertEquals("Erik", results.get(1).getFirstName());
    }

    @Test
    void testUpdatePatient_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        when(kafkaTemplate.send(eq("test-patient-events"), eq("1"), any(PatientDTO.class)))
                .thenReturn(null);

        PatientDTO updateDTO = new PatientDTO(
                1L, "Anna", "Andersson-Berg", "19900101-1234",
                LocalDate.of(1990, 1, 1), "anna.berg@example.com",
                "0701234567", "Nya gatan 5"
        );

        // Act
        PatientDTO result = patientService.updatePatient(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(patientRepository).findById(1L);
        verify(patientRepository).save(any(Patient.class));
        verify(kafkaTemplate).send(eq("test-patient-events"), eq("1"), any(PatientDTO.class));
    }

    @Test
    void testDeletePatient_Success() {
        // Arrange
        when(patientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(patientRepository).deleteById(1L);

        // Act
        patientService.deletePatient(1L);

        // Assert
        verify(patientRepository).existsById(1L);
        verify(patientRepository).deleteById(1L);
    }

    @Test
    void testDeletePatient_NotFound_ThrowsException() {
        // Arrange
        when(patientRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.deletePatient(999L);
        });

        assertTrue(exception.getMessage().contains("finns inte"));
        verify(patientRepository, never()).deleteById(anyLong());
    }
}
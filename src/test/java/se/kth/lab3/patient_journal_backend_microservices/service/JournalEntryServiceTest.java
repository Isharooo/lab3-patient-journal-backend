package se.kth.lab3.patient_journal_backend_microservices.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import se.kth.lab3.patient_journal_backend_microservices.dto.JournalEntryDTO;
import se.kth.lab3.patient_journal_backend_microservices.entity.JournalEntry;
import se.kth.lab3.patient_journal_backend_microservices.entity.Patient;
import se.kth.lab3.patient_journal_backend_microservices.repository.JournalEntryRepository;
import se.kth.lab3.patient_journal_backend_microservices.repository.PatientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private KafkaTemplate<String, JournalEntryDTO> kafkaTemplate;

    @InjectMocks
    private JournalEntryService journalEntryService;

    private Patient testPatient;
    private JournalEntry testJournalEntry;
    private JournalEntryDTO testJournalEntryDTO;

    @BeforeEach
    void setUp() {
        // Sätt Kafka topic för tester
        ReflectionTestUtils.setField(journalEntryService, "journalTopic", "test-journal-events");

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

        testJournalEntry = new JournalEntry(
                1L,
                testPatient,
                "Patient klagade på huvudvärk",
                LocalDateTime.now(),
                "Migrän",
                "Smärtstillande medicin"
        );

        testJournalEntryDTO = new JournalEntryDTO(
                1L,
                1L,
                "Patient klagade på huvudvärk",
                LocalDateTime.now(),
                "Migrän",
                "Smärtstillande medicin"
        );
    }

    @Test
    void testCreateJournalEntry_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(testJournalEntry);
        when(kafkaTemplate.send(eq("test-journal-events"), eq("1"), any(JournalEntryDTO.class)))
                .thenReturn(null);

        // Act
        JournalEntryDTO result = journalEntryService.createJournalEntry(testJournalEntryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals("Patient klagade på huvudvärk", result.getNote());
        assertEquals("Migrän", result.getDiagnosis());

        verify(patientRepository).findById(1L);
        verify(journalEntryRepository).save(any(JournalEntry.class));
        verify(kafkaTemplate).send(eq("test-journal-events"), eq("1"), any(JournalEntryDTO.class));
    }

    @Test
    void testCreateJournalEntry_PatientNotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());
        testJournalEntryDTO = new JournalEntryDTO(null, 999L, "Note", LocalDateTime.now(), "Diagnosis", "Treatment");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            journalEntryService.createJournalEntry(testJournalEntryDTO);
        });

        assertTrue(exception.getMessage().contains("finns inte"));
        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(JournalEntryDTO.class));
    }

    @Test
    void testGetJournalEntryById_Success() {
        // Arrange
        when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(testJournalEntry));

        // Act
        JournalEntryDTO result = journalEntryService.getJournalEntryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Patient klagade på huvudvärk", result.getNote());
        verify(journalEntryRepository).findById(1L);
    }

    @Test
    void testGetJournalEntryById_NotFound_ThrowsException() {
        // Arrange
        when(journalEntryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            journalEntryService.getJournalEntryById(999L);
        });

        assertTrue(exception.getMessage().contains("finns inte"));
    }

    @Test
    void testGetAllJournalEntries_Success() {
        // Arrange
        JournalEntry entry2 = new JournalEntry(2L, testPatient, "Följdbesök",
                LocalDateTime.now(), "Förbättring", "Fortsatt behandling");

        when(journalEntryRepository.findAll()).thenReturn(Arrays.asList(testJournalEntry, entry2));

        // Act
        List<JournalEntryDTO> results = journalEntryService.getAllJournalEntries();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testGetJournalEntriesByPatientId_Success() {
        // Arrange
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(journalEntryRepository.findByPatientIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(testJournalEntry));

        // Act
        List<JournalEntryDTO> results = journalEntryService.getJournalEntriesByPatientId(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getPatientId());
    }

    @Test
    void testGetJournalEntriesByPatientId_PatientNotFound_ThrowsException() {
        // Arrange
        when(patientRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            journalEntryService.getJournalEntriesByPatientId(999L);
        });

        assertTrue(exception.getMessage().contains("finns inte"));
    }

    @Test
    void testUpdateJournalEntry_Success() {
        // Arrange
        when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(testJournalEntry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(testJournalEntry);
        when(kafkaTemplate.send(eq("test-journal-events"), eq("1"), any(JournalEntryDTO.class)))
                .thenReturn(null);

        JournalEntryDTO updateDTO = new JournalEntryDTO(
                1L, 1L, "Uppdaterad anteckning",
                LocalDateTime.now(), "Ny diagnos", "Ny behandling"
        );

        // Act
        JournalEntryDTO result = journalEntryService.updateJournalEntry(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(journalEntryRepository).findById(1L);
        verify(journalEntryRepository).save(any(JournalEntry.class));
        verify(kafkaTemplate).send(eq("test-journal-events"), eq("1"), any(JournalEntryDTO.class));
    }

    @Test
    void testDeleteJournalEntry_Success() {
        // Arrange
        when(journalEntryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(journalEntryRepository).deleteById(1L);

        // Act
        journalEntryService.deleteJournalEntry(1L);

        // Assert
        verify(journalEntryRepository).existsById(1L);
        verify(journalEntryRepository).deleteById(1L);
    }

    @Test
    void testDeleteJournalEntry_NotFound_ThrowsException() {
        // Arrange
        when(journalEntryRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            journalEntryService.deleteJournalEntry(999L);
        });

        assertTrue(exception.getMessage().contains("finns inte"));
        verify(journalEntryRepository, never()).deleteById(anyLong());
    }
}
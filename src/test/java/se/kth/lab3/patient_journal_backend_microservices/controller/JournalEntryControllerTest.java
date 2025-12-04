package se.kth.lab3.patient_journal_backend_microservices.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import se.kth.lab3.patient_journal_backend_microservices.config.TestSecurityConfig;
import se.kth.lab3.patient_journal_backend_microservices.dto.JournalEntryDTO;
import se.kth.lab3.patient_journal_backend_microservices.service.JournalEntryService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JournalEntryController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class JournalEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JournalEntryService journalEntryService;

    private JournalEntryDTO testJournalEntryDTO;

    @BeforeEach
    void setUp() {
        testJournalEntryDTO = new JournalEntryDTO(
                1L, 1L, "Patient klagade på huvudvärk",
                LocalDateTime.now(), "Migrän", "Smärtstillande medicin"
        );
    }

    @Test
    void testCreateJournalEntry_Success() throws Exception {
        when(journalEntryService.createJournalEntry(any(JournalEntryDTO.class)))
                .thenReturn(testJournalEntryDTO);

        mockMvc.perform(post("/api/journal-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testJournalEntryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note").value("Patient klagade på huvudvärk"));
    }
}
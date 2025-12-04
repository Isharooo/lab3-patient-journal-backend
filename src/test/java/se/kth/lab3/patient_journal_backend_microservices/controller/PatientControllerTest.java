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
import se.kth.lab3.patient_journal_backend_microservices.config.TestControllerAdvice;
import se.kth.lab3.patient_journal_backend_microservices.config.TestSecurityConfig;
import se.kth.lab3.patient_journal_backend_microservices.dto.PatientDTO;
import se.kth.lab3.patient_journal_backend_microservices.service.PatientService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@ActiveProfiles("test")
@Import({TestControllerAdvice.class, TestSecurityConfig.class})
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientService patientService;

    private PatientDTO testPatientDTO;

    @BeforeEach
    void setUp() {
        testPatientDTO = new PatientDTO(
                1L, "Anna", "Andersson", "19900101-1234",
                LocalDate.of(1990, 1, 1), "anna@example.com",
                "0701234567", "Testgatan 1"
        );
    }

    @Test
    void testCreatePatient_Success() throws Exception {
        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(testPatientDTO);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Anna"));
    }

    @Test
    void testCreatePatient_InvalidData_ReturnsBadRequest() throws Exception {
        PatientDTO invalidPatient = new PatientDTO(); // Tomma fält

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPatient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPatientById_Success() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(testPatientDTO);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Anna"));
    }

    @Test
    void testGetPatientById_NotFound() throws Exception {
        when(patientService.getPatientById(999L))
                .thenThrow(new RuntimeException("Patient med ID 999 finns inte"));

        mockMvc.perform(get("/api/patients/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllPatients_Success() throws Exception {
        when(patientService.getAllPatients()).thenReturn(Arrays.asList(testPatientDTO));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testUpdatePatient_Success() throws Exception {
        when(patientService.updatePatient(eq(1L), any(PatientDTO.class))).thenReturn(testPatientDTO);

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPatientDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeletePatient_Success() throws Exception {
        doNothing().when(patientService).deletePatient(1L);

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());
    }
}
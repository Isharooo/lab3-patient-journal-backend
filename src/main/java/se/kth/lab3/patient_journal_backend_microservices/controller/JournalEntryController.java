package se.kth.lab3.patient_journal_backend_microservices.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.kth.lab3.patient_journal_backend_microservices.dto.JournalEntryDTO;
import se.kth.lab3.patient_journal_backend_microservices.service.JournalEntryService;

import java.util.List;

@RestController
@RequestMapping("/api/journal-entries")
@RequiredArgsConstructor
// OBS: @CrossOrigin borttagen här
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @PostMapping
    public ResponseEntity<JournalEntryDTO> createJournalEntry(@Valid @RequestBody JournalEntryDTO journalEntryDTO) {
        JournalEntryDTO createdEntry = journalEntryService.createJournalEntry(journalEntryDTO);
        return new ResponseEntity<>(createdEntry, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntryDTO> getJournalEntryById(@PathVariable Long id) {
        JournalEntryDTO entry = journalEntryService.getJournalEntryById(id);
        return ResponseEntity.ok(entry);
    }

    @GetMapping
    public ResponseEntity<List<JournalEntryDTO>> getAllJournalEntries() {
        List<JournalEntryDTO> entries = journalEntryService.getAllJournalEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<JournalEntryDTO>> getJournalEntriesByPatientId(@PathVariable Long patientId) {
        List<JournalEntryDTO> entries = journalEntryService.getJournalEntriesByPatientId(patientId);
        return ResponseEntity.ok(entries);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntryDTO> updateJournalEntry(
            @PathVariable Long id,
            @Valid @RequestBody JournalEntryDTO journalEntryDTO) {
        JournalEntryDTO updatedEntry = journalEntryService.updateJournalEntry(id, journalEntryDTO);
        return ResponseEntity.ok(updatedEntry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJournalEntry(@PathVariable Long id) {
        journalEntryService.deleteJournalEntry(id);
        return ResponseEntity.noContent().build();
    }
}
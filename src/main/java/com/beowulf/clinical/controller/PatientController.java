package com.beowulf.clinical.controller;

import com.beowulf.clinical.dto.PatientRequest;
import com.beowulf.clinical.dto.PatientUpdateRequest;
import com.beowulf.clinical.entity.Patient;
import com.beowulf.clinical.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Patient management endpoints")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @Operation(summary = "Create a new patient")
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody PatientRequest request) {
        Patient patient = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(patient);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID")
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping
    @Operation(summary = "List all patients or find by MRN")
    public ResponseEntity<?> getPatients(@RequestParam(required = false) String mrn) {
        if (mrn != null) {
            Optional<Patient> patient = patientService.findByMrn(mrn);
            return patient.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }
}

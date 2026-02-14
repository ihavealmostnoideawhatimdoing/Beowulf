package com.beowulf.clinical.service;

import com.beowulf.clinical.dto.PatientRequest;
import com.beowulf.clinical.dto.PatientUpdateRequest;
import com.beowulf.clinical.entity.Patient;
import com.beowulf.clinical.exception.ConflictException;
import com.beowulf.clinical.exception.ResourceNotFoundException;
import com.beowulf.clinical.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Patient createPatient(PatientRequest request) {
        if (patientRepository.existsByMrn(request.getMrn())) {
            throw new ConflictException("Patient with MRN '" + request.getMrn() + "' already exists");
        }
        LocalDate dob = parseDate(request.getDateOfBirth());
        Patient patient = new Patient(request.getMrn(), request.getFirstName(), request.getLastName(), dob);
        return patientRepository.save(patient);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    public Optional<Patient> findByMrn(String mrn) {
        return patientRepository.findByMrn(mrn);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Transactional
    public Patient updatePatient(Long id, PatientUpdateRequest request) {
        Patient patient = getPatientById(id);
        LocalDate dob = parseDate(request.getDateOfBirth());
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(dob);
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient findOrCreatePatient(String mrn, String firstName, String lastName, LocalDate dateOfBirth) {
        Optional<Patient> existing = patientRepository.findByMrn(mrn);
        if (existing.isPresent()) {
            Patient patient = existing.get();
            boolean nameChanged = !patient.getFirstName().equals(firstName) || !patient.getLastName().equals(lastName);
            boolean dobChanged = !patient.getDateOfBirth().equals(dateOfBirth);
            if (nameChanged || dobChanged) {
                throw new ConflictException(
                    "Patient with MRN '" + mrn + "' already exists with different demographics. " +
                    "Existing: " + patient.getFirstName() + " " + patient.getLastName() + " (DOB: " + patient.getDateOfBirth() + "). " +
                    "Use the patient update endpoint to change demographics first.");
            }
            return patient;
        }
        Patient patient = new Patient(mrn, firstName, lastName, dateOfBirth);
        return patientRepository.save(patient);
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO 8601 (YYYY-MM-DD)");
        }
    }
}

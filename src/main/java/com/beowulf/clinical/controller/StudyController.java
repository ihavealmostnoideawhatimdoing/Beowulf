package com.beowulf.clinical.controller;

import com.beowulf.clinical.dto.StudyUpdateRequest;
import com.beowulf.clinical.entity.Study;
import com.beowulf.clinical.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Studies", description = "Study management endpoints")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @GetMapping("/api/studies/{id}")
    @Operation(summary = "Get study by ID")
    public ResponseEntity<Study> getStudy(@PathVariable Long id) {
        return ResponseEntity.ok(studyService.getStudyById(id));
    }

    @PatchMapping("/api/studies/{id}")
    @Operation(summary = "Update study (set reportText, change status)")
    public ResponseEntity<Study> updateStudy(@PathVariable Long id, @Valid @RequestBody StudyUpdateRequest request) {
        return ResponseEntity.ok(studyService.updateStudy(id, request));
    }

    @DeleteMapping("/api/studies/{id}")
    @Operation(summary = "Delete study (only if not FINALIZED/AMENDED)")
    public ResponseEntity<Void> deleteStudy(@PathVariable Long id) {
        studyService.deleteStudy(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/orders/{orderId}/study")
    @Operation(summary = "Get study for a specific order")
    public ResponseEntity<Study> getStudyByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(studyService.getStudyByOrderId(orderId));
    }
}

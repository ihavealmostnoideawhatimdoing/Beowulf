package com.beowulf.clinical.controller;

import com.beowulf.clinical.service.OrderService;
import com.beowulf.clinical.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Reporting endpoints")
public class ReportController {

    private final StudyService studyService;
    private final OrderService orderService;

    public ReportController(StudyService studyService, OrderService orderService) {
        this.studyService = studyService;
        this.orderService = orderService;
    }

    @GetMapping("/study-status-summary")
    @Operation(summary = "Count of studies by status")
    public ResponseEntity<Map<String, Long>> getStudyStatusSummary() {
        return ResponseEntity.ok(studyService.getStudyStatusSummary());
    }

    @GetMapping("/orders-by-type")
    @Operation(summary = "Count of orders by type")
    public ResponseEntity<Map<String, Long>> getOrdersByType() {
        return ResponseEntity.ok(orderService.getOrdersByType());
    }
}

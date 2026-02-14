package com.beowulf.clinical.controller;

import com.beowulf.clinical.dto.OrderRequest;
import com.beowulf.clinical.entity.Order;
import com.beowulf.clinical.entity.Study;
import com.beowulf.clinical.service.OrderService;
import com.beowulf.clinical.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;
    private final StudyService studyService;

    public OrderController(OrderService orderService, StudyService studyService) {
        this.orderService = orderService;
        this.studyService = studyService;
    }

    @PostMapping
    @Operation(summary = "Create a new order (triggers patient create/update + study creation)")
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request);
        Study study = studyService.getStudyByOrderId(order.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", order.getId());
        response.put("patientId", order.getPatientId());
        response.put("mrn", order.getMrn());
        response.put("firstName", order.getFirstName());
        response.put("lastName", order.getLastName());
        response.put("dateOfBirth", order.getDateOfBirth());
        response.put("type", order.getType());
        response.put("studyId", study.getId());
        response.put("createDate", order.getCreateDate());
        response.put("updateDate", order.getUpdateDate());

        if ("LUPUS".equalsIgnoreCase(order.getMrn())) {
            response.put("easterEgg", "It's not lupus. (Except that one time it was.)");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID with nested study")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        Study study = studyService.getStudyByOrderId(order.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", order.getId());
        response.put("patientId", order.getPatientId());
        response.put("mrn", order.getMrn());
        response.put("firstName", order.getFirstName());
        response.put("lastName", order.getLastName());
        response.put("dateOfBirth", order.getDateOfBirth());
        response.put("type", order.getType());
        response.put("createDate", order.getCreateDate());
        response.put("updateDate", order.getUpdateDate());

        Map<String, Object> studyMap = new LinkedHashMap<>();
        studyMap.put("id", study.getId());
        studyMap.put("orderId", study.getOrderId());
        studyMap.put("reportText", study.getReportText());
        studyMap.put("status", study.getStatus());
        studyMap.put("version", study.getVersion());
        studyMap.put("createDate", study.getCreateDate());
        studyMap.put("updateDate", study.getUpdateDate());
        response.put("study", studyMap);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all orders with optional filters")
    public ResponseEntity<List<Map<String, Object>>> getOrders(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String type) {
        List<Order> orders = orderService.getAllOrders(patientId, type);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getId());
            map.put("patientId", order.getPatientId());
            map.put("mrn", order.getMrn());
            map.put("firstName", order.getFirstName());
            map.put("lastName", order.getLastName());
            map.put("dateOfBirth", order.getDateOfBirth());
            map.put("type", order.getType());
            map.put("createDate", order.getCreateDate());
            map.put("updateDate", order.getUpdateDate());
            try {
                Study study = studyService.getStudyByOrderId(order.getId());
                map.put("studyStatus", study.getStatus().name());
            } catch (Exception e) {
                map.put("studyStatus", "UNKNOWN");
            }
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}

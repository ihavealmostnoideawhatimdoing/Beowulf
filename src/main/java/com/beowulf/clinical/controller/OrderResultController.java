package com.beowulf.clinical.controller;

import com.beowulf.clinical.entity.OrderResult;
import com.beowulf.clinical.service.OrderResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Tag(name = "Order Results", description = "OrderResult read-only endpoints")
public class OrderResultController {

    private final OrderResultService orderResultService;

    public OrderResultController(OrderResultService orderResultService) {
        this.orderResultService = orderResultService;
    }

    @GetMapping("/api/results/{id}")
    @Operation(summary = "Get specific result version by ID")
    public ResponseEntity<OrderResult> getResult(@PathVariable Long id) {
        return ResponseEntity.ok(orderResultService.getResultById(id));
    }

    @GetMapping("/api/orders/{orderId}/results")
    @Operation(summary = "Get current result for an order")
    public ResponseEntity<OrderResult> getCurrentResult(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderResultService.getCurrentResultForOrder(orderId));
    }

    @GetMapping("/api/orders/{orderId}/results/history")
    @Operation(summary = "Get all result versions for an order")
    public ResponseEntity<List<OrderResult>> getResultHistory(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderResultService.getResultHistory(orderId));
    }
}

package com.beowulf.clinical.service;

import com.beowulf.clinical.entity.OrderResult;
import com.beowulf.clinical.exception.ResourceNotFoundException;
import com.beowulf.clinical.repository.OrderResultRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderResultService {

    private final OrderResultRepository orderResultRepository;

    public OrderResultService(OrderResultRepository orderResultRepository) {
        this.orderResultRepository = orderResultRepository;
    }

    public OrderResult getResultById(Long id) {
        return orderResultRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OrderResult not found with id: " + id));
    }

    public OrderResult getCurrentResultForOrder(Long orderId) {
        return orderResultRepository.findByOrderIdAndIsCurrentTrue(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("No current result found for order id: " + orderId));
    }

    public List<OrderResult> getResultHistory(Long orderId) {
        return orderResultRepository.findByOrderIdOrderByVersionAsc(orderId);
    }
}

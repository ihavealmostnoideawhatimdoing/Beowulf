package com.beowulf.clinical.service;

import com.beowulf.clinical.dto.OrderRequest;
import com.beowulf.clinical.entity.Order;
import com.beowulf.clinical.entity.Patient;
import com.beowulf.clinical.entity.Study;
import com.beowulf.clinical.enums.OrderType;
import com.beowulf.clinical.enums.StudyStatus;
import com.beowulf.clinical.exception.ResourceNotFoundException;
import com.beowulf.clinical.repository.OrderRepository;
import com.beowulf.clinical.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final StudyRepository studyRepository;
    private final PatientService patientService;

    public OrderService(OrderRepository orderRepository, StudyRepository studyRepository, PatientService patientService) {
        this.orderRepository = orderRepository;
        this.studyRepository = studyRepository;
        this.patientService = patientService;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        LocalDate dob;
        try {
            dob = LocalDate.parse(request.getDateOfBirth());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO 8601 (YYYY-MM-DD)");
        }

        OrderType orderType;
        try {
            orderType = OrderType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order type: " + request.getType() +
                ". Valid types: ECHO, XRAY, LAB, MRI, CT, ULTRASOUND");
        }

        Patient patient = patientService.findOrCreatePatient(
            request.getMrn(), request.getFirstName(), request.getLastName(), dob);

        Order order = new Order();
        order.setPatientId(patient.getId());
        order.setMrn(request.getMrn());
        order.setFirstName(request.getFirstName());
        order.setLastName(request.getLastName());
        order.setDateOfBirth(dob);
        order.setType(orderType);
        order = orderRepository.save(order);

        Study study = new Study();
        study.setOrderId(order.getId());
        study.setStatus(StudyStatus.ORDERED);
        studyRepository.save(study);

        return order;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<Order> getAllOrders(Long patientId, String type) {
        OrderType orderType = null;
        if (type != null) {
            try {
                orderType = OrderType.valueOf(type);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid order type: " + type +
                    ". Valid types: ECHO, XRAY, LAB, MRI, CT, ULTRASOUND");
            }
        }
        if (patientId != null && orderType != null) {
            return orderRepository.findByPatientIdAndType(patientId, orderType);
        }
        if (patientId != null) {
            return orderRepository.findByPatientId(patientId);
        }
        if (orderType != null) {
            return orderRepository.findByType(orderType);
        }
        return orderRepository.findAll();
    }

    public Map<String, Long> getOrdersByType() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : orderRepository.countByType()) {
            result.put(row[0].toString(), (Long) row[1]);
        }
        return result;
    }
}

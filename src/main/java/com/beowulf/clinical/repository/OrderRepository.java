package com.beowulf.clinical.repository;

import com.beowulf.clinical.entity.Order;
import com.beowulf.clinical.enums.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPatientId(Long patientId);
    List<Order> findByType(OrderType type);
    List<Order> findByPatientIdAndType(Long patientId, OrderType type);

    @Query("SELECT o.type AS type, COUNT(o) AS count FROM Order o GROUP BY o.type")
    List<Object[]> countByType();
}

package com.beowulf.clinical.repository;

import com.beowulf.clinical.entity.OrderResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderResultRepository extends JpaRepository<OrderResult, Long> {
    Optional<OrderResult> findByOrderIdAndIsCurrentTrue(Long orderId);
    List<OrderResult> findByOrderIdOrderByVersionAsc(Long orderId);
}

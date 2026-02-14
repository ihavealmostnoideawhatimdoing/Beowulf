package com.beowulf.clinical.repository;

import com.beowulf.clinical.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long> {
    Optional<Study> findByOrderId(Long orderId);

    @Query("SELECT s.status AS status, COUNT(s) AS count FROM Study s GROUP BY s.status")
    List<Object[]> countByStatus();
}

package com.beowulf.clinical.service;

import com.beowulf.clinical.dto.StudyUpdateRequest;
import com.beowulf.clinical.entity.OrderResult;
import com.beowulf.clinical.entity.Study;
import com.beowulf.clinical.enums.ResultStatus;
import com.beowulf.clinical.enums.StudyStatus;
import com.beowulf.clinical.exception.BusinessRuleException;
import com.beowulf.clinical.exception.ConflictException;
import com.beowulf.clinical.exception.ResourceNotFoundException;
import com.beowulf.clinical.repository.OrderResultRepository;
import com.beowulf.clinical.repository.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final OrderResultRepository orderResultRepository;

    public StudyService(StudyRepository studyRepository, OrderResultRepository orderResultRepository) {
        this.studyRepository = studyRepository;
        this.orderResultRepository = orderResultRepository;
    }

    public Study getStudyById(Long id) {
        return studyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Study not found with id: " + id));
    }

    public Study getStudyByOrderId(Long orderId) {
        return studyRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Study not found for order id: " + orderId));
    }

    @Transactional
    public Study updateStudy(Long id, StudyUpdateRequest request) {
        Study study = getStudyById(id);

        if (!study.getVersion().equals(request.getVersion())) {
            throw new ConflictException("Study was modified by another user. Please refresh and try again.");
        }

        if (request.getStatus() != null) {
            StudyStatus newStatus;
            try {
                newStatus = StudyStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + request.getStatus());
            }

            switch (newStatus) {
                case FINALIZED:
                    return finalizeStudy(study, request.getReportText());
                case AMENDED:
                    return amendStudy(study, request.getReportText());
                case CANCELED:
                    return cancelStudy(study);
                case ORDERED:
                    throw new BusinessRuleException("Cannot set status back to ORDERED");
            }
        }

        if (request.getReportText() != null) {
            if (study.getStatus() != StudyStatus.ORDERED) {
                throw new BusinessRuleException("Can only update report text when study status is ORDERED");
            }
            study.setReportText(request.getReportText());
        }

        return studyRepository.save(study);
    }

    private Study finalizeStudy(Study study, String reportText) {
        if (study.getStatus() != StudyStatus.ORDERED) {
            throw new BusinessRuleException("Can only finalize studies with status ORDERED. Current status: " + study.getStatus());
        }

        if (reportText != null) {
            study.setReportText(reportText);
        }

        if (study.getReportText() == null || study.getReportText().isBlank()) {
            throw new BusinessRuleException("Cannot finalize study without report text");
        }

        study.setStatus(StudyStatus.FINALIZED);

        OrderResult result = new OrderResult();
        result.setOrderId(study.getOrderId());
        result.setVersion(1);
        result.setReport(study.getReportText());
        result.setResultType("DIAGNOSTIC_REPORT");
        result.setStatus(ResultStatus.FINALIZED);
        result.setSignedOn(LocalDateTime.now());
        result.setIsCurrent(true);
        result.setSupersededById(null);
        orderResultRepository.save(result);

        return studyRepository.save(study);
    }

    private Study amendStudy(Study study, String reportText) {
        if (study.getStatus() != StudyStatus.FINALIZED && study.getStatus() != StudyStatus.AMENDED) {
            throw new BusinessRuleException("Can only amend studies with status FINALIZED or AMENDED. Current status: " + study.getStatus());
        }

        if (reportText == null || reportText.isBlank()) {
            throw new BusinessRuleException("Report text is required for amendment");
        }

        study.setReportText(reportText);
        study.setStatus(StudyStatus.AMENDED);

        Optional<OrderResult> currentResult = orderResultRepository.findByOrderIdAndIsCurrentTrue(study.getOrderId());
        int newVersion = currentResult.map(r -> r.getVersion() + 1).orElse(1);

        if (currentResult.isPresent()) {
            OrderResult prev = currentResult.get();
            prev.setIsCurrent(false);
            orderResultRepository.save(prev);
            orderResultRepository.flush();
        }

        OrderResult newResult = new OrderResult();
        newResult.setOrderId(study.getOrderId());
        newResult.setVersion(newVersion);
        newResult.setReport(reportText);
        newResult.setResultType("DIAGNOSTIC_REPORT");
        newResult.setStatus(ResultStatus.AMENDED);
        newResult.setSignedOn(LocalDateTime.now());
        newResult.setIsCurrent(true);
        newResult.setSupersededById(null);
        OrderResult savedNewResult = orderResultRepository.save(newResult);

        if (currentResult.isPresent()) {
            OrderResult prev = currentResult.get();
            prev.setSupersededById(savedNewResult.getId());
            orderResultRepository.save(prev);
        }

        return studyRepository.save(study);
    }

    private Study cancelStudy(Study study) {
        if (study.getStatus() != StudyStatus.ORDERED) {
            throw new BusinessRuleException("Can only cancel studies with status ORDERED. Current status: " + study.getStatus());
        }
        study.setStatus(StudyStatus.CANCELED);
        return studyRepository.save(study);
    }

    @Transactional
    public void deleteStudy(Long id) {
        Study study = getStudyById(id);
        if (study.getStatus() == StudyStatus.FINALIZED || study.getStatus() == StudyStatus.AMENDED) {
            throw new BusinessRuleException("Cannot delete finalized or amended study");
        }
        studyRepository.delete(study);
    }

    public Map<String, Long> getStudyStatusSummary() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : studyRepository.countByStatus()) {
            result.put(row[0].toString(), (Long) row[1]);
        }
        return result;
    }
}

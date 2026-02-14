package com.beowulf.clinical.dto;

import jakarta.validation.constraints.NotNull;

public class StudyUpdateRequest {

    private String status;
    private String reportText;

    @NotNull(message = "Field 'version' is required for optimistic locking")
    private Long version;

    public StudyUpdateRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReportText() { return reportText; }
    public void setReportText(String reportText) { this.reportText = reportText; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

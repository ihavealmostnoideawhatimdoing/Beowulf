package com.beowulf.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrderRequest {

    @NotBlank(message = "Field 'mrn' is required")
    @Size(max = 50, message = "MRN must be at most 50 characters")
    private String mrn;

    @NotBlank(message = "Field 'firstName' is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Field 'lastName' is required")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @NotNull(message = "Field 'dateOfBirth' is required")
    private String dateOfBirth;

    @NotNull(message = "Field 'type' is required")
    private String type;

    public OrderRequest() {}

    public OrderRequest(String mrn, String firstName, String lastName, String dateOfBirth, String type) {
        this.mrn = mrn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.type = type;
    }

    public String getMrn() { return mrn; }
    public void setMrn(String mrn) { this.mrn = mrn; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

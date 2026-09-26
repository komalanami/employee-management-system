package com.portfolio.ems.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmployeeRequest {

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phone;

    @NotNull(message = "Salary cannot be null")
    @DecimalMin(value = "0.01", message = "Salary must be greater than 0")
    @DecimalMax(value = "9999999.99", message = "Salary is too large")
    private BigDecimal salary;

    @NotBlank(message = "Designation cannot be blank")
    @Size(min = 2, max = 100, message = "Designation must be between 2 and 100 characters")
    private String designation;

    @NotNull(message = "Joining date cannot be null")
    @PastOrPresent(message = "Joining date cannot be in the future")
    private LocalDate joiningDate;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "Department ID cannot be null")
    private Long departmentId;
}

package com.portfolio.ems.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDepartmentRequest {

    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;

    @Size(min = 2, max = 200, message = "Location must be between 2 and 200 characters")
    private String location;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}

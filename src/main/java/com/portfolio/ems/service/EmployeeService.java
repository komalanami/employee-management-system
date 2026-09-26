package com.portfolio.ems.service;

import com.portfolio.ems.dto.request.CreateEmployeeRequest;
import com.portfolio.ems.dto.request.UpdateEmployeeRequest;
import com.portfolio.ems.dto.response.EmployeeResponse;
import com.portfolio.ems.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface EmployeeService {

    /**
     * Create a new employee
     */
    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    /**
     * Get employee by ID
     */
    EmployeeResponse getEmployeeById(Long id);

    /**
     * Get all employees with pagination
     */
    PageResponse<EmployeeResponse> getAllEmployees(Pageable pageable);

    /**
     * Update employee
     */
    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    /**
     * Soft delete employee
     */
    void deleteEmployee(Long id);

    /**
     * Search employees with advanced filtering
     */
    PageResponse<EmployeeResponse> searchEmployees(
            String firstName,
            String lastName,
            String email,
            String designation,
            Long departmentId,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Pageable pageable
    );

    /**
     * Get employees by department
     */
    PageResponse<EmployeeResponse> getEmployeesByDepartment(Long departmentId, Pageable pageable);

    /**
     * Get employees by designation
     */
    PageResponse<EmployeeResponse> getEmployeesByDesignation(String designation, Pageable pageable);
}

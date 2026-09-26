package com.portfolio.ems.service;

import com.portfolio.ems.dto.request.CreateDepartmentRequest;
import com.portfolio.ems.dto.request.UpdateDepartmentRequest;
import com.portfolio.ems.dto.response.DepartmentResponse;
import com.portfolio.ems.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {

    /**
     * Create a new department
     */
    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    /**
     * Get department by ID
     */
    DepartmentResponse getDepartmentById(Long id);

    /**
     * Get all departments with pagination
     */
    PageResponse<DepartmentResponse> getAllDepartments(Pageable pageable);

    /**
     * Update department
     */
    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);

    /**
     * Soft delete department
     */
    void deleteDepartment(Long id);

    /**
     * Search departments by name
     */
    PageResponse<DepartmentResponse> searchDepartmentsByName(String name, Pageable pageable);

    /**
     * Search departments by location
     */
    PageResponse<DepartmentResponse> searchDepartmentsByLocation(String location, Pageable pageable);
}

package com.portfolio.ems.service.impl;

import com.portfolio.ems.dto.request.CreateDepartmentRequest;
import com.portfolio.ems.dto.request.UpdateDepartmentRequest;
import com.portfolio.ems.dto.response.DepartmentResponse;
import com.portfolio.ems.dto.response.PageResponse;
import com.portfolio.ems.entity.Department;
import com.portfolio.ems.exception.InvalidRequestException;
import com.portfolio.ems.exception.ResourceNotFoundException;
import com.portfolio.ems.mapper.DepartmentMapper;
import com.portfolio.ems.repository.DepartmentRepository;
import com.portfolio.ems.repository.EmployeeRepository;
import com.portfolio.ems.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating new department: {}", request.getName());

        // Validate name uniqueness
        if (departmentRepository.existsByNameAndNotDeleted(request.getName())) {
            log.warn("Department name already exists: {}", request.getName());
            throw new InvalidRequestException("Department name already exists");
        }

        // Create department
        Department department = departmentMapper.toEntity(request);
        department = departmentRepository.save(department);

        log.info("Department created successfully with ID: {}", department.getId());
        return departmentMapper.toResponseWithoutEmployees(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        log.info("Fetching department with ID: {}", id);

        Department department = departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> {
                    log.error("Department not found: {}", id);
                    return new ResourceNotFoundException("Department not found with ID: " + id);
                });

        return departmentMapper.toResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> getAllDepartments(Pageable pageable) {
        log.info("Fetching all departments with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Department> page = departmentRepository.findAllNotDeleted(pageable);
        Page<DepartmentResponse> responsePage = page.map(departmentMapper::toResponseWithoutEmployees);

        return PageResponse.from(responsePage);
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        log.info("Updating department with ID: {}", id);

        Department department = departmentRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.error("Department not found: {}", id);
                    return new ResourceNotFoundException("Department not found with ID: " + id);
                });

        // Validate name uniqueness if name is being updated
        if (request.getName() != null && !request.getName().equals(department.getName())) {
            if (departmentRepository.existsByNameAndNotDeleted(request.getName(), id)) {
                log.warn("Department name already exists: {}", request.getName());
                throw new InvalidRequestException("Department name already exists");
            }
        }

        // Update department fields
        departmentMapper.updateEntityFromRequest(request, department);
        department = departmentRepository.save(department);

        log.info("Department updated successfully: {}", id);
        return departmentMapper.toResponseWithoutEmployees(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        log.info("Deleting department with ID: {}", id);

        Department department = departmentRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.error("Department not found: {}", id);
                    return new ResourceNotFoundException("Department not found with ID: " + id);
                });

        // Check if department has active employees
        Long activeEmployeeCount = employeeRepository.countActiveByDepartmentIdAndNotDeleted(id);
        if (activeEmployeeCount > 0) {
            log.warn("Cannot delete department with active employees: {}", id);
            throw new InvalidRequestException("Cannot delete department with active employees. Please deactivate or reassign employees first.");
        }

        // Soft delete
        department.setDeleted(true);
        department.setDeletedAt(LocalDateTime.now());
        departmentRepository.save(department);

        log.info("Department deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> searchDepartmentsByName(String name, Pageable pageable) {
        log.info("Searching departments by name: {}", name);

        Page<Department> page = departmentRepository.searchByNameAndNotDeleted(name, pageable);
        Page<DepartmentResponse> responsePage = page.map(departmentMapper::toResponseWithoutEmployees);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> searchDepartmentsByLocation(String location, Pageable pageable) {
        log.info("Searching departments by location: {}", location);

        Page<Department> page = departmentRepository.searchByLocationAndNotDeleted(location, pageable);
        Page<DepartmentResponse> responsePage = page.map(departmentMapper::toResponseWithoutEmployees);

        return PageResponse.from(responsePage);
    }
}

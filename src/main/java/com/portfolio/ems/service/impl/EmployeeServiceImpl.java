package com.portfolio.ems.service.impl;

import com.portfolio.ems.dto.request.CreateEmployeeRequest;
import com.portfolio.ems.dto.request.UpdateEmployeeRequest;
import com.portfolio.ems.dto.response.EmployeeResponse;
import com.portfolio.ems.dto.response.PageResponse;
import com.portfolio.ems.entity.Department;
import com.portfolio.ems.entity.Employee;
import com.portfolio.ems.exception.InvalidRequestException;
import com.portfolio.ems.exception.ResourceNotFoundException;
import com.portfolio.ems.mapper.EmployeeMapper;
import com.portfolio.ems.repository.DepartmentRepository;
import com.portfolio.ems.repository.EmployeeRepository;
import com.portfolio.ems.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Creating new employee: {}", request.getEmail());

        // Validate email uniqueness
        if (employeeRepository.existsByEmailAndNotDeleted(request.getEmail())) {
            log.warn("Email already in use: {}", request.getEmail());
            throw new InvalidRequestException("Email already in use");
        }

        // Fetch department
        Department department = departmentRepository.findByIdAndNotDeleted(request.getDepartmentId())
                .orElseThrow(() -> {
                    log.error("Department not found: {}", request.getDepartmentId());
                    return new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId());
                });

        // Create employee
        Employee employee = employeeMapper.toEntity(request, department);
        employee = employeeRepository.save(employee);

        log.info("Employee created successfully with ID: {}", employee.getId());
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);

        Employee employee = employeeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.error("Employee not found: {}", id);
                    return new ResourceNotFoundException("Employee not found with ID: " + id);
                });

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAllEmployees(Pageable pageable) {
        log.info("Fetching all employees with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Employee> page = employeeRepository.findAllNotDeleted(pageable);
        Page<EmployeeResponse> responsePage = page.map(employeeMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        log.info("Updating employee with ID: {}", id);

        Employee employee = employeeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.error("Employee not found: {}", id);
                    return new ResourceNotFoundException("Employee not found with ID: " + id);
                });

        // Validate email uniqueness if email is being updated
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmailAndNotDeleted(request.getEmail(), id)) {
                log.warn("Email already in use: {}", request.getEmail());
                throw new InvalidRequestException("Email already in use");
            }
        }

        // Validate department if department is being updated
        if (request.getDepartmentId() != null && !request.getDepartmentId().equals(employee.getDepartment().getId())) {
            Department newDepartment = departmentRepository.findByIdAndNotDeleted(request.getDepartmentId())
                    .orElseThrow(() -> {
                        log.error("Department not found: {}", request.getDepartmentId());
                        return new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId());
                    });
            employee.setDepartment(newDepartment);
        }

        // Update employee fields
        employeeMapper.updateEntityFromRequest(request, employee);
        employee = employeeRepository.save(employee);

        log.info("Employee updated successfully: {}", id);
        return employeeMapper.toResponse(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with ID: {}", id);

        Employee employee = employeeRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> {
                    log.error("Employee not found: {}", id);
                    return new ResourceNotFoundException("Employee not found with ID: " + id);
                });

        employee.setDeleted(true);
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);

        log.info("Employee deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(
            String firstName,
            String lastName,
            String email,
            String designation,
            Long departmentId,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Pageable pageable) {

        log.info("Searching employees with filters: firstName={}, departmentId={}", firstName, departmentId);

        Page<Employee> page = employeeRepository.searchEmployees(
                firstName, lastName, email, designation, departmentId, minSalary, maxSalary, pageable
        );

        Page<EmployeeResponse> responsePage = page.map(employeeMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        log.info("Fetching employees for department: {}", departmentId);

        // Verify department exists
        if (!departmentRepository.findByIdAndNotDeleted(departmentId).isPresent()) {
            log.error("Department not found: {}", departmentId);
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }

        Page<Employee> page = employeeRepository.findByDepartmentIdAndNotDeleted(departmentId, pageable);
        Page<EmployeeResponse> responsePage = page.map(employeeMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getEmployeesByDesignation(String designation, Pageable pageable) {
        log.info("Fetching employees with designation: {}", designation);

        Page<Employee> page = employeeRepository.findByDesignationAndNotDeleted(designation, pageable);
        Page<EmployeeResponse> responsePage = page.map(employeeMapper::toResponse);

        return PageResponse.from(responsePage);
    }
}

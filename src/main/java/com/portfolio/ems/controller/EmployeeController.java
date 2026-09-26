package com.portfolio.ems.controller;

import com.portfolio.ems.dto.request.CreateEmployeeRequest;
import com.portfolio.ems.dto.request.UpdateEmployeeRequest;
import com.portfolio.ems.dto.response.EmployeeResponse;
import com.portfolio.ems.dto.response.PageResponse;
import com.portfolio.ems.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        log.info("POST /api/employees - Creating employee: {}", request.getEmail());
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        log.info("GET /api/employees/{} - Fetching employee", id);
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<EmployeeResponse>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        log.info("GET /api/employees - Fetching all employees: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<EmployeeResponse> response = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {

        log.info("PUT /api/employees/{} - Updating employee", id);
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("DELETE /api/employees/{} - Deleting employee", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<EmployeeResponse>> searchEmployees(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        log.info("GET /api/employees/search - Searching employees: firstName={}, departmentId={}", firstName,
                departmentId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<EmployeeResponse> response = employeeService.searchEmployees(
                firstName, lastName, email, designation, departmentId, minSalary, maxSalary, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<PageResponse<EmployeeResponse>> getEmployeesByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        log.info("GET /api/employees/department/{} - Fetching employees by department", departmentId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<EmployeeResponse> response = employeeService.getEmployeesByDepartment(departmentId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/designation/{designation}")
    public ResponseEntity<PageResponse<EmployeeResponse>> getEmployeesByDesignation(
            @PathVariable String designation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        log.info("GET /api/employees/designation/{} - Fetching employees by designation", designation);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<EmployeeResponse> response = employeeService.getEmployeesByDesignation(designation, pageable);
        return ResponseEntity.ok(response);
    }

}
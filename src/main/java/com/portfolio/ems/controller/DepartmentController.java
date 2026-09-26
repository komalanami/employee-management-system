package com.portfolio.ems.controller;

import com.portfolio.ems.dto.request.CreateDepartmentRequest;
import com.portfolio.ems.dto.request.UpdateDepartmentRequest;
import com.portfolio.ems.dto.response.DepartmentResponse;
import com.portfolio.ems.dto.response.PageResponse;
import com.portfolio.ems.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        log.info("POST /api/departments - Creating department: {}", request.getName());
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        log.info("GET /api/departments/{} - Fetching department", id);
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<DepartmentResponse>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        log.info("GET /api/departments - Fetching all departments: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<DepartmentResponse> response = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {

        log.info("PUT /api/departments/{} - Updating department", id);
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        log.info("DELETE /api/departments/{} - Deleting department", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<PageResponse<DepartmentResponse>> searchDepartmentsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        log.info("GET /api/departments/search/by-name - Searching departments by name: {}", name);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<DepartmentResponse> response = departmentService.searchDepartmentsByName(name, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/by-location")
    public ResponseEntity<PageResponse<DepartmentResponse>> searchDepartmentsByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        log.info("GET /api/departments/search/by-location - Searching departments by location: {}", location);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<DepartmentResponse> response = departmentService.searchDepartmentsByLocation(location, pageable);
        return ResponseEntity.ok(response);
    }
}

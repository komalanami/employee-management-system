package com.portfolio.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.ems.dto.request.CreateEmployeeRequest;
import com.portfolio.ems.dto.request.UpdateEmployeeRequest;
import com.portfolio.ems.dto.response.EmployeeResponse;
import com.portfolio.ems.dto.response.PageResponse;
import com.portfolio.ems.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private CreateEmployeeRequest createEmployeeRequest;
    private EmployeeResponse employeeResponse;

    @BeforeEach
    void setUp() {
        createEmployeeRequest = CreateEmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .salary(new BigDecimal("75000.00"))
                .designation("Senior Engineer")
                .joiningDate(LocalDate.now())
                .address("123 Main St")
                .departmentId(1L)
                .build();

        employeeResponse = EmployeeResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .salary(new BigDecimal("75000.00"))
                .designation("Senior Engineer")
                .departmentId(1L)
                .departmentName("Engineering")
                .isActive(true)
                .build();
    }

    @Test
    void testCreateEmployeeSuccess() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createEmployeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.first_name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void testCreateEmployeeWithInvalidRequest() throws Exception {
        // Create request with missing required fields
        CreateEmployeeRequest invalidRequest = CreateEmployeeRequest.builder()
                .firstName("")  // Invalid: blank
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation_errors").isArray());
    }

    @Test
    void testGetEmployeeByIdSuccess() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L))
                .thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.first_name").value("John"));
    }

    @Test
    void testGetAllEmployees() throws Exception {
        // Arrange
        PageResponse<EmployeeResponse> pageResponse = PageResponse.<EmployeeResponse>builder()
                .content(Collections.singletonList(employeeResponse))
                .currentPage(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(employeeService.getAllEmployees(any(Pageable.class)))
                .thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees?page=0&size=20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.total_elements").value(1));
    }

    @Test
    void testUpdateEmployeeSuccess() throws Exception {
        // Arrange
        UpdateEmployeeRequest updateRequest = UpdateEmployeeRequest.builder()
                .firstName("Jane")
                .salary(new BigDecimal("85000.00"))
                .build();

        when(employeeService.updateEmployee(eq(1L), any(UpdateEmployeeRequest.class)))
                .thenReturn(employeeResponse);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testDeleteEmployeeSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSearchEmployees() throws Exception {
        // Arrange
        PageResponse<EmployeeResponse> pageResponse = PageResponse.<EmployeeResponse>builder()
                .content(Collections.singletonList(employeeResponse))
                .currentPage(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(employeeService.searchEmployees(
                eq("John"), isNull(), isNull(), isNull(), eq(1L),
                isNull(), isNull(), any(Pageable.class)))
                .thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees/search?firstName=John&departmentId=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetEmployeesByDepartment() throws Exception {
        // Arrange
        PageResponse<EmployeeResponse> pageResponse = PageResponse.<EmployeeResponse>builder()
                .content(Collections.singletonList(employeeResponse))
                .currentPage(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(employeeService.getEmployeesByDepartment(eq(1L), any(Pageable.class)))
                .thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/employees/department/1?page=0&size=20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

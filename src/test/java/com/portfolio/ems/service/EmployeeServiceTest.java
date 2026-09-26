package com.portfolio.ems.service;

import com.portfolio.ems.dto.request.CreateEmployeeRequest;
import com.portfolio.ems.dto.request.UpdateEmployeeRequest;
import com.portfolio.ems.dto.response.EmployeeResponse;
import com.portfolio.ems.entity.Department;
import com.portfolio.ems.entity.Employee;
import com.portfolio.ems.exception.InvalidRequestException;
import com.portfolio.ems.exception.ResourceNotFoundException;
import com.portfolio.ems.mapper.EmployeeMapper;
import com.portfolio.ems.repository.DepartmentRepository;
import com.portfolio.ems.repository.EmployeeRepository;
import com.portfolio.ems.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private CreateEmployeeRequest createEmployeeRequest;
    private Department department;
    private Employee employee;
    private EmployeeResponse employeeResponse;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .name("Engineering")
                .location("New York")
                .build();
        department.setId(1L);

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

        employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .salary(new BigDecimal("75000.00"))
                .designation("Senior Engineer")
                .joiningDate(LocalDate.now())
                .address("123 Main St")
                .isActive(true)
                .department(department)
                .build();
        employee.setId(1L);
        employee.setDeleted(false);

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
    void testCreateEmployeeSuccess() {
        // Arrange
        when(employeeRepository.existsByEmailAndNotDeleted(createEmployeeRequest.getEmail()))
                .thenReturn(false);
        when(departmentRepository.findByIdAndNotDeleted(createEmployeeRequest.getDepartmentId()))
                .thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(employee);
        when(employeeMapper.toEntity(createEmployeeRequest, department))
                .thenReturn(employee);
        when(employeeMapper.toResponse(employee))
                .thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.createEmployee(createEmployeeRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(1L, result.getDepartmentId());

        // Verify interactions
        verify(employeeRepository, times(1)).existsByEmailAndNotDeleted(createEmployeeRequest.getEmail());
        verify(departmentRepository, times(1)).findByIdAndNotDeleted(createEmployeeRequest.getDepartmentId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployeeFailsWithDuplicateEmail() {
        // Arrange
        when(employeeRepository.existsByEmailAndNotDeleted(createEmployeeRequest.getEmail()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            employeeService.createEmployee(createEmployeeRequest);
        });

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreateEmployeeFailsWithInvalidDepartment() {
        // Arrange
        when(employeeRepository.existsByEmailAndNotDeleted(createEmployeeRequest.getEmail()))
                .thenReturn(false);
        when(departmentRepository.findByIdAndNotDeleted(createEmployeeRequest.getDepartmentId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.createEmployee(createEmployeeRequest);
        });

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testGetEmployeeByIdSuccess() {
        // Arrange
        when(employeeRepository.findByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee))
                .thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.getEmployeeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());

        verify(employeeRepository, times(1)).findByIdAndNotDeleted(1L);
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        // Arrange
        when(employeeRepository.findByIdAndNotDeleted(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            employeeService.getEmployeeById(999L);
        });

        verify(employeeRepository, times(1)).findByIdAndNotDeleted(999L);
    }

    @Test
    void testUpdateEmployeeSuccess() {
        // Arrange
        UpdateEmployeeRequest updateRequest = UpdateEmployeeRequest.builder()
                .firstName("Jane")
                .salary(new BigDecimal("85000.00"))
                .build();

        when(employeeRepository.findByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(employee);
        when(employeeMapper.toResponse(employee))
                .thenReturn(employeeResponse);

        // Act
        EmployeeResponse result = employeeService.updateEmployee(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(employeeRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testDeleteEmployeeSuccess() {
        // Arrange
        when(employeeRepository.findByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(employee));

        // Act
        employeeService.deleteEmployee(1L);

        // Assert
        verify(employeeRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }
}

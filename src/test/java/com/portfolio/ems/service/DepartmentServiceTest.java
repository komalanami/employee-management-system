package com.portfolio.ems.service;

import com.portfolio.ems.dto.request.CreateDepartmentRequest;
import com.portfolio.ems.dto.request.UpdateDepartmentRequest;
import com.portfolio.ems.dto.response.DepartmentResponse;
import com.portfolio.ems.entity.Department;
import com.portfolio.ems.exception.InvalidRequestException;
import com.portfolio.ems.exception.ResourceNotFoundException;
import com.portfolio.ems.mapper.DepartmentMapper;
import com.portfolio.ems.repository.DepartmentRepository;
import com.portfolio.ems.repository.EmployeeRepository;
import com.portfolio.ems.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private CreateDepartmentRequest createDepartmentRequest;
    private Department department;
    private DepartmentResponse departmentResponse;

    @BeforeEach
    void setUp() {
        createDepartmentRequest = CreateDepartmentRequest.builder()
                .name("Engineering")
                .location("New York")
                .description("Engineering department")
                .build();

        department = Department.builder()
                .name("Engineering")
                .location("New York")
                .description("Engineering department")
                .build();
        department.setId(1L);
        department.setDeleted(false);

        departmentResponse = DepartmentResponse.builder()
                .id(1L)
                .name("Engineering")
                .location("New York")
                .description("Engineering department")
                .employeeCount(0)
                .build();
    }

    @Test
    void testCreateDepartmentSuccess() {
        // Arrange
        when(departmentRepository.existsByNameAndNotDeleted(createDepartmentRequest.getName()))
                .thenReturn(false);
        when(departmentRepository.save(any(Department.class)))
                .thenReturn(department);
        when(departmentMapper.toEntity(createDepartmentRequest))
                .thenReturn(department);
        when(departmentMapper.toResponseWithoutEmployees(department))
                .thenReturn(departmentResponse);

        // Act
        DepartmentResponse result = departmentService.createDepartment(createDepartmentRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Engineering", result.getName());
        assertEquals("New York", result.getLocation());

        verify(departmentRepository, times(1)).existsByNameAndNotDeleted(createDepartmentRequest.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testCreateDepartmentFailsWithDuplicateName() {
        // Arrange
        when(departmentRepository.existsByNameAndNotDeleted(createDepartmentRequest.getName()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            departmentService.createDepartment(createDepartmentRequest);
        });

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testGetDepartmentByIdSuccess() {
        // Arrange
        when(departmentRepository.findByIdWithEmployees(1L))
                .thenReturn(Optional.of(department));
        when(departmentMapper.toResponse(department))
                .thenReturn(departmentResponse);

        // Act
        DepartmentResponse result = departmentService.getDepartmentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Engineering", result.getName());

        verify(departmentRepository, times(1)).findByIdWithEmployees(1L);
    }

    @Test
    void testGetDepartmentByIdNotFound() {
        // Arrange
        when(departmentRepository.findByIdWithEmployees(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.getDepartmentById(999L);
        });

        verify(departmentRepository, times(1)).findByIdWithEmployees(999L);
    }

    @Test
    void testUpdateDepartmentSuccess() {
        // Arrange
        UpdateDepartmentRequest updateRequest = UpdateDepartmentRequest.builder()
                .name("Engineering Updated")
                .location("San Francisco")
                .build();

        when(departmentRepository.findByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameAndNotDeleted(updateRequest.getName(), 1L))
                .thenReturn(false);
        when(departmentRepository.save(any(Department.class)))
                .thenReturn(department);
        when(departmentMapper.toResponseWithoutEmployees(department))
                .thenReturn(departmentResponse);

        // Act
        DepartmentResponse result = departmentService.updateDepartment(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(departmentRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testDeleteDepartmentSuccess() {
        // Arrange
        when(departmentRepository.findByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(department));
        when(employeeRepository.countActiveByDepartmentIdAndNotDeleted(1L))
                .thenReturn(0L);

        // Act
        departmentService.deleteDepartment(1L);

        // Assert
        verify(departmentRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(employeeRepository, times(1)).countActiveByDepartmentIdAndNotDeleted(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testDeleteDepartmentFailsWithActiveEmployees() {
        // Arrange
        when(departmentRepository.findByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(department));
        when(employeeRepository.countActiveByDepartmentIdAndNotDeleted(1L))
                .thenReturn(5L);

        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            departmentService.deleteDepartment(1L);
        });

        verify(departmentRepository, never()).save(any(Department.class));
    }
}

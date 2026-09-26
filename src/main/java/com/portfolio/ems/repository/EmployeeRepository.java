package com.portfolio.ems.repository;

import com.portfolio.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Check if email exists (excluding soft deleted)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END " +
           "FROM Employee e WHERE e.email = :email AND e.deleted = false")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    // Check if email exists excluding a specific employee (for updates)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END " +
           "FROM Employee e WHERE e.email = :email AND e.id != :id AND e.deleted = false")
    boolean existsByEmailAndNotDeleted(@Param("email") String email, @Param("id") Long id);

    // Get employee by ID (not deleted)
    @Query("SELECT e FROM Employee e WHERE e.id = :id AND e.deleted = false")
    Optional<Employee> findByIdAndNotDeleted(@Param("id") Long id);

    // List all employees (not deleted) with pagination
    @Query("SELECT e FROM Employee e WHERE e.deleted = false ORDER BY e.id")
    Page<Employee> findAllNotDeleted(Pageable pageable);

    // Search employees with advanced filtering
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (:firstName IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) " +
           "AND (:lastName IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) " +
           "AND (:email IS NULL OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:designation IS NULL OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :designation, '%'))) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:minSalary IS NULL OR e.salary >= :minSalary) " +
           "AND (:maxSalary IS NULL OR e.salary <= :maxSalary)")
    Page<Employee> searchEmployees(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("designation") String designation,
            @Param("departmentId") Long departmentId,
            @Param("minSalary") BigDecimal minSalary,
            @Param("maxSalary") BigDecimal maxSalary,
            Pageable pageable
    );

    // Get employees by department
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    Page<Employee> findByDepartmentIdAndNotDeleted(@Param("departmentId") Long departmentId, Pageable pageable);

    // Get employees by designation
    @Query("SELECT e FROM Employee e WHERE LOWER(e.designation) = LOWER(:designation) AND e.deleted = false")
    Page<Employee> findByDesignationAndNotDeleted(@Param("designation") String designation, Pageable pageable);

    // Count employees in department
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    Long countByDepartmentIdAndNotDeleted(@Param("departmentId") Long departmentId);

    // Count active employees in department
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false AND e.isActive = true")
    Long countActiveByDepartmentIdAndNotDeleted(@Param("departmentId") Long departmentId);
}

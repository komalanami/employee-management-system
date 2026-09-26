package com.portfolio.ems.repository;

import com.portfolio.ems.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Get department by ID (not deleted)
    @Query("SELECT d FROM Department d WHERE d.id = :id AND d.deleted = false")
    Optional<Department> findByIdAndNotDeleted(@Param("id") Long id);

    // Check if department name exists (excluding soft deleted)
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END " +
           "FROM Department d WHERE d.name = :name AND d.deleted = false")
    boolean existsByNameAndNotDeleted(@Param("name") String name);

    // Check if department name exists excluding a specific department
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END " +
           "FROM Department d WHERE d.name = :name AND d.id != :id AND d.deleted = false")
    boolean existsByNameAndNotDeleted(@Param("name") String name, @Param("id") Long id);

    // List all departments (not deleted) with pagination
    @Query("SELECT d FROM Department d WHERE d.deleted = false ORDER BY d.name")
    Page<Department> findAllNotDeleted(Pageable pageable);

    // Search departments by name
    @Query("SELECT d FROM Department d WHERE d.deleted = false " +
           "AND LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Department> searchByNameAndNotDeleted(@Param("name") String name, Pageable pageable);

    // Search departments by location
    @Query("SELECT d FROM Department d WHERE d.deleted = false " +
           "AND LOWER(d.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Department> searchByLocationAndNotDeleted(@Param("location") String location, Pageable pageable);

    // Get department with all employees (eager load for specific query)
    @Query("SELECT DISTINCT d FROM Department d " +
           "LEFT JOIN FETCH d.employees e " +
           "WHERE d.id = :id AND d.deleted = false")
    Optional<Department> findByIdWithEmployees(@Param("id") Long id);
}

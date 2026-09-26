package com.portfolio.ems.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_emp_email", columnList = "email"),
    @Index(name = "idx_emp_first_name", columnList = "first_name"),
    @Index(name = "idx_emp_last_name", columnList = "last_name"),
    @Index(name = "idx_emp_department_id", columnList = "department_id"),
    @Index(name = "idx_emp_deleted", columnList = "is_deleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal salary;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

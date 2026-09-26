package com.portfolio.ems.mapper;

import com.portfolio.ems.dto.request.CreateDepartmentRequest;
import com.portfolio.ems.dto.request.UpdateDepartmentRequest;
import com.portfolio.ems.dto.response.DepartmentResponse;
import com.portfolio.ems.dto.response.EmployeeResponse;
import com.portfolio.ems.entity.Department;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = EmployeeMapper.class)
public interface DepartmentMapper {

    // Create -> Entity
    Department toEntity(CreateDepartmentRequest request);

    // Entity -> Response
    @Mapping(target = "employeeCount", source = "entity", qualifiedByName = "getEmployeeCount")
    @Mapping(target = "employees", source = "entity.employees")
    DepartmentResponse toResponse(Department entity);

    // Response without employees (to avoid recursion)
    @Mapping(target = "employeeCount", source = "entity", qualifiedByName = "getEmployeeCount")
    @Mapping(target = "employees", ignore = true)
    DepartmentResponse toResponseWithoutEmployees(Department entity);

    // Update -> Entity (partial update)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "employees", ignore = true)
    void updateEntityFromRequest(UpdateDepartmentRequest request, @MappingTarget Department entity);

    // Helper methods
    @Named("getEmployeeCount")
    default Integer getEmployeeCount(Department entity) {
        if (entity == null || entity.getEmployees() == null) {
            return 0;
        }
        return (int) entity.getEmployees().stream()
                .filter(emp -> !emp.getDeleted())
                .count();
    }
}

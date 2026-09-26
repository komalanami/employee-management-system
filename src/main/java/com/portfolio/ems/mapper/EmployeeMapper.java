package com.portfolio.ems.mapper;

import com.portfolio.ems.dto.request.CreateEmployeeRequest;
import com.portfolio.ems.dto.request.UpdateEmployeeRequest;
import com.portfolio.ems.dto.response.EmployeeResponse;
import com.portfolio.ems.entity.Department;
import com.portfolio.ems.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    // Create -> Entity
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "department", source = "department")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Employee toEntity(CreateEmployeeRequest request, Department department);

    // Entity -> Response
    @Mapping(target = "fullName", source = "entity", qualifiedByName = "getFullName")
    @Mapping(target = "departmentId", source = "entity.department.id")
    @Mapping(target = "departmentName", source = "entity.department.name")
    EmployeeResponse toResponse(Employee entity);

    // Update -> Entity (partial update)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "department", ignore = true)
    void updateEntityFromRequest(UpdateEmployeeRequest request, @MappingTarget Employee entity);

    // Helper methods
    @Named("getFullName")
    default String getFullName(Employee entity) {
        if (entity == null) {
            return null;
        }
        return entity.getFullName();
    }
}

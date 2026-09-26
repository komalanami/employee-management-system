# Design Decisions & Architecture Guide

This document explains every major design choice in the Employee Management System. Use this for interview preparation and to understand production-level thinking.

---

## 1. JPA Relationships: One-to-Many (Not Many-to-Many)

### The Decision
Department ↔ Employee is a **One-to-Many** relationship, not Many-to-Many.

### Why One-to-Many?

```
❌ WRONG (Many-to-Many):
Department can have Employees
Employee can have Departments
(Doesn't make sense - employee works in ONE department)

✅ CORRECT (One-to-Many):
Department has MANY Employees
Employee belongs to ONE Department
```

### Implementation

```java
@Entity
public class Department {
    @OneToMany(mappedBy = "department", 
               fetch = FetchType.LAZY,
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Employee> employees;
}

@Entity
public class Employee {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
```

### Interview Explanation

**Q: Explain the relationship between Department and Employee.**

A: "Department and Employee have a One-to-Many relationship. One department can have many employees, but each employee belongs to exactly one department.

- `@OneToMany` on Department represents the collection of employees
- `@ManyToOne` on Employee represents the parent department
- `fetch = FetchType.LAZY` means don't load employees unless explicitly requested (prevents N+1 queries)
- `cascade = CascadeType.ALL` means if a department is deleted, all its employees are deleted
- `orphanRemoval = true` means if an employee is removed from the collection, it's deleted from the database
- `optional = false` on the ManyToOne means every employee MUST have a department

This structure enforces referential integrity at the application level."

---

## 2. Lazy vs. Eager Loading

### The Decision
Use **LAZY loading by default**, explicit **EAGER loading when needed**.

### The Problem

**Eager Loading (Default JPA behavior):**
```java
Department dept = departmentRepository.findById(1L);  // 1 query
List<Employee> emps = dept.getEmployees();  // Already loaded!
```

**N+1 Query Problem:**
```java
List<Department> depts = departmentRepository.findAll();  // Query 1
for (Department dept : depts) {
    List<Employee> emps = dept.getEmployees();  // Query N+1 (one per department!)
}
// Executes: 1 + N queries = disaster for large datasets
```

### The Solution

**Lazy Loading (What we use):**
```java
@OneToMany(fetch = FetchType.LAZY)  // Don't load unless accessed
private List<Employee> employees;

Department dept = departmentRepository.findById(1L);  // Query 1
// dept.getEmployees() would trigger Query 2 only if called
```

**Explicit Eager Loading When Needed:**
```java
@Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
Optional<Department> findByIdWithEmployees(Long id);
```

### Interview Answer

**Q: What's the difference between lazy and eager loading? Why did you choose lazy?**

A: "Lazy loading defers loading related objects until they're explicitly accessed. Eager loading loads them immediately.

Lazy is better because:
1. Performance - Don't load 10,000 employees if you only need department name
2. N+1 prevention - Lazy loading combined with explicit joins prevents the N+1 problem
3. Memory efficiency - Only load what you need

Eager is useful when you know you'll always need related data. I use explicit JPQL JOIN FETCH for those cases:
```java
@Query(\"SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id\")
Optional<Department> findByIdWithEmployees(Long id);
```
This loads department and all employees in ONE query."

---

## 3. Soft Delete Pattern

### The Decision
Use **Soft Delete** (mark as deleted, don't remove from database).

### Why Soft Delete?

| Aspect | Soft Delete | Hard Delete |
|--------|------------|------------|
| **Data Recovery** | ✅ Easy to restore | ❌ Impossible |
| **Audit Trail** | ✅ Who deleted, when | ❌ Lost forever |
| **Referential Integrity** | ✅ Foreign keys intact | ❌ Orphaned data |
| **Compliance** | ✅ GDPR, auditing | ❌ Non-compliant |
| **Performance** | ✅ Faster (no constraints) | ❌ Slower (cascades) |

### Implementation

```java
@Entity
public class Employee extends BaseEntity {
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;  // Default: not deleted
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

// Soft delete:
employee.setDeleted(true);
employee.setDeletedAt(LocalDateTime.now());
employeeRepository.save(employee);

// Hard delete (we never do this):
// employeeRepository.delete(employee);
```

### All Queries Include

```java
@Query("SELECT e FROM Employee e WHERE e.deleted = false")
List<Employee> findAllNotDeleted();
```

### Interview Answer

**Q: Why use soft delete instead of actually removing data?**

A: "Soft delete preserves data without removing it. Instead of executing DELETE, we execute UPDATE with a flag:

```sql
-- Soft Delete
UPDATE employees SET is_deleted = true, deleted_at = NOW() WHERE id = 123;

-- Hard Delete (we never do this)
DELETE FROM employees WHERE id = 123;
```

Benefits:
1. **Data recovery** - Accidentally deleted an employee? Restore it by setting is_deleted = false
2. **Audit trail** - Keep history: who deleted, when, can investigate why
3. **Referential integrity** - Foreign keys still work. Hard delete cascades and breaks things
4. **Compliance** - GDPR, financial regulations, legal holds all prefer soft delete
5. **Performance** - Faster than hard delete (no constraint checking)

The tradeoff is every query must filter WHERE is_deleted = false. But that's negligible with proper indexing."

---

## 4. MapStruct for DTO Conversion

### The Decision
Use **MapStruct** for automatic DTO ↔ Entity conversion.

### The Problem: Manual Mapping

```java
// ❌ Manual mapping - boilerplate, error-prone
public EmployeeResponse toResponse(Employee entity) {
    EmployeeResponse response = new EmployeeResponse();
    response.setId(entity.getId());
    response.setFirstName(entity.getFirstName());
    response.setLastName(entity.getLastName());
    response.setEmail(entity.getEmail());
    response.setPhone(entity.getPhone());
    response.setSalary(entity.getSalary());
    response.setDesignation(entity.getDesignation());
    response.setFullName(entity.getFirstName() + " " + entity.getLastName());
    response.setDepartmentId(entity.getDepartment().getId());
    response.setDepartmentName(entity.getDepartment().getName());
    response.setJoiningDate(entity.getJoiningDate());
    response.setAddress(entity.getAddress());
    response.setIsActive(entity.getIsActive());
    response.setCreatedAt(entity.getCreatedAt());
    response.setUpdatedAt(entity.getUpdatedAt());
    return response;
}
```

### The Solution: MapStruct

```java
// ✅ MapStruct - 1 line, generates code at compile time
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    
    @Mapping(target = "fullName", source = "entity", qualifiedByName = "getFullName")
    @Mapping(target = "departmentId", source = "entity.department.id")
    @Mapping(target = "departmentName", source = "entity.department.name")
    EmployeeResponse toResponse(Employee entity);
    
    @Named("getFullName")
    default String getFullName(Employee entity) {
        return entity.getFirstName() + " " + entity.getLastName();
    }
}
```

### Generated Code (At Compile Time)

MapStruct generates plain Java code:

```java
// Generated by MapStruct compiler plugin
public class EmployeeMapperImpl implements EmployeeMapper {
    
    public EmployeeResponse toResponse(Employee entity) {
        if (entity == null) return null;
        
        EmployeeResponse response = new EmployeeResponse();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        // ... all fields
        response.setFullName(this.getFullName(entity));
        if (entity.getDepartment() != null) {
            response.setDepartmentId(entity.getDepartment().getId());
            response.setDepartmentName(entity.getDepartment().getName());
        }
        return response;
    }
}
```

### MapStruct vs. Alternatives

| Mapper | Compile-time | Type-safe | Performance | Flexibility |
|--------|-------------|-----------|-------------|------------|
| **MapStruct** | ✅ Yes | ✅ Yes | ✅✅ (generated code) | ✅ Good |
| **ModelMapper** | ❌ No | ❌ No | ⚠️ Reflection | ✅ Good |
| **Manual** | N/A | ✅ Yes | ✅✅ (no overhead) | ✅✅ Full control |

### Interview Answer

**Q: Why MapStruct instead of manual mapping or ModelMapper?**

A: "MapStruct is trending in 2026 because it combines the best of both worlds:

1. **Compile-time code generation** - No reflection overhead
2. **Type-safe** - Catches errors at compile time, not runtime
3. **Performance** - Generates plain Java methods, fastest option
4. **Flexibility** - Can customize complex mappings with @Named methods
5. **Spring integration** - Native componentModel = 'spring'

ModelMapper uses reflection at runtime (slower). Manual mapping has no overhead but is boilerplate. MapStruct eliminates boilerplate without runtime cost.

This is why Netflix, Google, and enterprise companies use MapStruct."

---

## 5. Pagination: Handling Large Datasets

### The Decision
Implement pagination for all list endpoints.

### The Problem

```java
// ❌ Load all employees into memory
List<Employee> allEmployees = employeeRepository.findAll();
// If 1 million employees: OutOfMemoryError!
```

### The Solution

```java
// ✅ Load page-by-page
Page<Employee> page = employeeRepository.findAll(
    PageRequest.of(0, 20, Sort.by("id").descending())
);
// Returns: 20 employees, totalCount = 1000000, totalPages = 50000, etc.
```

### API Usage

```http
GET /api/employees?page=0&size=20&sortBy=salary&direction=DESC
```

### Response

```json
{
  "content": [
    { "id": 1, "firstName": "John", "salary": 100000 },
    { "id": 2, "firstName": "Jane", "salary": 95000 }
    // ... 18 more
  ],
  "current_page": 0,
  "page_size": 20,
  "total_elements": 1000000,
  "total_pages": 50000,
  "has_next": true,
  "has_previous": false,
  "is_first": true,
  "is_last": false
}
```

### Interview Answer

**Q: How do you handle large datasets in REST APIs?**

A: "Pagination is essential. Loading 1 million records into memory causes OutOfMemoryError and terrible UX.

Instead:
```java
Page<Employee> page = employeeRepository.findAll(
    PageRequest.of(pageNumber, pageSize, Sort.by(sortField))
);
```

Benefits:
1. **Memory efficient** - Only load N records per page
2. **Scalable** - Same code works for 100 or 1 billion records
3. **UX friendly** - Browser pagination controls
4. **Database optimized** - SQL LIMIT OFFSET is fast

Offset-based pagination (what we use) is simple and sufficient for most cases. For massive datasets (1B+ records), use cursor-based pagination with unique IDs."

---

## 6. Advanced Filtering: @Query

### The Decision
Use explicit `@Query` for complex filtering instead of `findByXyzAndAbc`.

### The Problem

```java
// ❌ Overly complex derived queries
findByFirstNameContainsIgnoreCaseAndSalaryBetweenAndDepartmentIdAndDesignationIgnoreCase(...)
// Hard to read, maintains fragile method names
```

### The Solution

```java
// ✅ Explicit JPQL with @Query
@Query("SELECT e FROM Employee e WHERE " +
       "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) " +
       "AND (:lastName IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) " +
       "AND e.salary BETWEEN :minSalary AND :maxSalary " +
       "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
       "AND e.deleted = false")
Page<Employee> searchEmployees(
    @Param("firstName") String firstName,
    @Param("lastName") String lastName,
    @Param("minSalary") BigDecimal minSalary,
    @Param("maxSalary") BigDecimal maxSalary,
    @Param("departmentId") Long departmentId,
    Pageable pageable
);
```

### Interview Answer

**Q: How do you handle complex filtering queries?**

A: "For simple queries, Spring Data derived methods work:
```java
findByEmail(String email)
findByDepartmentIdAndIsActive(Long deptId, Boolean active)
```

But for complex filtering with multiple optional parameters, explicit `@Query` is clearer:
```java
@Query(\"SELECT e FROM Employee e WHERE \" +
       \"LOWER(e.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) \" +
       \"AND (:lastName IS NULL OR LOWER(e.lastName) LIKE ...) \" +
       \"AND e.deleted = false\")
Page<Employee> searchEmployees(@Param(\"firstName\") String firstName, ...);
```

Benefits:
1. **Readable** - Clear what the query does
2. **Debuggable** - Can log actual SQL
3. **Performant** - Exact SQL you want, not guessed
4. **Maintainable** - Changes are explicit, not hidden in method names"

---

## 7. Global Exception Handling

### The Decision
Use `@RestControllerAdvice` for centralized exception handling.

### Without Global Handler (Anti-pattern)

```java
@PostMapping
public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
    try {
        return ResponseEntity.status(201).body(employeeService.createEmployee(request));
    } catch (InvalidRequestException e) {
        return ResponseEntity.status(400).body(ErrorResponse.builder()
            .status(400)
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build());
    }
    // Repeat this in 50 endpoints!
}
```

### With Global Handler (Clean)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
        return ResponseEntity.status(400).body(ErrorResponse.builder()
            .status(400)
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build());
    }
}

@PostMapping
public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
    return ResponseEntity.status(201).body(employeeService.createEmployee(request));
    // Exception bubbles to @RestControllerAdvice automatically
}
```

### Interview Answer

**Q: Why use global exception handling?**

A: "@RestControllerAdvice provides centralized exception handling. One class handles all exceptions, not scattered try-catch in 50 endpoints.

Benefits:
1. **DRY** - Define error response format once
2. **Consistency** - All endpoints return same error structure
3. **Maintainability** - Change error response in one place
4. **Separation** - Business logic separate from error handling

We handle:
- `MethodArgumentNotValidException` → 400 with field errors
- `ResourceNotFoundException` → 404
- `InvalidRequestException` → 400
- `Exception` (catch-all) → 500"

---

## 8. Service Layer Pattern

### The Decision
Separate service interfaces from implementations.

### Why?

```java
// ❌ BAD: Direct dependency on implementation
@RestController
public class EmployeeController {
    private final EmployeeServiceImpl service;  // Concrete class
}

// ✅ GOOD: Depend on interface
@RestController
public class EmployeeController {
    private final EmployeeService service;  // Interface
}
```

### Reason

**Dependency Inversion Principle (SOLID):**
- High-level modules (Controller) shouldn't depend on low-level modules (ServiceImpl)
- Both should depend on abstractions (Service interface)

**Benefits:**
- Easy to mock in tests
- Easy to swap implementations
- Code is testable and flexible

---

## 9. Constructor Injection vs. Field Injection

### The Decision
Use **constructor injection** with Lombok.

### Constructor Injection (✅ Good)

```java
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;  // Lombok generates constructor
}

// Testing: Easy to mock
@Test
void test() {
    EmployeeRepository mockRepo = mock(EmployeeRepository.class);
    EmployeeService service = new EmployeeServiceImpl(mockRepo);  // Pass mock
}
```

### Field Injection (❌ Bad)

```java
@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;  // Magic injection
}

// Testing: Hard to mock (reflection needed)
@Test
void test() {
    // How do I pass a mock? Need to use reflection or Spring test context
}
```

### Interview Answer

**Q: Why constructor injection over field injection?**

A: "Constructor injection is clearer and testable:

1. **Explicit dependencies** - Constructor shows what a class needs
2. **Immutable** - `private final` prevents accidental changes
3. **Testable** - Easy to pass mocks in unit tests
4. **Failures early** - Missing dependency causes immediate error at construction

With Lombok's `@RequiredArgsConstructor`, boilerplate is eliminated:
```java
@RequiredArgsConstructor
public class Service {
    private final Repository repo;  // Lombok generates constructor
}
```

Field injection hides dependencies and requires reflection in tests, making it harder to debug."

---

## 10. Custom Exceptions

### The Decision
Create domain-specific exceptions instead of generic RuntimeException.

### Custom Exceptions

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
```

### Usage

```java
if (!employeeRepository.existsByIdAndNotDeleted(id)) {
    throw new ResourceNotFoundException("Employee not found");
}

if (employeeRepository.existsByEmailAndNotDeleted(email)) {
    throw new InvalidRequestException("Email already in use");
}
```

### Interview Answer

**Q: Why create custom exceptions?**

A: "Custom exceptions are semantic - they convey meaning.

`ResourceNotFoundException` means 'resource not found' (404 response).
`InvalidRequestException` means 'invalid input' (400 response).

This allows:
1. **Semantic meaning** - Exceptions name the problem
2. **Targeted handling** - Different exception = different response
3. **Cleaner code** - Don't catch RuntimeException and inspect message

Global exception handler maps these to HTTP responses:
```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(...) {
    return ResponseEntity.status(404).body(...);
}
```

Much cleaner than inspecting exception message strings."

---

## 11. Testing Strategy

### The Decision
Unit tests (mocked services) + Integration tests (real controller).

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository repo;
    
    @InjectMocks
    private EmployeeServiceImpl service;
    
    @Test
    void testCreateSuccess() {
        when(repo.save(any())).thenReturn(employee);
        EmployeeResponse result = service.createEmployee(request);
        assertEquals("John", result.getFirstName());
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateSuccess() throws Exception {
        mockMvc.perform(post("/api/employees")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```

### Interview Answer

**Q: What's your testing strategy?**

A: "Unit + Integration tests:

**Unit Tests** (test business logic):
- Mock all dependencies (Repository, etc.)
- Fast, isolated, test one class
- JUnit 5 + Mockito

**Integration Tests** (test API):
- Use MockMvc, test HTTP layer
- Verify status codes, response structure
- Spring Boot Test

Coverage target: >80% for service layer, >60% overall. Critical paths (create, update, delete) tested thoroughly."

---

## 12. Database Indexes

### The Decision
Create indexes on frequently queried columns.

### Entity Definition

```java
@Entity
@Table(indexes = {
    @Index(name = "idx_emp_email", columnList = "email"),
    @Index(name = "idx_emp_first_name", columnList = "first_name"),
    @Index(name = "idx_emp_department_id", columnList = "department_id"),
    @Index(name = "idx_emp_deleted", columnList = "is_deleted")
})
public class Employee { }
```

### Why?

- `email` - Unique lookup, frequent WHERE clause
- `first_name`, `last_name` - Used in search
- `department_id` - Foreign key, filtering
- `is_deleted` - In every query (soft delete pattern)

---

# Employee Management System

A production-quality REST API for managing employees and departments with comprehensive business logic, validation, pagination, filtering, and advanced search capabilities.

## 🎯 Project Overview

This project demonstrates:
- **Complex database relationships** (One-to-Many, Many-to-One)
- **Advanced search and filtering** with pagination
- **Soft delete** for data preservation
- **MapStruct** for automatic DTO conversion
- **Global exception handling** with validation
- **Unit & integration tests** with Mockito and Spring Boot Test
- **RESTful API best practices** with proper HTTP status codes
- **Production-ready architecture** following SOLID principles

## 📋 Tech Stack

- **Java 17**
- **Spring Boot 3.3.0**
- **Spring Data JPA** (Hibernate)
- **MySQL 8** (or any JDBC-compatible database)
- **MapStruct 1.5.5** (Automatic DTO/Entity mapping)
- **Lombok** (Reduce boilerplate)
- **JUnit 5 & Mockito** (Unit testing)
- **Maven 3.8+**

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│  Controller (HTTP Entry Point)          │
│  - Request validation                   │
│  - HTTP status codes                    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Service (Business Logic)               │
│  - Validation                           │
│  - Business rules                       │
│  - Transaction management               │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Repository (Data Access)               │
│  - Database queries                     │
│  - JPA operations                       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Entity (Database Model)                │
│  - Table mapping                        │
│  - Relationships                        │
└─────────────────────────────────────────┘
```

### Database Design

```
DEPARTMENTS                          EMPLOYEES
├── id (PK)                         ├── id (PK)
├── name (UNIQUE)                   ├── first_name
├── location                        ├── last_name
├── description                     ├── email (UNIQUE)
├── is_deleted (Soft Delete)        ├── phone
├── created_at                      ├── salary
├── updated_at                      ├── designation
├── deleted_at                      ├── joining_date
                                    ├── address
                                    ├── is_active
                                    ├── department_id (FK)
                                    ├── is_deleted
                                    ├── created_at
                                    ├── updated_at
                                    └── deleted_at
```

## 🚀 Quick Start

### Prerequisites

1. **Java 17+** installed
2. **Maven 3.8+** installed
3. **MySQL 8+** installed and running
4. Port `8080` available

### Setup

#### 1. Clone and Navigate

```bash
cd employee-management-system
```

#### 2. Configure Database

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ems_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
    username: root
    password: root  # Change to your MySQL password
```

#### 3. Build and Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Server will start on `http://localhost:8080`

#### 4. Verify

```bash
curl http://localhost:8080/api/departments
```

## 📚 API Documentation

### Base URL

```
http://localhost:8080/api
```

### 1. Department Endpoints

#### Create Department

```http
POST /departments
Content-Type: application/json

{
  "name": "Engineering",
  "location": "New York",
  "description": "Engineering team"
}
```

**Response:** `201 CREATED`

```json
{
  "id": 1,
  "name": "Engineering",
  "location": "New York",
  "description": "Engineering team",
  "employee_count": 0,
  "created_at": "2024-01-15T10:30:45.123456",
  "updated_at": "2024-01-15T10:30:45.123456"
}
```

#### Get All Departments

```http
GET /departments?page=0&size=20&sortBy=name&direction=ASC
```

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Records per page
- `sortBy` (default: name) - Field to sort by
- `direction` (default: ASC) - Sort direction

#### Get Department by ID

```http
GET /departments/{id}
```

**Response:** `200 OK` with department and nested employees

#### Update Department

```http
PUT /departments/{id}
Content-Type: application/json

{
  "name": "Engineering Updated",
  "location": "San Francisco"
}
```

**Response:** `200 OK`

#### Delete Department

```http
DELETE /departments/{id}
```

**Response:** `204 NO CONTENT`

**Note:** Cannot delete department with active employees

#### Search Departments

```http
GET /departments/search/by-name?name=Engineering&page=0&size=20
GET /departments/search/by-location?location=New%20York&page=0&size=20
```

### 2. Employee Endpoints

#### Create Employee

```http
POST /employees
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "1234567890",
  "salary": 75000.00,
  "designation": "Senior Engineer",
  "joiningDate": "2024-01-15",
  "address": "123 Main St",
  "departmentId": 1
}
```

**Response:** `201 CREATED`

#### Get All Employees

```http
GET /employees?page=0&size=20&sortBy=id&direction=DESC
```

#### Get Employee by ID

```http
GET /employees/{id}
```

#### Update Employee

```http
PUT /employees/{id}
Content-Type: application/json

{
  "firstName": "Jane",
  "salary": 85000.00
}
```

#### Delete Employee

```http
DELETE /employees/{id}
```

#### Advanced Search

```http
GET /employees/search?firstName=John&lastName=Doe&designation=Engineer&departmentId=1&minSalary=50000&maxSalary=100000&page=0&size=20
```

**Query Parameters:**
- `firstName` - Filter by first name (substring match)
- `lastName` - Filter by last name (substring match)
- `email` - Filter by email (substring match)
- `designation` - Filter by designation
- `departmentId` - Filter by department ID
- `minSalary` - Minimum salary
- `maxSalary` - Maximum salary
- `page` - Page number
- `size` - Records per page

#### Get Employees by Department

```http
GET /employees/department/{departmentId}?page=0&size=20
```

#### Get Employees by Designation

```http
GET /employees/designation/Engineer?page=0&size=20
```

## 🔑 Key Concepts (Interview Preparation)

### 1. JPA Relationships: One-to-Many

**The Problem:**
- Department has many Employees
- Employee belongs to one Department
- Need to manage this relationship efficiently

**The Solution:**

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}
```

**Interview Explanation:**
- `@OneToMany` = Department has multiple Employees
- `@ManyToOne` = Employee belongs to one Department
- `fetch = FetchType.LAZY` = Don't load related objects unless explicitly requested (prevents N+1 queries)
- `cascade = CascadeType.ALL` = If department is deleted, delete all its employees
- `orphanRemoval = true` = If employee is removed from list, delete it from database

### 2. Soft Delete Pattern

**Why Soft Delete?**
- Preserve data for auditing
- Enable data recovery
- Maintain referential integrity
- Compliance with data regulations

**Implementation:**
```sql
-- Instead of: DELETE FROM employees WHERE id = 1;
UPDATE employees SET is_deleted = true, deleted_at = NOW() WHERE id = 1;
```

**All queries include:**
```sql
WHERE is_deleted = false
```

### 3. Pagination & Filtering

**Problem:** Loading 1 million records into memory = OutOfMemoryError

**Solution:** Pagination

```java
Page<Employee> page = employeeRepository.findAllNotDeleted(
    PageRequest.of(0, 20, Sort.by("salary").descending())
);

// Returns only 20 employees, total count, total pages, etc.
```

**Advanced Filtering:**
```java
@Query("SELECT e FROM Employee e WHERE " +
       "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) " +
       "AND e.salary BETWEEN :minSalary AND :maxSalary " +
       "AND e.deleted = false")
Page<Employee> searchEmployees(
    @Param("firstName") String firstName,
    @Param("minSalary") BigDecimal minSalary,
    @Param("maxSalary") BigDecimal maxSalary,
    Pageable pageable
);
```

### 4. MapStruct: Automatic DTO Conversion

**The Problem:**
- Manual DTO ↔ Entity conversion is boilerplate
- Easy to miss fields
- Hard to refactor

**The Solution: MapStruct**

```java
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(CreateEmployeeRequest request, @Context Department dept);
    EmployeeResponse toResponse(Employee entity);
}
```

**Advantages over Manual Mapping:**
- ✅ **Compile-time code generation** (not reflection)
- ✅ **Type-safe** (catches errors at compile time)
- ✅ **Performance** (generates plain Java methods)
- ✅ **Flexible** (custom conversions for complex fields)

### 5. Service Layer Business Logic

**Service Interface:**
```java
public interface EmployeeService {
    EmployeeResponse createEmployee(CreateEmployeeRequest request);
    EmployeeResponse getEmployeeById(Long id);
    // ...
}
```

**Service Implementation:**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        // 1. Validation (email unique?)
        if (employeeRepository.existsByEmailAndNotDeleted(request.getEmail())) {
            throw new InvalidRequestException("Email already in use");
        }
        
        // 2. Fetch related entities (department exists?)
        Department dept = departmentRepository.findByIdAndNotDeleted(request.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        
        // 3. Create and save
        Employee employee = employeeMapper.toEntity(request, dept);
        employee = employeeRepository.save(employee);
        
        // 4. Return response
        return employeeMapper.toResponse(employee);
    }
}
```

**Why this pattern?**
- Separates concerns (validation, persistence, mapping)
- Reusable from multiple controllers
- Easier to test
- Single Responsibility Principle

### 6. Global Exception Handling

**Without Global Handler (Anti-pattern):**
```java
@PostMapping
public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
    try {
        // ... create employee
    } catch (ResourceNotFoundException e) {
        return ResponseEntity.status(404).body(errorResponse);
    } catch (InvalidRequestException e) {
        return ResponseEntity.status(400).body(errorResponse);
    }
    // Repeat in 50 endpoints!
}
```

**With Global Handler (Clean):**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(
            ErrorResponse.builder()
                .status(404)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}
```

### 7. Unit Testing with Mockito

**Mocking Dependencies:**
```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeRepository employeeRepository;  // Fake repo
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;     // Inject mocks into service
    
    @Test
    void testCreateEmployeeSuccess() {
        // Arrange - Set up mock behavior
        when(employeeRepository.existsByEmailAndNotDeleted("john@example.com"))
            .thenReturn(false);
        
        // Act - Call the service
        EmployeeResponse result = employeeService.createEmployee(request);
        
        // Assert - Verify result
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        
        // Verify - Ensure mocks were called
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }
}
```

## 🔍 Query Performance Tips

### N+1 Query Problem

**Bad (Causes N+1 queries):**
```java
List<Department> depts = departmentRepository.findAll();
for (Department dept : depts) {
    List<Employee> emps = dept.getEmployees();  // Query per department!
}
// Executes: 1 query for departments + N queries for employees
```

**Good (Eager load when needed):**
```java
@Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
Optional<Department> findByIdWithEmployees(Long id);
// Executes: 1 query with JOIN
```

**Or use Lazy Loading properly:**
```java
@OneToMany(fetch = FetchType.LAZY)  // Default, load only if accessed within transaction
private List<Employee> employees;
```

## 🧪 Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=EmployeeServiceTest
```

### Run with Coverage

```bash
mvn clean test jacoco:report
```

## 📊 Database Schema

### Auto-generated by Hibernate

Hibernate automatically creates tables based on entity annotations. Indexes are created for performance:

```sql
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(200),
    description TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_dept_name (name),
    INDEX idx_dept_deleted (is_deleted)
);

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    salary DECIMAL(19,2) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    joining_date DATE NOT NULL,
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    department_id BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    INDEX idx_emp_email (email),
    INDEX idx_emp_first_name (first_name),
    INDEX idx_emp_last_name (last_name),
    INDEX idx_emp_department_id (department_id),
    INDEX idx_emp_deleted (is_deleted)
);
```

## 🎓 Production Improvements (Not in This Demo)

### 1. **Caching**
```java
@Cacheable("departments")
public DepartmentResponse getDepartmentById(Long id) { }
```

### 2. **Audit Logging**
```java
@EntityListeners(AuditListener.class)
public class Employee {
    @CreatedBy
    private String createdBy;
    
    @LastModifiedBy
    private String lastModifiedBy;
}
```

### 3. **API Versioning**
```java
@RequestMapping("/api/v1/employees")  // vs /api/v2/employees
```

### 4. **Security (Spring Security)**
```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteEmployee(Long id) { }
```

### 5. **Async Processing**
```java
@Async
public void sendNotificationEmail(Employee employee) { }
```

### 6. **API Documentation (Springdoc/Swagger)**
```java
@OpenAPIDefinition(info = @Info(title = "Employee API", version = "1.0"))
@SpringBootApplication
public class Application { }
```

## 📝 Postman Collection

Import `employee-management-system.postman_collection.json` into Postman for ready-to-test endpoints.

## 🔗 GitHub Portfolio

Ready to push to GitHub:

```bash
git init
git add .
git commit -m "feat: Employee Management System with full CRUD, pagination, search, and tests"
git remote add origin https://github.com/YOUR_USERNAME/employee-management-system.git
git push -u origin main
```

## 📄 Freelancer Portfolio Description

---

### 💼 Employee Management System

**Production-quality REST API** for comprehensive employee and department management with advanced features.

**Key Features:**
✅ Full CRUD operations with soft delete  
✅ Advanced search & filtering with pagination  
✅ JPA relationships (One-to-Many)  
✅ MapStruct automatic DTO conversion  
✅ Global exception handling with validation  
✅ Unit & integration tests (JUnit 5, Mockito)  
✅ Proper HTTP status codes & RESTful design  
✅ MySQL database with optimized queries  

**Technologies:**
Java 17 • Spring Boot 3 • Spring Data JPA • Hibernate • MapStruct • MySQL • JUnit 5 • Mockito

**Why This Matters:**
- Demonstrates **production-ready architecture** (layered, SOLID)
- Shows understanding of **database relationships** and query optimization
- Proves **testing proficiency** (unit + integration tests)
- Exhibits **API design** best practices

**Use Cases:**
- HR management systems
- Corporate administration dashboards
- Employee tracking applications
- Any system with complex data relationships and filtering needs

---

## 📞 Support

For questions or issues, refer to:
1. **README.md** (this file) — Full documentation
2. **DESIGN_DECISIONS.md** — Architecture explanations
3. **Test files** — Working examples

## 📜 License

This project is part of a portfolio and is open for review.

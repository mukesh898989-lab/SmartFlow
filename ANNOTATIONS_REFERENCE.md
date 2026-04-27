# SmartApp - Complete Annotation Reference Guide

**All Annotations Used in the Project with Locations & Explanations**

---

## TABLE OF CONTENTS

1. [Spring Boot Core Annotations](#1-spring-boot-core-annotations)
2. [Spring Web (REST) Annotations](#2-spring-web-rest-annotations)
3. [Spring Data JPA Annotations](#3-spring-data-jpa-annotations)
4. [Spring Security Annotations](#4-spring-security-annotations)
5. [Validation (Jakarta/Javax) Annotations](#5-validation-annotations)
6. [Lombok Annotations](#6-lombok-annotations)
7. [Hibernate/JPA Annotations](#7-hibernatejpa-annotations)
8. [Spring WebSocket Annotations](#8-spring-websocket-annotations)
9. [Angular Decorators](#9-angular-decorators)
10. [Exception Handling Annotations](#10-exception-handling-annotations)

---

## 1. SPRING BOOT CORE ANNOTATIONS

### @SpringBootApplication
**Location:** `backend/src/main/java/com/hospital/queue/HospitalQueueApplication.java:6`

**Purpose:** Marks the main Spring Boot application class

**Explanation:** 
- Combines three annotations: `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`
- Entry point of the application
- Enables Spring Boot auto-configuration
- Scans for beans in the package and sub-packages

**Code Example:**
```java
@SpringBootApplication
public class HospitalQueueApplication {
    public static void main(String[] args) {
        SpringApplication.run(HospitalQueueApplication.class, args);
    }
}
```

---

### @Configuration
**Locations:** 
- `backend/src/main/java/com/hospital/queue/config/SecurityConfig.java:28`
- `backend/src/main/java/com/hospital/queue/config/WebSocketConfig.java:10`

**Purpose:** Marks a class as a configuration class containing bean definitions

**Explanation:**
- Class contains `@Bean` methods
- Spring uses this class to create beans during startup
- Can define application-wide configurations

**Code Example:**
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configuration code
        return http.build();
    }
}
```

---

### @Component
**Locations:** 
- `backend/src/main/java/com/hospital/queue/config/DataInitializer.java:12`
- `backend/src/main/java/com/hospital/queue/security/JwtAuthFilter.java:20`
- `backend/src/main/java/com/hospital/queue/util/SecurityUtils.java:9`

**Purpose:** Generic Spring-managed component

**Explanation:**
- Marks a class as a Spring-managed bean
- Automatically instantiated and registered in Spring context
- Can be injected into other beans via `@Autowired`

**Code Example:**
```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        // Filter logic
    }
}
```

---

### @Bean
**Locations:** 
- `backend/src/main/java/com/hospital/queue/config/SecurityConfig.java:40,71,79,84,89`

**Purpose:** Indicates a method produces a bean to be managed by Spring

**Explanation:**
- Used inside `@Configuration` classes
- Method return value becomes a bean
- Alternative to XML bean definition
- Can have parameters (dependencies injected)

**Code Example:**
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(...) throws Exception {
        // Returns configured AuthenticationManager
    }
}
```

---

### @Value
**Locations:**
- `backend/src/main/java/com/hospital/queue/config/SecurityConfig.java:37`
- `backend/src/main/java/com/hospital/queue/config/WebSocketConfig.java:14`
- `backend/src/main/java/com/hospital/queue/service/JwtService.java:21,24`

**Purpose:** Injects values from properties files into fields

**Explanation:**
- Reads from `application.properties`
- Can use `${property-name}` syntax
- Supports default values with `:` separator

**Code Example:**
```java
@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration-ms}")
    private Long jwtExpiration;
}
```

**Properties File (application.properties):**
```properties
app.jwt.secret=your-secret-key-here
app.jwt.expiration-ms=86400000
app.cors.allowed-origins=http://localhost:4200
```

---

## 2. SPRING WEB (REST) ANNOTATIONS

### @RestController
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:20`
- `backend/src/main/java/com/hospital/queue/controller/AuthController.java:11`
- `backend/src/main/java/com/hospital/queue/controller/DoctorController.java:15`
- `backend/src/main/java/com/hospital/queue/controller/PatientController.java:20,74`
- `backend/src/main/java/com/hospital/queue/controller/ReceptionistController.java:18`

**Purpose:** Marks a class as a REST controller that handles HTTP requests

**Explanation:**
- Combines `@Controller` + `@ResponseBody`
- All methods return JSON instead of views
- Maps incoming HTTP requests to methods
- Each method typically corresponds to one API endpoint

**Code Example:**
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    // Handler methods here
}
```

---

### @RequestMapping
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:21`
- `backend/src/main/java/com/hospital/queue/controller/AuthController.java:12`
- `backend/src/main/java/com/hospital/queue/controller/DoctorController.java:16`
- `backend/src/main/java/com/hospital/queue/controller/PatientController.java:21,75`
- `backend/src/main/java/com/hospital/queue/controller/ReceptionistController.java:19`

**Purpose:** Maps HTTP requests to handler methods or controller class

**Explanation:**
- Specifies the base path for all endpoints in a controller
- Can specify HTTP method (GET, POST, etc.) if needed
- Path is prepended to method-level path mappings

**Code Example:**
```java
@RestController
@RequestMapping("/api/admin")  // Base path: /api/admin
public class AdminController {
    
    // Full path: /api/admin/users
    @PostMapping("/users")
    public ResponseEntity<?> createUser(...) { }
    
    // Full path: /api/admin/departments
    @GetMapping("/departments")
    public ResponseEntity<?> getDepartments() { }
}
```

---

### @GetMapping
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:37,64,86,102`
- `backend/src/main/java/com/hospital/queue/controller/DoctorController.java:29`
- `backend/src/main/java/com/hospital/queue/controller/PatientController.java:55,64,83,91,100`
- Other controllers

**Purpose:** Maps HTTP GET requests to handler method

**Explanation:**
- Retrieves data (read-only)
- Does not modify server state
- Shorthand for `@RequestMapping(method = RequestMethod.GET)`
- Path specified as parameter

**Code Example:**
```java
@GetMapping("/users")  // GET /api/admin/users
public ResponseEntity<List<UserResponse>> getUsers() {
    return ResponseEntity.ok(adminService.getAllUsers());
}

@GetMapping("/users/{id}")  // GET /api/admin/users/1
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(adminService.getUserById(id));
}
```

---

### @PostMapping
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:31,57,80`
- `backend/src/main/java/com/hospital/queue/controller/AuthController.java:22`
- `backend/src/main/java/com/hospital/queue/controller/PatientController.java:32,44`
- Other controllers

**Purpose:** Maps HTTP POST requests to handler method

**Explanation:**
- Creates new data on server
- Modifies server state
- Body contains request data (usually JSON)
- Should return 201 Created status for successful creation

**Code Example:**
```java
@PostMapping("/users")  // POST /api/admin/users
public ResponseEntity<UserResponse> createUser(
    @Valid @RequestBody CreateUserRequest request) {
    UserResponse user = adminService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
}

@PostMapping("/login")  // POST /api/auth/login
public ResponseEntity<LoginResponse> login(
    @Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(response);
}
```

---

### @PutMapping
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:43,49,70,92`
- `backend/src/main/java/com/hospital/queue/controller/DoctorController.java:53`
- `backend/src/main/java/com/hospital/queue/controller/ReceptionistController.java:59`

**Purpose:** Maps HTTP PUT requests to handler method

**Explanation:**
- Updates existing data on server
- Full resource update (all fields)
- PATCH used for partial updates (not used in this project)
- Requires resource ID to identify what to update

**Code Example:**
```java
@PutMapping("/users/{id}/activate")  // PUT /api/admin/users/1/activate
public ResponseEntity<UserResponse> activateUser(
    @PathVariable Long id) {
    UserResponse user = adminService.activateUser(id);
    return ResponseEntity.ok(user);
}

@PutMapping("/departments/{id}")  // PUT /api/admin/departments/1
public ResponseEntity<DepartmentResponse> updateDepartment(
    @PathVariable Long id,
    @Valid @RequestBody CreateDepartmentRequest request) {
    return ResponseEntity.ok(adminService.updateDepartment(id, request));
}
```

---

### @DeleteMapping
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/ReceptionistController.java:69`

**Purpose:** Maps HTTP DELETE requests to handler method

**Explanation:**
- Deletes existing data
- Should return 204 No Content on success
- Requires resource ID to identify what to delete

**Code Example:**
```java
@DeleteMapping("/tokens/{id}")  // DELETE /api/receptionist/tokens/5
public ResponseEntity<Void> removeToken(@PathVariable Long id) {
    receptionistService.removeToken(id);
    return ResponseEntity.noContent().build();
}
```

---

### @PathVariable
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:44,72,94`
- `backend/src/main/java/com/hospital/queue/controller/DoctorController.java:55`
- `backend/src/main/java/com/hospital/queue/controller/PatientController.java:56,65,102`
- Other controllers

**Purpose:** Extracts path parameter from URL and binds to method parameter

**Explanation:**
- Value comes from URL path
- Name matches path variable in `@GetMapping` etc.
- Type conversion automatic (String → Long, etc.)
- Required by default

**Code Example:**
```java
// URL: GET /api/admin/users/42
@GetMapping("/users/{id}")
public ResponseEntity<UserResponse> getUser(
    @PathVariable Long id) {  // id = 42
    return ResponseEntity.ok(adminService.getUserById(id));
}

// URL: PUT /api/receptionist/tokens/7/priority
@PutMapping("/tokens/{id}/priority")
public ResponseEntity<TokenResponse> updatePriority(
    @PathVariable Long id,  // id = 7
    @Valid @RequestBody UpdatePriorityRequest request) {
    return ResponseEntity.ok(receptionistService.updatePriority(id, request));
}
```

---

### @RequestBody
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:32,59,81,95`
- `backend/src/main/java/com/hospital/queue/controller/AuthController.java:23`
- `backend/src/main/java/com/hospital/queue/controller/PatientController.java:34,46`
- Other controllers

**Purpose:** Binds HTTP request body to method parameter

**Explanation:**
- Body is parsed (usually JSON)
- Mapped to Java object
- Automatic deserialization
- Usually combined with `@Valid` for validation
- Only one `@RequestBody` per method allowed

**Code Example:**
```java
// Request:
// POST /api/auth/login
// Content-Type: application/json
// {
//   "email": "admin@hospital.com",
//   "password": "Admin@123"
// }

@PostMapping("/login")
public ResponseEntity<LoginResponse> login(
    @Valid @RequestBody LoginRequest request) {
    // request.email = "admin@hospital.com"
    // request.password = "Admin@123"
    return ResponseEntity.ok(authService.login(request));
}
```

---

### @Valid
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:32,59,81,95`
- `backend/src/main/java/com/hospital/queue/controller/AuthController.java:23`
- `backend/src/main/java/com/hospital/queue/controller/PatientController.java:34,46`
- Other controllers

**Purpose:** Triggers validation of annotated object using annotations like `@NotNull`, `@Email`, etc.

**Explanation:**
- Validates nested object properties
- If validation fails, returns 400 Bad Request
- Works with validation annotations on DTO fields
- Cascade validation to nested objects

**Code Example:**
```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
    @Valid @RequestBody CreateUserRequest request) {
    // If email is null → validation error
    // If password < 6 chars → validation error
    // If role is null → validation error
    return ResponseEntity.ok(adminService.createUser(request));
}
```

---

## 3. SPRING DATA JPA ANNOTATIONS

### @Repository
**Locations:**
- `backend/src/main/java/com/hospital/queue/repository/DepartmentRepository.java:9`
- `backend/src/main/java/com/hospital/queue/repository/DoctorRepository.java:16`
- `backend/src/main/java/com/hospital/queue/repository/PatientRepository.java:9`
- `backend/src/main/java/com/hospital/queue/repository/TokenRepository.java:16`
- `backend/src/main/java/com/hospital/queue/repository/UserRepository.java:11`

**Purpose:** Marks a class as a JPA repository for database access

**Explanation:**
- Extends `JpaRepository<Entity, ID>` interface
- Spring provides implementation automatically
- No need to write SQL queries for basic CRUD
- Custom queries possible with `@Query` annotation

**Code Example:**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Inherited methods: save(), findById(), findAll(), delete(), etc.
    
    // Custom query methods
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    // Custom query with pessimistic locking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);
}
```

---

### @Query
**Locations:**
- `backend/src/main/java/com/hospital/queue/repository/DoctorRepository.java:30`
- `backend/src/main/java/com/hospital/queue/repository/TokenRepository.java:28,34,47`

**Purpose:** Defines custom JPQL queries for repository methods

**Explanation:**
- JPQL (Java Persistence Query Language) - SQL-like syntax
- Queries on entities, not tables
- More readable than native SQL
- Type-safe (compile-time checking)
- Parameters via `@Param` annotation

**Code Example:**
```java
@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    // Find in-progress token for a doctor
    @Query("SELECT t FROM Token t WHERE t.doctor = :doctor AND t.status = 'IN_PROGRESS'")
    Optional<Token> findInProgressByDoctor(@Param("doctor") Doctor doctor);
    
    // Count tokens created in a date range
    @Query("SELECT COUNT(t) FROM Token t WHERE t.doctor = :doctor " +
           "AND t.createdAt >= :start AND t.createdAt < :end")
    long countByDoctorAndCreatedAtBetween(
        @Param("doctor") Doctor doctor,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
    
    // Find all active tokens
    @Query("SELECT t FROM Token t WHERE t.status IN ('WAITING', 'IN_PROGRESS') " +
           "ORDER BY t.createdAt ASC")
    List<Token> findAllActiveTokens();
}
```

---

### @Param
**Locations:**
- `backend/src/main/java/com/hospital/queue/repository/DoctorRepository.java:31`
- `backend/src/main/java/com/hospital/queue/repository/TokenRepository.java:29,35,36,37`

**Purpose:** Labels method parameters to map to JPQL query parameters

**Explanation:**
- Names parameters in `@Query` annotation
- Replaces positional parameters with named parameters
- More readable and less error-prone
- `:paramName` syntax in query

**Code Example:**
```java
// Method definition
@Query("SELECT t FROM Token t WHERE t.doctor = :doctor AND t.status = :status")
List<Token> findByDoctorAndStatus(
    @Param("doctor") Doctor doctor,
    @Param("status") TokenStatus status);

// Usage
List<Token> tokens = tokenRepository.findByDoctorAndStatus(
    doctor,           // Maps to :doctor
    TokenStatus.WAITING  // Maps to :status
);
```

---

### @Lock
**Locations:**
- `backend/src/main/java/com/hospital/queue/repository/DoctorRepository.java:29`

**Purpose:** Applies database-level locking to prevent concurrent modifications

**Explanation:**
- Pessimistic locking: `PESSIMISTIC_WRITE` - Lock row immediately
- Optimistic locking: `OPTIMISTIC` - Detect conflicts
- Used for critical concurrent operations
- In SmartApp: prevents duplicate token numbers

**Code Example:**
```java
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    // Lock mechanism: SELECT...FOR UPDATE (MySQL)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);
}

// Usage in service
@Transactional
public String generateTokenNumber(Doctor doctor) {
    Doctor doctorLocked = doctorRepository.findByIdForUpdate(doctor.getId()).get();
    // Now doctor row is locked in database
    // Other threads wait here until lock is released
    
    doctorLocked.setTokenCounter(doctorLocked.getTokenCounter() + 1);
    doctorRepository.save(doctorLocked);
    
    return generateToken(doctorLocked);
}
```

---

## 4. SPRING SECURITY ANNOTATIONS

### @PreAuthorize
**Locations:**
- `backend/src/main/java/com/hospital/queue/controller/AdminController.java:22`
- `backend/src/main/java/com/hospital/queue/controller/DoctorController.java:17`
- `backend/src/main/java/com/hospital/queue/controller/ReceptionistController.java:20`

**Purpose:** Authorization check at method level - allows or denies access based on expression

**Explanation:**
- Uses Spring Expression Language (SpEL)
- Checks user roles/permissions BEFORE method executes
- Method not called if authorization fails
- Returns 403 Forbidden if denied

**Code Example:**
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN role
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(...) {
        // Only admins can reach here
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")  // Either role
    @GetMapping("/queue")
    public ResponseEntity<List<TokenResponse>> getQueue() {
        // Admin or Receptionist can access
    }
}
```

---

### @EnableWebSecurity
**Locations:**
- `backend/src/main/java/com/hospital/queue/config/SecurityConfig.java:29`

**Purpose:** Enables Spring Security configuration for the application

**Explanation:**
- Enables security filters
- Allows custom security configuration in class
- Only one `@EnableWebSecurity` per application
- Must be used with `@Configuration`

**Code Example:**
```java
@Configuration
@EnableWebSecurity  // Enables Security features
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Security configuration
        return http.build();
    }
}
```

---

### @EnableMethodSecurity
**Locations:**
- `backend/src/main/java/com/hospital/queue/config/SecurityConfig.java:30`

**Purpose:** Enables method-level security (authorization annotations like `@PreAuthorize`)

**Explanation:**
- Allows `@PreAuthorize`, `@PostAuthorize` annotations on methods
- Intercepts method calls to check permissions
- Replaces deprecated `@EnableGlobalMethodSecurity`
- Must be used with `@Configuration`

**Code Example:**
```java
@Configuration
@EnableMethodSecurity  // Enables @PreAuthorize, @PostAuthorize
public class SecurityConfig {
    // ...
}

@Service
public class AdminService {
    
    @PreAuthorize("hasRole('ADMIN')")  // This works because @EnableMethodSecurity
    public void deleteUser(Long id) {
        // Only accessible to admins
    }
}
```

---

## 5. VALIDATION ANNOTATIONS

### @NotBlank
**Locations:**
- `backend/src/main/java/com/hospital/queue/dto/request/CreateDepartmentRequest.java:10`
- `backend/src/main/java/com/hospital/queue/dto/request/CreateDoctorRequest.java:12,16,20`
- `backend/src/main/java/com/hospital/queue/dto/request/CreateUserRequest.java:13,17,21`
- `backend/src/main/java/com/hospital/queue/dto/request/LoginRequest.java:10,14`
- `backend/src/main/java/com/hospital/queue/dto/request/RegisterPatientRequest.java:9,13`
- Other DTOs

**Purpose:** Validates that field is not null and contains at least one non-whitespace character

**Explanation:**
- String field validation
- Rejects null, empty strings, and whitespace-only strings
- Custom error message in annotation
- Used for required text fields

**Code Example:**
```java
@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;  // Cannot be null or blank
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;  // Must be not blank AND valid email
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;  // Cannot be blank, min 6 chars
}
```

---

### @Email
**Locations:**
- `backend/src/main/java/com/hospital/queue/dto/request/CreateDoctorRequest.java:17`
- `backend/src/main/java/com/hospital/queue/dto/request/CreateUserRequest.java:18`
- `backend/src/main/java/com/hospital/queue/dto/request/LoginRequest.java:11`
- `backend/src/main/java/com/hospital/queue/dto/request/RegisterPatientRequest.java:17`

**Purpose:** Validates that field contains valid email format

**Explanation:**
- Checks basic email structure (user@domain.extension)
- Regex pattern validation
- Custom error message
- Null values are allowed (use `@NotNull` to require)

**Code Example:**
```java
@Data
public class LoginRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;  // Must be valid email and not blank
    
    @NotBlank(message = "Password is required")
    private String password;
}
```

---

### @Size
**Locations:**
- `backend/src/main/java/com/hospital/queue/dto/request/CreateDepartmentRequest.java:11,14`
- `backend/src/main/java/com/hospital/queue/dto/request/CreateDoctorRequest.java:13,21`
- `backend/src/main/java/com/hospital/queue/dto/request/CreateUserRequest.java:14,22`
- `backend/src/main/java/com/hospital/queue/dto/request/RegisterPatientRequest.java:10`

**Purpose:** Validates string/collection size (length) constraints

**Explanation:**
- String: validates character count
- Collections: validates element count
- `min` and `max` parameters
- Custom error message

**Code Example:**
```java
@Data
public class CreateDoctorRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)  // Name must be 2-100 characters
    private String name;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6)  // At least 6 characters (no max)
    private String password;
}
```

---

### @NotNull
**Locations:**
- `backend/src/main/java/com/hospital/queue/dto/request/CreateDoctorRequest.java:24`
- `backend/src/main/java/com/hospital/queue/dto/request/CreateUserRequest.java:25`
- `backend/src/main/java/com/hospital/queue/dto/request/GenerateTokenRequest.java:10,13`
- `backend/src/main/java/com/hospital/queue/dto/request/UpdatePriorityRequest.java:10`
- `backend/src/main/java/com/hospital/queue/dto/request/UpdateStatusRequest.java:10`

**Purpose:** Validates that field is not null

**Explanation:**
- Object-level validation (not just strings)
- Allows empty strings, empty collections
- Different from `@NotBlank` (no whitespace check)
- For required fields with type Object/Long/Integer

**Code Example:**
```java
@Data
public class GenerateTokenRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;  // Cannot be null
    
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;  // Cannot be null
}

@Data
public class CreateUserRequest {
    
    @NotNull(message = "Role is required")
    private Role role;  // Cannot be null
}
```

---

### @Pattern
**Locations:**
- `backend/src/main/java/com/hospital/queue/dto/request/RegisterPatientRequest.java:14,24`

**Purpose:** Validates that field matches a regular expression pattern

**Explanation:**
- Field must match regex pattern
- Common for phone/zip code validation
- Custom error message
- Null values are allowed (use `@NotNull` if required)

**Code Example:**
```java
@Data
public class RegisterPatientRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", 
             message = "Phone must be 10–15 digits")
    private String phone;  // Must be exactly 10-15 digits
    
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", 
             message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;  // Only these values allowed
}
```

---

### @Min / @Max
**Locations:**
- `backend/src/main/java/com/hospital/queue/dto/request/RegisterPatientRequest.java:20,21`

**Purpose:** Validates numeric field is within range

**Explanation:**
- `@Min`: Minimum value (inclusive)
- `@Max`: Maximum value (inclusive)
- Works with numbers: Integer, Long, BigDecimal, etc.
- Custom error message

**Code Example:**
```java
@Data
public class RegisterPatientRequest {
    
    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age is not valid")
    private Integer age;  // Must be 0-150
}
```

---

## 6. LOMBOK ANNOTATIONS

### @Data
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:11`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:11`
- `backend/src/main/java/com/hospital/queue/entity/Patient.java:15`
- `backend/src/main/java/com/hospital/queue/entity/Token.java:18`
- `backend/src/main/java/com/hospital/queue/entity/User.java:15`
- All DTOs

**Purpose:** Auto-generates getters, setters, equals(), hashCode(), toString()

**Explanation:**
- Reduces boilerplate code
- Works on fields with any access modifier
- Combined with other Lombok annotations
- Helps keep code clean and readable

**Code Example:**
```java
@Entity
@Table(name = "users")
@Data  // Generates: getters, setters, equals(), hashCode(), toString()
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    
    // Lombok generates these automatically:
    // public String getName() { return name; }
    // public void setName(String name) { this.name = name; }
    // public boolean equals(Object obj) { ... }
    // public int hashCode() { ... }
    // public String toString() { ... }
}
```

---

### @Builder
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:12`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:12`
- All entities and DTOs

**Purpose:** Generates Builder pattern for object construction

**Explanation:**
- Creates inner Builder class
- Fluent API: `new Patient.builder().name("Ali").phone("9876543210").build()`
- Optional fields can be skipped
- Immutable-like construction
- Works with `@Data` for complete class definition

**Code Example:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private Role role;
}

// Usage (Builder pattern)
User user = User.builder()
    .name("Ali")
    .email("ali@hospital.com")
    .role(Role.RECEPTIONIST)
    .build();

// vs traditional constructor
User user = new User(null, "Ali", "ali@hospital.com", Role.RECEPTIONIST);
```

---

### @NoArgsConstructor
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:13`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:13`
- All entities and DTOs

**Purpose:** Generates no-argument constructor

**Explanation:**
- Required for JPA entities
- Lombok creates empty default constructor
- Allows `new Entity()` without parameters
- JPA uses reflection to instantiate

**Code Example:**
```java
@Entity
@Data
@NoArgsConstructor  // Generates: public Department() { }
@AllArgsConstructor
public class Department {
    private Long id;
    private String name;
    
    // Can now do:
    Department dept = new Department();
}
```

---

### @AllArgsConstructor
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:14`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:14`
- All entities and DTOs

**Purpose:** Generates constructor with all fields as parameters

**Explanation:**
- Creates full constructor with all fields
- Useful for builder + constructor
- Initializes all fields via constructor
- Parameter order matches field declaration order

**Code Example:**
```java
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor  // Generates full constructor
public class User {
    private Long id;
    private String name;
    private String email;
    
    // Lombok generates:
    // public User(Long id, String name, String email) {
    //     this.id = id;
    //     this.name = name;
    //     this.email = email;
    // }
    
    // Can now do:
    User user = new User(1L, "Ali", "ali@email.com");
}
```

---

### @RequiredArgsConstructor
**Locations:**
- `backend/src/main/java/com/hospital/queue/config/DataInitializer.java:13`
- `backend/src/main/java/com/hospital/queue/config/SecurityConfig.java:31`
- Service classes with `@Autowired` fields
- Controllers

**Purpose:** Generates constructor for final (required) fields

**Explanation:**
- Commonly used with dependency injection
- Creates constructor with all `final` fields
- Used instead of `@Autowired` for field injection
- Constructor-based injection (preferred over field injection)

**Code Example:**
```java
@Service
@RequiredArgsConstructor  // Generates constructor for final fields
public class AdminService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Lombok generates:
    // public AdminService(UserRepository userRepository,
    //                     DoctorRepository doctorRepository,
    //                     PasswordEncoder passwordEncoder) {
    //     this.userRepository = userRepository;
    //     this.doctorRepository = doctorRepository;
    //     this.passwordEncoder = passwordEncoder;
    // }
}

// Spring automatically passes dependencies:
@Service
public class PatientService {
    
    @RequiredArgsConstructor
    public class PatientService {
        private final PatientRepository patientRepository;
        private final QueueService queueService;
    }
}
```

---

### @Slf4j
**Locations:**
- `backend/src/main/java/com/hospital/queue/config/DataInitializer.java:14`

**Purpose:** Auto-generates SLF4J logger field

**Explanation:**
- Creates `private static final Logger log` automatically
- No need to manually create logger
- Use `log.info()`, `log.error()`, `log.debug()`, etc.
- Reduces boilerplate

**Code Example:**
```java
@Component
@RequiredArgsConstructor
@Slf4j  // Generates: private static final Logger log = LoggerFactory.getLogger(...);
public class DataInitializer implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing default admin user...");
        
        User admin = User.builder()
            .email("admin@hospital.com")
            .build();
        
        log.info("Admin user created successfully");
    }
}

// vs without @Slf4j:
// private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
```

---

### @Builder.Default
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:28`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:33`
- Entity and DTO fields with default values

**Purpose:** Provides default values for builder fields

**Explanation:**
- Builder normally ignores unset fields
- `@Builder.Default` uses default value if field not set in builder
- Field must have initializer

**Code Example:**
```java
@Entity
@Data
@Builder
public class Token {
    private Long id;
    
    @Builder.Default
    private TokenStatus status = TokenStatus.WAITING;
    // If not specified in builder, defaults to WAITING
    
    @Builder.Default
    private Priority priority = Priority.NORMAL;
    // If not specified in builder, defaults to NORMAL
}

// Usage
Token token = Token.builder()
    .tokenNumber("CARD-20260422-001")
    // status and priority use defaults
    .build();
```

---

## 7. HIBERNATE/JPA ANNOTATIONS

### @Entity
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:9`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:9`
- `backend/src/main/java/com/hospital/queue/entity/Patient.java:13`
- `backend/src/main/java/com/hospital/queue/entity/Token.java:16`
- `backend/src/main/java/com/hospital/queue/entity/User.java:13`

**Purpose:** Marks a class as a JPA entity (mapped to database table)

**Explanation:**
- Class represents database table
- Each instance represents one row
- JPA automatically manages persistence
- Must have `@Id` field
- Hibernate creates tables automatically (with `ddl-auto=update`)

**Code Example:**
```java
@Entity  // This class maps to database table
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    // ... other fields
}

// Hibernate creates:
// CREATE TABLE users (
//     id BIGINT PRIMARY KEY AUTO_INCREMENT,
//     email VARCHAR(100) NOT NULL UNIQUE,
//     ...
// );
```

---

### @Table
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:10`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:10`
- All entities

**Purpose:** Specifies table name and options for entity

**Explanation:**
- Maps entity to specific database table
- Can define indexes, unique constraints
- Without it, table name defaults to entity class name
- Name usually lowercase/underscore format

**Code Example:**
```java
@Entity
@Table(name = "departments")  // Maps to table named "departments"
public class Department {
    @Id
    private Long id;
    
    // If @Table not specified, table would be named "Department"
}
```

---

### @Id
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:17`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:17`
- All entities

**Purpose:** Marks field as primary key

**Explanation:**
- Uniquely identifies each row
- Each entity must have exactly one `@Id`
- Usually combined with `@GeneratedValue`
- Required for JPA entities

**Code Example:**
```java
@Entity
public class User {
    @Id  // This is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Auto-incremented
    
    private String email;
}
```

---

### @GeneratedValue
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Department.java:18`
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:18`
- All entities

**Purpose:** Specifies ID generation strategy

**Explanation:**
- `IDENTITY`: Auto-increment at database level (MySQL AUTO_INCREMENT)
- `SEQUENCE`: Database sequence (PostgreSQL, Oracle)
- `TABLE`: Separate table stores IDs
- `UUID`: Random UUID string

**Code Example:**
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto-increment: 1, 2, 3, 4, 5...
    private Long id;
}

// SQL generated: CREATE TABLE users (id BIGINT AUTO_INCREMENT PRIMARY KEY, ...);
```

---

### @Column
**Locations:**
- Throughout all entities (Department, Doctor, Patient, Token, User)

**Purpose:** Specifies database column properties

**Explanation:**
- `nullable`: Allow NULL values
- `unique`: Column value must be unique
- `length`: String maximum length
- `updatable`: Can be updated after insert
- `insertable`: Can be inserted

**Code Examples:**
```java
@Entity
@Table(name = "users")
public class User {
    
    @Column(nullable = false, length = 100)
    private String name;  // NOT NULL, max 100 chars
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;  // NOT NULL, UNIQUE
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // Cannot be updated after insert
}

// SQL: CREATE TABLE users (
//     name VARCHAR(100) NOT NULL,
//     email VARCHAR(100) NOT NULL UNIQUE,
//     created_at TIMESTAMP NOT NULL,
//     ...
// );
```

---

### @Enumerated
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Patient.java:39`
- `backend/src/main/java/com/hospital/queue/entity/Token.java:43,48,53`
- `backend/src/main/java/com/hospital/queue/entity/User.java:34`

**Purpose:** Maps Java enum to database column

**Explanation:**
- `EnumType.STRING`: Stores as string ("ADMIN", "NORMAL", etc.)
- `EnumType.ORDINAL`: Stores as integer (0, 1, 2, etc.)
- Better than storing integers (more readable)

**Code Example:**
```java
public enum Role {
    ADMIN,
    RECEPTIONIST,
    DOCTOR
}

public enum Priority {
    EMERGENCY,    // order = 1
    URGENT,       // order = 2
    NORMAL        // order = 3
}

@Entity
@Table(name = "users")
public class User {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;  // Stored as "ADMIN", "RECEPTIONIST", "DOCTOR"
}

@Entity
@Table(name = "tokens")
public class Token {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;  // Stored as "EMERGENCY", "URGENT", "NORMAL"
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenStatus status;  // Stored as "WAITING", "IN_PROGRESS", "COMPLETED"
}

// SQL: CREATE TABLE users (
//     role VARCHAR(20) NOT NULL,
//     ...
// );
```

---

### @OneToOne
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:21`

**Purpose:** Maps one-to-one relationship between entities

**Explanation:**
- One Doctor has one User
- One User can have one Doctor (or none)
- `fetch = FetchType.EAGER`: Load related entity immediately
- Combined with `@JoinColumn` to specify foreign key

**Code Example:**
```java
@Entity
@Table(name = "doctors")
public class Doctor {
    
    @Id
    private Long id;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;  // One doctor has one user
}

// SQL: CREATE TABLE doctors (
//     id BIGINT PRIMARY KEY,
//     user_id BIGINT NOT NULL UNIQUE,
//     FOREIGN KEY (user_id) REFERENCES users(id)
// );
```

---

### @ManyToOne
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:25`
- `backend/src/main/java/com/hospital/queue/entity/Token.java:31,35,39`

**Purpose:** Maps many-to-one relationship between entities

**Explanation:**
- Many Doctors can belong to one Department
- Many Tokens belong to one Doctor
- `fetch = FetchType.EAGER`: Fetch related entity immediately
- Combined with `@JoinColumn` for foreign key

**Code Example:**
```java
@Entity
@Table(name = "doctors")
public class Doctor {
    
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;  // Many doctors, one department
}

@Entity
@Table(name = "tokens")
public class Token {
    
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;  // Many tokens, one doctor
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;  // Many tokens, one patient
}

// SQL: CREATE TABLE doctors (
//     id BIGINT PRIMARY KEY,
//     department_id BIGINT NOT NULL,
//     FOREIGN KEY (department_id) REFERENCES departments(id)
// );
```

---

### @JoinColumn
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Doctor.java:22,26`
- `backend/src/main/java/com/hospital/queue/entity/Token.java:32,36,40`

**Purpose:** Specifies the foreign key column name and properties

**Explanation:**
- Defines database column name for relationship
- `nullable`: Allow NULL foreign key values
- `unique`: Relationship must be unique (one-to-one)
- Default column name would be: [field-name]_id

**Code Example:**
```java
@Entity
@Table(name = "doctors")
public class Doctor {
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",          // Column name in doctor table
                nullable = false,          // Cannot be NULL
                unique = true)             // Each doctor has unique user
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id",    // Column name
                nullable = false)          // Cannot be NULL
    private Department department;
}

// SQL: CREATE TABLE doctors (
//     user_id BIGINT NOT NULL UNIQUE,
//     department_id BIGINT NOT NULL,
//     FOREIGN KEY (user_id) REFERENCES users(id),
//     FOREIGN KEY (department_id) REFERENCES departments(id)
// );
```

---

### @CreationTimestamp
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Patient.java:44`
- `backend/src/main/java/com/hospital/queue/entity/Token.java:58`
- `backend/src/main/java/com/hospital/queue/entity/User.java:42`

**Purpose:** Automatically sets field to current timestamp when entity is created

**Explanation:**
- Hibernatefeature (not standard JPA)
- Only set once at creation
- Useful for audit fields
- Cannot be updated manually

**Code Example:**
```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    private Long id;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // Auto-set to current time on insert
}

// When user is saved:
User user = new User();
// createdAt is automatically set to now()
// userRepository.save(user);
// → INSERT INTO users (id, created_at) VALUES (1, '2026-04-22 10:30:00')
```

---

### @UpdateTimestamp
**Locations:**
- `backend/src/main/java/com/hospital/queue/entity/Token.java:62`

**Purpose:** Automatically sets field to current timestamp whenever entity is updated

**Explanation:**
- Hibernate feature
- Updated every time entity is modified and saved
- Different from `@CreationTimestamp` (only on creation)
- Useful for audit/tracking

**Code Example:**
```java
@Entity
@Table(name = "tokens")
public class Token {
    
    @Id
    private Long id;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // Set once at creation
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // Updated every time token changes
}

// Creation:
// Token token = new Token();
// tokenRepository.save(token);
// → created_at = '2026-04-22 10:30:00', updated_at = '2026-04-22 10:30:00'

// Update:
// token.setStatus(COMPLETED);
// tokenRepository.save(token);
// → updated_at = '2026-04-22 10:45:00' (but created_at stays same)
```

---

## 8. SPRING WEBSOCKET ANNOTATIONS

### @EnableWebSocketMessageBroker
**Locations:**
- `backend/src/main/java/com/hospital/queue/config/WebSocketConfig.java:11`

**Purpose:** Enables WebSocket message broker configuration

**Explanation:**
- Enables STOMP over WebSocket
- Allows configuration of message broker
- Must be used with `@Configuration`
- Enables `@MessageMapping` in controllers

**Code Example:**
```java
@Configuration
@EnableWebSocketMessageBroker  // Enables WebSocket features
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

---

## 9. ANGULAR DECORATORS

### @Component
**Locations:**
- All `.component.ts` files in frontend

**Purpose:** Declares a class as an Angular component

**Explanation:**
- Component = Reusable UI element
- Has template (HTML), styles (CSS), logic (TS)
- Selector name used as HTML tag
- `templateUrl` and `styleUrls` point to files

**Code Example:**
```typescript
// admin-dashboard.component.ts
@Component({
  selector: 'app-admin-dashboard',           // <app-admin-dashboard></app-admin-dashboard>
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  title = 'Hospital Queue System';
  
  ngOnInit() {
    // Runs when component initializes
  }
}

// In HTML:
// <app-admin-dashboard></app-admin-dashboard> ← Uses selector
```

---

### @NgModule
**Locations:**
- `app.module.ts`, `admin.module.ts`, `auth.module.ts`, etc.

**Purpose:** Declares an Angular module containing components, services, etc.

**Explanation:**
- Module = Collection of components, services, guards
- `declarations`: Components in this module
- `imports`: Modules this module depends on
- `providers`: Services available in this module
- Root module: AppModule

**Code Example:**
```typescript
@NgModule({
  declarations: [
    AdminLayoutComponent,
    DashboardComponent,
    UsersComponent,
    DepartmentsComponent,
    DoctorsComponent
  ],
  imports: [
    CommonModule,
    AdminRoutingModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  providers: []
})
export class AdminModule { }
```

---

### @Injectable
**Locations:**
- All services in `core/services/` directory
- Guards, interceptors

**Purpose:** Declares a class as injectable service

**Explanation:**
- Service = Reusable business logic
- Can be injected into components/other services
- `providedIn: 'root'`: Singleton (one instance app-wide)
- Alternative: Add to module providers

**Code Example:**
```typescript
@Injectable({ providedIn: 'root' })  // Singleton service
export class AuthService {
    private currentUser$ = new BehaviorSubject<User | null>(null);
    
    constructor(private http: HttpClient) { }
    
    login(email: string, password: string) {
        return this.http.post('/api/auth/login', { email, password });
    }
}

// Usage in component:
@Component(...)
export class LoginComponent {
    constructor(private authService: AuthService) {  // Injected
        // ...
    }
}
```

---

## 10. EXCEPTION HANDLING ANNOTATIONS

### @ResponseStatus
**Locations:**
- `backend/src/main/java/com/hospital/queue/exception/BusinessException.java:6`
- `backend/src/main/java/com/hospital/queue/exception/ResourceNotFoundException.java:6`

**Purpose:** Sets HTTP status code when exception is thrown

**Explanation:**
- Exception thrown → Spring returns HTTP status
- Works on custom exception classes
- Can override with `@ExceptionHandler`
- 404, 400, 500, etc.

**Code Example:**
```java
@ResponseStatus(HttpStatus.NOT_FOUND)  // 404 when thrown
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)  // 400 when thrown
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// Usage:
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // → Returns 404 NOT_FOUND
}
```

---

### @RestControllerAdvice
**Locations:**
- `backend/src/main/java/com/hospital/queue/exception/GlobalExceptionHandler.java:16`

**Purpose:** Centralized exception handling for all controllers

**Explanation:**
- Class-level annotation
- `@ExceptionHandler` methods catch exceptions globally
- Applied to all `@RestController` classes
- Returns consistent error response format

**Code Example:**
```java
@RestControllerAdvice  // Handles exceptions from all endpoints
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessError(
            BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An error occurred"));
    }
}
```

---

### @ExceptionHandler
**Locations:**
- `backend/src/main/java/com/hospital/queue/exception/GlobalExceptionHandler.java:19,24,29,34,39,53`

**Purpose:** Handles specific exception types and returns custom response

**Explanation:**
- Catches specified exception type
- Can return ResponseEntity with custom response
- Works inside `@RestControllerAdvice`
- Prevents default error response

**Code Example:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(404)
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(404).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Validation Error")
            .details(fieldErrors)
            .build();
        return ResponseEntity.status(400).body(error);
    }
}
```

---

## SUMMARY TABLE

| Annotation | Type | Purpose | Example Location |
|---|---|---|---|
| `@SpringBootApplication` | Core | Main app class | HospitalQueueApplication.java |
| `@Configuration` | Core | Config class with beans | SecurityConfig.java |
| `@Bean` | Core | Produces managed bean | SecurityConfig.java |
| `@Component` | Core | Generic Spring bean | JwtAuthFilter.java |
| `@Value` | Core | Property injection | JwtService.java |
| `@RestController` | Web | REST endpoint handler | AdminController.java |
| `@RequestMapping` | Web | Base route path | All controllers |
| `@GetMapping` | Web | GET endpoint | AdminController.java |
| `@PostMapping` | Web | POST endpoint | AuthController.java |
| `@PutMapping` | Web | PUT endpoint | AdminController.java |
| `@DeleteMapping` | Web | DELETE endpoint | ReceptionistController.java |
| `@PathVariable` | Web | Extract URL parameter | All controllers |
| `@RequestBody` | Web | Parse request body | All controllers |
| `@Valid` | Web | Trigger validation | All controllers |
| `@Repository` | JPA | DB access object | DoctorRepository.java |
| `@Query` | JPA | Custom JPQL query | TokenRepository.java |
| `@Param` | JPA | Name query parameter | DoctorRepository.java |
| `@Lock` | JPA | Pessimistic locking | DoctorRepository.java |
| `@PreAuthorize` | Security | Role-based access | AdminController.java |
| `@EnableWebSecurity` | Security | Security configuration | SecurityConfig.java |
| `@EnableMethodSecurity` | Security | Enable method auth | SecurityConfig.java |
| `@NotBlank` | Validation | Non-empty string | CreateUserRequest.java |
| `@Email` | Validation | Valid email | LoginRequest.java |
| `@Size` | Validation | String length | CreateDoctorRequest.java |
| `@NotNull` | Validation | Non-null value | GenerateTokenRequest.java |
| `@Pattern` | Validation | Regex pattern | RegisterPatientRequest.java |
| `@Min / @Max` | Validation | Numeric range | RegisterPatientRequest.java |
| `@Data` | Lombok | Getters, setters, toString | All entities/DTOs |
| `@Builder` | Lombok | Builder pattern | All entities/DTOs |
| `@NoArgsConstructor` | Lombok | No-arg constructor | All entities |
| `@AllArgsConstructor` | Lombok | Full constructor | All entities |
| `@RequiredArgsConstructor` | Lombok | Final fields constructor | Services/Controllers |
| `@Slf4j` | Lombok | Logger field | DataInitializer.java |
| `@Builder.Default` | Lombok | Default builder value | Entity fields |
| `@Entity` | JPA | DB table mapping | User.java |
| `@Table` | JPA | Table properties | All entities |
| `@Id` | JPA | Primary key | All entities |
| `@GeneratedValue` | JPA | ID generation | All entities |
| `@Column` | JPA | Column properties | All entities |
| `@Enumerated` | JPA | Enum mapping | Token.java |
| `@OneToOne` | JPA | One-to-one relationship | Doctor.java |
| `@ManyToOne` | JPA | Many-to-one relationship | Doctor.java |
| `@JoinColumn` | JPA | Foreign key column | Doctor.java |
| `@CreationTimestamp` | Hibernate | Auto-set on creation | User.java |
| `@UpdateTimestamp` | Hibernate | Auto-set on update | Token.java |
| `@EnableWebSocketMessageBroker` | WebSocket | Enable WebSocket | WebSocketConfig.java |
| `@Component` (Angular) | Angular | Component declaration | All .component.ts |
| `@NgModule` | Angular | Module declaration | app.module.ts |
| `@Injectable` | Angular | Service declaration | All services |
| `@ResponseStatus` | Exception | HTTP status mapping | BusinessException.java |
| `@RestControllerAdvice` | Exception | Global error handler | GlobalExceptionHandler.java |
| `@ExceptionHandler` | Exception | Specific error handling | GlobalExceptionHandler.java |

---

*SmartApp - Complete Annotation Reference*  
*Prepared: 2026-04-22*

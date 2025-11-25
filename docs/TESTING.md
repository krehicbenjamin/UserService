# Testing Documentation

This document describes the testing strategy and how to run tests for the UserService application.

## Test Structure

The test suite follows the same clean architecture structure as the main application:

```
src/test/
├── java/
│   └── com/user/UserService/
│       ├── TestFixtures.java                    # Central test data factory
│       ├── user/
│       │   ├── domain/
│       │   │   ├── value/
│       │   │   │   └── EmailTest.java          # Value object tests
│       │   │   └── service/
│       │   │       ├── PasswordPolicyTest.java  # Domain service tests
│       │   │       └── TokenGeneratorTest.java
│       │   ├── repository/
│       │   │   ├── UserRepositoryTest.java      # Repository tests (@DataJpaTest)
│       │   │   └── RefreshTokenRepositoryTest.java
│       │   ├── service/
│       │   │   ├── AuthServiceTest.java         # Application service tests
│       │   │   ├── TokenServiceTest.java
│       │   │   └── UserServiceTest.java
│       │   └── web/
│       │       └── controller/
│       │           └── AuthControllerTest.java  # Controller tests (@WebMvcTest)
│       └── integration/
│           ├── AuthenticationIntegrationTest.java  # Full integration tests
│           └── SecurityIntegrationTest.java         # Security integration tests
└── resources/
    └── application-test.yaml                     # Test-specific configuration
```

## Test Categories

### 1. Unit Tests

**Value Object Tests** (`EmailTest.java`)
- Email validation and normalization
- Format validation
- Edge cases (null, empty, invalid formats)

**Domain Service Tests**
- `PasswordPolicyTest.java`: Password strength validation
- `TokenGeneratorTest.java`: JWT generation and parsing
- `DomainUserValidatorTest.java`: Domain validation logic

**Application Service Tests**
- `AuthServiceTest.java`: User registration and login logic
- `TokenServiceTest.java`: Token refresh and revocation
- `UserServiceTest.java`: User management operations
- Uses Mockito for mocking dependencies

**Controller Tests** (`AuthControllerTest.java`)
- REST endpoint validation
- Request/response mapping
- HTTP status codes
- Uses `@WebMvcTest` for lightweight controller testing

### 2. Integration Tests

**Repository Tests** (`@DataJpaTest`)
- `UserRepositoryTest.java`: Database operations for users
- `RefreshTokenRepositoryTest.java`: Token persistence
- Uses H2 in-memory database
- Tests JPA queries and relationships

**API Integration Tests** (`@SpringBootTest`)
- `AuthenticationIntegrationTest.java`: Full authentication flow
  - Registration → Login → Protected endpoint access
  - Token refresh flow
  - Session management
- Uses full Spring context with MockMvc

**Security Integration Tests**
- `SecurityIntegrationTest.java`: Security features
  - Authentication and authorization
  - Rate limiting
  - Input validation
  - Security headers
  - CORS enforcement

## Running Tests

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=AuthServiceTest
```

### Run Specific Test Method

```bash
./mvnw test -Dtest=AuthServiceTest#shouldRegisterNewUser
```

### Run Tests with Coverage

```bash
./mvnw test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

### Run Only Unit Tests

```bash
./mvnw test -Dgroups=unit
```

### Run Only Integration Tests

```bash
./mvnw test -Dgroups=integration
```

### Run Tests in IDE

**IntelliJ IDEA:**
- Right-click on test class or method
- Select "Run" or "Debug"
- Use `Ctrl+Shift+F10` (Windows/Linux) or `Cmd+Shift+R` (Mac)

**VS Code:**
- Install "Java Test Runner" extension
- Click "Run Test" above test method

## Test Configuration

### Test Database

Tests use H2 in-memory database configured in `application-test.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
```

H2 is configured in PostgreSQL compatibility mode to match production behavior.

### Test Fixtures

`TestFixtures.java` provides factory methods for creating test data:

```java
// Create test user
User user = TestFixtures.Users.createUser();

// Create test user with custom data
User admin = TestFixtures.Users.createUser("admin@example.com", "Admin User");

// Create test requests
RegisterRequest request = TestFixtures.Requests.createRegisterRequest();
LoginRequest login = TestFixtures.Requests.createLoginRequest();
```

### Mocking

Tests use Mockito for mocking dependencies:

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void shouldRegisterUser() {
        when(userRepository.save(any())).thenReturn(testUser);
        // ... test logic
    }
}
```

## Test Data Management

### Database Cleanup

Integration tests use `@Transactional` to automatically rollback after each test:

```java
@SpringBootTest
@Transactional
class AuthenticationIntegrationTest {
    // Test data is rolled back after each test
}
```

### Test Isolation

Each test is independent and doesn't rely on other tests:
- Use `@BeforeEach` for setup
- Use `@AfterEach` for cleanup (if needed)
- Don't share mutable state between tests

## Assertions

Tests use AssertJ for fluent assertions:

```java
// Simple assertions
assertThat(user.getEmail()).isEqualTo(expectedEmail);
assertThat(users).hasSize(3);

// Exception assertions
assertThatThrownBy(() -> service.someMethod())
    .isInstanceOf(CustomException.class)
    .hasMessageContaining("expected message");

// Collection assertions
assertThat(sessions)
    .hasSize(2)
    .extracting(DeviceSession::getDeviceName)
    .containsOnly("Device 1", "Device 2");
```

## Code Coverage

### Current Coverage Targets

- **Overall**: 80%+
- **Service Layer**: 90%+
- **Domain Layer**: 95%+
- **Controller Layer**: 80%+

### Viewing Coverage

```bash
./mvnw test jacoco:report
open target/site/jacoco/index.html
```

### Excluding from Coverage

Some classes are excluded from coverage:
- DTOs (data classes)
- Configuration classes
- Application entry point
- Lombok-generated code

## Continuous Integration

Tests run automatically in CI/CD pipeline:

```yaml
# .github/workflows/ci-cd.yaml
- name: Run tests
  run: ./mvnw test

- name: Generate coverage report
  run: ./mvnw jacoco:report
```

## Best Practices

### 1. Test Naming

Use descriptive test names that explain what is being tested:

```java
@Test
void shouldRegisterNewUserWithValidCredentials() { }

@Test
void shouldThrowExceptionWhenEmailAlreadyExists() { }

@Test
void shouldReturnUserWhenIdExists() { }
```

### 2. AAA Pattern

Structure tests using Arrange-Act-Assert:

```java
@Test
void shouldLoginSuccessfully() {
    // Arrange
    LoginRequest request = TestFixtures.Requests.createLoginRequest();
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
    
    // Act
    TokenResponse response = authService.login(request);
    
    // Assert
    assertThat(response.accessToken()).isNotNull();
    verify(userRepository).findByEmail(any());
}
```

### 3. Single Responsibility

Each test should verify one behavior:

```java
// Good
@Test
void shouldValidateEmailFormat() { }

@Test
void shouldNormalizeEmailToLowerCase() { }

// Bad
@Test
void shouldValidateAndNormalizeEmail() { }
```

### 4. Use Test Fixtures

Don't create test data inline, use fixtures:

```java
// Good
User user = TestFixtures.Users.createUser();

// Bad
User user = User.builder()
    .id(UUID.randomUUID())
    .email(new Email("test@example.com"))
    // ... many more fields
    .build();
```

### 5. Mock External Dependencies

Don't test external systems:

```java
// Good - mock external service
@Mock
private EmailService emailService;

// Bad - actually call external service
emailService.sendEmail(...); // Hits real SMTP server
```

## Troubleshooting

### Tests Fail Locally But Pass in CI

- Check Java version (should be Java 21)
- Clear Maven cache: `./mvnw clean`
- Check timezone differences
- Verify test database state

### Slow Tests

- Use `@DataJpaTest` instead of `@SpringBootTest` for repository tests
- Use `@WebMvcTest` for controller tests
- Mock expensive operations
- Use in-memory H2 instead of PostgreSQL

### Flaky Tests

- Remove sleep() calls
- Don't rely on execution order
- Use proper synchronization for async operations
- Don't use fixed timestamps, use Clock abstraction

### Database Errors

- Check Flyway migrations are compatible with H2
- Ensure H2 is in PostgreSQL mode
- Verify test transactions are rolling back
- Check for foreign key constraint violations

## Additional Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Test Containers](https://www.testcontainers.org/) (for future PostgreSQL integration tests)


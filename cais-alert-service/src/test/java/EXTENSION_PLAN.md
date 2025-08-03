# Testing Framework Extension Plan

## 🎯 **Extension Strategy Overview**

The framework is designed with **modularity** and **reusability** at its core. Each module follows the same pattern, making extension straightforward and consistent.

## 📁 **Recommended Module Structure**

```
src/test/java/
├── com/dair/cais/
│   ├── CaisAlertApplicationTest.java          # ✅ Global app context test
│   │
│   ├── cases/                                 # ✅ COMPLETED - Case Module
│   │   ├── TestDataFactory.java              
│   │   ├── ComprehensiveCaseWorkflowTest.java 
│   │   └── workflow/service/
│   │       ├── CaseWorkflowServiceTest.java
│   │       └── CaseWorkflowServiceSimpleTest.java
│   │
│   ├── alerts/                                # 🔄 NEXT - Alert Module
│   │   ├── AlertTestDataFactory.java
│   │   ├── AlertControllerTest.java
│   │   ├── AlertServiceTest.java
│   │   └── AlertWorkflowTest.java
│   │
│   ├── workflow/                              # 🔄 Workflow Module
│   │   ├── WorkflowTestDataFactory.java
│   │   ├── WorkflowControllerTest.java
│   │   ├── WorkflowServiceTest.java
│   │   └── engine/
│   │       └── WorkflowEngineTest.java
│   │
│   ├── reports/                               # 🔄 Reports Module
│   │   ├── ReportTestDataFactory.java
│   │   ├── ReportControllerTest.java
│   │   └── ReportServiceTest.java
│   │
│   ├── access/                                # 🔄 Access Control Module
│   │   ├── AccessTestDataFactory.java
│   │   ├── UserPermissionServiceTest.java
│   │   └── RoleManagementTest.java
│   │
│   ├── audit/                                 # 🔄 Audit Module
│   │   ├── AuditTestDataFactory.java
│   │   └── AuditTrailServiceTest.java
│   │
│   └── common/                                # 🔄 Shared Testing Utilities
│       ├── BaseTestDataFactory.java          # Common test data patterns
│       ├── BaseControllerTest.java           # Common controller test setup
│       ├── BaseServiceTest.java              # Common service test setup
│       └── TestUtils.java                    # Common test utilities
```

## 🏗️ **Step-by-Step Extension Process**

### **Phase 1: Create Module Foundation**

#### **1.1 Create Test Data Factory**
```java
// Example: src/test/java/com/dair/cais/alerts/AlertTestDataFactory.java
@Component
public class AlertTestDataFactory extends BaseTestDataFactory {
    
    public static Alert createTestAlert() {
        Alert alert = new Alert();
        alert.setAlertId("TEST_ALERT_" + generateId());
        alert.setTitle("Test Alert for Integration Testing");
        alert.setStatus("New");
        alert.setPriority("High");
        alert.setAlertType("account-review");
        alert.setCreatedBy("TEST_SYSTEM");
        alert.setIsActive(true);
        return alert;
    }
    
    public static BulkAlertRequest createBulkAlertRequest(List<String> alertIds, String action) {
        BulkAlertRequest request = new BulkAlertRequest();
        request.setAlertIds(alertIds);
        request.setAction(action);
        request.setReason("Bulk operation for testing");
        return request;
    }
    
    // Alert-specific test data constants
    public static final String DEFAULT_ALERT_TYPE = "account-review";
    public static final String DEFAULT_WORKFLOW_ID = "104";
    public static final String DEFAULT_STEP_ID = "70";
}
```

#### **1.2 Create Service Test**
```java
// Example: src/test/java/com/dair/cais/alerts/AlertServiceTest.java
@ExtendWith(MockitoExtension.class)
@DisplayName("Alert Service Tests")
class AlertServiceTest extends BaseServiceTest {

    @Mock
    private AlertRepository alertRepository;
    
    @InjectMocks
    private AlertService alertService;

    @Test
    @DisplayName("Should retrieve alert by ID")
    void getAlert_ValidId_ReturnsAlert() {
        // Given
        String alertId = "TEST_ALERT_123";
        Alert mockAlert = AlertTestDataFactory.createTestAlert();
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        // When
        Alert result = alertService.getAlert(alertId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAlertId()).isEqualTo(alertId);
        verify(alertRepository).findById(alertId);
    }
}
```

#### **1.3 Create Controller Test**
```java
// Example: src/test/java/com/dair/cais/alerts/AlertControllerTest.java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Alert Controller Integration Tests")
class AlertControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("GET: Should retrieve alert by ID")
    void getAlert_ValidId_ReturnsAlert() throws Exception {
        // Create test alert using existing endpoint or directly in DB
        Alert testAlert = createTestAlertInDatabase();
        
        mockMvc.perform(get("/alerts/{id}", testAlert.getAlertId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId", is(testAlert.getAlertId())))
                .andExpect(jsonPath("$.title", is(testAlert.getTitle())));
    }
}
```

### **Phase 2: Create Base Classes for Reusability**

#### **2.1 Base Test Data Factory**
```java
// src/test/java/com/dair/cais/common/BaseTestDataFactory.java
public abstract class BaseTestDataFactory {
    
    protected static final Random RANDOM = new Random();
    
    protected static String generateId() {
        return String.valueOf(System.currentTimeMillis() + RANDOM.nextInt(1000));
    }
    
    protected static LocalDateTime futureDate() {
        return LocalDateTime.now().plusDays(1);
    }
    
    protected static LocalDateTime pastDate() {
        return LocalDateTime.now().minusDays(1);
    }
    
    // Common audit request creation
    public static AuditLogRequest createBaseAuditRequest() {
        AuditLogRequest audit = new AuditLogRequest();
        audit.setUserId(123L);
        audit.setUserRole("TEST_ANALYST");
        audit.setActionId(1);
        audit.setDescription("Test operation");
        audit.setCategory("TEST_CATEGORY");
        return audit;
    }
}
```

#### **2.2 Base Controller Test**
```java
// src/test/java/com/dair/cais/common/BaseControllerTest.java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public abstract class BaseControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @BeforeEach
    void baseSetUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
    }
    
    protected <T> T parseResponse(MvcResult result, Class<T> responseType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
    }
    
    protected void assertSuccessResponse(MvcResult result) throws Exception {
        assertThat(result.getResponse().getStatus()).isBetween(200, 299);
    }
}
```

#### **2.3 Base Service Test**
```java
// src/test/java/com/dair/cais/common/BaseServiceTest.java
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {
    
    protected void verifyNoMoreInteractionsOnMocks(Object... mocks) {
        verifyNoMoreInteractions(mocks);
    }
    
    protected <T> ArgumentCaptor<T> captorFor(Class<T> clazz) {
        return ArgumentCaptor.forClass(clazz);
    }
    
    protected void assertEntityNotFound(Executable executable, String expectedMessage) {
        assertThatThrownBy(executable)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(expectedMessage);
    }
}
```

### **Phase 3: Module-Specific Implementation**

#### **3.1 Alert Module Extension**
```bash
# Create alert module tests
mkdir -p src/test/java/com/dair/cais/alerts/service
mkdir -p src/test/java/com/dair/cais/alerts/controller

# Copy and adapt from cases module
cp src/test/java/com/dair/cais/cases/TestDataFactory.java src/test/java/com/dair/cais/alerts/AlertTestDataFactory.java
cp src/test/java/com/dair/cais/cases/workflow/service/CaseWorkflowServiceSimpleTest.java src/test/java/com/dair/cais/alerts/service/AlertServiceTest.java

# Modify for alert-specific logic
```

#### **3.2 Workflow Module Extension**
```bash
# Create workflow module tests
mkdir -p src/test/java/com/dair/cais/workflow/service
mkdir -p src/test/java/com/dair/cais/workflow/engine

# Focus on workflow engine testing
# Test workflow transitions, validations, rule engine
```

#### **3.3 Reports Module Extension**
```bash
# Create reports module tests
mkdir -p src/test/java/com/dair/cais/reports/service
mkdir -p src/test/java/com/dair/cais/reports/controller

# Focus on report generation, parameterization, execution
```

## 📋 **Module Extension Checklist**

### **For Each New Module:**

#### **✅ Test Data Factory**
- [ ] Create `{Module}TestDataFactory.java`
- [ ] Extend `BaseTestDataFactory`
- [ ] Define module-specific test objects
- [ ] Add constant values for testing
- [ ] Include bulk operation requests if applicable

#### **✅ Service Layer Tests**
- [ ] Create `{Module}ServiceTest.java`
- [ ] Extend `BaseServiceTest`
- [ ] Test business logic with mocks
- [ ] Cover error scenarios
- [ ] Test validation logic
- [ ] Include performance-critical operations

#### **✅ Controller Layer Tests**
- [ ] Create `{Module}ControllerTest.java`
- [ ] Extend `BaseControllerTest`
- [ ] Test all HTTP methods (GET, POST, PUT, PATCH, DELETE)
- [ ] Cover request/response mapping
- [ ] Test error handling (404, 500, 400)
- [ ] Include authentication/authorization tests

#### **✅ Integration Tests**
- [ ] Create comprehensive integration test
- [ ] Test end-to-end workflows
- [ ] Include database operations
- [ ] Test external service integrations
- [ ] Cover audit logging

#### **✅ Configuration**
- [ ] Update `application-test.yml` if needed
- [ ] Add module-specific test profiles
- [ ] Configure test-specific beans
- [ ] Set up test database schemas

## 🔄 **Execution Strategy**

### **Priority Order for Extension:**
1. **🟦 Alerts Module** (Core business entity, similar to cases)
2. **🟨 Workflow Module** (Core engine functionality)  
3. **🟩 Reports Module** (Data processing and generation)
4. **🟪 Access Control Module** (Security and permissions)
5. **🟫 Audit Module** (Logging and compliance)

### **Per Module Timeline:**
- **Week 1**: Test Data Factory + Service Tests
- **Week 2**: Controller Tests + Integration Tests
- **Week 3**: End-to-end testing + CI/CD integration
- **Week 4**: Documentation + Knowledge transfer

## 🚀 **Advanced Features to Add**

### **Performance Testing**
```java
@Test
@DisplayName("Performance: Should handle bulk operations efficiently")
void bulkOperation_LargeDataSet_PerformsWithinLimits() {
    // Test with 1000+ records
    // Assert execution time < 5 seconds
}
```

### **Parameterized Testing**
```java
@ParameterizedTest
@ValueSource(strings = {"account-review", "AML", "customer-support"})
@DisplayName("Should handle different alert types")
void processAlert_DifferentTypes_HandlesCorrectly(String alertType) {
    // Test logic for different alert types
}
```

### **Test Containers Integration**
```java
@Testcontainers
class DatabaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test_case_manager")
            .withUsername("test")
            .withPassword("test");
}
```

### **Contract Testing**
```java
@AutoConfigureWireMock
class ExternalServiceContractTest {
    // Test API contracts with external services
}
```

## 🎯 **Success Metrics**

### **Code Coverage Targets:**
- **Service Layer**: > 90%
- **Controller Layer**: > 85%
- **Integration Tests**: > 80%
- **Overall Project**: > 85%

### **Test Quality Metrics:**
- All tests must be deterministic (no flaky tests)
- Each test must be independent
- Test execution time < 10 minutes for full suite
- Zero test dependencies on external services

### **Maintenance Guidelines:**
- Update tests with every feature addition
- Refactor tests when refactoring code
- Keep test data factories up to date
- Document test scenarios and edge cases

---

This framework provides you with a **scalable, maintainable, and comprehensive testing solution** that can grow with your application. Each module follows the same patterns, making it easy for any developer to understand and extend the test suite.
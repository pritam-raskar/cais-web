# Case Workflow Endpoints Implementation Plan

## Overview
Implement case workflow endpoints similar to the existing alert workflow system. This will allow managing case step transitions, retrieving available steps, and performing bulk operations on cases.

## Analysis Summary

### Current Alert System Architecture
- **Alert Entity**: Contains `alertStepId` and workflow information
- **AlertController**: Has endpoints for step transitions and bulk operations
- **AlertService**: Contains business logic for step changes and workflow management
- **StepTransitionDTO**: Used for returning next/back steps
- **BulkStepChangeRequest/Response**: DTOs for bulk operations

### Current Case System Architecture
- **Case Entity**: Contains `caseType`, `workflowId`, `currentStepId` fields
- **CaseWorkflowController**: Already exists with basic workflow operations
- **CaseWorkflowService**: Handles workflow assignment and transitions
- **Case-CaseType-Workflow relationship**: Cases have caseType which maps to workflowId

### Required Endpoints Analysis
From AlertChangeStep.postman_collection.json, we need to implement:

1. **Single Case Step Changes**:
   - `PATCH /cases/changestep/{caseId}`
   - `PATCH /cases/audit/changestep/{caseId}`

2. **Bulk Case Step Changes**:
   - `POST /cases/bulk/step-change`
   - `POST /cases/audit/bulk/step-change`
   - `POST /cases/bulk/change-step`
   - `POST /cases/audit/bulk/change-step`

3. **Step Information**:
   - `GET /cases/{caseId}/step-transitions` (similar to alert)
   - `GET /case-workflows/cases/{caseId}/available-steps` (already exists but needs enhancement)

## Implementation Plan

### Phase 1: Core DTOs and Data Structures
1. **Create Case-specific DTOs**:
   - `CaseBulkStepChangeRequest` (similar to `BulkStepChangeRequest`)
   - `CaseBulkStepChangeResponse` (similar to `BulkStepChangeResponse`)
   - Reuse `StepTransitionDTO` for case step transitions

### Phase 2: Enhance CaseWorkflowService
1. **Add new methods to CaseWorkflowService**:
   - `getCaseStepTransitions(Long caseId)` - Get next/back steps for a case
   - `changeStepBulk(CaseBulkStepChangeRequest request)` - Bulk step change
   - `changeStepBulkWithAudit(...)` - Bulk step change with audit
   - `changeStep(Long caseId, Long stepId, String userId)` - Single step change
   - `changeStepWithAudit(...)` - Single step change with audit

2. **Logic Implementation**:
   - Use case's `caseType` to get `workflowId` from CaseType entity
   - From `workflowId`, get all available workflow steps
   - Implement transition validation logic similar to alerts
   - Handle bulk operations with individual validation

### Phase 3: Create New CaseController Endpoints
1. **Create new CaseController class** (separate from CaseWorkflowController):
   - Handle case-specific operations similar to AlertController
   - Implement all endpoints from AlertChangeStep collection pattern

2. **Endpoint Implementation**:
   - Single step change endpoints with and without audit
   - Bulk step change endpoints with validation
   - Step transitions endpoint (`/cases/{caseId}/step-transitions`)

### Phase 4: Enhance Existing CaseWorkflowController
1. **Update available-steps endpoint**:
   - Current: `/case-workflows/cases/{caseId}/available-steps`
   - Enhance to follow the case → caseType → workflow → steps pattern
   - Return all steps in the workflow, not just next available steps

### Phase 5: Database Schema Validation
1. **Verify CaseType-Workflow relationship**:
   - Ensure `cm_case_type` table has `workflow_id` column
   - Validate foreign key relationships
   - Check if case → caseType mapping is properly implemented

### Phase 6: Testing and Validation
1. **Create comprehensive tests**:
   - Unit tests for service methods
   - Integration tests for controller endpoints
   - Test case → caseType → workflow → steps flow
   - Test bulk operations and error handling

## Technical Implementation Details

### URL Patterns to Implement
```
# Single case step changes
PATCH /cases/changestep/{caseId}
PATCH /cases/audit/changestep/{caseId}

# Bulk case step changes  
POST /cases/bulk/step-change
POST /cases/audit/bulk/step-change
POST /cases/bulk/change-step
POST /cases/audit/bulk/change-step

# Step information
GET /cases/{caseId}/step-transitions
GET /case-workflows/cases/{caseId}/available-steps (enhance existing)
```

### Data Flow
1. **Case ID** → **Case Entity** (get caseType)
2. **Case Type** → **CaseType Entity** (get workflowId) 
3. **Workflow ID** → **Workflow Steps** (get available steps)
4. **Current Step** → **Transitions** (get next/back steps)

### DTO Structure
```java
// Reuse from alert system
public class CaseBulkStepChangeRequest {
    private List<Long> caseIds;
    private Long stepId;
    private String userId;
    private String reason;
}

public class CaseBulkStepChangeResponse {
    private int successCount;
    private int failureCount;
    private List<String> successfulCaseIds;
    private List<String> failedCaseIds;
    private Map<String, String> failures;
}
```

## Dependencies and Prerequisites
- Existing workflow engine infrastructure
- Case-CaseType-Workflow relationship properly established
- Audit logging system integration
- Error handling and validation framework

## Success Criteria
1. All endpoints from AlertChangeStep collection working for cases
2. Proper case → caseType → workflow → steps flow
3. Bulk operations with individual validation
4. Comprehensive error handling and audit logging
5. Full integration with existing workflow system

## Implementation Status

### ✅ COMPLETED COMPONENTS

#### Phase 1: Core DTOs and Data Structures
- ✅ **BulkStepChangeRequest** - Created at `src/main/java/com/dair/cais/cases/dto/BulkStepChangeRequest.java`
- ✅ **BulkStepChangeResponse** - Created at `src/main/java/com/dair/cais/cases/dto/BulkStepChangeResponse.java`
- ✅ **StepTransitionDTO** - Reused from existing workflow system

#### Phase 2: Enhanced CaseWorkflowService
- ✅ **changeStep()** - Single case step change method
- ✅ **changeStepWithAudit()** - Single case step change with audit logging
- ✅ **changeStepBulk()** - Bulk step change for multiple cases
- ✅ **changeStepBulkWithAudit()** - Bulk step change with audit logging
- ✅ **getCaseStepTransitions()** - Get next/back steps for a case (already existed)
- ✅ **getAvailableStepsViaCaseType()** - NEW: case → caseType → workflow → steps flow

#### Phase 3: Enhanced CaseController Endpoints
- ✅ **PATCH /cases/changestep/{caseId}** - Single step change
- ✅ **PATCH /cases/audit/changestep/{caseId}** - Single step change with audit
- ✅ **POST /cases/bulk/step-change** - Bulk step change
- ✅ **POST /cases/audit/bulk/step-change** - Bulk step change with audit
- ✅ **POST /cases/bulk/change-step** - Alternative bulk step change endpoint
- ✅ **POST /cases/audit/bulk/change-step** - Alternative bulk step change with audit
- ✅ **GET /cases/{caseId}/step-transitions** - Get next/back steps for a case

#### Phase 4: Enhanced CaseWorkflowController
- ✅ **GET /case-workflows/cases/{caseId}/available-steps** - Enhanced to use case → caseType → workflow pattern

### 🔧 IMPLEMENTATION DETAILS

#### Service Layer Enhancements
- **CaseWorkflowService**: Added `CaseTypeRepository` dependency
- **Audit Integration**: Full audit trail support using `AuditTrailService`
- **Error Handling**: Comprehensive exception handling with proper HTTP status codes
- **Transaction Management**: All operations properly annotated with `@Transactional`

#### Data Flow Implementation
1. **Case ID** → **Case Entity** (get caseType)
2. **Case Type Name** → **CaseType Entity** (get workflowId)
3. **Workflow ID** → **Workflow Steps** (get all available steps)
4. **Current Step** → **Transitions** (get next/back steps)

#### URL Patterns Implemented
```
# Single case step changes
✅ PATCH /cases/changestep/{caseId}
✅ PATCH /cases/audit/changestep/{caseId}

# Bulk case step changes  
✅ POST /cases/bulk/step-change
✅ POST /cases/audit/bulk/step-change
✅ POST /cases/bulk/change-step
✅ POST /cases/audit/bulk/change-step

# Step information
✅ GET /cases/{caseId}/step-transitions
✅ GET /case-workflows/cases/{caseId}/available-steps (enhanced)
```

### 📊 VALIDATION RESULTS
- ✅ **Build Status**: `mvn clean compile` - SUCCESS
- ✅ **Pattern Matching**: All endpoints mirror AlertController functionality
- ✅ **Case-Type-Workflow Flow**: Properly implemented via `getAvailableStepsViaCaseType()`
- ✅ **Audit Integration**: Full audit logging support across all operations
- ✅ **Error Handling**: Comprehensive exception handling with proper HTTP responses

### 🚀 READY FOR TESTING

All core functionality has been implemented and the project compiles successfully. The case workflow endpoints now provide the same capabilities as the alert system:

1. **Single Operations**: Individual case step changes with optional audit
2. **Bulk Operations**: Multi-case step changes with validation and audit
3. **Step Information**: Get available transitions and workflow steps
4. **Proper Data Flow**: case → caseType → workflow → steps pattern implemented

## Risk Mitigation
- ✅ Verified database schema relationships during implementation
- ✅ Implemented proper transaction management for bulk operations  
- ✅ Ensured audit trail consistency across all operations
- 🔄 **Next**: Test with existing case data to ensure compatibility
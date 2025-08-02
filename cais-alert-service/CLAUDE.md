# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Plan & Review

### Before starting work
- Always in plan mode to make a plan
- After get the plan, make sure you Write the plan to .claude/tasks/TASK_NAME.md
- The plan should be a detailed implementation plan and the reasoning behind them, as well as tasks broken down.
- If the task require external knowledge or certain package, also research to get latest knowledge (Use Task tool for research)
- Don't over plan it, always think MVP.
- Once you write the plan, firstly ask me to review it. Do not continue until I approve the plan.

### While implementing
- You should update the plan as you work on task.
- After you complete tasks in the plan, you should update and append detailed descriptions of the changes you made, so following tasks can be easily hand over to other engineers.

## Project Overview

CAIS Alert Service is a Spring Boot 3.x Java application that manages alerts, cases, workflows, and associated operations in a Case Alert Information System. It's part of a larger microservices architecture (`cais-web`) with dependencies on `cais-base`.

## Build and Development Commands

### Maven Commands
- **Build**: `mvn clean compile` - Compiles the application
- **Package**: `mvn clean package` - Creates JAR file in `target/`
- **Run locally**: `mvn spring-boot:run` - Starts the application on port 8081
- **Tests**: `mvn test` - Runs unit tests
- **Clean**: `mvn clean` - Removes target directory

### Running the Application
The application runs on port 8081 by default (configurable via PORT environment variable).

## Architecture Overview

### Core Domain Components
- **Alerts**: Primary business entity with workflow management, filtering, attachments, notes
- **Cases**: Container for multiple alerts with audit trails and workflows  
- **Workflows**: Configurable step-based processes with transitions, deadlines, and notifications
- **Users & Access Control**: Role-based permissions with organization units and policies
- **Reports**: Dynamic report generation with customizable parameters and execution
- **Attachments**: File management with local and S3 storage options

### Data Layer Architecture
- **Dual Database**: PostgreSQL (RDBMS) + MongoDB (document store)
  - PostgreSQL: User management, workflow definitions, metadata
  - MongoDB: Alert documents, audit trails, large JSON structures
- **JPA/Hibernate**: Entity management with custom repositories
- **Connection abstraction**: Multi-database connectivity (PostgreSQL, MySQL, Snowflake)

### Key Packages Structure
- `com.dair.cais.alert.*` - Alert management and filtering
- `com.dair.cais.cases.*` - Case management with alerts
- `com.dair.cais.workflow.*` - Workflow engine and configurations  
- `com.dair.cais.access.*` - Permission and role management
- `com.dair.cais.reports.*` - Report designer and execution
- `com.dair.cais.steps.*` - Workflow step management with SLA
- `com.dair.cais.common.config.*` - Shared configuration

### Configuration Management
- Main config: `src/main/resources/application.yml`
- Database connection in `DataSourceConfig.java`
- JPA configuration in `JpaConfig.java`
- Async processing enabled via `@EnableAsync`

## Development Guidelines

### Database Considerations
- PostgreSQL schema: `info_alert` (default)
- MongoDB database: `CMP_DB` (configurable)
- Use appropriate repository based on data type (JPA for structured, MongoDB for documents)

### Security & Authentication
- JWT-based authentication with configurable expiration
- Role-based access control with organization-level permissions
- Encryption service for sensitive connection data

### File Storage
- Configurable storage: local filesystem or AWS S3
- Large file handling up to 100MB
- Attachment audit trails maintained

### Workflow Engine
- Step-based workflows with configurable transitions
- SLA tracking and deadline management
- Notification system for step changes
- Bulk operations support for alert state changes

### Testing Strategy
- Test files located in `src/test/java/`
- Use appropriate database configurations for testing
- Mock external dependencies (S3, external databases)

## Common Operations

### Alert Management
- Create/update alerts with MongoDB storage
- Apply filters using `MongoQueryBuilder`
- Bulk step transitions via `BulkStepChangeRequest`

### Case Management  
- Link multiple alerts to cases
- Maintain audit trails for case operations
- Generate case reports and statistics

### Workflow Operations
- Define workflows with steps and transitions
- Configure deadlines and notification rules
- Handle step assignments and permissions

### Report Generation
- Design reports with configurable columns
- Execute queries against various data sources
- Export results in multiple formats

### Debug Endpoints for Issue Investigation
**Note**: These endpoints are for development/debugging only. Remove from production environments.

#### Available Debug Endpoints
- **GET** `/debug/test-db` - Test PostgreSQL and MongoDB connections
- **POST** `/debug/sql` - Execute direct SQL queries against PostgreSQL
- **GET** `/debug/alert/{alertId}` - Get alert details from both databases
- **POST** `/debug/update-alert-step` - Update alert step IDs in MongoDB

#### Usage Examples
```bash
# Test database connections
curl -X GET "http://localhost:8081/debug/test-db"

# Execute SQL query for workflow investigation
curl -X POST "http://localhost:8081/debug/sql" \
  -H "Content-Type: application/json" \
  -d '{"sql": "SELECT * FROM info_alert.cm_workflow_step WHERE workflow_id = 104"}'

# Check alert details
curl -X GET "http://localhost:8081/debug/alert/AccRev_20241120195724_3247548734"

# Update alert step (use carefully)
curl -X POST "http://localhost:8081/debug/update-alert-step" \
  -H "Content-Type: application/json" \
  -d '{"alertId": "AccRev_123", "alertStepId": "17"}'
```

#### Common Investigation Queries
```sql
-- Check workflow transitions for step-transitions debugging
SELECT * FROM info_alert.cm_workflow_transition 
WHERE workflow_id = 104 AND source_step_id = 17;

-- Validate alert step consistency
SELECT a.alert_id, a.alert_step_id, ws.label 
FROM info_alert.cm_alerts a
LEFT JOIN info_alert.cm_workflow_step ws ON a.alert_step_id::int = ws.workflow_step_id
WHERE a.is_active = true AND a.is_deleted = false;

-- Find alerts with invalid workflow step IDs
SELECT a.alert_id, a.alert_step_id 
FROM info_alert.cm_alerts a
LEFT JOIN info_alert.cm_workflow_step ws ON a.alert_step_id::int = ws.workflow_step_id
WHERE a.is_active = true AND ws.workflow_step_id IS NULL;
```

#### Safety Guidelines
- Always use READ queries first to investigate issues
- Test UPDATE queries on development data before production
- Ensure both PostgreSQL and MongoDB are updated for consistency
- Remove debug endpoints before deploying to production
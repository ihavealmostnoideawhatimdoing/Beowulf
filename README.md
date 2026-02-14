# Beowulf - Clinical Order Management System

A CRUD-style API that models a clinical workflow: **patients**, **orders**, **studies**, and **results**, with real-world healthcare constraints including result versioning and optimistic locking. Includes a minimal web UI to demonstrate the API functionality.

## Quick Start

### Running Locally

```bash
# Requires Java 17+ and Maven
mvn clean spring-boot:run
# App runs at http://localhost:5000
```

Once running, access:
- **UI**: http://localhost:5000
- **API**: All endpoints under `/api/`
- **Swagger Docs**: http://localhost:5000/swagger-ui.html
- **H2 Console**: http://localhost:5000/h2-console (JDBC URL: `jdbc:h2:mem:clinicaldb`, user: `sa`, no password)

### Running with Docker

```bash
docker-compose up --build
# App runs at http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

## Architecture

### Tech Stack
- **Backend**: Java 17, Spring Boot 3.2, Spring Data JPA, Hibernate
- **Database**: H2 in-memory (auto-created on startup)
- **Migrations**: Flyway
- **API Docs**: SpringDoc OpenAPI (Swagger UI)
- **Frontend**: Plain HTML/JavaScript/CSS with Bootstrap 5

### Domain Model

```
Patient (1) <---- (many) Order
Order (1) <---- (1) Study
Order (1) <---- (many) OrderResult
```

- **Patient**: Identified by unique MRN (Medical Record Number)
- **Order**: Physician's request for a diagnostic test, snapshots patient data at order time
- **Study**: Execution/processing of an Order (1:1). Status: ORDERED -> FINALIZED -> AMENDED
- **OrderResult**: Immutable, versioned signed reports. Created automatically when studies are signed/amended

## Database Schema

### Entity Relationships
```
Patient (1) ←──── (many) Order
Order (1) ←──── (1) Study
Order (1) ←──── (many) OrderResult
```

### Tables
- **patient** - Patient demographics with unique MRN
- **orders** - Clinical orders with patient data snapshot
- **study** - Diagnostic study execution (1:1 with Order)
- **order_result** - Versioned, immutable signed reports

### Migration Files
Schema is managed with Flyway migrations:
- `src/main/resources/db/migration/V1__create_tables.sql`
- `src/main/resources/db/migration/V2__add_indexes.sql`

### Viewing the Schema
Run the application and access H2 Console:
- URL: http://localhost:5000/h2-console
- JDBC URL: `jdbc:h2:mem:clinicaldb`
- Username: `sa`
- Password: (blank)

### Key Workflows

1. **Order Creation** (`POST /api/orders`): Creates/updates patient, creates order + study atomically
2. **Study Signing** (`PATCH /api/studies/{id}`): Transitions study to FINALIZED, creates first OrderResult
3. **Amendment** (`PATCH /api/studies/{id}`): Creates new OrderResult version, supersedes previous
4. **Cancellation** (`PATCH /api/studies/{id}`): Only allowed for ORDERED studies

### Optimistic Locking
Studies use JPA `@Version` for optimistic locking. Every PATCH request must include the current `version` number. If the version doesn't match (another user modified it), the API returns `409 Conflict`.

### OrderResult Immutability
Once created, OrderResults cannot be modified or deleted. Amendments create new versions with a `supersededById` chain linking back to previous versions.

## API Endpoints

### Patients
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/patients` | Create patient |
| GET | `/api/patients/{id}` | Get patient by ID |
| GET | `/api/patients?mrn={mrn}` | Find patient by MRN |
| GET | `/api/patients` | List all patients |
| PUT | `/api/patients/{id}` | Update patient |

### Orders
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/orders` | Create order (auto-creates patient + study) |
| GET | `/api/orders/{id}` | Get order with nested study |
| GET | `/api/orders` | List orders (filter: `patientId`, `type`) |

### Studies
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/studies/{id}` | Get study |
| PATCH | `/api/studies/{id}` | Update study (report, status) |
| DELETE | `/api/studies/{id}` | Delete study (ORDERED/CANCELED only) |
| GET | `/api/orders/{orderId}/study` | Get study for order |

### Results (Read-only)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/results/{id}` | Get result by ID |
| GET | `/api/orders/{orderId}/results` | Get current result |
| GET | `/api/orders/{orderId}/results/history` | Get all versions |

### Reports
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/reports/study-status-summary` | Studies count by status |
| GET | `/api/reports/orders-by-type` | Orders count by type |

## Design Decisions & Trade-offs

1. **H2 In-Memory Database**: Data resets on restart. Suitable for demo/development. For production, swap to PostgreSQL via configuration.
2. **Manual Version Check**: The optimistic lock version is checked manually in the service layer (comparing request version with DB version) before JPA's `@Version` kicks in. This provides clearer error messages.
3. **Patient Snapshot on Order**: Order stores a copy of patient data at order time, preserving historical accuracy even if patient info is updated later.
4. **No Authentication**: This is a demo application. Production would need OAuth2/JWT.
5. **Atomic Transactions**: All study state transitions (finalize, amend) are wrapped in `@Transactional` to ensure data consistency.

## What I'd Improve Next

- Add comprehensive input validation with field-level error details
- Implement pagination for all list endpoints
- Add audit logging for all state transitions
- Add authentication and authorization (role-based: physician, radiologist, admin)
- Switch to PostgreSQL for production persistence
- Add WebSocket support for real-time study status updates
- Add comprehensive unit tests alongside integration tests

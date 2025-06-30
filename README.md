# Banking as a Service (BaaS) Platform - Product Requirements Document

## Overview

A minimalist Banking as a Service (BaaS) platform demonstrating microservices architecture capabilities using Spring Boot. The focus is on core banking operations with emphasis on service communication patterns and distributed transaction management.

## Key Requirements

- **Architecture**: Microservices using Spring Boot and Maven
- **Database**: MySQL shared database with service-specific schemas (Database per Microservice pattern)
- **Authentication**: Keycloak with JWT tokens
- **Communication**: REST APIs + Apache Kafka (event-driven)
- **Transaction Management**: Saga Choreography Pattern
- **Core Services**: API Gateway, Service Discovery, User, Account, Transaction, Payment, Notification, Audit

## Technical Architecture

- **Framework**: Spring Boot with Java 21
- **Build Tool**: Maven (Multi-module project)
- **Database**: MySQL 8.0 - Shared database with service-specific schemas maintaining logical separation
- **Authentication**: Keycloak (OAuth 2.0, OpenID Connect, JWT)
- **Service Communication**: REST APIs + Apache Kafka for event-driven communication
- **Pattern**: Saga Choreography Pattern for distributed transactions

### Core Microservices

1. **API Gateway** - Single entry point, routing, authentication integration
2. **Service Discovery** - Service registration and health checks
3. **User Service** - User registration/management, authentication integration
4. **Account Service** - Account creation/management, balance inquiry
5. **Transaction Service** - Money transfer, transaction history
6. **Payment Service** - Internal account-to-account transfers, payment status tracking
7. **Notification Service** - Email notifications for transactions
8. **Audit Service** - Transaction logging, audit trail
9. **Common Library** - Shared DTOs and utilities

## Security & Authentication

- **Keycloak Integration**: OAuth 2.0, OpenID Connect, JWT-based access tokens
- **Authorization**: Role-based access control (RBAC) with defined roles (BAAS_ADMIN, ACCOUNT_HOLDER)
- **API Security**: Bearer token authentication, token validation at API Gateway, method-level security enabled
- **Password Policy**: Minimum length 8, uppercase, number, special character
- **JWT Authentication**: Custom JwtAuthenticationConverter for authority mapping
- **Centralized Security Utilities**: Common SecurityUtil class for JWT role extraction and user information
- **Service-Specific Access Controls**:
  - **Audit Service**: All audit logs restricted to BAAS_ADMIN role only
  - **Account Service**: Administrative functions restricted to BAAS_ADMIN, account-specific operations available to
    both BAAS_ADMIN and ACCOUNT_HOLDER roles
  - **User Service**: User management restricted to BAAS_ADMIN, self-service operations available to authenticated users
  - **Payment Service**: Payment creation restricted to ACCOUNT_HOLDER role only; separate endpoints for admin (all
    payments) and users (my-payments)
  - **Notification Service**: All notification endpoints restricted to BAAS_ADMIN role only

## Saga Choreography Pattern

- Distributed transactions managed using the Saga Choreography pattern
- Each microservice listens to relevant Kafka topics and reacts to events independently
- No central orchestrator; services coordinate by publishing and consuming events
- Compensation logic handled by services when needed

## Saga Flows

### 1. User Onboarding Saga ✅ COMPLETED

**Services**: user-service, account-service, notification-service, audit-service

**Flow Steps:**

1. **Register User** - REST endpoint `POST /users` receives registration details and creates user
2. **Publish UserRegisteredEvent** - user-service publishes event to Kafka with user roles information
3. **Open Account** - account-service subscribes to UserRegisteredEvent, creates default account for regular users
   only (skips BAAS_ADMIN users), publishes AccountOpenedEvent/AccountOpenFailedEvent
4. **Notify User** - notification-service subscribes to AccountOpenedEvent and sends welcome notification
5. **Audit Logging** - audit-service subscribes to all events and logs each step

**Error Handling**: AccountOpenFailedEvent triggers compensating actions including user deletion

**Role-Based Logic**: Account creation is skipped for users with BAAS_ADMIN role

**Event Flow Summary:**

| Step          | Publisher            | Event Name                                  | Subscriber(s)                       |
|---------------|----------------------|---------------------------------------------|-------------------------------------|
| Register User | user-service         | UserRegisteredEvent                         | account-service                     |
| Open Account  | account-service      | AccountOpenedEvent / AccountOpenFailedEvent | notification-service, audit-service |
| Notify User   | notification-service | (notification sent)                         | -                                   |
| Audit Logging | all services         | All above events                            | audit-service                       |

### 2. Payment Processing Saga ✅ COMPLETED

**Services**: payment-service, account-service, transaction-service, notification-service, audit-service

**Flow Steps:**

1. **Initiate Payment** - REST endpoint `POST /payments` receives payment details and creates payment
2. **Publish PaymentInitiatedEvent** - payment-service publishes event to Kafka
3. **Validate Account** - account-service validates source account, publishes PaymentValidatedEvent/PaymentFailedEvent
4. **Update Account Balance** - account-service updates balances, publishes AccountBalanceUpdatedEvent
5. **Process Payment** - payment-service marks payment as processed, publishes PaymentProcessedEvent
6. **Record Transaction** - transaction-service records DEBIT/CREDIT transactions, publishes TransactionRecordedEvent
7. **Notify User** - notification-service sends payment notification
8. **Audit Logging** - audit-service logs all events

**Error Handling**: PaymentFailedEvent triggers compensation with detailed error messages

**Event Flow Summary:**

| Step                   | Publisher            | Event Name                                 | Subscriber(s)                         |
|------------------------|----------------------|--------------------------------------------|---------------------------------------|
| Initiate Payment       | payment-service      | PaymentInitiatedEvent                      | account-service                       |
| Validate Account       | account-service      | PaymentValidatedEvent / PaymentFailedEvent | payment-service                       |
| Update Account Balance | account-service      | AccountBalanceUpdatedEvent                 | payment-service, notification-service |
| Process Payment        | payment-service      | PaymentProcessedEvent                      | transaction-service, audit-service    |
| Record Transaction     | transaction-service  | TransactionRecordedEvent                   | audit-service                         |
| Notify User            | notification-service | (notification sent)                        | -                                     |
| Audit Logging          | all services         | All above events                           | audit-service                         |

### 3. Account Closure Saga ❌ TO BE IMPLEMENTED

**Services**: account-service, transaction-service, notification-service, audit-service

**Flow Steps:**

1. **Request Account Closure** - REST endpoint `POST /accounts/{id}/close` receives closure request
2. **Publish AccountClosureRequestedEvent** - account-service publishes event to Kafka
3. **Check Pending Transactions** - transaction-service validates no pending transactions, publishes
   AccountClosureValidatedEvent/AccountClosureBlockedEvent
4. **Close Account** - account-service closes the account, publishes AccountClosedEvent
5. **Notify User** - notification-service sends closure confirmation
6. **Audit Logging** - audit-service logs all events

**Error Handling**: AccountClosureBlockedEvent prevents closure and notifies user

**Event Flow Summary:**

| Step                       | Publisher            | Event Name                                                | Subscriber(s)        |
|----------------------------|----------------------|-----------------------------------------------------------|----------------------|
| Request Account Closure    | account-service      | AccountClosureRequestedEvent                              | transaction-service  |
| Check Pending Transactions | transaction-service  | AccountClosureValidatedEvent / AccountClosureBlockedEvent | account-service      |
| Close Account              | account-service      | AccountClosedEvent                                        | notification-service |
| Notify User                | notification-service | (notification sent)                                       | -                    |
| Audit Logging              | all services         | All above events                                          | audit-service        |

### 4. Transaction Dispute Saga ❌ TO BE IMPLEMENTED

**Services**: transaction-service, account-service, notification-service, audit-service

**Flow Steps:**

1. **Raise Dispute** - REST endpoint `POST /transactions/{id}/dispute` receives dispute request
2. **Publish TransactionDisputeRaisedEvent** - transaction-service publishes event to Kafka
3. **Freeze Transaction/Account** - account-service freezes relevant account/transaction, publishes
   TransactionFrozenEvent
4. **Investigate Dispute** - transaction-service investigates dispute, publishes DisputeResolvedEvent
5. **Notify User** - notification-service sends dispute resolution notification
6. **Audit Logging** - audit-service logs all events

**Error Handling**: DisputeRejectedEvent handles failed investigations

**Event Flow Summary:**

| Step                       | Publisher            | Event Name                    | Subscriber(s)        |
|----------------------------|----------------------|-------------------------------|----------------------|
| Raise Dispute              | transaction-service  | TransactionDisputeRaisedEvent | account-service      |
| Freeze Transaction/Account | account-service      | TransactionFrozenEvent        | transaction-service  |
| Investigate Dispute        | transaction-service  | DisputeResolvedEvent          | notification-service |
| Notify User                | notification-service | (notification sent)           | -                    |
| Audit Logging              | all services         | All above events              | audit-service        |

## Recent Changes (June 2025)

### Database Migration: H2 to MySQL (June 18, 2025)
- **Complete migration** from H2 in-memory databases to MySQL 8.0 for all microservices
- **Shared MySQL database** (`baas_db`) with logical service separation through table naming and schema organization
- **Docker Compose updates**: Added MySQL service with ephemeral storage (data reset on container restart)
- **Maven dependency management**: Centralized MySQL connector version (8.0.33) in parent POM
- **Configuration updates**: All services now use MySQL connection settings with proper dialect configuration
- **Java 21 compatibility**: Enhanced startup script with automatic Java 21 detection and installation
- **Service startup sequencing**: MySQL starts before all other services with proper health checks
- **Environment standardization**: Consistent database credentials and connection parameters across all services

### Notification Service Enhancements
- **User identification**: Changed from `userId` to `userName` for better user tracking in notifications
- **Timestamp tracking**: Added automatic timestamp field to all notification records
- **Event correlation**: Enhanced payment failure notifications to include proper user context

### Transaction Recording and Account Balance
- The `transaction-service` now records the current account balance for each transaction using a new `balance` field in the `Transaction` entity.
- The `AccountBalanceUpdatedEvent` event now includes `sourceAccountBalance` and `destinationAccountBalance` fields, which are set by the `account-service` after updating balances.
- The `transaction-service` listens for `AccountBalanceUpdatedEvent` and records both debit and credit transactions, including the updated balance for each account.
- The old `PaymentProcessedEventListener` has been removed. All transaction recording is now event-driven and triggered only after account balances are updated.

### Event and Data Model Updates
- `Transaction` entity: Added `balance` field to store the account's balance after each transaction.
- `AccountBalanceUpdatedEvent`: Now includes `sourceAccountBalance` and `destinationAccountBalance`.
- `Notification` entity: Changed `userId` to `userName` and added `timestamp` field.

### Configuration
- Spring Cloud Stream bindings and function definitions updated to use only `AccountBalanceUpdatedEvent` for transaction recording.
- All microservices configured to use MySQL with connection pooling and optimized JPA settings
- Maven parent POM manages MySQL connector version centrally (8.0.33)

### Environment Setup
- **Database**: MySQL 8.0 accessible at `localhost:3306/baas_db`
- **Credentials**: `baas_user/baas_password` (configurable via environment variables)
- **Java Runtime**: Auto-detection and setup of Java 21 with architecture-specific installation
- **Container Orchestration**: Docker Compose with proper service dependencies and health checks

### Saga Orchestrator Service
-   **State Management:**
    -   Added `SagaInstance` and `SagaStepInstance` entities to track the state of saga workflows.
    -   Configured a MySQL database connection for persistence.

## Implementation Status

*Last Updated: June 18, 2025*

### ✅ Completed Features

#### Infrastructure & Core Services

- ✅ Maven multi-module project structure
- ✅ Spring Boot microservices (9 services)
- ✅ MySQL database migration complete (Database per Microservice pattern with shared MySQL)
- ✅ Kafka event-driven communication with Spring Cloud Stream
- ✅ Saga Choreography Pattern implementation
- ✅ Common-lib with shared events and utilities
- ✅ Docker Compose configuration with MySQL service
- ✅ Keycloak authentication setup
- ✅ Service Discovery (Eureka)
- ✅ API Gateway routing
- ✅ Correlation ID tracking across services
- ✅ Comprehensive audit logging
- ✅ Java 21 environment setup and detection

#### User Service Endpoints

- ✅ User registration and management
- ✅ getCurrentUser endpoint for retrieving authenticated user information
- ✅ Role-based access control using BAAS_ADMIN and ACCOUNT_HOLDER roles
- ❌ No deleteUser endpoint (removed for security reasons)

#### Saga Implementations

- ✅ **User Onboarding Saga** (100% Complete)
  - Flow: User registration → Account creation → Welcome notification → Audit logging
  - Compensation: User deletion on account creation failure
  - Events: UserRegisteredEvent, AccountOpenedEvent, AccountOpenFailedEvent

- ✅ **Payment Processing Saga** (100% Complete)
  - Flow: Payment initiation → Account validation → Balance updates → Payment processing → Transaction recording → User
    notification → Audit logging
  - Compensation: Payment failure handling with detailed error messages
  - Events: PaymentInitiatedEvent, PaymentValidatedEvent, PaymentFailedEvent, AccountBalanceUpdatedEvent,
    PaymentProcessedEvent, TransactionRecordedEvent

### 🔄 Pending Implementation

#### Saga Flows

- ❌ **Account Closure Saga** (Not Implemented)
  - Estimated Complexity: Medium
  - Required Events: AccountClosureRequestedEvent, AccountClosureValidatedEvent, AccountClosureBlockedEvent,
    AccountClosedEvent

- ❌ **Transaction Dispute Saga** (Not Implemented)
  - Estimated Complexity: Medium
  - Required Events: TransactionDisputeRaisedEvent, TransactionFrozenEvent, DisputeResolvedEvent

#### Technical Improvements

- ❌ Explicit authorization annotations (@PreAuthorize) in REST endpoints
- ❌ Enhanced error handling and retry mechanisms
- ❌ Comprehensive integration testing
- ❌ API documentation (OpenAPI/Swagger)
- ❌ Monitoring and observability (metrics, traces)
- ❌ Circuit breaker patterns for resilience

### 📊 Progress Summary

**Overall Progress**: 80% of core saga flows implemented

The Banking as a Service platform demonstrates a solid microservices architecture with User Onboarding and Payment
Processing sagas fully functional with proper event choreography, compensation logic, and audit trails.

**Next Priority**: Implement Account Closure and Transaction Dispute sagas to complete core banking operations coverage.

## Development Guidelines

### Technical Standards

- **Project Structure**: Maven multi-module architecture
- **Java Version**: Java 21 with modern syntax and features
- **Code Organization**: All common constants and utilities in `common-lib`
- **Naming Conventions**: Standard Java conventions throughout
- **Event Construction**: Use builder pattern for saga events
- **Database Strategy**: Shared MySQL database with service-specific table organization
- **Dependency Management**: Centralized version management in parent POM for consistency

### API & Communication

- **REST APIs**: RESTful endpoints with basic Swagger documentation
- **Event-Driven**: Kafka for asynchronous service communication
- **Messaging**: Simple event publishing and listening patterns

## Implementation Roadmap

### Phase 1: Core Banking Operations (Current)

- [x] User registration and authentication
- [x] Account creation and management
- [x] Payment processing and fund transfers
- [x] Transaction recording and history
- [x] Email notifications for key events

### Phase 2: Advanced Features (Next)

- [ ] Account closure workflows
- [ ] Transaction dispute handling
- [ ] Enhanced security and authorization
- [ ] Comprehensive error handling and retry mechanisms

### Phase 3: Production Readiness (Future)

- [ ] Circuit breaker patterns for resilience
- [ ] Comprehensive integration testing
- [ ] Monitoring and observability
- [ ] API documentation (OpenAPI/Swagger)
- [ ] Performance optimization

## Testing & Quality Assurance

### Testing Strategy

- [ ] Unit tests for business logic
- [ ] Integration tests for saga flows
- [ ] Contract testing between services
- [ ] End-to-end testing of complete workflows

### Quality Gates

- [ ] Code coverage thresholds
- [ ] Static code analysis
- [ ] Security scanning
- [ ] Performance benchmarks

## Deployment & Operations

### Local Development

- [x] Docker Compose for local environment
- [x] MySQL database for persistent development data
- [x] Kafka and Keycloak containerized
- [x] Java 21 auto-detection and environment setup
- [x] Service startup sequencing with dependency management

### Production Considerations

- [x] MySQL database integration (ready for production MySQL clusters)
- [ ] Container orchestration (Kubernetes)
- [ ] Database clustering and high availability
- [ ] Kafka cluster configuration
- [ ] Load balancing and scaling
- [ ] Monitoring and alerting
- [ ] Backup and disaster recovery

## Monitoring & Observability

### Health & Status

- [ ] Service health checks
- [ ] Application metrics and KPIs
- [ ] Distributed tracing
- [ ] Centralized logging

### Business Metrics

- [ ] Transaction success rates
- [ ] Payment processing times
- [ ] User onboarding completion rates
- [ ] System availability and uptime

## Changelog

## [2025-06-30] Cleanup
- Removed obsolete `UserRegisteredEventListener.java` from `account-service` as part of full migration to orchestrated user onboarding saga.

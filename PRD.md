# Banking as a Service (BaaS) Platform - Product Requirements Document

## Overview
A minimalist Banking as a Service (BaaS) platform demonstrating microservices architecture capabilities using Spring Boot. The focus is on core banking operations with emphasis on service communication patterns and distributed transaction management.

## Key Requirements

- Microservices architecture using Spring Boot, Maven
- Each service has its own H2 database
- Authentication via Keycloak (JWT)
- Service communication: REST + Apache Kafka (event-driven)
- Saga Choreography Pattern for distributed transactions
- Core services: API Gateway, Service Discovery, User, Account, Transaction, Payment, Notification, Audit

## Technical Architecture
- **Framework**: Spring Boot
- **Build Tool**: Maven (Multi-module project)
- **Database**: H2 (Development/Testing) - Each microservice has its own database (Database per Microservice pattern)
- **Authentication**: Keycloak
- **Service Communication**: REST APIs, Apache Kafka for event-driven communication
- **Pattern**: Saga Choreography Pattern

### Core Microservices

1. **API Gateway**: Single entry point, routing, authentication integration
2. **Service Discovery**: Service registration, health checks
3. **User Service**: User registration/management, authentication integration
4. **Account Service**: Account creation/management, balance inquiry
5. **Transaction Service**: Money transfer, transaction history
6. **Payment Service**: Internal account-to-account transfers, payment status tracking
7. **Notification Service**: Email notifications for transactions
8. **Audit Service**: Transaction logging, audit trail
9. **Common Library**: Shared DTOs and utilities

## Security & Authentication

- **Keycloak Integration**: OAuth 2.0, OpenID Connect, JWT-based access tokens
- **Authorization**: Role-based access control (RBAC), scope-based authorization
- **API Security**: Bearer token authentication, token validation at API Gateway
- **Password Policy**: Minimum length 8, uppercase, number, special character

## Saga Choreography Pattern

- Distributed transactions managed using the Saga Choreography pattern
- Each microservice listens to relevant Kafka topics and reacts to events independently
- No central orchestrator; services coordinate by publishing and consuming events
- Compensation logic handled by services if needed

## Saga Flows

### 1. User Onboarding Saga (Implemented)

- **Services**: user-service, account-service, notification-service, audit-service
- **Flow Plan:**
    1. **Register User**: REST endpoint in user-service (POST /users) receives user registration details and creates a
       user.
    2. **Publish UserRegisteredEvent**: user-service publishes **UserRegisteredEvent** to Kafka.
    3. **Open Account**: account-service subscribes to **UserRegisteredEvent**, creates a default account for the user,
       and publishes **AccountOpenedEvent** or **AccountOpenFailedEvent**.
    4. **Notify User**: notification-service subscribes to **AccountOpenedEvent** and sends a notification to the user.
    5. **Audit Logging**: audit-service subscribes to all relevant events (e.g., **UserRegisteredEvent**, *
       *AccountOpenedEvent**, **AccountOpenFailedEvent**) and logs each step for audit purposes.
- **Error Handling**: If account creation fails, **AccountOpenFailedEvent** is published. Downstream services listen for
  failure events to perform compensating actions or notify users.
- **Summary Table:**

  | Step          | Publisher            | Event Name                                                      | Subscriber(s)                       |
    |---------------|----------------------|-----------------------------------------------------------------|-------------------------------------|
  | Register User | user-service         | UserRegisteredEvent                                             | account-service                     |
  | Open Account  | account-service      | AccountOpenedEvent / AccountOpenFailedEvent                     | notification-service, audit-service |
  | Notify User   | notification-service | (notification sent)                                             | -                                   |
  | Audit Logging | all services         | UserRegisteredEvent, AccountOpenedEvent, AccountOpenFailedEvent | audit-service                       |

### 2. Payment Processing Saga (To be implemented)

- Services: payment-service, account-service, transaction-service, notification-service, audit-service
- Flow Plan:
    1. **Initiate Payment**: REST endpoint in payment-service (POST /payments) receives payment details and creates a
       payment.
    2. **Publish PaymentInitiatedEvent**: payment-service publishes **PaymentInitiatedEvent** to Kafka.
    3. **Validate Account**: account-service subscribes to **PaymentInitiatedEvent**, validates the source account, and
       publishes **PaymentValidatedEvent** or **PaymentFailedEvent**.
    4. **Process Payment**: payment-service subscribes to **PaymentValidatedEvent**, processes the payment, and
       publishes **PaymentProcessedEvent**.
    5. **Update Account Balance**: account-service subscribes to **PaymentProcessedEvent**, updates the account balance,
       and publishes **AccountBalanceUpdatedEvent**.
    6. **Record Transaction**: transaction-service subscribes to **PaymentProcessedEvent** and records the transaction
       in its database.
    7. **Notify User**: notification-service subscribes to **AccountBalanceUpdatedEvent** and sends a notification to
       the user.
    8. **Audit Logging**: audit-service subscribes to all relevant events (e.g., **PaymentInitiatedEvent**, *
       *PaymentValidatedEvent**, **PaymentProcessedEvent**, **AccountBalanceUpdatedEvent**, **PaymentFailedEvent**) and
       logs each step for audit purposes.
- Error Handling: If any step fails, a corresponding failure event (e.g., **PaymentFailedEvent**) is published.
  Downstream services listen for failure events to perform compensating actions or notify users.
- Summary Table:

  | Step                   | Publisher           | Event Name                                                                                                          | Subscriber(s)                        |
    |------------------------|---------------------|---------------------------------------------------------------------------------------------------------------------|--------------------------------------|
  | Initiate Payment       | payment-service     | PaymentInitiatedEvent                                                                                               | account-service                      |
  | Validate Account       | account-service     | PaymentValidatedEvent / PaymentFailedEvent                                                                          | payment-service                      |
  | Process Payment        | payment-service     | PaymentProcessedEvent                                                                                               | account-service, transaction-service |
  | Update Account Balance | account-service     | AccountBalanceUpdatedEvent                                                                                          | notification-service                 |
  | Record Transaction     | transaction-service | (transaction recorded)                                                                                              | -                                    |
  | Notify User            | notification-svc    | (notification sent)                                                                                                 | -                                    |
  | Audit Logging          | all services        | PaymentInitiatedEvent, PaymentValidatedEvent, PaymentProcessedEvent, AccountBalanceUpdatedEvent, PaymentFailedEvent | audit-service                        |

### 3. Account Closure Saga (To be implemented)

- Services: account-service, transaction-service, notification-service, audit-service
- Flow Plan:
    1. **Request Account Closure**: REST endpoint in account-service (POST /accounts/{id}/close) receives closure
       request and starts the saga.
    2. **Publish AccountClosureRequestedEvent**: account-service publishes AccountClosureRequestedEvent to Kafka.
    3. **Check Pending Transactions**: transaction-service subscribes to AccountClosureRequestedEvent, checks for
       pending transactions, and publishes AccountClosureValidatedEvent or AccountClosureBlockedEvent.
    4. **Close Account**: account-service subscribes to AccountClosureValidatedEvent, closes the account, and publishes
       AccountClosedEvent.
    5. **Notify User**: notification-service subscribes to AccountClosedEvent and sends a notification to the user.
    6. **Audit Logging**: audit-service subscribes to all relevant events and logs each step for audit purposes.
- Error Handling: If any step fails, a corresponding failure event (e.g., AccountClosureBlockedEvent) is published.
  Downstream services listen for failure events to perform compensating actions or notify users.
- Summary Table:

  | Step                       | Publisher           | Subscriber(s)        |
    |----------------------------|---------------------|----------------------|
  | Request Account Closure    | account-service     | transaction-service  |
  | Check Pending Transactions | transaction-service | account-service      |
  | Close Account              | account-service     | notification-service |
  | Notify User                | notification-svc    | -                    |
  | Audit Logging              | all services        | audit-service        |

### 4. Transaction Dispute Saga (To be implemented)

- Services: transaction-service, account-service, notification-service, audit-service
- Flow Plan:
    1. **Raise Dispute**: REST endpoint in transaction-service (POST /transactions/{id}/dispute) receives dispute
       request and starts the saga.
    2. **Publish TransactionDisputeRaisedEvent**: transaction-service publishes TransactionDisputeRaisedEvent to Kafka.
    3. **Freeze Transaction/Account**: account-service subscribes to TransactionDisputeRaisedEvent, freezes the relevant
       account/transaction, and publishes TransactionFrozenEvent.
    4. **Investigate Dispute**: transaction-service subscribes to TransactionFrozenEvent, investigates the dispute, and
       publishes DisputeResolvedEvent.
    5. **Notify User**: notification-service subscribes to DisputeResolvedEvent and sends a notification to the user.
    6. **Audit Logging**: audit-service subscribes to all relevant events and logs each step for audit purposes.
- Error Handling: If any step fails, a corresponding failure event (e.g., DisputeRejectedEvent) is published. Downstream
  services listen for failure events to perform compensating actions or notify users.
- Summary Table:

  | Step                       | Publisher           | Subscriber(s)        |
    |----------------------------|---------------------|----------------------|
  | Raise Dispute              | transaction-service | account-service      |
  | Freeze Transaction/Account | account-service     | transaction-service  |
  | Investigate Dispute        | transaction-service | notification-service |
  | Notify User                | notification-svc    | -                    |
  | Audit Logging              | all services        | audit-service        |

## Gaps & Recommendations

- Kafka event-driven logic is configured but not always explicit in code (likely via Spring Cloud Stream)
- Saga pattern is partially implemented; orchestration logic may need expansion
- Keycloak is configured, but explicit authorization annotations (e.g., `@PreAuthorize`) are missing in code
- User onboarding saga is implemented, but additional saga flows (e.g., payment processing, account closure, transaction
  dispute) should be added for full business coverage

## Next Steps

- Ensure all event-driven logic is implemented in code
- Add explicit authorization checks where needed
- Expand Saga orchestration if required for complex transactions
- Implement additional saga flows for core banking operations as listed above
- Use this summary for future Copilot analysis to avoid repeating the full review

## Development Guidelines
- Maven multi-module structure
- Java 21 syntax and features
- All common constants in `common-lib`
- Standard Java naming conventions
- Encapsulate reusable logic in `common-lib` when appropriate
- Use builders to construct saga events
- Each service uses its own H2 database
- RESTful endpoints, basic Swagger documentation
- Kafka for service communication, simple event publishing/listening

## Testing Strategy
- [ ] Basic unit tests
- [ ] Simple integration tests

## Deployment
- [ ] Docker containers
- [ ] Docker Compose for local deployment

## Phase 1 Implementation (MVP)
- [ ] User registration and login
- [ ] Account creation
- [ ] Basic fund transfer
- [ ] Simple transaction history
- [ ] Basic email notifications

## Monitoring
- [ ] Basic health checks
- [ ] Simple logging
- [ ] Service status dashboard

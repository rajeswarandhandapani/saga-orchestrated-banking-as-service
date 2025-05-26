# Banking as a Service (BaaS) Platform - Product Requirements Document

## Overview
A minimalist Banking as a Service (BaaS) platform demonstrating microservices architecture capabilities using Spring Boot. The focus is on core banking operations with emphasis on service communication patterns and distributed transaction management.

## Technical Architecture

### Technology Stack
- **Framework**: Spring Boot
- **Build Tool**: Maven (Multi-module project)
- **Database**: H2 (Development/Testing) - Each microservice has its own database (Database per Microservice pattern)
- **Authentication**: Keycloak
- **Service Communication**: 
  - REST APIs
  - Apache Kafka for event-driven communication
- **Pattern**: Saga Choreography Pattern

### Core Microservices

1. **API Gateway**
   - Single entry point for all requests
   - Basic routing
   - Authentication integration

2. **Service Discovery**
   - Service registration
   - Basic health checks

3. **User Service**
   - User registration and management
   - Authentication integration

4. **Account Service**
   - Account creation and management
   - Balance inquiry

5. **Transaction Service**
   - Money transfer between accounts
   - Transaction history

6. **Payment Service**
   - Internal account-to-account transfers
   - Payment status tracking

7. **Notification Service**
   - Email notifications for transactions

8. **Audit Service**
   - Transaction logging
   - Audit trail

9. **Common Library**
    - Shared DTOs and utilities

## Saga Choreography Pattern

- Distributed transactions are managed using the Saga Choreography pattern.
- Each microservice listens to relevant Kafka topics and reacts to events independently.
- There is no central orchestrator; services coordinate by publishing and consuming events.
- Compensation logic is handled by the services themselves if needed.

## Core Features

### User Management
- Basic user registration
- View profile

### Account Management
- Single account per user
- View balance
- Basic account details

### Transaction Processing
- Account-to-account transfers
- View transaction history

## Security & Authentication
- **Keycloak Integration**
  - OAuth 2.0 and OpenID Connect protocols
  - Authorization Code Flow for web applications
  - Client Credentials Flow for service-to-service communication
  - Refresh Token support

- **User Authentication**
  - OpenID Connect for user authentication
  - JWT-based access tokens
  - Standard claims in JWT (sub, iat, exp, etc.)
  - Token-based session management

- **Authorization**
  - Role-based access control (RBAC)
  - Default roles:
    - ROLE_USER: Basic banking operations
    - ROLE_ADMIN: Administrative functions
  - Scope-based authorization
    - read:account
    - write:account
    - read:transaction
    - write:transaction

- **Keycloak Configuration**
  - Realm: banking-service
  - Clients:
    - web-client (public)
    - api-gateway (confidential)
    - service-clients (confidential)
  - Token Settings:
    - Access Token Lifespan: 15 minutes
    - Refresh Token Lifespan: 24 hours
  - SSL Required: external

- **API Security**
  - Bearer token authentication
  - Token validation at API Gateway
  - Service-to-service authentication using client credentials

- **Password Policy**
  - Minimum length: 8 characters
  - At least 1 uppercase letter
  - At least 1 number
  - At least 1 special character

## Technical Requirements

### Scalability
- Basic horizontal scaling
- Simple load balancing

### Reliability
- Basic circuit breaker
- Simple retry mechanism

### Performance
- Response time < 3 seconds
- Basic concurrent transaction handling

## Development Guidelines

### Code Organization
- Maven multi-module structure
- Basic package organization
- Simple shared library

### Database
- H2 for all services
- Basic JPA entities
- Simple data model

### API Design
- RESTful endpoints
- Basic Swagger documentation

### Message Queue
- Kafka for service communication
- Simple event publishing/listening

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

# API Gateway - Admin Dashboard Endpoint

## Overview

The API Gateway now implements the **API Composition Pattern** with a new admin dashboard endpoint that aggregates data from multiple microservices in a single request.

## Endpoint

```
GET /api/admin-dashboard
Accept: application/json
Authorization: Bearer <JWT_TOKEN>
```

## Implementation Details

### Architecture Pattern: API Composition
- **Pattern**: Aggregator pattern using reactive WebFlux
- **Technology**: Spring WebClient with `@GetExchange` annotations
- **Concurrency**: Parallel calls to all microservices using `Mono.zip()`
- **Error Handling**: Graceful degradation - if one service fails, others continue

### Microservices Called
The endpoint aggregates data from the following services using **Service Discovery**:
1. **Account Service** - `lb://ACCOUNT-SERVICE/api/accounts` - All accounts
2. **Payment Service** - `lb://PAYMENT-SERVICE/api/payments` - All payments  
3. **Transaction Service** - `lb://TRANSACTION-SERVICE/api/transactions` - All transactions
4. **Audit Service** - `lb://AUDIT-SERVICE/api/audit-logs` - All audit logs
5. **Notification Service** - `lb://NOTIFICATION-SERVICE/api/notifications` - All notifications
6. **User Service** - `lb://USER-SERVICE/api/users` - All users

### Response Format
```json
{
  "accounts": [...],
  "payments": [...], 
  "transactions": [...],
  "auditLogs": [...],
  "notifications": [...],
  "users": [...]
}
```

### Key Features
- **Reactive**: Uses Spring WebFlux for non-blocking I/O
- **Fault Tolerant**: If any service fails, returns empty array for that service
- **Parallel Execution**: All service calls made concurrently
- **Security**: Requires authentication (JWT token)
- **Service Discovery**: Uses load balancer URLs (lb://SERVICE-NAME)

### Configuration
The configuration uses Spring Cloud LoadBalancer with Eureka service discovery:
```java
@Bean
@LoadBalanced
public WebClient.Builder loadBalancedWebClientBuilder() {
    return WebClient.builder();
}
```

No additional application.yml configuration needed - it uses the existing Eureka service discovery.

## Usage Example

```bash
# Get JWT token first
TOKEN="your-jwt-token-here"

# Call the admin dashboard endpoint
curl -X GET "http://localhost:8080/api/admin-dashboard" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer $TOKEN"
```

## Code Components

1. **AdminDashboardClient** - HTTP client interface with `@GetExchange` methods
2. **AdminDashboardHandler** - Reactive handler that aggregates responses
3. **AdminDashboardRouter** - Router function configuration
4. **AdminDashboardClientConfig** - WebClient bean configuration
5. **AdminDashboardDto** - Response DTO using Java records

This implementation follows microservices best practices and provides a clean way for admin users to get a comprehensive view of the system state in a single API call.

# Optimized Correlation ID Architecture

## Overview
The correlation ID architecture has been refactored to eliminate redundancy and simplify the flow while maintaining complete traceability across all services.

## Architecture Components

### 1. **API Gateway - Single Point of Entry**
**File**: `/api-gateway/src/main/java/com/rajeswaran/apigateway/filter/CorrelationIdFilter.java`
**Type**: Spring Cloud Gateway `GlobalFilter`
**Responsibility**: System-wide correlation ID management

```java
// Generates correlation ID if missing, preserves existing if present
String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
if (correlationId == null || correlationId.isEmpty()) {
    correlationId = UUID.randomUUID().toString(); // Generate new
    // Add to request headers for downstream services
}
```

### 2. **Message Layer - Automatic Propagation**
**File**: `/common-lib/src/main/java/com/rajeswaran/common/messaging/CorrelationIdChannelInterceptor.java`
**Type**: Spring Cloud Stream `ChannelInterceptor`
**Responsibility**: Message correlation ID handling

```java
// Automatically adds correlation ID to outgoing messages
// Automatically extracts correlation ID from incoming messages
// Sets/clears MDC context for logging
```

### 3. **HTTP Client - Optional Service-to-Service**
**File**: `/common-lib/src/main/java/com/rajeswaran/common/http/CorrelationIdHttpInterceptor.java`
**Type**: HTTP `ClientHttpRequestInterceptor`
**Responsibility**: Service-to-service HTTP correlation ID propagation

```java
// Usage with RestTemplate:
@Bean
public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new CorrelationIdHttpInterceptor());
    return restTemplate;
}
```

## Flow Architecture

### **Primary Flow (Through API Gateway)**
```
External Client 
    ↓ HTTP Request
API Gateway [CorrelationIdFilter] → Generates/Preserves Correlation ID
    ↓ HTTP Request (with X-Correlation-Id header)
Microservice [REST Controller]
    ↓ Message Publishing
Message Layer [CorrelationIdChannelInterceptor] → Propagates Correlation ID
    ↓ Message (with X-Correlation-Id header)
Another Microservice [Message Listener]
    ↓ Message Processing
[CorrelationIdChannelInterceptor] → Extracts to MDC
```

### **Service-to-Service HTTP (If Needed)**
```
Service A [Business Logic]
    ↓ HTTP Call
HTTP Client [CorrelationIdHttpInterceptor] → Adds Correlation ID from MDC
    ↓ HTTP Request (with X-Correlation-Id header)
Service B [REST Controller] → Correlation ID available in MDC
```

## Key Benefits

### ✅ **Eliminated Redundancy**
- **Removed**: Duplicate servlet filters in each service
- **Removed**: Redundant WebConfig classes
- **Kept**: Single API Gateway filter for system entry

### ✅ **Simplified Architecture**
- **Single Source of Truth**: API Gateway generates correlation IDs
- **Automatic Propagation**: Message interceptors handle async communication
- **Optional HTTP Support**: Available for service-to-service calls

### ✅ **Clean Domain Objects**
- Commands and events remain correlation ID-free
- Business logic focuses on domain concerns
- Transport layer handles correlation ID concerns

### ✅ **Consistent Logging**
```
2025-07-07 15:30:01.123 [main] INFO [550e8400-e29b-41d4-a716-446655440000] - API Gateway: Processing user registration
2025-07-07 15:30:01.234 [main] INFO [550e8400-e29b-41d4-a716-446655440000] - Saga Orchestrator: Starting user onboarding saga
2025-07-07 15:30:01.345 [main] INFO [550e8400-e29b-41d4-a716-446655440000] - User Service: Creating user john.doe
2025-07-07 15:30:01.456 [main] INFO [550e8400-e29b-41d4-a716-446655440000] - Account Service: Opening account for user
```

## Configuration Requirements

### **No Additional Configuration Needed**
- API Gateway filter is automatically active
- Message interceptor is auto-configured via `CorrelationIdAutoConfiguration`
- HTTP interceptor is optional and manually configured per service

### **Removed Configurations**
- ❌ Service-level servlet filters
- ❌ FilterRegistrationBean configurations
- ❌ WebConfig classes

## UserOnboardingSaga Correlation ID Flow

```
1. Client → API Gateway
   └─ CorrelationIdFilter generates: 550e8400-e29b-41d4-a716-446655440000

2. API Gateway → Saga Orchestrator (HTTP)
   └─ Headers: X-Correlation-Id: 550e8400-e29b-41d4-a716-446655440000

3. Saga Orchestrator publishes CreateUserCommand
   └─ CorrelationIdChannelInterceptor adds to message headers

4. User Service receives CreateUserCommand
   └─ CorrelationIdChannelInterceptor extracts to MDC

5. User Service publishes UserCreatedEvent
   └─ CorrelationIdChannelInterceptor adds to message headers

6. Saga Orchestrator receives UserCreatedEvent
   └─ CorrelationIdChannelInterceptor extracts to MDC

... continues through entire saga flow
```

## Result
- **Cleaner Architecture**: Single responsibility for each component
- **Better Performance**: No redundant filter processing
- **Easier Maintenance**: One place to manage correlation ID generation
- **Complete Traceability**: End-to-end correlation across all services
- **Transport Agnostic**: Works with HTTP and any messaging system

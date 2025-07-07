# Simple Correlation ID Setup

## What We Have

### 1. API Gateway Filter
- **File**: `api-gateway/.../CorrelationIdFilter.java`
- **Does**: Generates correlation ID if missing, adds to headers, sets in MDC
- **Simple**: No extra logging, just does the job

### 2. Message Interceptor  
- **File**: `common-lib/.../CorrelationIdChannelInterceptor.java`
- **Does**: Automatically handles correlation ID for all messages
- **Works**: With Kafka, RabbitMQ, any Spring Cloud Stream binder

### 3. Simple Logging
- **Pattern**: `%d{HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId:-N/A}] %logger{36} - %msg%n`
- **Shows**: `15:30:01.123 [main] INFO [550e8400-e29b-41d4-a716-446655440000] UserService - Creating user`

## Flow

```
Client → API Gateway (adds correlation ID) → Services (logs with correlation ID)
                ↓
            Messages (correlation ID in headers) → Other Services (logs with correlation ID)
```

## Result
- Every log line shows the correlation ID
- No domain object pollution  
- Automatic propagation
- Simple and clean

That's it!

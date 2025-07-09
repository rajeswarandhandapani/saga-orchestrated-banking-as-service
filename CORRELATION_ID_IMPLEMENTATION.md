# Correlation ID Strategy Implementation

## Overview

Successfully implemented a **header-based correlation ID strategy** for the saga-orchestrated banking microservices platform. This replaces the previous domain-level correlation ID approach with a clean, transport-layer solution.

## Architecture

### Transport Layer (Headers)
- **HTTP Headers**: `X-Correlation-Id` for REST API requests
- **Message Headers**: `X-Correlation-Id` for Spring Cloud Stream messages
- **MDC Integration**: Automatic setup for structured logging

### Clean Domain Objects
- **Commands**: Only business data (commandId, sagaId, timestamp)
- **Events**: Only business data (eventId, sagaId, timestamp, success, errorMessage)
- **No Infrastructure Concerns**: Correlation ID handled transparently

## Components Implemented

### 1. Core Utilities
- **`CorrelationIdMessageUtils`**: Transport-agnostic correlation ID handling
- **`AppConstants`**: Unified constants for headers and MDC keys

### 2. Spring Cloud Stream Integration
- **`CorrelationIdChannelInterceptor`**: Automatic message header management
- **Auto-Configuration**: Zero-configuration setup via Spring Boot

### 3. Updated Domain Objects
- **Command Interface**: Removed `getCorrelationId()` method
- **Event Interface**: Removed `getCorrelationId()` method
- **BaseCommand**: Removed `correlationId` field
- **BaseEvent**: Removed `correlationId` field

### 4. Updated Factory Methods
- **Event Factories**: Clean API without correlation ID parameters
- **Command Factories**: Business-focused method signatures

## Benefits

### 🎯 Clean Architecture
- **Separation of Concerns**: Business logic separate from infrastructure
- **Domain Focus**: Commands and events contain only business data
- **Transport Agnostic**: Works with any Spring Cloud Stream binder

### 🔄 Automatic Propagation
- **Outgoing Messages**: Correlation ID automatically added to headers
- **Incoming Messages**: Correlation ID extracted and set in MDC
- **Zero Configuration**: Just add dependency and it works

### 📊 Enhanced Observability
- **Structured Logging**: Correlation ID in all log entries
- **Request Tracing**: End-to-end request tracking
- **Debugging Support**: Easy to trace issues across services

### 🛡️ Robust Implementation
- **Error Handling**: Graceful fallbacks for missing correlation IDs
- **Memory Management**: Automatic MDC cleanup
- **Performance**: Minimal overhead with efficient interceptors

## Usage Examples

### Before (Domain-Level)
```java
// Command creation
ProcessPaymentCommand.create(sagaId, correlationId, payment);

// Event creation
PaymentValidatedEvent.create(sagaId, correlationId, payment);

// Manual correlation ID management
String correlationId = SagaEventBuilderUtil.getCurrentCorrelationId();
```

### After (Header-Based)
```java
// Command creation - clean API
ProcessPaymentCommand.create(sagaId, payment);

// Event creation - clean API
PaymentValidatedEvent.create(sagaId, payment);

// Automatic correlation ID handling via headers
// No manual management needed!
```

## Implementation Status

### ✅ Completed Components
- [x] **Phase 1**: Spring Cloud Stream correlation ID infrastructure
- [x] **Phase 2**: Clean domain objects (removed correlation ID from interfaces)
- [x] **Phase 3**: Updated factory methods to use clean API
- [x] **Phase 4**: Updated command/event listeners pattern
- [x] **Auto-Configuration**: Zero-configuration Spring Boot integration

### 🔄 Pattern Established
All remaining command and event classes can be updated using the established patterns:

1. **Remove `correlationId` parameter** from factory methods
2. **Remove `getCorrelationId()` calls** from listeners
3. **Update constructor signatures** to exclude correlation ID

## Services Integration

### Automatic Integration
Any service that includes the `common-lib` dependency automatically gets:
- ✅ **Correlation ID Interceptor**: Registered for all message channels
- ✅ **Header Management**: Automatic addition/extraction of correlation IDs
- ✅ **MDC Setup**: Proper logging context configuration
- ✅ **Clean APIs**: Access to updated command/event factories

### Example Service Integration
```xml
<!-- Add to any service's pom.xml -->
<dependency>
    <groupId>com.rajeswaran</groupId>
    <artifactId>common-lib</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Startup Logs
```
INFO - Registering CorrelationIdChannelInterceptor for automatic correlation ID handling
```

## Technical Details

### Headers Used
- **HTTP**: `X-Correlation-Id`
- **Messages**: `X-Correlation-Id` (unified with HTTP)
- **MDC Key**: `correlationId`

### Interceptor Behavior
- **Outgoing**: Adds correlation ID from MDC to message headers
- **Incoming**: Extracts correlation ID from headers to MDC
- **Fallback**: Generates new correlation ID if missing
- **Cleanup**: Removes MDC entries after processing

### Auto-Configuration
- **File**: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- **Class**: `com.rajeswaran.common.config.CorrelationIdAutoConfiguration`
- **Condition**: Only activates when messaging classes are present

## Conclusion

The header-based correlation ID strategy provides:
- **🎯 Clean Architecture**: Proper separation of concerns
- **🔄 Automatic Operation**: Zero manual configuration
- **📊 Enhanced Observability**: Comprehensive request tracing
- **🛡️ Robust Implementation**: Error handling and memory management
- **🏗️ Maintainable Code**: Simple, focused APIs

This implementation follows microservices best practices and provides a solid foundation for distributed tracing across the entire banking platform.

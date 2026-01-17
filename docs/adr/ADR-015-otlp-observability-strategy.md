# ADR-015: OTLP Observability Strategy

## Status
Accepted

## Context

The Investment Tracker requires comprehensive observability to:

1. **Trace Requests**: Follow request flow through all hexagonal architecture layers (NFR-051)
2. **Diagnose Issues**: Quickly identify performance bottlenecks and errors
3. **Monitor Production**: Track application health in real-time
4. **Include Trace IDs**: Return trace IDs in error responses for troubleshooting (ADR-010)
5. **Support Development**: Visualize traces during local development

### Requirements

- **NFR-051**: OTLP (OpenTelemetry Protocol) tracing integrated into application
- **NFR-052**: Trace propagation through all layers (web → application → domain → infrastructure)
- **Local Development**: Easy trace visualization with Grafana + Tempo
- **Production Ready**: Configurable sampling rates and exporters

### Technology Stack

- **Spring Boot Actuator**: Built-in observability support
- **Micrometer Tracing**: Spring's abstraction over OpenTelemetry
- **OpenTelemetry Java Agent**: Automatic instrumentation (optional)
- **OTLP Exporter**: Send traces to Grafana Tempo
- **Grafana Tempo**: Trace storage and query backend
- **Grafana**: Trace visualization UI

---

## Decision

### OTLP Collector: Grafana Tempo

**Decision**: Use Grafana Tempo as the OTLP trace collector for both local development and production.

**Rationale**:
- **Lightweight**: Single binary, minimal resource usage
- **OTLP Native**: Purpose-built for OpenTelemetry Protocol
- **Grafana Integration**: Seamless visualization in Grafana UI
- **Simple Setup**: No complex configuration required
- **Cost**: Free and open-source
- **Scalability**: Can scale from local dev to production

**Alternatives Considered**:
- **Jaeger**: More complex setup, requires multiple containers
- **Zipkin**: Legacy, not OTLP-first
- **Elastic APM**: Overkill for MVP, vendor lock-in concerns

---

### Spring Boot OTLP Configuration

**Decision**: Use Spring Boot Actuator with Micrometer Tracing for automatic OTLP integration.

**Configuration** (`application.yml`):

```yaml
management:
  # Enable tracing
  tracing:
    enabled: true
    sampling:
      probability: 1.0  # 100% sampling in local dev

  # OTLP exporter configuration
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
      compression: gzip
      timeout: 10s

  # Expose trace endpoints
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics

  # Metrics with traces correlation
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true

# Logging with trace ID
logging:
  pattern:
    level: '%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]'
  level:
    root: INFO
    com.investments.tracker: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

**Key Configuration Decisions**:

1. **Sampling Probability**: `1.0` (100%) for local development
   - In production: `0.1` (10%) to reduce overhead
   - Configurable via environment variable

2. **OTLP Endpoint**: HTTP endpoint `http://localhost:4318/v1/traces`
   - Uses HTTP instead of gRPC for simplicity
   - Port 4318 is Tempo's OTLP HTTP receiver

3. **Compression**: gzip enabled to reduce network bandwidth

4. **Timeout**: 10 seconds to prevent blocking on export failures

5. **Trace ID in Logs**: MDC context automatically includes traceId and spanId
   - Format: `[application-name,traceId,spanId]`
   - Allows correlation between logs and traces

---

### Environment-Specific Configuration

**Decision**: Use Spring profiles to configure different observability settings per environment.

#### Local Development Profile (`application-local.yml`)

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces

logging:
  level:
    root: INFO
    com.investments.tracker: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**Rationale**:
- 100% sampling: Capture every trace for debugging
- Verbose logging: See SQL queries and parameters
- Local Tempo: No authentication required

#### Docker Profile (`application-docker.yml`)

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://tempo:4318/v1/traces  # Docker service name

logging:
  level:
    root: INFO
    com.investments.tracker: DEBUG
```

**Rationale**:
- Uses Docker service name `tempo` for endpoint
- Same as local but with container networking

#### Production Profile (`application-prod.yml`)

```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # 10% sampling
  otlp:
    tracing:
      endpoint: ${OTLP_ENDPOINT:http://tempo:4318/v1/traces}
      headers:
        Authorization: ${OTLP_AUTH_TOKEN:}

logging:
  level:
    root: WARN
    com.investments.tracker: INFO
    org.springframework.web: WARN
```

**Rationale**:
- 10% sampling: Reduce overhead in production
- Environment variable for endpoint: Supports cloud providers
- Optional authentication: Header-based auth for production Tempo
- Less verbose logging: Reduce log volume

---

### Tracing Through Hexagonal Layers

**Decision**: Use Spring Boot's automatic instrumentation with custom `@Observed` annotations for domain layer.

#### Automatic Tracing (No Code Changes)

Spring Boot Actuator automatically creates spans for:

1. **HTTP Requests**: Every REST API call
   - Span name: `GET /api/v1/positions`
   - Includes HTTP method, status code, URL

2. **Database Queries**: Every SQL query via JDBC
   - Span name: `SELECT position`
   - Includes SQL statement (sanitized)

3. **Outbound HTTP Calls**: RestTemplate, WebClient
   - Span name: `GET https://external-api.com`

#### Manual Tracing for Domain Layer

**Problem**: Spring Boot doesn't automatically instrument domain services (they're not @Component by default in pure DDD).

**Solution**: Use `@Observed` annotation from Micrometer Observability API.

**Example**:

```java
package com.investments.tracker.domain.service;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "domain.price-calculation")
public class PriceCalculationService {

    /**
     * Calculate position value.
     * This method will be automatically traced with span name "domain.price-calculation.calculateValue"
     */
    @Observed(name = "calculateValue", contextualName = "calculate-position-value")
    public Money calculateValue(Quantity quantity, Money currentPrice) {
        // Domain logic here
        return currentPrice.multiply(quantity.value());
    }
}
```

**Configuration Required**:

```java
@Configuration
public class ObservabilityConfig {

    /**
     * Enable @Observed annotation processing for domain classes.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
```

**When to Use @Observed**:

- **Use**: Domain services with complex calculations or business logic
- **Use**: Critical performance paths that need monitoring
- **Don't Use**: Simple value objects or entities
- **Don't Use**: Private methods (AOP can't intercept them)

---

### Trace Propagation Across Layers

**Decision**: Rely on Spring Boot's automatic trace context propagation.

**How It Works**:

1. **HTTP Request Arrives**: Spring creates root span with trace ID
2. **Controller Layer**: Automatically traced (no code changes)
3. **Application Layer**: Span created if @Service annotated
4. **Domain Layer**: Span created if @Observed annotated
5. **Infrastructure Layer**: Repository/JPA calls automatically traced
6. **HTTP Response**: Root span closed, trace ID included in response headers

**Trace Context Headers**:

Spring Boot automatically propagates:
- `traceparent`: W3C Trace Context standard
- `tracestate`: Optional vendor-specific data

**Example Trace Hierarchy**:

```
Root Span: GET /api/v1/positions
├── Span: PositionController.createPosition()
│   ├── Span: ManualEntryService.addPosition()
│   │   ├── Span: domain.position.Position.create() [@Observed]
│   │   ├── Span: PositionRepository.save()
│   │   │   └── Span: INSERT INTO positions (Hibernate/JDBC)
│   │   └── Span: YahooFinancePriceProvider.getCurrentPrice() [external HTTP call]
│   └── Span: PositionMapper.toDTO()
└── Response: 201 Created (trace ID in header)
```

**Configuration** (already enabled by default):

```yaml
management:
  tracing:
    propagation:
      type: W3C  # Use W3C Trace Context standard (default)
```

---

### Including Trace ID in Error Responses

**Decision**: Extract trace ID from MDC context and include in ErrorResponseDTO (already documented in ADR-010).

**Implementation**:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Get current trace ID from MDC (Mapped Diagnostic Context).
     * Spring Boot Actuator automatically populates MDC with trace ID.
     */
    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : "N/A";
    }

    @ExceptionHandler(PositionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFound(
        PositionNotFoundException ex,
        HttpServletRequest request
    ) {
        log.error("Position not found: {}", ex.getMessage());

        return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .traceId(getTraceId())  // ← Trace ID from MDC
            .build();
    }
}
```

**Benefits**:
- Users can provide trace ID when reporting issues
- Support team can find exact trace in Grafana
- Correlate error response with logs and traces

---

### Spring Boot Dependencies

**Decision**: Use Spring Boot Actuator with Micrometer Tracing (OTLP bridge).

**Required Dependencies** (`build.gradle.kts`):

```kotlin
dependencies {
    // Spring Boot Actuator (includes Micrometer)
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Micrometer Tracing Bridge for OTLP
    implementation("io.micrometer:micrometer-tracing-bridge-otel")

    // OTLP Exporter
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // Optional: Automatic instrumentation for JDBC
    runtimeOnly("io.opentelemetry.instrumentation:opentelemetry-jdbc")
}
```

**Why These Dependencies**:

1. **spring-boot-starter-actuator**: Core observability features
2. **micrometer-tracing-bridge-otel**: Bridges Micrometer API to OpenTelemetry
3. **opentelemetry-exporter-otlp**: Exports traces to Tempo via OTLP protocol
4. **opentelemetry-jdbc**: Automatic SQL query tracing

**Alternative (Java Agent)**:

For automatic instrumentation without code changes, use OpenTelemetry Java Agent:

```bash
java -javaagent:opentelemetry-javaagent.jar \
     -Dotel.service.name=investments-tracker \
     -Dotel.exporter.otlp.endpoint=http://localhost:4318 \
     -jar investments-tracker.jar
```

**Decision**: Don't use Java Agent for MVP
- **Rationale**: Spring Boot Actuator provides sufficient tracing with explicit control
- **Future**: Consider Java Agent if more automatic instrumentation is needed

---

### Tempo Configuration

**Decision**: Use simple Tempo configuration for local development (already documented in ADR-014).

**File**: `infrastructure/observability/tempo.yaml`

```yaml
server:
  http_listen_port: 3200

distributor:
  receivers:
    otlp:
      protocols:
        grpc:
          endpoint: 0.0.0.0:4317
        http:
          endpoint: 0.0.0.0:4318  # ← Spring Boot uses this

storage:
  trace:
    backend: local
    local:
      path: /tmp/tempo/traces
    wal:
      path: /tmp/tempo/wal

query_frontend:
  search:
    max_duration: 0s  # No limit on trace search duration
```

**Key Decisions**:
- **HTTP Receiver**: Port 4318 for OTLP HTTP (simpler than gRPC)
- **Local Storage**: Sufficient for development (no S3/GCS needed)
- **Search Duration**: No limit (allow searching all traces)

---

### Grafana Configuration

**Decision**: Pre-configure Grafana with Tempo datasource (already documented in ADR-014).

**File**: `infrastructure/observability/grafana-datasources.yml`

```yaml
apiVersion: 1

datasources:
  - name: Tempo
    type: tempo
    access: proxy
    url: http://tempo:3200
    isDefault: true
    editable: true
```

**Access Grafana**:
- URL: http://localhost:3000
- Authentication: Disabled for local dev (anonymous admin access)

**How to View Traces**:
1. Open Grafana: http://localhost:3000
2. Navigate to Explore
3. Select Tempo datasource
4. Search by:
   - Trace ID (from error response or logs)
   - Service name: `investments-tracker`
   - HTTP method: `GET /api/v1/positions`
   - Duration: > 100ms
5. View waterfall diagram showing all spans

---

### Sampling Strategy

**Decision**: Environment-based sampling rates.

| Environment | Sampling Rate | Rationale |
|-------------|---------------|-----------|
| Local | 100% (`1.0`) | Capture every trace for debugging |
| Docker | 100% (`1.0`) | Same as local, testing environment |
| Staging | 50% (`0.5`) | Balance observability and overhead |
| Production | 10% (`0.1`) | Reduce overhead while maintaining visibility |

**Configuration**:

```yaml
management:
  tracing:
    sampling:
      probability: ${TRACING_SAMPLING_RATE:1.0}
```

**Environment Variable**:

```bash
# Production deployment
export TRACING_SAMPLING_RATE=0.1
```

**Rationale**:
- 100% sampling in dev: No guessing, every request traced
- 10% in prod: 1 in 10 requests traced (sufficient for most issues)
- Configurable: Can increase temporarily for debugging production issues

---

### Custom Span Attributes

**Decision**: Add custom attributes to spans for richer context.

**Example**:

```java
@Service
@RequiredArgsConstructor
public class ManualEntryService {

    private final ObservationRegistry observationRegistry;
    private final PositionRepository positionRepository;

    public PositionDetailDTO addPosition(AddPositionCommand command) {
        // Create observation with custom attributes
        Observation observation = Observation.createNotStarted("manual-entry.add-position", observationRegistry)
            .lowCardinalityKeyValue("instrument.type", command.instrumentType().toString())
            .highCardinalityKeyValue("instrument.symbol", command.instrumentSymbol())
            .highCardinalityKeyValue("account.id", command.accountId().toString());

        return observation.observe(() -> {
            // Business logic here
            Position position = Position.create(/* ... */);
            positionRepository.save(position);
            return PositionMapper.toDTO(position);
        });
    }
}
```

**Key Decisions**:

1. **Low Cardinality**: Attributes with few possible values (e.g., instrument type: STOCK, ETF)
   - Safe for indexing and filtering
   - Won't explode cardinality

2. **High Cardinality**: Attributes with many possible values (e.g., UUID, symbol)
   - Not indexed, but useful for debugging specific traces
   - Tempo stores but doesn't index

3. **Don't Include**:
   - Sensitive data (passwords, API keys)
   - Large payloads (JSON blobs > 1KB)

**When to Use Custom Attributes**:
- **Use**: Critical business identifiers (account ID, position ID)
- **Use**: Enum values (instrument type, transaction type)
- **Use**: Performance-critical flags (cache hit/miss)
- **Don't Use**: Every method parameter (too verbose)

---

### Logging Integration

**Decision**: Use SLF4J with Logback, automatically populated with trace IDs via MDC.

**Configuration** (`logback-spring.xml`):

```xml
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %5level [${spring.application.name:},%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n
            </pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

**Log Output Example**:

```
2026-01-11 14:23:45.123 [http-nio-8080-exec-1] INFO  [investments-tracker,a1b2c3d4e5f6g7h8,1234567890abcdef] c.i.t.a.s.ManualEntryService - Creating position for AAPL
2026-01-11 14:23:45.234 [http-nio-8080-exec-1] DEBUG [investments-tracker,a1b2c3d4e5f6g7h8,1234567890abcdef] o.h.SQL - insert into positions (id, instrument_name, quantity, average_cost) values (?, ?, ?, ?)
2026-01-11 14:23:45.345 [http-nio-8080-exec-1] INFO  [investments-tracker,a1b2c3d4e5f6g7h8,1234567890abcdef] c.i.t.a.s.ManualEntryService - Position created successfully: id=uuid-here
```

**Benefits**:
- Trace ID visible in every log line
- Can filter logs by trace ID in log aggregation tools
- Correlate logs with traces in Grafana

---

### Performance Considerations

**Decision**: OTLP tracing should have minimal performance impact.

**Expected Overhead**:

| Operation | Overhead | Mitigation |
|-----------|----------|------------|
| Creating spans | < 1ms per span | Automatic, no avoidable cost |
| Exporting traces | Async, batched | Non-blocking, 10s timeout |
| Logging | ~0.5ms per log line | Already accounted for |
| Total per request | < 5ms (0.5% at 1s response time) | Acceptable for MVP |

**Mitigation Strategies**:

1. **Async Export**: Traces sent asynchronously, don't block request threads
2. **Batching**: Spans batched before export (reduces network calls)
3. **Compression**: gzip reduces network bandwidth
4. **Sampling**: 10% in production reduces overhead by 90%
5. **Timeout**: 10s timeout prevents hanging on exporter failures

**Monitoring Tracing Overhead**:

```yaml
management:
  metrics:
    distribution:
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

Compare p95 latency with and without tracing enabled.

---

### Health Checks and Monitoring

**Decision**: Expose health endpoint for Docker Compose health checks.

**Configuration**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true  # Enable liveness and readiness probes
```

**Health Check Endpoint**:

```
GET /actuator/health
```

**Response**:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Docker Compose Health Check** (in docker-compose.yml):

```yaml
app:
  healthcheck:
    test: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 30s
```

---

## Consequences

### Positive

1. **Comprehensive Observability**: Trace every request through all layers
2. **Production Ready**: OTLP is industry standard, supported by all major vendors
3. **Developer Friendly**: Grafana UI makes trace exploration intuitive
4. **Low Overhead**: Async export and batching minimize performance impact
5. **Trace ID in Errors**: Users can report trace ID for troubleshooting
6. **Automatic Instrumentation**: Spring Boot handles 90% of tracing automatically
7. **Flexible Sampling**: Can adjust sampling rate per environment
8. **Log Correlation**: Trace IDs in logs enable log-trace correlation
9. **Hexagonal Architecture**: Manual @Observed allows tracing domain layer

### Negative

1. **Learning Curve**: Developers must understand OTLP concepts (spans, traces, attributes)
2. **Dependency**: Adds runtime dependency on Tempo (container required)
3. **Network Overhead**: Traces sent over network (mitigated by compression and batching)
4. **Sampling Tradeoff**: 10% sampling means 90% of requests aren't traced in production
5. **Manual Domain Tracing**: Domain services need @Observed annotation (not automatic)

### Mitigation Strategies

1. **Learning Curve**: Document common troubleshooting scenarios in README
2. **Dependency**: Tempo container included in Docker Compose (ADR-014)
3. **Network Overhead**: Use async export, compression, and appropriate sampling
4. **Sampling**: Increase temporarily for production debugging
5. **Manual Tracing**: Document @Observed usage in domain service templates

---

## Alternatives Considered

### Alternative 1: Jaeger Instead of Tempo

**Rejected**:
- Jaeger requires multiple containers (collector, query, storage)
- Tempo simpler, single binary
- Grafana + Tempo tighter integration
- Tempo is OTLP-native, Jaeger supports OTLP but not primary focus

### Alternative 2: OpenTelemetry Java Agent

**Rejected for MVP**:
- Automatic instrumentation via bytecode manipulation
- Less explicit control over tracing
- Harder to debug when things go wrong
- Spring Boot Actuator sufficient for MVP
- Can add later if needed

### Alternative 3: No Observability in MVP

**Rejected**:
- Observability is NFR-051 (required non-functional requirement)
- Harder to add later than to include from day one
- Critical for diagnosing issues in production
- Low overhead with Spring Boot Actuator

### Alternative 4: Custom Tracing Implementation

**Rejected**:
- Reinventing the wheel
- OTLP is industry standard
- Spring Boot has excellent built-in support
- No reason to build custom solution

---

## Usage Instructions

### Enable Tracing in Spring Boot Application

**1. Add Dependencies** (build.gradle.kts):

```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-tracing-bridge-otel")
implementation("io.opentelemetry:opentelemetry-exporter-otlp")
```

**2. Configure OTLP** (application-local.yml):

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

**3. Run Application**:

```bash
# Start infrastructure
docker-compose up -d postgres tempo grafana

# Run Spring Boot locally
./gradlew bootRun --args='--spring.profiles.active=local'
```

**4. Make Request**:

```bash
curl http://localhost:8080/api/v1/positions
```

**5. View Traces in Grafana**:
- Open http://localhost:3000
- Navigate to Explore → Tempo
- Search for service: `investments-tracker`
- Click trace to see waterfall diagram

### Add Custom Tracing to Domain Service

```java
import io.micrometer.observation.annotation.Observed;

@Observed(name = "domain.position")
public class Position {

    @Observed(name = "create", contextualName = "create-position")
    public static Position create(
        InstrumentName name,
        InstrumentSymbol symbol,
        InstrumentType type,
        Quantity quantity,
        Money averageCost
    ) {
        // Domain logic
        return new Position(/* ... */);
    }
}
```

### Extract Trace ID in Exception Handler

```java
import org.slf4j.MDC;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getTraceId() {
        return MDC.get("traceId");
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponseDTO handleException(Exception ex, HttpServletRequest request) {
        return ErrorResponseDTO.builder()
            .traceId(getTraceId())  // ← Include in error response
            .message(ex.getMessage())
            .build();
    }
}
```

---

## Implementation Checklist

- [ ] Add Spring Boot Actuator dependencies to build.gradle.kts
- [ ] Add Micrometer Tracing and OTLP exporter dependencies
- [ ] Create application-local.yml with OTLP configuration
- [ ] Create application-docker.yml with Tempo service name
- [ ] Create application-prod.yml with 10% sampling
- [ ] Create ObservabilityConfig.java with ObservedAspect bean
- [ ] Update GlobalExceptionHandler to include trace ID
- [ ] Add @Observed annotations to domain services (when implemented)
- [ ] Test tracing locally with Grafana
- [ ] Document trace viewing instructions in README.md

---

## Related Decisions

- [ADR-010: Error Handling Strategy](ADR-010-error-handling-strategy.md) - Trace ID in error responses
- [ADR-014: Docker Compose Configuration](ADR-014-docker-compose-configuration.md) - Tempo and Grafana setup
- [ADR-016: Database Migration Strategy](ADR-016-database-migration-strategy.md) - Flyway migrations (future)
- [ADR-017: Transaction Boundaries](ADR-017-transaction-boundaries.md) - Transaction management (future)

---

## References

- Spring Boot Actuator Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- Micrometer Tracing Documentation: https://micrometer.io/docs/tracing
- OpenTelemetry Java Documentation: https://opentelemetry.io/docs/instrumentation/java/
- Grafana Tempo Documentation: https://grafana.com/docs/tempo/latest/
- W3C Trace Context Specification: https://www.w3.org/TR/trace-context/
- NFR-051: OTLP observability integration
- NFR-052: Trace propagation through all layers

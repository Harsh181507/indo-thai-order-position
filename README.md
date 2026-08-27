# Indo Thai — Order Update & Position Maintaining Services

Two independently runnable Spring Boot services that together process a stream of trading
order updates and maintain the current net position for every symbol:

- **`order-update-service`** — reads `order_updates.csv` row by row, validates each row,
  throttles the output rate, and forwards valid events to the Position Maintaining Service.
- **`position-service`** — receives events over HTTP, keeps an in-memory net position per
  symbol, de-duplicates by `event_id`, and exposes `GET /position`.

## Architecture & Design Choices

| Decision | Choice | Reason |
|---|---|---|
| Language / framework | Java 21 + Spring Boot 3.5.5 (Maven, multi-module) | Strong typing, mature HTTP client/server stack, and built-in dependency injection made it easy to keep validation, throttling, and transport concerns cleanly separated. |
| Inter-service transport | Plain HTTP/JSON (`RestClient` → `POST /events`) | The assessment allows any justified mechanism and calls out HTTP as acceptable. No broker is required for this scope, HTTP is trivial to run locally with no extra infrastructure, and it keeps the two services fully independent processes with a simple, inspectable contract. |
| CSV parsing | Apache Commons CSV streamed over a `BufferedReader` | Reads and dispatches one row at a time (`CSVParser` iterator) so the whole file is never loaded into memory, satisfying the "read incrementally" requirement even for large files. |
| Throttling | A hand-rolled token-interval `EventThrottle` (`LockSupport.parkNanos`) | Enforces a configurable max events/second without pulling in a rate-limiting library; avoids flaky precision by working off a computed interval rather than a fixed-window counter. |
| Idempotency | `ConcurrentHashMap.newKeySet()` of accepted `event_id`s, checked in **both** services | The Order Update Service skips resending a duplicate `event_id` it has already forwarded; the Position Service independently ignores any duplicate it receives, so the position stays correct even if a client retries or sends the same event twice. |
| Position storage | `ConcurrentHashMap<String, Long>` updated via `merge` | Thread-safe without external locking, so concurrent `POST /events` calls and concurrent `GET /position` reads stay correct. |
| Validation | A single `OrderEventValidator` returning a `ValidationResult(valid, reason)` | Every rejection carries a human-readable reason for logging, and validation is fully decoupled from CSV parsing / HTTP transport so it's unit-testable in isolation. |

## Event Contract

```json
{
  "event_id": "evt-0001",
  "symbol": "RELIANCE",
  "transaction_type": "BUY",
  "quantity": 90
}
```

- `event_id` — non-empty string, unique. First valid event for a given ID wins; later
  duplicates (from the CSV or over HTTP) are ignored.
- `symbol` — non-empty string, case and value preserved exactly as supplied.
- `transaction_type` — must be exactly `BUY` or `SELL` (case-insensitive on input, anything
  else is rejected).
- `quantity` — must parse as a positive integer.
- Any row failing a rule above is logged with the specific reason and skipped; it never stops
  processing of subsequent rows.

## Inter-Service Communication

- **Mechanism:** synchronous HTTP `POST /events` from `order-update-service` to
  `position-service`, using Spring's `RestClient`.
- **Payload:** the JSON event shown above, one request per valid, throttled event, sent in
  CSV order.
- **Error surfacing:** if the HTTP call fails (connection refused, non-2xx, timeout, etc.),
  `PositionServiceClient` wraps the failure and `OrderProcessingService` catches it, logs the
  event ID, symbol, and reason at `ERROR`, increments a `failedEvents` counter, and — critically
  — **does not** stop processing of the remaining CSV rows. The event's ID is also released from
  the in-memory "already sent" set so a later manual retry (e.g. re-invoking `POST /orders/process`)
  can attempt it again.
- **Delivery limitations:** delivery is at-most-once per process lifetime and not durable —
  there is no retry queue, broker, or persistence. If the Position Service is down or restarts,
  in-flight events are lost and its in-memory idempotency set resets, both of which are
  explicitly out of scope per the assessment.

## Project Layout

```
indo-thai-order-position/
├── order-update-service/   # reads CSV, validates, throttles, sends events
├── position-service/       # receives events, maintains positions, exposes GET /position
├── data/
│   └── order_updates.csv   # sample/synthetic input data
└── pom.xml                 # parent Maven POM
```

Both services are separate Maven modules with their own `pom.xml`, main class, and test suite,
and each can be built and run on its own.

## Setup & Run Instructions

### Prerequisites
- Java 21
- Maven 3.9+ (or use the bundled `./mvnw` wrapper)

### 1. Start the Position Maintaining Service first

```bash
cd position-service
./mvnw spring-boot:run
```

By default it listens on `http://localhost:8081`.

### 2. Start the Order Update Service

```bash
cd order-update-service
./mvnw spring-boot:run
```

By default it listens on `http://localhost:8082`, points at `position-service` on
`http://localhost:8081`, and reads `../data/order_updates.csv`.

On startup, the Order Update Service automatically processes the configured CSV file once
(see `StartupProcessor`) and logs a summary line when it finishes. Processing can also be
re-triggered manually at any time via:

```bash
curl -X POST http://localhost:8082/orders/process
```

### 3. Query the current positions

```bash
curl http://localhost:8081/position
```

## Configuration

All values are externalized via Spring configuration properties (`application.yml`) — none are
hardcoded to a machine-specific path.

**`order-update-service/src/main/resources/application.yml`**

| Property | Default | Description |
|---|---|---|
| `server.port` | `8082` | Port the Order Update Service listens on. |
| `order.input-file` | `../data/order_updates.csv` | Path to the input CSV, read incrementally. |
| `order.publisher.max-events-per-second` | `50` | Throttle cap on outbound events/sec. |
| `position-service.base-url` | `http://localhost:8081` | Base URL of the Position Maintaining Service. |

**`position-service/src/main/resources/application.yml`**

| Property | Default | Description |
|---|---|---|
| `server.port` | `8081` | Port the Position Maintaining Service listens on. |

Any of these can be overridden at launch, e.g.:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=9090 --order.input-file=/absolute/path/order_updates.csv --position-service.base-url=http://localhost:9091"
```

or via environment variables (`SERVER_PORT`, `ORDER_INPUT_FILE`,
`ORDER_PUBLISHER_MAX_EVENTS_PER_SECOND`, `POSITION_SERVICE_BASE_URL`).

## API Usage & Example Response

### Position Maintaining Service

**`POST /events`** — accepts a single order event (used internally by the Order Update Service,
but callable directly for testing):

```bash
curl -X POST http://localhost:8081/events \
  -H "Content-Type: application/json" \
  -d '{"eventId":"evt-0001","symbol":"RELIANCE","transactionType":"BUY","quantity":90}'
```

**`GET /position`** — returns the current net position for every symbol seen in an accepted
event, including symbols whose net position has settled to zero:

```bash
curl http://localhost:8081/position
```

```json
{
  "RELIANCE": 90,
  "TCS": -75
}
```

The endpoint remains available and correct while events are actively being processed, since
positions are stored in a `ConcurrentHashMap`.

### Order Update Service

**`POST /orders/process`** — (re-)triggers processing of the configured CSV file:

```bash
curl -X POST http://localhost:8082/orders/process
```

**`GET /processing/statistics`** — returns a running count of total, successful, invalid,
duplicate, and failed events for observability:

```bash
curl http://localhost:8082/processing/statistics
```

```json
{
  "totalEvents": 200,
  "successfulEvents": 190,
  "invalidEvents": 8,
  "duplicateEvents": 2,
  "failedEvents": 0
}
```

## Running the Tests

Each module has its own test suite (unit, controller-slice, and integration tests). Run them
per module:

```bash
cd position-service && ./mvnw test
cd order-update-service && ./mvnw test
```

Or build/test everything from the repo root:

```bash
./mvnw -pl position-service,order-update-service test
```

### Test coverage

- **`position-service`**
    - `PositionServiceTest` — BUY/SELL position math, multiple symbols, negative and zero net
      positions, duplicate `event_id` handling.
    - `PositionControllerTest` / `PositionControllerConcurrencyTest` — `GET /position` response
      shape and correctness under concurrent reads/writes.
    - `PositionIntegrationTest`, `PositionServiceConcurrencyTest`,
      `PositionServiceDuplicateConcurrencyTest` — end-to-end and concurrency behavior of the
      running service.
- **`order-update-service`**
    - `OrderEventValidatorTest` — invalid transaction types; zero, negative, non-integer, and
      blank quantities; blank event IDs and symbols.
    - `CsvOrderReaderTest`, `OrderFileProcessorTest`, `OrderFileProcessorRealCsvTest`,
      `OrderFileProcessorIntegrationTest` — incremental CSV parsing, and continuing to process
      later rows after an invalid row.
    - `OrderProcessingServiceTest`, `OrderProcessingConcurrencyTest` — duplicate `event_id`
      suppression and thread-safety.
    - `EventThrottleTest` — throttle honors the configured max-events-per-second without relying
      on brittle sub-millisecond timing assertions.
    - `PositionServiceClientTest`, `PositionServiceClientIntegrationTest` — HTTP send behavior
      and error surfacing when the downstream service is unreachable.
    - `OrderProcessingControllerTest` — the `/orders/process` endpoint.

## Known Limitations & Trade-offs

- **No durable delivery.** Events are sent over plain HTTP with no message broker, retry queue,
  or outbox; if the Position Service is unreachable, that event is logged as failed and skipped
  going forward unless processing is re-triggered.
- **In-memory state only.** Both the accepted-`event_id` sets and the position map live in
  memory and are lost on restart — persistence is explicitly out of scope per the assessment.
- **At-most-once semantics.** Combined with in-memory idempotency, a full restart of the
  Position Service can allow a previously-processed `event_id` to be re-applied if the Order
  Update Service is re-run against the same CSV — acceptable per the stated non-goals
  (recovery after a complete process restart is out of scope).
- **Throttle is approximate.** `EventThrottle` targets an even spacing between events based on
  the configured rate; it guarantees the cap is respected but does not guarantee
  sub-millisecond precision, by design.
- **Single-file input.** The Order Update Service processes one configured CSV path per run;
  it does not watch a directory or accept file uploads.

## AI-Assisted Tooling

This project was developed with the help of AI coding assistance for scaffolding and
iteration; all code and design decisions were reviewed and can be explained on request.
# Inventory Service

Standalone Spring Boot service that manages inventory quantities only. Product data is neither stored nor retrieved.

## Run

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` if the defaults do not fit your MySQL setup, then run:

```bash
mvn spring-boot:run
```

The service uses port `8082`; Swagger UI is at `http://localhost:8082/swagger-ui.html`.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/inventories` | Create inventory |
| GET | `/api/v1/inventories` | List inventory |
| GET | `/api/v1/inventories/{id}` | Get by inventory ID |
| GET | `/api/v1/inventories/product/{productId}` | Get by product ID |
| PUT | `/api/v1/inventories/{id}` | Update inventory |
| DELETE | `/api/v1/inventories/{id}` | Delete inventory |
| PATCH | `/api/v1/inventories/{productId}/reserve` | Move quantity from available to reserved |
| PATCH | `/api/v1/inventories/{productId}/release` | Move quantity from reserved to available |
| PATCH | `/api/v1/inventories/{productId}/deduct` | Consume reserved quantity |

The three business endpoints accept `{"quantity": 1}`. A request that exceeds the relevant available or reserved quantity returns `409 Conflict`.

When creating inventory, the service verifies the product through Product Service. Configure its address with `PRODUCT_SERVICE_URL` (default `http://localhost:8081`). A missing product returns `404`; an unreachable Product Service returns `503`.

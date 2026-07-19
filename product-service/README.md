# Product Service

Spring Boot service that owns product catalog information only. Inventory and stock are deliberately out of scope.

## Run

1. Start MySQL (default: `localhost:3306`, credentials `root` / `admin`) or set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
2. Run `mvn spring-boot:run`.

API: `http://localhost:8081/api/v1/products`  
Swagger UI: `http://localhost:8081/swagger-ui.html`

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/products` | Create product |
| GET | `/api/v1/products` | List products |
| GET | `/api/v1/products/{id}` | Get product |
| GET | `/api/v1/products/search?name=...` | Search by name |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |

```json
{"name":"Paracetamol 500mg","description":"Pain relief tablets","price":25.50,"category":"Pain Relief","manufacturer":"Example Pharma","imageUrl":"https://example.com/paracetamol.jpg"}
```

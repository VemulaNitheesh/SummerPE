# Order Service

Spring Boot orchestration service. For every order it reads the product from
Product Service, reserves stock in Inventory Service, calculates the price, and
persists a confirmed order.

## Run

Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PRODUCT_SERVICE_URL`, and
`INVENTORY_SERVICE_URL` if the defaults do not fit your local setup, then run:

```bash
mvn spring-boot:run
```

API: `http://localhost:8083/api/v1/orders`  
Swagger UI: `http://localhost:8083/swagger-ui.html`

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/orders` | Validate product, reserve inventory, and create an order |
| GET | `/api/v1/orders` | List orders |
| GET | `/api/v1/orders/{id}` | Get an order |
| DELETE | `/api/v1/orders/{id}` | Release its reserved inventory and delete the order |

```bash
curl -X POST http://localhost:8083/api/v1/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerName":"Asha Kumar","customerEmail":"asha@example.com","productId":1,"quantity":2}'
```

`POST` returns `201 Created`. A missing product returns `404`, insufficient
inventory returns `409`, and an unavailable Product or Inventory Service returns
`503`. No order is persisted for those failed create requests.

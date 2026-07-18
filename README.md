# Medicine E-commerce Prototype

A local microservices prototype for a medicine catalogue and inventory system.
It demonstrates service versioning, reverse-proxy routing, service-to-service
communication, containerised deployment, health checks, and Ansible automation.

## Architecture

```text
Client
  |
  +-- http://localhost:8090 (Nginx gateway)
        |-- /api/v1/products... and /api/v1/version -> product-service v2
        `-- /api/v1/inventories...                    -> inventory-service

product-service v1 (legacy): localhost:8081
product-service v2 (current): localhost:8083
inventory-service:              localhost:8082
MySQL:                           internal Docker network only
```

`product-service-v1` is a supplied legacy image used to demonstrate versioned
services. `product-service-v2` is built from this repository and is the version
exposed by Nginx. Inventory validates product IDs through the Nginx gateway, so
it communicates with the active product-service version rather than directly
with v1.

## Services and endpoints

| Service | Direct URL | Gateway URL | Purpose |
| --- | --- | --- | --- |
| Product v1 (legacy) | `http://localhost:8081` | Not routed | Legacy comparison image |
| Product v2 | `http://localhost:8083` | `http://localhost:8090/api/v1/products` | Product catalogue |
| Inventory | `http://localhost:8082` | `http://localhost:8090/api/v1/inventories` | Available and reserved stock |
| Nginx | `http://localhost:8090` | — | Routes public API traffic |

Nginx uses path prefixes, not one rule per endpoint. For example,
`/api/v1/products`, `/api/v1/products/1`, and
`/api/v1/products/search?name=para` all reach product v2. Likewise, all
`/api/v1/inventories...` endpoints reach inventory-service.

## Health checks

The current services expose Spring Boot Actuator health endpoints:

```bash
curl http://localhost:8083/actuator/health  # product-service v2
curl http://localhost:8082/actuator/health  # inventory-service
```

Both should return a JSON response whose `status` is `UP`.

Product v1 on port `8081` is an old prebuilt image and does not contain the
Actuator dependency. Do not use `/actuator/health` on v1 as a deployment check.

## Prerequisites

- Docker Engine
- Docker Compose (`docker-compose` command)
- Java 21 and Maven, for a local/Ansible build
- Ansible, when using the deployment playbook

The environment variables are provided in `.env`. The `product-service:v1`
image must already be present locally because its source is intentionally not
included in this repository.

## Run with Ansible (recommended)

From the repository root:

```bash
ansible-playbook -i ansible/inventory.ini ansible/deploy.yml
```

The playbook packages both Spring Boot applications, builds the v2 and
inventory images, starts the Compose stack, waits for both health endpoints,
and confirms that Nginx can serve the inventory API.

## Run with Docker Compose

Build the Java JARs, then start the stack:

```bash
(cd product-service && mvn -DskipTests package)
(cd inventory-service && mvn -DskipTests package)
docker-compose up -d --build
```

Check containers and verify the deployment:

```bash
docker-compose ps
curl http://localhost:8083/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8090/api/v1/version
curl http://localhost:8090/api/v1/inventories
```

Stop the local stack:

```bash
docker-compose down
```

## API examples

Create a product through the gateway:

```bash
curl -X POST http://localhost:8090/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Paracetamol 500mg","description":"Pain relief","price":25.50,"category":"Pain Relief","manufacturer":"Example Pharma","imageUrl":"https://example.com/paracetamol.jpg"}'
```

Create inventory for product ID `1`:

```bash
curl -X POST http://localhost:8090/api/v1/inventories \
  -H 'Content-Type: application/json' \
  -d '{"productId":1,"availableQuantity":100,"reservedQuantity":0}'
```

## Prototype notes

- MySQL is not published to the host; only containers in the Docker network can access it.
- Nginx exposes the current product version (v2). Calls to port `8081` bypass the gateway and target legacy v1 directly.
- A new endpoint underneath an existing prefix is automatically proxied. For example, a new `/api/v1/products/featured` endpoint needs no Nginx change.
- A new top-level service path, such as `/api/v1/orders`, requires one new Nginx route to the orders service.
- The Ansible playbook checks the deployment while it runs. It is not a long-running monitor and does not perform delayed rollback after it exits.

## To-do / next steps

- [ ] Add automated unit and integration tests for both services.
- [ ] Add Docker health checks to `docker-compose.yml` so container health is visible in `docker-compose ps`.
- [ ] Add a CI workflow to build, test, and validate the Docker images on every push.
- [ ] Implement a versioned release strategy (tagged images and a controlled Nginx switch) before adding automatic rollback.
- [ ] Add observability: application metrics, centralised logs, and alerting.
- [ ] Add authentication/authorisation and keep secrets out of `.env` before any non-local deployment.
- [ ] Add API documentation at the gateway and contract tests for inter-service calls.

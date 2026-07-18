# Local deployment with Ansible

This playbook packages the Spring services, builds the current Docker images,
starts the Compose stack, and verifies the application health checks.

Run it from the repository root:

```bash
ansible-playbook -i ansible/inventory.ini ansible/deploy.yml
```

Prerequisites: Docker, Docker Compose (`docker-compose`), Maven, and Ansible.
The legacy `product-service:v1` image must already exist locally because its
source is not part of this repository. It is started for the v1/v2 prototype,
but is not health-checked: it predates the Actuator endpoint. The current
product service is v2 on port 8083.

After a successful run:

```bash
curl http://localhost:8083/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8090/api/v1/inventories
```

Stop the stack with:

```bash
docker-compose down
```

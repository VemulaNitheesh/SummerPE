# Local deployment with Ansible

This playbook packages the Spring services, builds the current Docker images,
starts the Compose stack, and verifies the application health checks.

Run it from the repository root:

```bash
ansible-playbook -i ansible/inventory.ini ansible/deploy.yml
```

Prerequisites: Docker, Docker Compose (`docker-compose`), Maven, and Ansible.

After a successful run:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8090/api/v1/inventories
```

Stop the stack with:

```bash
docker-compose down
```

# MSA Deadlock Demo

## How to Run Tests

### Quick Start (From Clean State)

```bash
./gradlew clean bootJar && \
docker build -t server-a:latest server-a/ && \
docker build -t server-b:latest server-b/ && \
./gradlew :e2e-tests:test
```

### Step by Step

```bash
# 1. Clean build artifacts and Docker images
./gradlew clean
docker rmi server-a:latest server-b:latest

# 2. Build JAR files
./gradlew bootJar

# 3. Build Docker images
docker build -t server-a:latest server-a/
docker build -t server-b:latest server-b/

# 4. Run E2E tests
./gradlew :e2e-tests:test
```

### Notes

- **Gradle** builds JAR files
- **Docker** builds container images
- Tests require Docker images to exist before running

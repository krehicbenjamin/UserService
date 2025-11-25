# User Service

A simple Spring Boot 3.3.5 microservice providing complete user management, JWT authentication, refresh token rotation, and device session tracking.

## Features

- **Authentication & Authorization**
  - JWT-based stateless authentication
  - Refresh token rotation
  - BCrypt password hashing (12 rounds)
  - Strong password policy enforcement
  - Role-based access control (USER, ADMIN)

- **Security**
  - Rate limiting (10 req/min per IP on auth endpoints)
  - Security headers (CSP, HSTS, X-Frame-Options, etc.)
  - CORS configuration
  - XSS prevention through input sanitization
  - SQL injection prevention
  - OWASP Dependency Check integration

- **User Management**
  - User registration with validation
  - Login with credentials
  - Device session tracking
  - Session revocation
  - Soft delete support

- **Architecture**
  - Clean Architecture / Domain-Driven Design (Hybrid)
  - Domain entities, value objects, and domain services
  - Repository pattern
  - DTO mapping layer
  - Comprehensive exception handling

- **Database**
  - PostgreSQL
  - Flyway migrations
  - JPA/Hibernate
  - Connection pooling (HikariCP)

- **Documentation**
  - Swagger/OpenAPI 3.0
  - Interactive API documentation

## Prerequisites

- Java 21
- Docker & Docker Compose
- Maven 3.9+ (if building locally)

## Deployment

### Local Development

See [deployment/docker/README.md](deployment/docker/README.md) for detailed local development instructions.

Quick start:
```bash
cd deployment/docker
cp env.example .env
# Edit .env and set JWT_SECRET
docker-compose up -d
```

### Kubernetes Deployment

See [deployment/kubernetes/README.md](deployment/kubernetes/README.md) for Kubernetes deployment.

### Helm Deployment

See [deployment/helm/README.md](deployment/helm/README.md) for Helm chart usage.

### Terraform Deployment

See [deployment/terraform/README.md](deployment/terraform/README.md) for Infrastructure as Code deployment.

### Using Makefile
```bash
# Build and test
make build
make test
make security-scan

# Deploy
make deploy-staging
make deploy-production

# Or use Helm
IMAGE_TAG=v1.0.0 make helm-deploy
```

## Local Development

### Setup
```bash
# Set environment variables
export JWT_SECRET=$(openssl rand -base64 64)
export DATABASE_URL=jdbc:postgresql://localhost:5432/user
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres

# Start PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_DB=user \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Build and run
./mvnw clean install
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Documentation

- [API Documentation](docs/API.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Security Documentation](docs/SECURITY.md)
- [Testing Guide](docs/TESTING.md)

## API Endpoints

### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | Login user | No |
| POST | `/auth/refresh` | Refresh access token | No |

### User Management
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/users/me` | Get current user profile | Yes |
| GET | `/users/me/sessions` | Get active device sessions | Yes |
| DELETE | `/users/me/sessions/{id}` | Revoke device session | Yes |

### Example Requests

**Register:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!@#",
    "fullName": "John Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!@#"
  }'
```

**Get User Profile:**
```bash
curl http://localhost:8080/users/me \
  -H "Authorization: Bearer <your-access-token>"
```

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `JWT_SECRET` | JWT signing secret (min 256 bits) | - | Yes |
| `DATABASE_URL` | PostgreSQL connection URL | localhost:5432/user | Yes |
| `DATABASE_USERNAME` | Database username | postgres | Yes |
| `DATABASE_PASSWORD` | Database password | postgres | Yes |
| `JWT_ACCESS_EXPIRATION` | Access token TTL (seconds) | 900 (15min) | No |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL (seconds) | 604800 (7days) | No |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | localhost:3000,4200 | No |
| `APP_PORT` | Application port | 8080 | No |

## Testing

### Run Tests
```bash
./mvnw test
```

### Security Scan
```bash
./mvnw dependency-check:check
```

## Database Schema

### Tables
- `users` - User accounts
- `user_roles` - User role assignments
- `refresh_tokens` - Refresh token storage (hashed)
- `device_sessions` - Active device sessions
- `flyway_schema_history` - Migration history

### Key Security Features
- OWASP Top 10 Compliant (10/10)
- JWT with refresh token rotation
- Rate limiting on authentication endpoints
- Comprehensive security headers
- Input sanitization (XSS prevention)
- SQL injection prevention
- Password policy enforcement
- Security audit logging
- OWASP Dependency Check

## Monitoring

### Actuator Endpoints
- `/actuator/health` - Health check (public)
- `/actuator/info` - Application info (public)
- `/actuator/metrics` - Metrics (admin only)
- `/actuator/prometheus` - Prometheus metrics (admin only)

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

## Architecture

```
src/main/java/com/user/UserService/
├── common/              # Shared utilities
├── config/              # Configuration classes
├── security/            # Security filters & config
└── user/
    ├── domain/          # Domain layer
    │   ├── entity/      # JPA entities
    │   ├── value/       # Value objects
    │   ├── exception/   # Domain exceptions
    │   ├── event/       # Domain events
    │   └── service/     # Domain services
    ├── repository/      # Data access layer
    ├── service/         # Application services
    └── web/             # Presentation layer
        ├── controller/  # REST controllers
        ├── dto/         # Data transfer objects
        └── mapper/      # DTO mappers
```

## CI/CD

### GitHub Actions
- Automated testing on PR
- Security scanning (OWASP, Trivy)
- Docker image build and push
- Automated deployment to staging/production

See `.github/workflows/ci-cd.yaml` for full pipeline configuration.

## Project Structure

```
UserService/
├── .github/workflows/           # CI/CD pipelines
├── deployment/                  # All deployment configurations
│   ├── docker/                 # Docker Compose for local development
│   │   ├── docker-compose.yml
│   │   ├── env.example
│   │   ├── init-db.sql
│   │   └── README.md
│   ├── kubernetes/             # Raw Kubernetes manifests
│   │   ├── manifests/
│   │   └── README.md
│   ├── helm/                   # Helm charts
│   │   ├── charts/userservice/
│   │   └── README.md
│   └── terraform/              # Infrastructure as Code
│       ├── main.tf
│       ├── variables.tf
│       ├── outputs.tf
│       └── README.md
├── docs/                        # Documentation
│   ├── API.md                  # API documentation
│   ├── DEPLOYMENT.md           # Deployment guide
├── src/                        # Application source code
│   ├── main/java/com/user/UserService/
│   │   ├── user/
│   │   │   ├── domain/         # Domain layer (entities, value objects, services)
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── service/        # Application services
│   │   │   └── web/            # Web layer (controllers, DTOs, mappers)
│   │   ├── security/           # Security configuration
│   │   ├── config/             # Application configuration
│   │   └── common/             # Shared utilities
│   └── resources/
│       ├── application.yaml
│       └── db/migration/       # Flyway migrations
├── Dockerfile                   # Container image definition
├── Makefile                    # Build automation
├── pom.xml                     # Maven configuration
└── README.md                   # This file
```

## Deployment Options

This project supports multiple deployment methods, each in its own organized directory:

1. **Docker Compose** - `deployment/docker/` - Local development and testing
2. **Kubernetes** - `deployment/kubernetes/` - Raw Kubernetes manifests
3. **Helm** - `deployment/helm/` - Templated Kubernetes deployment
4. **Terraform** - `deployment/terraform/` - Infrastructure as Code

Each deployment method has its own comprehensive README with instructions.

## License

This project is licensed under the MIT License.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
# Docker Compose Deployment

This directory contains everything needed to run the UserService locally using Docker Compose.

## Quick Start

1. Copy the example environment file:
   ```bash
   cp env.example .env
   ```

2. Generate a secure JWT secret:
   ```bash
   openssl rand -base64 64
   ```

3. Edit `.env` and set `JWT_SECRET` to the generated value

4. Start the services:
   ```bash
   docker-compose up -d
   ```

5. Check the logs:
   ```bash
   docker-compose logs -f userservice
   ```

6. Access the application:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health: http://localhost:8080/actuator/health

## Services

### userservice
- Spring Boot application
- Exposed on port 8080
- Automatically runs Flyway migrations

### postgres
- PostgreSQL 16
- Exposed on port 5432
- Data persisted in Docker volume `postgres_data`
- Initial database created via `init-db.sql`

## Environment Variables

See `env.example` for all available configuration options.

Required:
- `JWT_SECRET` - Secret key for JWT signing (use openssl to generate)

Optional (with defaults):
- `DATABASE_URL` - PostgreSQL connection string
- `DATABASE_USERNAME` - Database user
- `DATABASE_PASSWORD` - Database password
- `JWT_ACCESS_EXPIRATION` - Access token lifetime in seconds (default: 900)
- `JWT_REFRESH_EXPIRATION` - Refresh token lifetime in seconds (default: 604800)

## Commands

Start services:
```bash
docker-compose up -d
```

View logs:
```bash
docker-compose logs -f
docker-compose logs -f userservice
docker-compose logs -f postgres
```

Stop services:
```bash
docker-compose stop
```

Remove services and volumes:
```bash
docker-compose down -v
```

Rebuild and restart:
```bash
docker-compose up -d --build
```

## Troubleshooting

### Application fails to connect to database

Check if PostgreSQL is ready:
```bash
docker-compose logs postgres
```

Wait a few seconds and restart the userservice:
```bash
docker-compose restart userservice
```

### Port already in use

If port 8080 or 5432 is already in use, edit `docker-compose.yml` to use different ports:

```yaml
services:
  userservice:
    ports:
      - "8081:8080"  # Change host port to 8081
  
  postgres:
    ports:
      - "5433:5432"  # Change host port to 5433
```

### View database

Connect to PostgreSQL:
```bash
docker-compose exec postgres psql -U postgres -d user
```

Run SQL commands:
```sql
\dt                  -- List tables
SELECT * FROM users; -- Query users
\q                   -- Quit
```

## Production Use

DO NOT use this docker-compose setup in production. It is intended for local development only.

For production deployments, see:
- [../kubernetes/README.md](../kubernetes/README.md) - Kubernetes deployment
- [../helm/README.md](../helm/README.md) - Helm chart deployment
- [../../docs/DEPLOYMENT.md](../../docs/DEPLOYMENT.md) - Full deployment guide


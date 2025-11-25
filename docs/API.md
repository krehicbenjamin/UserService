# API Documentation

## Base URL

- Local: `http://localhost:8080`
- Production: `https://api.yourdomain.com`

## Swagger UI

Interactive API documentation available at:
- `/swagger-ui.html`
- `/v3/api-docs` (OpenAPI JSON spec)

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### Authentication

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!@#",
  "fullName": "John Doe"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!@#"
}
```

#### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### User Management

#### Get Current User
```http
GET /users/me
Authorization: Bearer <access-token>
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "fullName": "John Doe",
  "roles": ["USER"],
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### Get Active Sessions
```http
GET /users/me/sessions
Authorization: Bearer <access-token>
```

**Response:**
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "deviceName": "Windows PC",
    "os": "Windows 10/11",
    "ipAddress": "192.168.1.100",
    "lastUsedAt": "2024-01-01T12:00:00Z",
    "createdAt": "2024-01-01T10:00:00Z"
  }
]
```

#### Revoke Session
```http
DELETE /users/me/sessions/{sessionId}
Authorization: Bearer <access-token>
```

## Error Responses

All errors follow this format:

```json
{
  "error": "Error message",
  "code": "ERROR_CODE",
  "status": 400
}
```

### Common Error Codes

- `INVALID_CREDENTIALS` - Invalid email or password
- `EMAIL_ALREADY_USED` - Email already registered
- `WEAK_PASSWORD` - Password doesn't meet requirements
- `USER_NOT_FOUND` - User not found
- `TOKEN_EXPIRED` - JWT token has expired
- `TOKEN_REVOKED` - Refresh token has been revoked
- `INVALID_TOKEN` - Invalid or malformed token
- `SESSION_NOT_FOUND` - Device session not found
- `VALIDATION_ERROR` - Request validation failed
- `RATE_LIMIT_EXCEEDED` - Too many requests
- `ACCESS_DENIED` - Insufficient permissions
- `INTERNAL_ERROR` - Internal server error

## Rate Limiting

Authentication endpoints are rate-limited to 10 requests per minute per IP address.

When rate limit is exceeded:
```json
{
  "error": "Too many requests",
  "code": "RATE_LIMIT_EXCEEDED",
  "status": 429
}
```

## Password Requirements

- Minimum 8 characters
- Maximum 128 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (!@#$%^&*...)

## Token Lifetimes

- **Access Token**: 15 minutes (900 seconds)
- **Refresh Token**: 7 days (604800 seconds)

## Examples

### Complete Registration Flow

```bash
# 1. Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "SecurePass123!@#",
    "fullName": "New User"
  }'

# Response includes accessToken and refreshToken
```

### Complete Login Flow

```bash
# 1. Login
RESPONSE=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!@#"
  }')

# 2. Extract access token
ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.accessToken')

# 3. Use token to access protected endpoint
curl http://localhost:8080/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Token Refresh Flow

```bash
# 1. Use refresh token to get new tokens
REFRESH_TOKEN="your-refresh-token"

curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}"
```

### Session Management

```bash
# 1. Get active sessions
curl http://localhost:8080/users/me/sessions \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# 2. Revoke a specific session
SESSION_ID="660e8400-e29b-41d4-a716-446655440001"
curl -X DELETE "http://localhost:8080/users/me/sessions/$SESSION_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```


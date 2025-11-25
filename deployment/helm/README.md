# Helm Chart Deployment

This directory contains the Helm chart for deploying the UserService application.

## Prerequisites

- Helm 3.0+
- Kubernetes cluster
- kubectl configured

## Quick Start

1. Install the chart:
   ```bash
   helm install userservice charts/userservice \
     --namespace production \
     --create-namespace \
     --set secrets.jwt.secret=$(openssl rand -base64 64) \
     --set secrets.database.password=YOUR_PASSWORD
   ```

2. Verify installation:
   ```bash
   helm status userservice -n production
   kubectl get pods -n production
   ```

## Configuration

### Using values file

Create `values-prod.yaml`:
```yaml
replicaCount: 3

image:
  repository: yourusername/userservice
  tag: "v1.0.0"
  pullPolicy: IfNotPresent

secrets:
  database:
    username: "postgres"
    password: "YOUR_DB_PASSWORD"
  jwt:
    secret: "YOUR_JWT_SECRET"

config:
  database:
    url: "jdbc:postgresql://postgres-service.production.svc.cluster.local:5432/userdb"
  jwt:
    accessExpiration: 900
    refreshExpiration: 604800
  cors:
    allowedOrigins: "https://app.yourdomain.com"

ingress:
  enabled: true
  className: "nginx"
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: api.yourdomain.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: userservice-tls
      hosts:
        - api.yourdomain.com

resources:
  limits:
    cpu: 1000m
    memory: 512Mi
  requests:
    cpu: 500m
    memory: 256Mi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80
```

Install with custom values:
```bash
helm install userservice charts/userservice \
  --namespace production \
  --create-namespace \
  --values values-prod.yaml
```

## Upgrading

Update the deployment:
```bash
helm upgrade userservice charts/userservice \
  --namespace production \
  --values values-prod.yaml \
  --set image.tag=v2.0.0
```

## Rollback

View release history:
```bash
helm history userservice -n production
```

Rollback to previous version:
```bash
helm rollback userservice -n production
```

Rollback to specific revision:
```bash
helm rollback userservice 2 -n production
```

## Uninstall

Remove the release:
```bash
helm uninstall userservice -n production
```

## Chart Structure

```
charts/userservice/
├── Chart.yaml              # Chart metadata
├── values.yaml            # Default configuration values
└── templates/             # Kubernetes manifest templates
    ├── _helpers.tpl       # Template helpers
    └── deployment.yaml    # Combined deployment manifest
```

## Available Configuration Values

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `3` |
| `image.repository` | Image repository | `yourusername/userservice` |
| `image.tag` | Image tag | `latest` |
| `image.pullPolicy` | Image pull policy | `IfNotPresent` |
| `secrets.database.username` | Database username | `postgres` |
| `secrets.database.password` | Database password | `postgres` |
| `secrets.jwt.secret` | JWT secret key | `` |
| `config.database.url` | Database JDBC URL | See values.yaml |
| `config.jwt.accessExpiration` | Access token expiration (seconds) | `900` |
| `config.jwt.refreshExpiration` | Refresh token expiration (seconds) | `604800` |
| `config.cors.allowedOrigins` | Allowed CORS origins | `http://localhost:3000` |
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.port` | Service port | `8080` |
| `ingress.enabled` | Enable ingress | `false` |
| `ingress.className` | Ingress class | `nginx` |
| `ingress.hosts` | Ingress hosts | See values.yaml |
| `resources.limits.cpu` | CPU limit | `1000m` |
| `resources.limits.memory` | Memory limit | `512Mi` |
| `resources.requests.cpu` | CPU request | `500m` |
| `resources.requests.memory` | Memory request | `256Mi` |
| `autoscaling.enabled` | Enable HPA | `true` |
| `autoscaling.minReplicas` | Minimum replicas | `3` |
| `autoscaling.maxReplicas` | Maximum replicas | `10` |
| `autoscaling.targetCPUUtilizationPercentage` | Target CPU % | `70` |
| `autoscaling.targetMemoryUtilizationPercentage` | Target Memory % | `80` |

## Examples

### Development Environment

```bash
helm install userservice charts/userservice \
  --namespace dev \
  --create-namespace \
  --set replicaCount=1 \
  --set autoscaling.enabled=false \
  --set ingress.enabled=false \
  --set secrets.jwt.secret=dev-secret \
  --set secrets.database.password=dev-password
```

### Production with External Database

```bash
helm install userservice charts/userservice \
  --namespace production \
  --create-namespace \
  --set config.database.url="jdbc:postgresql://prod-db.example.com:5432/userdb" \
  --set secrets.database.username=prod_user \
  --set secrets.database.password=$DB_PASSWORD \
  --set secrets.jwt.secret=$JWT_SECRET \
  --set ingress.enabled=true \
  --set ingress.hosts[0].host=api.example.com
```

### High Availability Setup

```bash
helm install userservice charts/userservice \
  --namespace production \
  --create-namespace \
  --values values-prod.yaml \
  --set replicaCount=5 \
  --set autoscaling.minReplicas=5 \
  --set autoscaling.maxReplicas=20 \
  --set resources.limits.cpu=2000m \
  --set resources.limits.memory=1Gi
```

## Testing

Test chart template rendering:
```bash
helm template userservice charts/userservice --values values-prod.yaml
```

Dry run installation:
```bash
helm install userservice charts/userservice \
  --namespace production \
  --values values-prod.yaml \
  --dry-run --debug
```

## CI/CD Integration

Add to your pipeline:
```yaml
- name: Deploy with Helm
  run: |
    helm upgrade --install userservice ./deployment/helm/charts/userservice \
      --namespace production \
      --create-namespace \
      --values ./deployment/helm/charts/userservice/values-prod.yaml \
      --set image.tag=${{ github.sha }} \
      --wait \
      --timeout 5m
```


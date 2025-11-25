# Deployment Guide

This guide covers all deployment methods for the UserService application.

## Prerequisites

- Java 21
- Docker & Docker Compose
- kubectl (for Kubernetes)
- Helm 3+ (for Helm deployments)
- Terraform 1.0+ (for Terraform deployments)

## Local Development

### Using Docker Compose

1. Navigate to the docker deployment directory:
   ```bash
   cd deployment/docker
   ```

2. Create environment file:
   ```bash
   cp env.example .env
   ```

3. Generate secure secrets:
   ```bash
   openssl rand -base64 64
   ```

4. Edit `.env` with your secrets

5. Start services:
   ```bash
   docker-compose up -d
   ```

6. Verify deployment:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

7. Access Swagger UI:
   ```
   http://localhost:8080/swagger-ui.html
   ```

### Using Maven

1. Set environment variables:
   ```bash
   export JWT_SECRET=$(openssl rand -base64 64)
   export DATABASE_URL=jdbc:postgresql://localhost:5432/user
   export DATABASE_USERNAME=postgres
   export DATABASE_PASSWORD=postgres
   ```

2. Start PostgreSQL:
   ```bash
   docker run -d --name postgres \
     -e POSTGRES_DB=user \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:16-alpine
   ```

3. Run application:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## Kubernetes Deployment

### Using Raw Manifests

1. Create namespace:
   ```bash
   kubectl create namespace production
   ```

2. Create secrets:
   ```bash
   kubectl create secret generic userservice-secrets \
     --from-literal=database.username=postgres \
     --from-literal=database.password=YOUR_PASSWORD \
     --from-literal=jwt.secret=$(openssl rand -base64 64) \
     -n production
   ```

3. Update ConfigMap:
   ```bash
   kubectl apply -f deployment/kubernetes/manifests/configmap.yaml
   ```

4. Deploy application:
   ```bash
   kubectl apply -f deployment/kubernetes/manifests/ -n production
   ```

5. Verify deployment:
   ```bash
   kubectl get pods -n production
   kubectl logs -f -l app=userservice -n production
   ```

### Using Helm

1. Navigate to helm directory:
   ```bash
   cd deployment/helm/charts/userservice
   ```

2. Create values override file:
   ```bash
   cat > values-prod.yaml <<EOF
   replicaCount: 3
   image:
     tag: "v1.0.0"
   secrets:
     database:
       username: "postgres"
       password: "YOUR_PASSWORD"
     jwt:
       secret: "YOUR_JWT_SECRET"
   config:
     database:
       url: "jdbc:postgresql://postgres-service:5432/userdb"
   EOF
   ```

3. Install chart:
   ```bash
   helm install userservice . \
     --namespace production \
     --create-namespace \
     --values values-prod.yaml
   ```

4. Upgrade deployment:
   ```bash
   helm upgrade userservice . \
     --namespace production \
     --values values-prod.yaml
   ```

5. Rollback if needed:
   ```bash
   helm rollback userservice -n production
   ```

## Terraform Deployment

1. Navigate to terraform directory:
   ```bash
   cd deployment/terraform
   ```

2. Create terraform.tfvars:
   ```bash
   cat > terraform.tfvars <<EOF
   environment          = "production"
   image_tag           = "v1.0.0"
   domain              = "api.yourdomain.com"
   database_username   = "postgres"
   database_password   = "YOUR_PASSWORD"
   jwt_secret          = "YOUR_JWT_SECRET"
   EOF
   ```

3. Initialize Terraform:
   ```bash
   terraform init
   ```

4. Plan deployment:
   ```bash
   terraform plan
   ```

5. Apply configuration:
   ```bash
   terraform apply
   ```

6. View outputs:
   ```bash
   terraform output
   ```

## CI/CD Deployment

### GitHub Actions

The application automatically deploys through GitHub Actions:

- **Pull Requests**: Run tests and security scans
- **Develop branch**: Deploy to staging environment
- **Main branch**: Deploy to production environment

Required GitHub Secrets:
- `KUBE_CONFIG_STAGING`: Base64-encoded kubeconfig for staging
- `KUBE_CONFIG_PROD`: Base64-encoded kubeconfig for production
- `GITHUB_TOKEN`: Automatically provided

### Manual Deployment via Makefile

```bash
# Build and test
make build
make test

# Deploy to staging
make deploy-staging

# Deploy to production
make deploy-production

# Or use Helm
IMAGE_TAG=v1.0.0 make helm-deploy

# Rollback deployment
make rollback

# Scale deployment
REPLICAS=5 make scale
```

## Post-Deployment Verification

1. Check pod status:
   ```bash
   kubectl get pods -n production
   ```

2. Check service endpoints:
   ```bash
   kubectl get svc -n production
   ```

3. Check ingress:
   ```bash
   kubectl get ingress -n production
   ```

4. Test health endpoint:
   ```bash
   curl https://api.yourdomain.com/actuator/health
   ```

5. View logs:
   ```bash
   kubectl logs -f -l app=userservice -n production
   ```

6. Check metrics:
   ```bash
   kubectl top pods -n production
   ```

## Troubleshooting

### Pods not starting

```bash
kubectl describe pod <pod-name> -n production
kubectl logs <pod-name> -n production
```

### Database connection issues

```bash
kubectl exec -it <pod-name> -n production -- env | grep DATABASE
```

### Ingress not working

```bash
kubectl describe ingress userservice -n production
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

### Rolling back deployment

```bash
# Using kubectl
kubectl rollout undo deployment/userservice -n production

# Using Helm
helm rollback userservice -n production

# Using Terraform
terraform apply -target=helm_release.userservice
```

## Monitoring

### Prometheus Metrics

Metrics available at: `/actuator/prometheus`

### Application Logs

```bash
kubectl logs -f -l app=userservice -n production --tail=100
```

### Health Checks

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

## Scaling

### Manual scaling

```bash
kubectl scale deployment userservice --replicas=5 -n production
```

### Auto-scaling

HPA automatically scales between 3-10 replicas based on:
- CPU utilization (target: 70%)
- Memory utilization (target: 80%)

Check HPA status:
```bash
kubectl get hpa -n production
kubectl describe hpa userservice -n production
```

## Security Considerations

1. **Never commit secrets** - Use Kubernetes Secrets, Vault, or Sealed Secrets
2. **Use TLS** - Enable cert-manager for automatic SSL certificates
3. **Network Policies** - Restrict pod-to-pod communication
4. **RBAC** - Apply least privilege access
5. **Image Scanning** - Scan images with Trivy before deployment
6. **Update regularly** - Keep dependencies and base images updated


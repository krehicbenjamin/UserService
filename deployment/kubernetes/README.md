# Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the UserService application.

## Prerequisites

- Kubernetes cluster (1.25+)
- kubectl configured to access your cluster
- PostgreSQL database (managed or self-hosted)
- Ingress controller (nginx-ingress recommended)

## Quick Start

1. Create namespace:
   ```bash
   kubectl create namespace production
   ```

2. Create secrets:
   ```bash
   kubectl create secret generic userservice-secrets \
     --from-literal=database.username=postgres \
     --from-literal=database.password=YOUR_DB_PASSWORD \
     --from-literal=jwt.secret=$(openssl rand -base64 64) \
     -n production
   ```

3. Update ConfigMap with your database URL:
   ```bash
   kubectl edit configmap userservice-config -n production
   ```

4. Apply manifests:
   ```bash
   kubectl apply -f manifests/ -n production
   ```

5. Verify deployment:
   ```bash
   kubectl get pods -n production
   kubectl logs -f -l app=userservice -n production
   ```

## Manifests

### configmap.yaml
Application configuration including database URL and CORS settings.

### secret.yaml
Template for sensitive data. In production, use Sealed Secrets or external secret management.

### deployment.yaml
Main application deployment with:
- 3 replicas for high availability
- Resource limits (CPU: 1 core, Memory: 512Mi)
- Liveness and readiness probes
- Environment variables from ConfigMap and Secrets

### service.yaml
ClusterIP service exposing port 8080.

### ingress.yaml
Ingress configuration for external access with TLS.

### hpa.yaml
Horizontal Pod Autoscaler:
- Min replicas: 3
- Max replicas: 10
- Target CPU: 70%
- Target Memory: 80%

### pdb.yaml
Pod Disruption Budget ensuring at least 2 pods always available during disruptions.

### networkpolicy.yaml
Network policies restricting pod-to-pod communication.

### serviceaccount.yaml
Dedicated service account with minimal permissions.

## Configuration

### Update Database Connection

Edit `configmap.yaml`:
```yaml
data:
  database.url: "jdbc:postgresql://your-postgres-host:5432/userdb"
```

### Update Ingress

Edit `ingress.yaml`:
```yaml
spec:
  rules:
  - host: api.yourdomain.com
```

### Update Resource Limits

Edit `deployment.yaml`:
```yaml
resources:
  limits:
    cpu: "2"
    memory: 1Gi
  requests:
    cpu: "1"
    memory: 512Mi
```

## Monitoring

### View Logs

```bash
kubectl logs -f -l app=userservice -n production
```

### Check Pod Status

```bash
kubectl get pods -n production
kubectl describe pod <pod-name> -n production
```

### Check HPA Status

```bash
kubectl get hpa -n production
kubectl describe hpa userservice -n production
```

### Access Metrics

```bash
kubectl port-forward svc/userservice 8080:8080 -n production
curl http://localhost:8080/actuator/prometheus
```

## Troubleshooting

### Pods CrashLooping

Check logs:
```bash
kubectl logs <pod-name> -n production --previous
```

Common causes:
- Database connection failure
- Missing secrets
- Invalid configuration

### Database Connection Issues

Verify secrets:
```bash
kubectl get secret userservice-secrets -n production -o yaml
```

Test connection from pod:
```bash
kubectl exec -it <pod-name> -n production -- env | grep DATABASE
```

### Ingress Not Working

Check ingress controller:
```bash
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

Verify ingress:
```bash
kubectl describe ingress userservice -n production
```

## Security Best Practices

1. Use external secret management (Vault, AWS Secrets Manager, Azure Key Vault)
2. Enable network policies
3. Use Pod Security Standards
4. Regularly update images
5. Scan images for vulnerabilities
6. Use RBAC with minimal permissions
7. Enable audit logging

## Scaling

Manual scaling:
```bash
kubectl scale deployment userservice --replicas=5 -n production
```

Auto-scaling is handled by HPA based on CPU and memory metrics.

## Rolling Updates

Update image version:
```bash
kubectl set image deployment/userservice \
  userservice=yourusername/userservice:v2.0.0 \
  -n production
```

Check rollout status:
```bash
kubectl rollout status deployment/userservice -n production
```

Rollback if needed:
```bash
kubectl rollout undo deployment/userservice -n production
```

## Clean Up

Remove all resources:
```bash
kubectl delete -f manifests/ -n production
kubectl delete namespace production
```


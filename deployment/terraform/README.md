# Terraform Deployment

This directory contains Terraform configurations for deploying the UserService infrastructure.

## Prerequisites

- Terraform 1.0+
- Kubernetes cluster
- kubectl configured
- Helm 3+

## Quick Start

1. Initialize Terraform:
   ```bash
   terraform init
   ```

2. Create `terraform.tfvars`:
   ```hcl
   environment          = "production"
   image_tag           = "v1.0.0"
   domain              = "api.yourdomain.com"
   database_username   = "postgres"
   database_password   = "YOUR_DB_PASSWORD"
   jwt_secret          = "YOUR_JWT_SECRET"
   ```

3. Plan deployment:
   ```bash
   terraform plan
   ```

4. Apply configuration:
   ```bash
   terraform apply
   ```

## Files

- `main.tf` - Main infrastructure configuration
- `variables.tf` - Input variable definitions
- `outputs.tf` - Output value definitions

## Variables

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|----------|
| `environment` | Environment name | `string` | `production` | no |
| `image_tag` | Docker image tag | `string` | `latest` | no |
| `replica_count` | Number of replicas | `number` | `3` | no |
| `database_url` | PostgreSQL JDBC URL | `string` | - | yes |
| `database_username` | Database username | `string` | - | yes |
| `database_password` | Database password | `string` | - | yes |
| `jwt_secret` | JWT secret key | `string` | - | yes |
| `domain` | Application domain | `string` | - | yes |
| `enable_ingress` | Enable ingress | `bool` | `true` | no |
| `enable_autoscaling` | Enable HPA | `bool` | `true` | no |
| `min_replicas` | Minimum replicas for HPA | `number` | `3` | no |
| `max_replicas` | Maximum replicas for HPA | `number` | `10` | no |

## Outputs

- `namespace` - Kubernetes namespace
- `service_name` - Kubernetes service name
- `ingress_hostname` - Ingress hostname
- `helm_release_name` - Helm release name

## State Management

### Local State (Development)

For development, state is stored locally in `terraform.tfstate`.

### Remote State (Production)

For production, use remote state backend:

```hcl
terraform {
  backend "s3" {
    bucket = "your-terraform-state-bucket"
    key    = "userservice/production/terraform.tfstate"
    region = "us-east-1"
  }
}
```

Or use Terraform Cloud:

```hcl
terraform {
  backend "remote" {
    organization = "your-organization"
    workspaces {
      name = "userservice-production"
    }
  }
}
```

## Usage Examples

### Development Environment

```bash
terraform apply \
  -var="environment=development" \
  -var="replica_count=1" \
  -var="enable_autoscaling=false" \
  -var="database_url=jdbc:postgresql://dev-db:5432/userdb" \
  -var="database_username=dev_user" \
  -var="database_password=dev_pass" \
  -var="jwt_secret=dev-secret" \
  -var="domain=dev.example.com"
```

### Production Environment

```bash
terraform apply \
  -var="environment=production" \
  -var="replica_count=5" \
  -var="database_url=$DATABASE_URL" \
  -var="database_username=$DATABASE_USERNAME" \
  -var="database_password=$DATABASE_PASSWORD" \
  -var="jwt_secret=$JWT_SECRET" \
  -var="domain=api.example.com"
```

## Updating Deployment

Update image version:
```bash
terraform apply -var="image_tag=v2.0.0"
```

Scale replicas:
```bash
terraform apply -var="replica_count=10"
```

## Destroying Infrastructure

Remove all resources:
```bash
terraform destroy
```

Destroy specific resources:
```bash
terraform destroy -target=helm_release.userservice
```

## CI/CD Integration

Example GitHub Actions workflow:

```yaml
name: Deploy with Terraform

on:
  push:
    branches: [main]

jobs:
  terraform:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: 1.5.0
      
      - name: Terraform Init
        run: terraform init
        working-directory: ./deployment/terraform
      
      - name: Terraform Plan
        run: terraform plan
        working-directory: ./deployment/terraform
        env:
          TF_VAR_database_password: ${{ secrets.DB_PASSWORD }}
          TF_VAR_jwt_secret: ${{ secrets.JWT_SECRET }}
      
      - name: Terraform Apply
        if: github.ref == 'refs/heads/main'
        run: terraform apply -auto-approve
        working-directory: ./deployment/terraform
        env:
          TF_VAR_database_password: ${{ secrets.DB_PASSWORD }}
          TF_VAR_jwt_secret: ${{ secrets.JWT_SECRET }}
```

## Security Best Practices

1. **Never commit secrets** to version control
2. **Use environment variables** for sensitive data
3. **Enable state encryption** for remote backends
4. **Use workspace separation** for different environments
5. **Implement state locking** to prevent concurrent modifications
6. **Review plans** before applying
7. **Use IAM roles** instead of access keys when possible

## Troubleshooting

### State Lock Issues

Unlock state if operation was interrupted:
```bash
terraform force-unlock <lock-id>
```

### Provider Issues

Refresh provider plugins:
```bash
terraform init -upgrade
```

### View Current State

```bash
terraform show
terraform state list
```

### Debug Mode

Enable debug logging:
```bash
export TF_LOG=DEBUG
terraform apply
```

## Maintenance

### State Management

List all resources:
```bash
terraform state list
```

Remove resource from state:
```bash
terraform state rm helm_release.userservice
```

Import existing resource:
```bash
terraform import helm_release.userservice production/userservice
```

### Drift Detection

Check for configuration drift:
```bash
terraform plan -detailed-exitcode
```

Refresh state from real infrastructure:
```bash
terraform refresh
```


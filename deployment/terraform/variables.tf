variable "kubeconfig_path" {
  description = "Path to kubeconfig file"
  type        = string
  default     = "~/.kube/config"
}

variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
  default     = "production"
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "replica_count" {
  description = "Number of replicas"
  type        = number
  default     = 3
}

variable "image_tag" {
  description = "Docker image tag"
  type        = string
}

variable "domain" {
  description = "Application domain"
  type        = string
}

variable "database_username" {
  description = "Database username"
  type        = string
  sensitive   = true
}

variable "database_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT signing secret"
  type        = string
  sensitive   = true
}


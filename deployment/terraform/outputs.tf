output "namespace" {
  description = "Kubernetes namespace"
  value       = kubernetes_namespace.userservice.metadata[0].name
}

output "service_url" {
  description = "Service URL"
  value       = "https://${var.domain}"
}


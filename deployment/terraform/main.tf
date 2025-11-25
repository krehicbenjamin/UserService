terraform {
  required_version = ">= 1.0"
  
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.11"
    }
  }

  backend "s3" {
    bucket = "your-terraform-state"
    key    = "userservice/terraform.tfstate"
    region = "us-east-1"
  }
}

provider "kubernetes" {
  config_path = var.kubeconfig_path
}

provider "helm" {
  kubernetes {
    config_path = var.kubeconfig_path
  }
}

resource "kubernetes_namespace" "userservice" {
  metadata {
    name = var.namespace
    labels = {
      name        = var.namespace
      environment = var.environment
    }
  }
}

resource "helm_release" "userservice" {
  name       = "userservice"
  namespace  = kubernetes_namespace.userservice.metadata[0].name
  chart      = "../helm/charts/userservice"
  
  set_sensitive {
    name  = "secrets.database.username"
    value = var.database_username
  }

  set_sensitive {
    name  = "secrets.database.password"
    value = var.database_password
  }

  set_sensitive {
    name  = "secrets.jwt.secret"
    value = var.jwt_secret
  }

  set {
    name  = "replicaCount"
    value = var.replica_count
  }

  set {
    name  = "image.tag"
    value = var.image_tag
  }

  set {
    name  = "ingress.hosts[0].host"
    value = var.domain
  }

  depends_on = [kubernetes_namespace.userservice]
}


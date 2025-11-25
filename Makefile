.PHONY: help build test deploy clean

help:
	@echo "Enterprise UserService - Available commands:"
	@echo "  make build              - Build Docker image"
	@echo "  make test               - Run all tests"
	@echo "  make security-scan      - Run security scans"
	@echo "  make deploy-staging     - Deploy to staging"
	@echo "  make deploy-production  - Deploy to production"
	@echo "  make rollback           - Rollback deployment"
	@echo "  make clean              - Clean build artifacts"

build:
	./mvnw clean package -DskipTests -Ddependency-check.skip=true
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .

test:
	./mvnw clean verify

security-scan:
	./mvnw dependency-check:check
	trivy image $(IMAGE_NAME):$(IMAGE_TAG)

deploy-staging:
	kubectl apply -f deployment/kubernetes/manifests/ -n staging
	kubectl rollout status deployment/userservice -n staging

deploy-production:
	kubectl apply -f deployment/kubernetes/manifests/ -n production
	kubectl rollout status deployment/userservice -n production

helm-deploy:
	helm upgrade --install userservice deployment/helm/charts/userservice \
		--namespace production \
		--create-namespace \
		--values deployment/helm/charts/userservice/values.yaml \
		--set image.tag=$(IMAGE_TAG)

terraform-init:
	cd deployment/terraform && terraform init

terraform-plan:
	cd deployment/terraform && terraform plan

terraform-apply:
	cd deployment/terraform && terraform apply -auto-approve

rollback:
	kubectl rollout undo deployment/userservice -n production

clean:
	./mvnw clean
	docker rmi $(IMAGE_NAME):$(IMAGE_TAG) || true

logs:
	kubectl logs -f -l app=userservice -n production

scale:
	kubectl scale deployment/userservice --replicas=$(REPLICAS) -n production


.PHONY: up down api-run web-install web-run api-test

# Start infrastructure (Postgres, Redis, MinIO)
up:
	cd infra/docker-compose && docker compose up -d

# Stop infrastructure
down:
	cd infra/docker-compose && docker compose down

JDK21=/Users/Phayao/.antigravity/extensions/redhat.java-1.54.0-darwin-arm64/jre/21.0.10-macosx-aarch64

# Run Spring Boot API (Lombok 1.18.32 requires JDK 21; Java 25 breaks Lombok annotation processing)
api-run:
	cd apps/agento-api && JAVA_HOME=$(JDK21) mvn spring-boot:run

# Run API tests
api-test:
	cd apps/agento-api && JAVA_HOME=$(JDK21) mvn test

# Install Next.js dependencies
web-install:
	cd apps/agento-web && npm install

# Run Next.js dev server
web-run:
	cd apps/agento-web && npm run dev

# Full local dev setup in sequence
dev-setup: up web-install
	@echo "Infrastructure started."
	@echo "Set AI_API_KEY in apps/agento-api/.env"
	@echo "Then run: make api-run (in one terminal) and make web-run (in another)"

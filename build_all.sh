#!/bin/bash
set -e

SERVICES=(
  "eureka"
  "api-gateway"
  "customer-party-service"
  "quote-policy-service"
  "risk-underwriting-service"
  "claims-service"
  "vendor-partner-service"
  "recovery-service"
  "workflow-notification-service"
  "document-audit-service"
  "analytics-intelligence-service"
)

for NAME in "${SERVICES[@]}"; do
  echo "Building $NAME..."
  cd $NAME
  ./mvnw clean install -DskipTests
  cd ..
done

echo "All services built successfully."

#!/bin/bash
# Launch all IntelliSure backend services in background with managed heap memory

set -e
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$ROOT/.running_services.pid"

> "$PID_FILE"

SERVICES=(
  "eureka:8761:eureka/target/eureka-0.0.1-SNAPSHOT.jar:12"
  "api-gateway:8080:api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar:10"
  "customer-party-service:8081:customer-party-service/target/customer-party-service-0.0.1-SNAPSHOT.jar:5"
  "quote-policy-service:8082:quote-policy-service/target/quote-policy-service-0.0.1-SNAPSHOT.jar:5"
  "risk-underwriting-service:8083:risk-underwriting-service/target/risk-underwriting-service-0.0.1-SNAPSHOT.jar:5"
  "claims-service:8084:claims-service/target/claims-service-0.0.1-SNAPSHOT.jar:5"
  "vendor-partner-service:8085:vendor-partner-service/target/vendor-partner-service-0.0.1-SNAPSHOT.jar:5"
  "recovery-service:8086:recovery-service/target/recovery-service-0.0.1-SNAPSHOT.jar:5"
  "workflow-notification-service:8087:workflow-notification-service/target/workflow-notification-service-0.0.1-SNAPSHOT.jar:5"
  "document-audit-service:8088:document-audit-service/target/document-audit-service-0.0.1-SNAPSHOT.jar:5"
  "analytics-intelligence-service:8089:analytics-intelligence-service/target/analytics-intelligence-service-0.0.1-SNAPSHOT.jar:5"
)

echo "================================================="
echo " Starting 11 IntelliSure Backend Services"
echo "================================================="

for ITEM in "${SERVICES[@]}"; do
  IFS=':' read -r NAME PORT JAR DELAY <<< "$ITEM"
  JAR_PATH="$ROOT/$JAR"

  if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: JAR not found: $JAR_PATH. Please run ./build_all.sh first."
    exit 1
  fi

  echo "Starting $NAME on port $PORT..."
  java -Xms128m -Xmx256m -jar "$JAR_PATH" > /dev/null 2>&1 &
  PID=$!
  echo "$PID" >> "$PID_FILE"
  echo "  Started $NAME (PID: $PID)"
  sleep "$DELAY"
done

echo ""
echo "All 11 services started in background."
echo "PIDs saved to $PID_FILE"
echo "Check Eureka at http://localhost:8761 and Gateway at http://localhost:8080"

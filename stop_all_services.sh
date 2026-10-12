#!/bin/bash
# Stop all running IntelliSure backend services on macOS/Linux/WSL

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$ROOT/.running_services.pid"

echo "================================================="
echo " Stopping IntelliSure Backend Services"
echo "================================================="

if [ -f "$PID_FILE" ]; then
  while IFS= read -r PID; do
    if kill -0 "$PID" 2>/dev/null; then
      echo "Stopping PID $PID..."
      kill "$PID" 2>/dev/null || true
    fi
  done < "$PID_FILE"
  rm -f "$PID_FILE"
fi

# Fallback: kill by port
PORTS=(8761 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089)
for PORT in "${PORTS[@]}"; do
  PIDS=$(lsof -ti :$PORT 2>/dev/null || true)
  if [ -n "$PIDS" ]; then
    echo "Stopping remaining process on port $PORT (PIDs: $PIDS)..."
    kill -9 $PIDS 2>/dev/null || true
  fi
done

echo ""
echo "All IntelliSure backend services stopped."

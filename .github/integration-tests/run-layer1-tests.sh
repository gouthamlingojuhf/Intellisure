#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker is not running. Start Docker before running Layer 1 tests."
  exit 1
fi

status=0
for module in customer-party-service claims-service document-audit-service \
  quote-policy-service recovery-service vendor-partner-service workflow-notification-service; do
  echo "Running Layer 1 tests in ${module}"
  (cd "${module}" && ./mvnw verify -Dgroups=Layer1) || status=$?
done

echo
if [ "$status" -eq 0 ]; then
  echo "✅ Layer 1 integration tests PASSED"
else
  echo "❌ Layer 1 integration tests FAILED"
fi
exit "$status"

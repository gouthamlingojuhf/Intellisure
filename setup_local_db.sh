#!/bin/bash
# IntelliSure Local Database Initialization Script (No Docker)
# Creates the 9 microservice databases and seeds all baseline demo accounts

set -e
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
USER="${1:-root}"
PASSWORD="${2:-}"

echo "================================================="
echo " IntelliSure Local MySQL Database Setup"
echo "================================================="

if ! command -v mysql &> /dev/null; then
  echo "WARNING: 'mysql' client command not found in PATH."
  echo "Please run the following SQL scripts in MySQL Workbench / DBeaver / TablePlus:"
  echo "  1. $ROOT/docker/mysql/init/00-create-databases.sql"
  echo "  2. $ROOT/docker/mysql/init/reset_and_seed.sql"
  exit 1
fi

MYSQL_ARGS="-u $USER"
if [ -n "$PASSWORD" ]; then
  MYSQL_ARGS="$MYSQL_ARGS -p$PASSWORD"
else
  echo "Connecting as '$USER' with prompt for password (press Enter if blank)..."
  MYSQL_ARGS="$MYSQL_ARGS -p"
fi

echo "1. Creating the 9 microservice databases..."
mysql $MYSQL_ARGS < "$ROOT/docker/mysql/init/00-create-databases.sql"
echo "   -> Databases created successfully!"

echo "2. Verifying and applying schema column migrations..."
if [ -f "$ROOT/docker/mysql/init/fix_missing_columns.sql" ]; then
  mysql $MYSQL_ARGS < "$ROOT/docker/mysql/init/fix_missing_columns.sql"
  echo "   -> Column migrations verified!"
fi

echo "3. Seeding demo accounts and master configuration..."
mysql $MYSQL_ARGS < "$ROOT/docker/mysql/init/reset_and_seed.sql"
echo "   -> Demo accounts, vendors, and risk rules seeded successfully!"

echo ""
echo "Database setup complete! You can now start the backend with ./run_all_services.sh"

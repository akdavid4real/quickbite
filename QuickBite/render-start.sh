#!/bin/sh
set -eu

DB_PATH="${DB_PATH:-/tmp/quickbite.db}"
mkdir -p "$(dirname "$DB_PATH")"

if [ ! -f "$DB_PATH" ]; then
  cp /app/seed/quickbite.db "$DB_PATH"
fi

export DB_URL="jdbc:sqlite:$DB_PATH"
exec java -jar /app/quickbite.jar

#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/nacos-config/custom.env"

if [ -f "$ENV_FILE" ]; then
  echo "Loading env from $ENV_FILE"
  # export variables from custom.env so docker-compose interpolation (host:container) works
  set -a
  source "$ENV_FILE"
  set +a
fi

echo "Starting Nacos (docker-compose)..."
echo "  host port  -> ${NACOS_HOST_PORT:-8848}"
echo "  container port -> ${NACOS_CONTAINER_PORT:-8848}"

docker-compose -f "$ROOT_DIR/docker-compose.yml" up -d --remove-orphans
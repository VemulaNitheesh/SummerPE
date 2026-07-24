#!/usr/bin/env bash
# seed_products.sh
#
# Creates N products (with a large starting inventory each) through the
# live public gateway, so the load test can spread orders across many
# rows instead of hammering a single inventory row's pessimistic lock.
#
# Usage:
#   ./seed_products.sh                 # creates 10 products
#   ./seed_products.sh 20              # creates 20 products
#
# Prints the created product IDs at the end. Export them before running
# wrk2, e.g.:
#   export LOADTEST_PRODUCT_IDS="11,12,13,14,15,16,17,18,19,20"

set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://127.0.0.1:8090}"
COUNT="${1:-10}"
STARTING_STOCK="${STARTING_STOCK:-100000}"

echo "Seeding $COUNT products via $GATEWAY_URL ..."

ids=()

for i in $(seq 1 "$COUNT"); do
  product_json=$(curl -sf -X POST "$GATEWAY_URL/api/v1/products" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"Loadtest Product $i\",
      \"description\": \"Synthetic product created for downtime load testing\",
      \"price\": 9.99,
      \"category\": \"Loadtest\",
      \"manufacturer\": \"Loadtest Labs\",
      \"imageUrl\": \"\"
    }")

  product_id=$(echo "$product_json" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

  curl -sf -X POST "$GATEWAY_URL/api/v1/inventories" \
    -H "Content-Type: application/json" \
    -d "{
      \"productId\": $product_id,
      \"availableQuantity\": $STARTING_STOCK,
      \"reservedQuantity\": 0
    }" > /dev/null

  echo "  Created product $product_id with $STARTING_STOCK units of stock."
  ids+=("$product_id")
done

joined=$(IFS=,; echo "${ids[*]}")
echo
echo "Done. Export this before running the load test:"
echo
echo "  export LOADTEST_PRODUCT_IDS=\"$joined\""
echo

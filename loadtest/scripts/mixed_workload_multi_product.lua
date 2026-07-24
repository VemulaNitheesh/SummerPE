-- Mixed, weighted workload across all 3 services — MULTI-PRODUCT variant.
--
-- Identical to mixed_workload.lua except: order-creation requests are
-- spread across a set of product IDs (read from the LOADTEST_PRODUCT_IDS
-- environment variable) instead of always hitting the same productId.
-- This isolates whether pessimistic-lock contention on a single inventory
-- row (rather than the blue-green switch, or connection-pool sizing) was
-- responsible for observed request timeouts.
--
-- Setup:
--   ./scripts/seed_products.sh 10
--   export LOADTEST_PRODUCT_IDS="11,12,13,14,15,16,17,18,19,20"   (from its output)
--
-- Usage:
--   wrk -t4 -c20 -d60s -R50 -s scripts/mixed_workload_multi_product.lua http://127.0.0.1:8090

math.randomseed(os.time())

local ids_env = os.getenv("LOADTEST_PRODUCT_IDS") or "3"
local product_ids = {}
for id in string.gmatch(ids_env, "([^,]+)") do
  table.insert(product_ids, tonumber(id))
end

local get_headers = { ["Accept"] = "application/json" }
local post_headers = { ["Content-Type"] = "application/json" }

request = function()
  local pid = product_ids[math.random(1, #product_ids)]
  local r = math.random(1, 10)

  if r <= 5 then
    return wrk.format("GET", "/api/v1/products", get_headers)
  elseif r <= 9 then
    return wrk.format("GET", "/api/v1/inventories", get_headers)
  else
    local order_body = string.format([[
{
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "productId": %d,
  "quantity": 1
}
]], pid)
    return wrk.format("POST", "/api/v1/orders", post_headers, order_body)
  end
end

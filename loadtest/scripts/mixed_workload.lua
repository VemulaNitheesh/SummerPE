-- Mixed, weighted workload across all 3 services, simulating realistic
-- storefront traffic (mostly reads, some order writes). This is the
-- script used during the actual blue-green switch so wrk2's aggregate
-- throughput/latency numbers reflect what a real client would see.
--
-- Weighting: 50% GET /products, 40% GET /inventories, 10% POST /orders
--
-- Usage:
--   wrk -t4 -c20 -d60s -R50 -s scripts/mixed_workload.lua http://127.0.0.1:8090

math.randomseed(os.time())

local order_body = [[
{
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "productId": 3,
  "quantity": 1
}
]]

local get_headers = { ["Accept"] = "application/json" }
local post_headers = { ["Content-Type"] = "application/json" }

request = function()
  local r = math.random(1, 10)
  if r <= 5 then
    return wrk.format("GET", "/api/v1/products", get_headers)
  elseif r <= 9 then
    return wrk.format("GET", "/api/v1/inventories", get_headers)
  else
    return wrk.format("POST", "/api/v1/orders", post_headers, order_body)
  end
end

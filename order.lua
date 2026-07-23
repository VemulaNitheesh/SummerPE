wrk.method = "POST"

wrk.body = [[
{
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "productId": 3,
  "quantity": 1
}
]]

wrk.headers["Content-Type"] = "application/json"

#  Coffee Shop API with Tiered Rate Limiting

A Spring Boot REST API that demonstrates **rate limiting** using **Bucket4j**, with tiered access for different user types (`GUEST`, `STANDARD`, `PREMIUM`).

Live API: [https://coffee-shop-4jza.onrender.com](https://coffee-shop-4jza.onrender.com)

---

## Features
- Tiered **rate limiting** (Guest, Standard, Premium customers).
- **Bucket4j** in-memory implementation.
- **Custom headers** (`X-Customer-Id`, `X-Customer-Type`) to simulate different users.
- Endpoints for **menu, ordering, and rate-limit status**.
- Deployed on **Render**.

---

## API Endpoints

### 1. Menu
**GET** `/api/coffee/menu`
```bash
curl -X GET "https://coffee-shop-4jza.onrender.com/api/coffee/menu"
```

---

### 2. Place Order
**POST** `/api/coffee/order`  
Headers:
- `X-Customer-Id` → unique user ID
- `X-Customer-Type` → `GUEST` | `STANDARD` | `PREMIUM`

```bash
curl -X POST "https://coffee-shop-4jza.onrender.com/api/coffee/order"   -H "Content-Type: application/json"   -H "X-Customer-Id: guest-user-001"   -H "X-Customer-Type: GUEST"   -d '{ "coffeeType": "LATTE", "size": "MEDIUM", "quantity": 1 }'
```

Try sending **3 quick requests as a GUEST** — you’ll hit the rate limit after 2 requests (`429 Too Many Requests`).

---

### 3. Rate Limit Status
**GET** `/api/coffee/rate-limit-status`
```bash
curl -X GET "https://coffee-shop-4jza.onrender.com/api/coffee/rate-limit-status"   -H "X-Customer-Id: standard-user-001"   -H "X-Customer-Type: STANDARD"
```

Response Example:
```json
{
  "customerId": "standard-user-001",
  "customerType": "STANDARD",
  "remainingTokens": 9,
  "capacity": 10,
  "refillTimeInSeconds": 60
}
```

---

## Rate Limits per Tier
| Customer Type | Requests Allowed | Time Window |
|---------------|-----------------|-------------|
| **GUEST**     | 2 requests      | per minute  |
| **STANDARD**  | 10 requests     | per minute  |
| **PREMIUM**   | 50 requests     | per minute  |

---

## Tech Stack
- Spring Boot 3
- Bucket4j (Rate Limiting)
- Render (Deployment)  

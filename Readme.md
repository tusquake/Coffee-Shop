##  Coffee Shop API with Tiered Rate Limiting

This project implements a simple Spring Boot REST API for a coffee ordering system, featuring robust API rate limiting using the **Bucket4j** library to enforce differentiated service tiers.

---

##   Key Features

* **Tiered Rate Limiting:** Limits order placement based on the customer's tier.
* **Token Bucket Algorithm:** Uses Bucket4j (Token Bucket) for smooth and accurate consumption and refill logic.
* **Custom Header Authentication:** Uses `X-Customer-Id` and `X-Customer-Type` to identify users and apply the correct limits.
* **CORS Enabled:** Configured for cross-origin requests, allowing interaction with the provided HTML/JS frontend demo.
* **Order Management:** Endpoints for placing orders, retrieving menus, and checking order history/rate limit status.

---

## Rate Limit Configuration

The rate limits are set based on the customer's tier, defined in the service layer (`RateLimiterService` and corresponding `RateLimiterConfig`):

| Customer Type | Limit (Capacity & Refill Rate) | Description | HTTP Response |
| :--- | :--- | :--- | :--- |
| **GUEST** | **2 orders per minute** | Highly restricted access, used for anonymous or unregistered users. | **429 Too Many Requests** |
| **STANDARD** | **5 orders per minute** | Default limit for standard customers. | **429 Too Many Requests** |
| **VIP** | **20 orders per minute** | High-priority access for premium customers. | **429 Too Many Requests** |

---

## ⚙️ Technology Stack

* **Backend:** Java 17+
* **Framework:** Spring Boot 3
* **Rate Limiting:** `io.github.bucket4j`
* **Build Tool:** Maven

---

## 🚀 Getting Started

### Prerequisites

* JDK 17+
* Maven 3.6+

### 1. Build and Run the API

1.  Clone the repository and navigate to the project root.
2.  Build and run the Spring Boot application:
    ```bash
    ./mvnw spring-boot:run
    ```
The API will start on `http://localhost:8080`.

### 2. Run the Frontend Demo

1.  Open the provided `index.html` file in your web browser (using a tool like VS Code's Live Server, typically at `http://127.0.0.1:5500`).
2.  Ensure your backend is running, then use the frontend interface to:
    * Set the **Customer ID** (e.g., `test-001`).
    * Select the **Type** (e.g., `GUEST`).
    * Click "Place Order" repeatedly to observe the rate limit take effect (the 3rd request for a GUEST user should return HTTP 429).

---

##  API Endpoints

All base paths start with `http://localhost:8080/api/coffee`.

| Method | Endpoint | Description | Required Headers | Rate Limited? |
| :--- | :--- | :--- | :--- | :--- |
| `POST`| `/order` | Places a new coffee order. | `X-Customer-Id`, `X-Customer-Type` | **Yes** |
| `GET` | `/orders`| Retrieves order history for a customer. | `X-Customer-Id` | No |
| `GET` | `/rate-limit-status` | Checks remaining token quota. | `X-Customer-Id`, `X-Customer-Type` | No |
| `GET` | `/menu` | Retrieves the available coffee menu. | None | No |
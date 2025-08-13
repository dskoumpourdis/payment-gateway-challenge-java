# Payment Gateway Challenge – Java (Spring Boot)

This project is a **Payment Gateway** API implemented with **Java**, **Spring Boot**, and **Gradle**.  
It demonstrates an end-to-end payment processing flow, integration with a **bank simulator**, and includes **unit** and **integration tests**.

---

## 📋 Features
- REST API for processing payments and retrieving payment details
- Integration with a **bank simulator** that approves/declines based on card number rules
- In-memory persistence for storing processed payments
- Exception handling with structured error responses
- H2 database for integration testing

---

## 🏦 Bank Simulator Rules
The bank simulator determines its response based on the request:

1. **Missing required field** → `400 Bad Request` with error message.
2. **All fields present**:
  - Card ends with **odd digit** (`1,3,5,7,9`) → `200 OK` **Approved** (with random `authorization_code`)
  - Card ends with **even digit** (`2,4,6,8`) → `200 OK` **Declined**
  - Card ends with **zero** (`0`) → `503 Service Unavailable`

---

## 📂 Project Structure
```
src/
  main/
    java/com/checkout/payment/gateway/    # Application code
    resources/                            # Application configuration
  test/
    java/com/checkout/payment/gateway/    # Unit tests
  integrationTest/
    java/com/checkout/payment/gateway/    # Integration tests
    resources/application-test.properties # H2 DB config
```

---

## 🛠 Requirements
- **Java 17+**
- **Gradle 7+**
- **Docker** (optional, for running the simulator via `docker-compose`)

---

## 🚀 Running the Application

### 1. Start the Bank Simulator
The simulator is included in `docker-compose.yml`:

```bash
docker-compose up
```

### 2. Run the Spring Boot Application
```bash
./gradlew bootRun
```
The API will be available at:
```
http://localhost:8080
```

---

## 📡 API Endpoints

### **POST /payments**
Process a payment request.

**Request Example:**
```json
{
  "amount": 1000,
  "currency": "USD",
  "cardNumber": "4111111111111111",
  "expiryMonth": 12,
  "expiryYear": 2030,
  "cvv": "123"
}
```

**Response Example (Approved):**
```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "APPROVED",
  "authorizationCode": "a1b2c3d4"
}
```

---

### **GET /payments/{paymentId}**
Retrieve payment details by ID.

---

## 🧪 Running Tests

### **Unit Tests**
```bash
./gradlew test
```

### **Integration Tests** (H2 in-memory DB)
```bash
./gradlew integrationTest
```
Integration tests verify:
- Successful payment flow
- Decline flow
- Bank error handling
- Missing field validation
- Payment retrieval

---

## ⚙️ Tech Stack
- **Java 17**
- **Spring Boot**
- **Gradle**
- **H2 Database** (testing)
- **JUnit 5** + **AssertJ**
- **TestRestTemplate** for integration tests

---

## API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**

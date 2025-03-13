# BCNC Group Payments Test API

This project is a RESTful API built with **Spring Boot** to manage payments in the BCNC Group system. The API is
designed to facilitate payment validation and management, using automated tests to ensure proper functionality.

## Description

The API allows payment management operations, ensuring that data is properly validated through unit and integration
tests. This project uses **JUnit** for testing and **Spring Boot** as the main framework.

## Architecture
The architecture used in this project is Hexagonal Architecture (also known as Ports and Adapters). 
This approach decouples the core business logic from the external systems, allowing for better testability and maintainability.
The project is structured into different layers:

### Adapters
Adapters are responsible for interacting with external systems and frameworks. In this project, we have:

- `Inbound Adapters` (receiving input):
  - `PaymentController` (inside `adapter.in.rest`): Manages RESTful API requests related to payment processing.
  - `GRPCPaymentService` (inside `adapter.in.grpc`): Handles gRPC requests for payment services.
- `Outbound Adapters` (sending output):
  - `PaymentService` (inside `adapter.out.rest`): Calls external payment gateways and services.

### Application Layer
This layer contains the business logic and service orchestration:

- `config`: Configuration files.
- `dto`: Data Transfer Objects (DTOs) used for communication between layers.
- `mapper`: Utility classes for mapping between domain models and DTOs.

### Domain Layer
The core of the system, where business entities and rules are defined:

- `payment`: Contains payment-related domain models.
- `error`: Handles domain-specific errors, like invalid payment details or failed transactions.

### Ports
Ports define the interfaces for communication between the core domain and external adapters. In this project, we have:

- `Inbound Ports` (used by adapters to interact with the application):
  - `PaymentInPort` (inside `port.in.rest`): Defines REST-based interactions for payment processing. 
- `Outbound Ports` (used by the application to communicate with external systems):
  - `PaymentOutPort` (inside `port.out.rest`): Defines interactions with external payment gateways.

### Diagram
Below is a diagram illustrating the Hexagonal Architecture used in this project:

```mermaid
graph TD
;

%% Definition of subgroups
    subgraph Domain
        Payment
        Error
    end

    subgraph Application
        Config
        DTO
        Mapper
    end

    subgraph Ports
        subgraph Inbound
            PaymentInPort
        end
        subgraph Outbound
            PaymentOutPort
        end
    end

    subgraph Adapters
        subgraph InboundAdapters
            GRPCPaymentService
            PaymentController
        end
        subgraph OutboundAdapters
            PaymentService
        end
    end

%% Connections between layers
    Payment -->|Uses| DTO
    Error -->|Handles| Payment
    DTO -->|Mapped by| Mapper
    Mapper -->|Used by| Application
    Application -->|Uses| PaymentInPort
    Application -->|Uses| PaymentOutPort

%% Connections between ports and adapters
    PaymentInPort -->|Implemented by| PaymentController
    PaymentInPort -->|Implemented by| GRPCPaymentService
    PaymentOutPort -->|Implemented by| PaymentService

%% Connections between adapters and external systems
    GRPCPaymentService -->|gRPC| PaymentInPort
    PaymentController -->|REST| PaymentInPort
    PaymentService -->|REST| PaymentOutPort

```

## Requirements

- **JDK 21 or higher**
- **Maven 3.6 or higher**
- **Spring Boot 2.x**
- **JUnit 5**
- **H2 Database** (in-memory database for testing)
- **Docker** (if you want to run the service in a container)

## Running the Application (Docker)

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/edeldelgado90/bcnc-group-payments-test-api
   cd bcnc-group-payments-test-api
   ```

2. **Build and Run the application**:
    ```bash
    docker-compose up --build
    ```

```bash
mvn spring-boot:run
```

## Running the Application (Manual)

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/edeldelgado90/bcnc-group-payments-test-api
   cd bcnc-group-payments-test-api
   ```
2. **Build the Project**:
    ```bash
    mvn clean install
    ```
3. **Run the application**:
    ```bash
    mvn spring-boot:run
    ```

## Accessing the OpenAPI Documentation

Once the application is running, you can access the OpenAPI documentation through the following URL:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Database Information

The application inserts the following default data into the tables upon startup during the migration process:

### Brands

| id | name |
|----|------|
| 1  | ZARA |

### Products

| id    | name         | description                                                                                                    |
|-------|--------------|----------------------------------------------------------------------------------------------------------------|
| 35455 | JEANS MARINE | Jeans marine fit con cinturilla interior ajustable y cierre botón frontal. Bolsillos tipo plastrón en espalda. |

### Price List

| id | description                     |
|----|---------------------------------|
| 1  | Listado de Precios del 2020 - 1 |
| 2  | Listado de Precios del 2020 - 2 |
| 3  | Listado de Precios del 2020 - 3 |
| 4  | Listado de Precios del 2020 - 4 |

### Prices

| brand_id | start_date          | end_date            | price_list | product_id | priority | price | curr |
|----------|---------------------|---------------------|------------|------------|----------|-------|------|
| 1        | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 1          | 35455      | 0        | 35.50 | EUR  |
| 1        | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 2          | 35455      | 1        | 25.45 | EUR  |
| 1        | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 3          | 35455      | 1        | 30.50 | EUR  |
| 1        | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 4          | 35455      | 1        | 38.95 | EUR  |

## Database Schema Diagram

Below is a diagram representing the relationships between the tables using Mermaid syntax. You can render this using a
Mermaid-compatible viewer.

```mermaid
erDiagram
    brands {
        BIGINT id PK "primary key"
        STRING name "brand name"
    }

    products {
        BIGINT id PK "primary key"
        STRING name "product name"
        STRING description "product description"
        STRING photo "product photo"
    }

    price_list {
        BIGINT id PK "primary key"
        STRING description "price list description"
    }

    prices {
        BIGINT id PK "primary key"
        BIGINT brand_id FK "foreign key"
        TIMESTAMP start_date
        TIMESTAMP end_date
        BIGINT price_list FK "foreign key"
        BIGINT product_id FK "foreign key"
        INT priority
        DECIMAL price
        STRING curr "currency"
    }

    brands ||--o{ prices: has
    products ||--o{ prices: has
    price_list ||--o{ prices: contains
```

## REST API Endpoints

### Get Current Price

#### GET `/api/prices/current?product_id={id}&brand_id={id}&date={date}`

Curl Example:

```bash
curl -X GET http://localhost:8080/api/prices/current?product_id=35455&brand_id=1&date=2020-07-01T12:00:00 \
  -H "Content-Type: application/json"
```

### Get All Prices

#### GET `/api/prices/current?product_id={id}&brand_id={id}&date={date}`

Curl Example:

```bash
curl -X GET http://localhost:8080/api/prices/?page=0&size=10 \
 -H 'Accept-Encoding: application/json'
```

### Create a Price

#### POST `/api/prices`

Example Request Body:

```json
{
  "brandIdd": 1,
  "startDate": "2023-01-01T00:00:00",
  "endDate": "2023-12-31T23:59:59",
  "priceList": 1,
  "productId": 35455,
  "priority": 1,
  "price": 100.00,
  "curr": "EUR"
}
```

Curl Example:

```bash
curl -X POST http://localhost:8080/api/prices \
  -H "Content-Type: application/json" \
  -d '{
        "brandId": 1,
        "startDate": "2023-01-01T00:00:00",
        "endDate": "2023-12-31T23:59:59",
        "priceList": 1,
        "productId": 35455,
        "priority": 1,
        "price": 100.00,
        "curr": "EUR"
      }'
```

### Delete a Price

#### DELETE `/api/prices/{id}`

Curl Example:

```bash
curl -X DELETE http://localhost:8080/api/prices/1 \
  -H "Content-Type: application/json"
```

## gRPC Usage

To use gRPC, ensure you have grpcurl installed and use the following command to invoke the service:

```bash
grpcurl -plaintext -d '{
    "productId": 35455,
    "brandId": 1,
    "date": "2020-06-14T20:32:05Z"
}' localhost:9090 prices.PriceService/getCurrentPriceByProductAndBrand
```

## Running Tests

To run unit and integration tests:

```bash
mvn test
```

# Online Marketplace

Microservices-based online marketplace built with Java/Spring Boot.

## Architecture

The application consists of 5 independent services:

| Service | Port | Tech | Description |
|---|---|---|---|
| **API Gateway** | 8080 | Spring Cloud Gateway, Redis | Routing, rate limiting (Token Bucket) |
| **User Service** | 8081 | Spring Boot, PostgreSQL | Registration, authentication, JWT tokens |
| **Product Service** | 8082 | Spring Boot, MongoDB, Redis | Product catalog, search, caching |
| **Order Service** | 8083 | Spring Boot, PostgreSQL, Kafka | Order management, async post-processing |
| **Notification Service** | 8084 | Spring Boot, Kafka | Event-driven notifications |

```
                          ┌──────────────┐
                          │  API Gateway │
                          │   (8080)     │
                          └──────┬───────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                   │
     ┌────────▼──────┐  ┌───────▼───────┐  ┌───────▼───────┐
     │ User Service  │  │Product Service│  │ Order Service │
     │   (8081)      │  │   (8082)      │  │   (8083)      │
     │  PostgreSQL   │  │   MongoDB     │  │  PostgreSQL   │
     └───────────────┘  │   Redis       │  │   Kafka ──────┼──┐
                        └───────────────┘  └───────────────┘  │
                                                              │
                                                    ┌─────────▼─────────┐
                                                    │Notification Service│
                                                    │     (8084)        │
                                                    └───────────────────┘
```

## Tech Stack

- **Backend:** Java 17, Spring Boot 3, Spring Cloud Gateway
- **Security:** Spring Security, JWT (stateless authentication between services)
- **Databases:** PostgreSQL, MongoDB
- **Caching:** Redis (product caching + rate limiting)
- **Messaging:** Apache Kafka (async event-driven communication)
- **Containerization:** Docker, Docker Compose
- **CI/CD:** GitHub Actions
- **Concurrency:** Optimistic Locking, @Async with thread pools

## Key Features

- **API Gateway** with IP-based rate limiting (Redis Token Bucket algorithm)
- **JWT authentication** with userId/role claims passed between services
- **Optimistic Locking** on Product entity to prevent race conditions on stock
- **Async post-processing** for orders — client gets instant response, stock reduction and Kafka events happen in background
- **Redis caching** for product catalog with cache eviction on updates
- **Kafka event streaming** — Order Service publishes events, Notification Service consumes
- **Spring Profiles** — separate dev/prod configurations
- **Global Exception Handling** with consistent error responses across all services
- **Pagination** for product listings

## Running Locally

### Prerequisites
- Docker & Docker Compose

### Start all services
```bash
docker-compose up -d
```

### API Endpoints

**User Service:**
- `POST /api/v1/auth/register` — Register
- `POST /api/v1/auth/login` — Login (returns JWT)

**Product Service:**
- `GET /api/v1/products` — All active products
- `GET /api/v1/products/{id}` — Product by ID
- `GET /api/v1/products/search?keyword=` — Search
- `GET /api/v1/products/page?page=0&size=20` — Paginated

**Order Service (requires JWT):**
- `POST /api/v1/orders` — Create order
- `GET /api/v1/orders` — My orders
- `GET /api/v1/orders/{id}` — Order by ID

## Roadmap

- [ ] Email notifications in Notification Service
- [x] Integration tests with Testcontainers
- [x] Monitoring with Grafana + Prometheus
- [x] S3-compatible file storage (MinIO) for product images
- [x] Idempotency keys for order creation
- [ ] Saga Pattern for distributed transactions
- [ ] Distributed tracing with Micrometer
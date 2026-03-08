# springboot-azure-logs-demo

Spring Boot 3 + Postgres + CRUD + validations + global exception handler + request/response logging (good for Azure App Service logs).

## Run locally (Postgres)
1) Start Postgres (example docker):
   docker run --name demo-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=demo_db -p 5432:5432 -d postgres:16

2) Run app:
   mvn spring-boot:run

Health:
- GET http://localhost:8080/actuator/health

## Postman endpoints
- Customers: /api/customers
- Products: /api/products
- Orders:   /api/orders

Each request returns `X-Trace-Id` header, also logged as `traceId=...` in console logs.

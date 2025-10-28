# Hotel Booker

## Prerequisites
- Java 17+

## Build and Run

Необходимо запускать в разных процессах

```bash
./gradlew :api-gateway:bootrun
./gradlew :booking-service:bootrun
./gradlew :eureka-server:bootrun
./gradlew :hotel-service:bootrun
```

## JWT Configuration

Для конфигурации JWT ключа необходимо указать переменную в свойстве `security.jwt.secret` внутри файлов:
- `api-gateway/src/main/resources/application.yml`
- `hotel-service/src/main/resources/application.yml`
- `booking-service/src/main/resources/application.yml`

## How to Use

1) Регистрация пользователя
    ```bash
    curl -X POST http://localhost:8080/auth/register \
      -H 'Content-Type: application/json' \
      -d '{"username":"user1","password":"pass"}'
    ```

2) Вход и получение JWT
    ```bash
    TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
      -H 'Content-Type: application/json' \
      -d '{"username":"user1","password":"pass"}' | jq -r .access_token)
    ```

3) Создание отеля и номера (нужны права admin — можно зарегистрировать админа с `{"admin":true}` и войти под ним):
    ```bash
    curl -X POST http://localhost:8080/hotels \
      -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
      -d '{"name":"Hotel A","city":"Moscow","address":"Red Square, 1"}'
    ```

4) Подсказки по номерам
    ```bash
    curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/bookings/suggestions
    ```

5) Создание бронирования (идемпотентный `requestId` обязателен):
    ```bash
    curl -X POST http://localhost:8080/bookings \
      -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
      -d '{"roomId":1, "startDate":"2025-10-20", "endDate":"2025-10-22", "requestId":"req-123"}'
    ```

6) История бронирований пользователя
    ```bash
    curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/bookings
    ```

## Endpoints
- Аутентификация (Booking):
  - POST `/auth/register`
  - POST `/auth/login`
- Бронирования (Booking):
  - GET `/bookings`
  - POST `/bookings`
  - GET `/bookings/suggestions`
  - GET `/bookings/all`
- Пользователи (Booking, admin):
  - GET `/admin/users`, GET `/admin/users/{id}`, PUT `/admin/users/{id}`, DELETE `/admin/users/{id}`
- Отели и номера (Hotel):
  - GET `/hotels`, GET `/hotels/{id}`, POST `/hotels`, PUT `/hotels/{id}`, DELETE `/hotels/{id}`
  - GET `/rooms/{id}`, POST `/rooms`, PUT `/rooms/{id}`, DELETE `/rooms/{id}`
  - POST `/rooms/{id}/hold`
  - POST `/rooms/{id}/confirm`
  - POST `/rooms/{id}/release`
- Статистика (Hotel):
  - GET `/stats/rooms/popular`

## Swagger / OpenAPI
- `http://localhost:<booking-port>/swagger-ui.html`
- `http://localhost:<hotel-port>/swagger-ui.html`
- `http://localhost:8080/swagger-ui.html`

## Tests

```bash
./gradlew test
```

# Hotel booking system

Это многомодульный приложение, в котором находятся 4 микросервиса:
- `booking-service` (управление бронированиями и пользователями)
- `eureka-service` (динамическое обнаружение сервисов)
- `gateway-service` (проксирование трафика)
- `hotel-service` (управление Отелем и Номерами)

Все сервисы используют встроенную БД H2. Взаимодействие между сервисами выполняется при помощи протокола HTTP как 
последовательность локальных транзакций (без учета глобальных распределённых транзакций).

## Возможности приложения
- Регистрация и вход пользователей (JWT) через booking-service
- Создание бронирований с двухшаговой согласованностью между сервисами booking-service и hotel-service посредством использования статусов бронирования (PENDING, CONFIRMED, CANCELLED)
- Идемпотентность запросов с `requestId`
- Создание бронирований с автовыбором Номера
- Администрирование всех сущностей приложения с помощью REST API

## Технологический стек
- Java 17
- Spring Boot 3.5.9
- Spring Cloud 
- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
- H2 Database
- Spring Data JPA
- Spring Security
- JWT Authentication
- Lombok
- MapStruct
- Maven
- SpringDoc OpenAPI (Swagger)
- JUnit 5 / Spring Boot Test

## Архитектура и порты
- `booking-service,` порт 8082, регистрируется в eureka-service под названием **BOOKING-SERVICE**
- `eureka-service,` порт 8761
- `gateway-service,` порт 8080, регистрируется в eureka-service под названием **GATEWAY-SERVICE**
- `hotel-service,` порт 8081, регистрируется в eureka-service под названием **HOTEL-SERVICE**	

## Запуск приложения
Для запуска приложения необходимо: 
1. Склонировать репозиторий https://github.com/KlimovMaksim/hotel-booking-system
2. Собраться приложение с использованием Maven
3. Последовательно запустить eureka-service, hotel-service, booking-service, gateway-service

## Конфигурация JWT
Для демонстрации используется симметричный ключ HMAC, значение которого задаётся свойством `app.jwt.secret`, а время 
жизни свойством `app.jwt.expiration` сроком в 1 час в виде миллисекунд.
Указанные свойства можно найти в конфигурационных файлах ***application.yml*** в ресурсах модулей `booking-service` и 
`hotel-service` соответственно.

## API
Эндпоинты надо использовать через gateway-service. Для обращения к эндпоинтам booking-service - 
`localhost:8080/api/bookings`, для hotel-service - `localhost:8080/api/hotels`

#### booking-service
- POST — /user — Создать нового пользователя, только **ADMIN**
- PATCH — /user — Обновить данные пользователя, только **ADMIN**
- DELETE — /user/{id} — Удалить пользователя, только **ADMIN**
- POST — /auth/register — Регистрация нового пользователя
- POST — /auth/login — Аутентификация пользователя
- GET — /booking — Получить список всех бронирований, только **ADMIN**
- GET — /booking/by-username/{username} — Получить список бронирований по имени пользователя
- POST — /booking — Создать новое бронирование
- GET — /booking/offers — Получить доступные предложения номеров
- GET — /booking/{requestId} — Найти бронирование по идентификатору запроса
- DELETE — /booking/{requestId} — Отменить бронирование

#### hotel-service
- GET — /hotels — Получить список всех отелей
- POST — /hotels — Создать новый отель, только **ADMIN**
- POST — /rooms — Создать новый номер, только **ADMIN**
- GET — /rooms/recommend — Получить рекомендованные номера
- GET — /rooms — Получить все доступные номера
- POST — /rooms/{id}/confirm-availability — Подтвердить доступность номера
- POST — /rooms/{id}/release/{requestId} — Освободить номер

## Swagger
Для более удобного использования API приложения:
- booking-service — `http://localhost:8082/swagger-ui/index.html`
- hotel-service — `http://localhost:8081/swagger-ui/index.html`

## Алгоритм использования приложения
В hotel-service при запуске приложения инициализируются тестовые данные — 1 Отель и 2 Номера
Порядок вызовов API для взаимодействия с приложением
1. Регистрация нового пользователя
2. Аутентификация пользователя — получаем jwt токен
3. Получить доступные предложения номеров
4. Создать новое бронирование
5. Получить список бронирований по имени пользователя

## Тестирование
Тестами покрыт весь функционал модулей **booking-service** и **hotel-service**. Запустить тесты можно при сборке проекта или
отдельно запуская каждый тестовый класс

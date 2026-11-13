# Backend API

Java 17 / Spring Boot 3.5.16 project. Flyway manages the database schema, and Spring Data JPA maps database rows to Java objects. Migrations V1-V3 create the hotel catalogue and booking tables and seed sample hotels.

Configure the database and environment variables first using `../database/README.md`. Then run from this directory in PowerShell or the VS Code terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

Run backend tests with:

```powershell
.\mvnw.cmd test
```

The project includes Spring Web, Spring Data JPA, the MySQL driver, Flyway migrations, validation and Spring Boot Test. Automated tests use isolated H2 databases in MySQL compatibility mode.

## Hotel catalogue API

- `GET /api/hotels` — list all hotels, sorted by name.
- `GET /api/hotels?location=goa` — search by a case-insensitive location fragment.
- `GET /api/hotels/{id}` — retrieve one hotel's details; unknown IDs return HTTP 404.

Example response from the list endpoint:

```json
[
  {
    "id": 1,
    "name": "Aurora Bay Resort",
    "location": "Goa",
    "description": "A relaxed coastal resort close to the beach, with a quiet garden courtyard.",
    "amenities": ["Beach access", "Breakfast", "Swimming pool", "Wi-Fi"],
    "nightlyPrice": 6500.00,
    "roomCapacity": 24
  }
]
```

## Availability and booking API

- `GET /api/hotels/{id}/availability?checkIn=2026-12-10&checkOut=2026-12-12` — returns nights, capacity and available rooms.
- `POST /api/bookings` — creates a booking. Required JSON fields: `hotelId`, `customerName`, `customerEmail`, `checkIn`, and `checkOut`. Any client-supplied total is ignored; the response total comes from the backend.
- `POST /api/bookings/lookup` — JSON body with `bookingReference` and `customerEmail`.
- `POST /api/bookings/{reference}/cancel` — JSON body with `customerEmail`; repeat cancellation safely returns `CANCELLED`.

Example create request:

```json
{
  "hotelId": 1,
  "customerName": "Asha Rao",
  "customerEmail": "asha@example.com",
  "checkIn": "2026-12-10",
  "checkOut": "2026-12-12"
}
```

Check-in uses `BOOKING_TIME_ZONE` (default `Asia/Kolkata`). Stays are half-open `[check-in, check-out)`, so adjacent stays are permitted. See `docs/DESIGN.md` for booking and database locking details.

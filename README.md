# Hotel Booking Portal

A full-stack hotel booking portal built as a technical assignment. Guests can browse 15 sample hotels across five cities, check availability, reserve a room, retrieve or cancel a booking, and compare up to three hotels.

## Features

- Hotel catalogue with descriptions, amenities, prices, and photos.
- Search hotels by city and check availability for check-in/check-out dates.
- Backend-calculated booking totals and unique booking references.
- Booking retrieval and cancellation using the reference and customer email.
- Room-capacity checks with transactional locking during reservation.
- Hotel comparison by city, amenities, and nightly price.
- Versioned MySQL schema and seed data managed with Flyway.

## Technology

- Java 17, Spring Boot 3.5.16, Spring Data JPA, Maven
- React 19, Vite, JavaScript
- MySQL 8, Flyway
- JUnit 5, Spring Boot Test, H2 for automated integration tests

## Project layout

- `backend/` — Spring Boot REST API and automated tests.
- `frontend/` — React application and hotel images in `public/images/hotels/`.
- `database/migrations/` — ordered schema, seed, and catalogue data migrations.
- `docs/DESIGN.md` — architecture, database, APIs, booking rules, trade-offs, and limitations.
- `docs/ASSIGNMENT_CHECKLIST.md` — assignment requirements and verification status.
- `docs/screenshots/` — application screenshots/demo evidence.

## Prerequisites

- JDK 17
- Node.js and npm
- MySQL Server 8
- Git (optional for running locally)

## Database setup

1. Start MySQL Server.
2. In MySQL Workbench, create the database:

   ```sql
   CREATE DATABASE hotel_booking
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_0900_ai_ci;
   ```

3. In a PowerShell terminal, move into `backend/` and set connection variables. The password prompt is masked and the secret stays in the terminal session:

   ```powershell
   $mysqlCredential = Get-Credential -UserName "root" -Message "Enter your MySQL username and password"
   $env:DB_HOST = "localhost"
   $env:DB_PORT = "3306"
   $env:DB_NAME = "hotel_booking"
   $env:DB_USERNAME = $mysqlCredential.UserName
   $env:DB_PASSWORD = $mysqlCredential.GetNetworkCredential().Password
   ```

   Replace `root` if you use another MySQL account. Do not put a real password in a project file.

On startup, Flyway applies pending migrations. The migrations create the hotel and booking tables and seed 15 sample hotels: three each in Goa, Manali, Mumbai, Udaipur, and Jaipur. See [database setup details](database/README.md).

## Run locally

Start the backend from `backend/` in the same PowerShell terminal where the database variables are set:

```powershell
.\mvnw.cmd spring-boot:run
```

In a second terminal, install frontend dependencies and start Vite from `frontend/`:

```powershell
npm install
npm run dev
```

Open the local URL Vite prints (usually `http://localhost:5173`). Vite forwards `/api` calls to `http://localhost:8080`. The API base can be overridden with `VITE_API_BASE_URL` in an untracked `.env.local`; the checked-in example contains only a safe placeholder.

## API overview

- `GET /api/hotels` — list hotels.
- `GET /api/hotels?location=goa` — search hotels by city/location fragment.
- `GET /api/hotels/{id}` — hotel details.
- `GET /api/hotels/{id}/availability?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD` — availability and night count.
- `POST /api/bookings` — create a booking from hotel, guest, and stay fields.
- `POST /api/bookings/lookup` — retrieve booking using reference and email.
- `POST /api/bookings/{reference}/cancel` — cancel using the reference and email.

See [backend API details and examples](backend/README.md).

## Verification

From `backend/`:

```powershell
.\mvnw.cmd clean verify
```

From `frontend/`:

```powershell
npm run build
```

The backend suite uses isolated H2 databases in MySQL compatibility mode. Its simultaneous-last-room test validates application locking with H2; MySQL-specific concurrency behavior has not been independently load-tested. See [assignment verification status](docs/ASSIGNMENT_CHECKLIST.md).

## Demo images

See the [hotel catalogue screenshot](docs/screenshots/hotel-catalogue.png) for the current portal appearance.

## Scope and limitations

This assignment does not include authentication, payment processing, an admin panel, or cloud deployment. Hotels and room capacities are sample data. Booking dates use `Asia/Kolkata` by default and can be configured through `BOOKING_TIME_ZONE`. Image files are served by the frontend and are mapped to hotel names; they are not stored in MySQL.

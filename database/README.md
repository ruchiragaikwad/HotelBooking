# Database setup

The application uses **Flyway** as its only schema-management tool. Versioned migrations live in `migrations/`; Maven packages them into the backend so Flyway applies them when Spring Boot starts. Migration `V1` creates the hotel tables, `V2` inserts five sample hotels and their amenities, and `V3` creates the bookings table and its indexes and constraints. Keeping seed data in a versioned migration makes it reproducible on a new database without introducing a second schema/seed mechanism.

## Create the local database

In MySQL Workbench, connect to your local MySQL 8 server and run:

```sql
CREATE DATABASE hotel_booking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

The database user configured for the application must be allowed to create and change tables in `hotel_booking`. Do not put a real password in this repository.

## Configure the backend

In PowerShell, open the project’s `backend` folder. Set the connection values for that terminal session; the password prompt is masked and the password is not written to a project file:

```powershell
$mysqlCredential = Get-Credential -UserName "root" -Message "Enter your MySQL username and password"
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "hotel_booking"
$env:DB_USERNAME = $mysqlCredential.UserName
$env:DB_PASSWORD = $mysqlCredential.GetNetworkCredential().Password
```

Replace `root` with the MySQL username you intend to use if needed. Start the backend in the same terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

On startup, Flyway applies any pending migration and records it in `flyway_schema_history`. Hibernate checks that the database matches the entity mapping; it does not create or change tables. The connection values in `.env.example` are placeholders for reference only; Spring Boot does not load that file automatically. If you already applied V1 and V2, starting the app after Phase 4 applies only V3 and preserves the existing schema and hotel data.

## Automated persistence test

From `backend/`, run `.\mvnw.cmd test`. The tests use isolated in-memory H2 databases in MySQL compatibility mode. They apply the migrations and cover hotel persistence, booking totals, availability, cancellation, date overlap and concurrent attempts to book the last room. The concurrency test uses H2 rather than MySQL, so it does not replace a MySQL-specific locking test. Tests do not need a running MySQL server or personal credentials.

# Hotel Booking Portal

A beginner-friendly full-stack hotel booking assignment. The application is being built in phases; this phase provides the backend and frontend project skeletons only.

## Technology
- Java 17, Spring Boot 3.5.16 and Maven
- React 19, Vite and JavaScript
- MySQL 8 (database integration is planned for a later phase)

Spring Boot 3.5.16 requires Java 17 or newer. See the [Spring Boot system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

## Folders
- `backend/` — Spring Boot REST API project
- `frontend/` — React application built with Vite
- `database/` — planned schema migrations and sample data
- `docs/` — assignment requirements, design notes, phase tracking and demo materials

## Prerequisites
- JDK 17
- Node.js and npm
- MySQL 8 for later database phases

## Set up MySQL

Create the `hotel_booking` database in MySQL Workbench using the SQL in `database/README.md`. The database migration runs when the backend starts. Set connection values as environment variables in the same PowerShell terminal; the README in `database/` shows a masked password prompt.

## Run the applications

In PowerShell or the VS Code terminal, start the backend from `backend/` after configuring MySQL:

```powershell
.\mvnw.cmd spring-boot:run
```

Start the frontend in a second terminal from `frontend/`:

```powershell
npm install
npm run dev
```

Vite prints the local URL to open in your browser. The applications do not yet implement hotel or booking features; those are added in later approved phases.

## Hotel catalogue API

After starting the backend, use `GET http://localhost:8080/api/hotels` to list the five seeded hotels. Add `?location=goa` to filter by a case-insensitive location fragment, or request a detail record with `GET http://localhost:8080/api/hotels/1`. Availability and booking endpoints are documented in `backend/README.md`.

## Configuration and secrets

Never put real credentials in source files or commit them. `.env.example` contains placeholders only. Local `.env` files and Spring local configuration files are ignored by Git.

## Development workflow

Review the project guide in `docs/Hotel_Booking_Portal_Codex_Implementation_Guide.docx` and `docs/PHASE_TRACKER.md`. Complete and review one phase at a time before approving the next.

# Design and Implementation Notes

## 1. Architecture

The browser runs a React application built with Vite. React gets hotel and booking data through the small API module in `frontend/src/services/api.js`; page components do not call the backend directly. Vite forwards `/api` requests to the Spring Boot service during local development.

Spring Boot exposes JSON REST endpoints. Controllers translate HTTP requests into service calls. Services enforce booking rules and coordinate transactions. Spring Data repositories query and persist JPA entities. DTO records define the request and response formats, so database entities are not exposed directly to clients. Flyway applies SQL migrations during backend startup.

Typical booking request flow:

1. React submits hotel ID, guest name/email, and check-in/check-out dates.
2. `BookingController` validates the request DTO and calls `BookingService`.
3. The service validates dates and locks the selected hotel row.
4. It counts active overlapping bookings and rejects the request if capacity is full.
5. It calculates nights and total using the hotel’s current nightly price, stores a price snapshot, and saves the booking.
6. The API returns a unique reference and authoritative total; React displays that response.

## 2. Database design

Flyway migration files in `database/migrations/` define the schema and sample data:

- V1 creates `hotels` and `hotel_amenities`.
- V2 seeds the first five sample hotels.
- V3 creates `bookings` and its constraints/index.
- V4 adds two hotels per existing city, resulting in 15 hotels (three in each of Goa, Manali, Mumbai, Udaipur, and Jaipur).
- V5 expands the descriptions of all 15 sample hotels.

`hotels` stores a hotel’s name, city/location, description, nightly price, and room capacity. `hotel_amenities` stores one amenity per hotel row; `(hotel_id, amenity)` is its primary key and `hotel_id` references `hotels` with cascade delete.

`bookings` stores a unique 36-character reference, the hotel foreign key, customer name/email, stay dates, number of nights, nightly price snapshot, total, status, and timestamps. Constraints require valid date order, positive nights and prices, and a supported status. An index on `(hotel_id, status, check_in, check_out)` supports availability queries. Each booking reserves exactly one room.

Hotel pictures are static frontend assets in `frontend/public/images/hotels/`, mapped to hotel names in `frontend/src/App.jsx`. They are not stored in the database or uploaded to the API.

## 3. REST API

All routes below are prefixed with `/api`.

| Method and route | Purpose |
| --- | --- |
| `GET /hotels` | List hotels, alphabetically. |
| `GET /hotels?location=goa` | Case-insensitive location fragment search. |
| `GET /hotels/{id}` | Read one hotel. Unknown IDs return 404. |
| `GET /hotels/{id}/availability?checkIn=2026-12-10&checkOut=2026-12-12` | Return night count, capacity, booked rooms, and available rooms. |
| `POST /bookings` | Create one-room booking. |
| `POST /bookings/lookup` | Retrieve a booking using reference and email. |
| `POST /bookings/{reference}/cancel` | Cancel using the reference and email. |

Booking request example:

```json
{
  "hotelId": 1,
  "customerName": "Asha Rao",
  "customerEmail": "asha@example.com",
  "checkIn": "2026-12-10",
  "checkOut": "2026-12-12"
}
```

The create response contains `bookingReference`, `hotelId`, `hotelName`, guest fields, `checkIn`, `checkOut`, `nights`, `nightlyPrice`, `totalAmount`, `status`, and timestamps. The server does not accept a client total as authoritative.

Lookup request:

```json
{"bookingReference":"<reference>","customerEmail":"asha@example.com"}
```

Cancellation request body:

```json
{"customerEmail":"asha@example.com"}
```

The response DTO for a booking includes its reference, hotel and guest details, stay dates, night count, stored nightly price and total, status, creation time, and cancellation time. Unknown reference and email mismatch share a not-found response to avoid revealing whether a reference exists.

Errors use a consistent JSON object: `timestamp`, `status`, `error`, `message`, and `path`. Stack traces and credentials are not returned.

## 4. Booking and availability rules

- Dates use `BOOKING_TIME_ZONE`, default `Asia/Kolkata`. Check-in may be today or later in that zone; checkout must be strictly later.
- A stay is the half-open interval `[check-in, check-out)`. Bookings overlap if `existing.checkIn < requested.checkOut AND existing.checkOut > requested.checkIn`; a checkout date may therefore equal another guest’s check-in date.
- Nights are the date difference. The backend calculates `nights × nightly price` using `BigDecimal`; browser estimates are informational.
- Booking creation is transactional and obtains a pessimistic write lock on the hotel row before counting overlaps and inserting. Concurrent attempts for that hotel serialize while checking its remaining capacity.
- Availability counts only `CONFIRMED` overlapping bookings. Cancellation changes status to `CANCELLED`, so the room is released. Repeated cancellation is idempotent.
- A UUID booking reference is uppercased and protected by a unique database constraint.
- The booking stores a nightly-price snapshot, so future hotel price changes do not alter an existing reservation total.

The automated simultaneous-last-room test uses H2 in MySQL compatibility mode. It verifies the service’s locking behavior on H2; MySQL-specific contention has not been separately load-tested.

## 5. Comparison feature

Guests can select up to three hotels and compare location, nightly price, and amenities in one responsive table. It uses the catalogue data already in browser memory, needs no extra API route, and does not persist a comparison or alter booking state. Selection can be removed individually or cleared.

## 6. Design trade-offs

Availability is calculated from booking rows instead of maintaining a second daily inventory counter. This avoids keeping duplicated capacity state synchronized during booking and cancellation. Locking one hotel row is easy to explain and protects capacity, but also serializes bookings for that hotel even when requested dates do not overlap. This is acceptable for the scale of the assignment.

The comparison list is frontend-only because all compared fields are already available from the hotel catalogue API. Images use static public files and an explicit mapping in the frontend, avoiding a schema migration for assets.

## 7. Limitations and future improvements

- The sample hotels and descriptions are fictional demonstration data; verify image reuse rights and attribution before public release.
- The automated concurrency test uses H2 rather than a dedicated MySQL integration environment.
- Comparison selection is not persisted after a page reload.
- Hotel images are associated by frontend name mapping, so a hotel rename requires updating that mapping.
- Authentication, online payment, administration, email notifications, deployment, and production monitoring are outside assignment scope.
- Time spent was not tracked reliably during development, so no precise duration is claimed.
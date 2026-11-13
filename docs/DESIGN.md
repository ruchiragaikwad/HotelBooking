# Design Document

## 1. Architecture
TODO: Describe frontend, backend layers, and request flow.

## 2. Database design
TODO: Tables, relationships, constraints, and indexes.

## 3. REST API design
TODO: Endpoints and request/response examples.

Implemented catalogue and booking endpoints:

- `GET /api/hotels` and `GET /api/hotels?location=goa` list and search hotels.
- `GET /api/hotels/{hotelId}` returns a hotel detail DTO.
- `GET /api/hotels/{hotelId}/availability?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD` returns capacity for a stay.
- `POST /api/bookings` creates a one-room booking; the response contains its reference and backend-calculated total.
- `POST /api/bookings/lookup` accepts the booking reference and customer email in JSON.
- `POST /api/bookings/{reference}/cancel` accepts the customer's email in JSON.

Lookup and cancellation send email in the request body rather than a URL query string. Errors use a shared JSON shape with timestamp, HTTP status, error label, message and request path.

## 4. Booking and availability logic

- Booking dates use `BOOKING_TIME_ZONE`, defaulting to `Asia/Kolkata`. Check-in may be today or later in that configured zone; check-out must be later than check-in.
- Stays use the half-open interval `[check-in, check-out)`. Existing and requested stays overlap when `existing.checkIn < requested.checkOut AND existing.checkOut > requested.checkIn`. A checkout on the next guest's check-in date is allowed.
- Availability counts only `CONFIRMED` bookings that overlap the requested dates, then subtracts that count from hotel room capacity. Cancelled bookings no longer consume a room.
- Nights are the date difference. The backend multiplies nights by the hotel's nightly price using `BigDecimal`. Each booking stores the nightly price snapshot so later catalogue price changes do not change existing totals.
- Booking creation runs in a transaction and locks the hotel row with a pessimistic write lock before checking capacity and inserting the booking. This serializes booking attempts for the same hotel while they decide whether the last room remains available.
- Cancellation verifies both reference and email, changes status to `CANCELLED`, and is idempotent: repeating it returns the already-cancelled booking.
- The automated last-room concurrency test uses H2 in MySQL compatibility mode. MySQL-specific locking behavior still merits a manual or MySQL integration run.

## 5. Design trade-offs and assumptions

- Availability is calculated from booking rows instead of maintaining a separate mutable daily room counter. This avoids keeping another value in sync with bookings and cancellations.
- Locking a hotel row is simple and protects all booking requests for that hotel, though it serializes reservations for different date ranges at the same hotel.
- The default booking timezone is India Standard Time (`Asia/Kolkata`) and can be changed with `BOOKING_TIME_ZONE`.
- Booking lookup and cancellation return the same not-found response for an unknown reference and a mismatched email, avoiding a response that reveals whether a reference exists.

## 6. Surprise feature
TODO: Describe the implemented feature and its value.

## 7. Limitations and future improvements
TODO.

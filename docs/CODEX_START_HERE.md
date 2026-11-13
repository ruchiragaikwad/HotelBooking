# Codex Instructions — Hotel Booking Portal

Start with **Phase 0 only**. Inspect the workspace and installed tools; do not modify files during Phase 0. Do not begin another phase without explicit approval.

## Working rules
- Inspect existing files before changing anything.
- Preserve existing work; ask before destructive or broad changes.
- Explain code and decisions in beginner-friendly language.
- Run relevant tests after every implementation phase.
- Report exact commands and actual results. Never invent test results or claim unrun tests passed.
- Stop after each phase and request approval.

## Required functionality
- At least five sample hotels with name, location, description, amenities, and nightly price.
- Search by location and check-in/check-out dates.
- Hotel details and room availability.
- Book one room with customer name and email.
- Backend calculates total = nights × nightly price.
- Unique booking reference and confirmation.
- Retrieve and cancel using booking reference plus email.
- Check-out must be after check-in; check-in cannot be in the past.
- Prevent overbooking; cancellation releases availability.
- Document overlapping-stay assumptions.
- Include one useful surprise feature.
- Persist hotels, availability, and bookings in MySQL.
- Validate on backend and return consistent API errors.
- Automated tests for booking totals and availability.
- Keep secrets out of source control.

## Booking rule
Treat stays as half-open intervals `[check-in, check-out)`. Two stays overlap if:
`existing.checkIn < requested.checkOut AND existing.checkOut > requested.checkIn`.

The backend is authoritative for price and availability. Protect the availability check and reservation write with a transaction and suitable database locking. Test simultaneous attempts to reserve the last available room.

## Architecture
Use clear controller, service, repository, entity, DTO, and exception responsibilities. Do not expose persistence entities directly. Keep frontend API calls in a dedicated service layer.

## Completion report
Use the required report format in `docs/PHASE_TRACKER.md`, update the tracker only when work is genuinely complete, and ask for approval before moving on.

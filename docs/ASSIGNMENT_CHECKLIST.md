# Assignment Completion Checklist

## Hotel catalogue

- [x] At least five hotels; currently 15 sample hotels with three each in Goa, Manali, Mumbai, Udaipur, and Jaipur.
- [x] Hotel name, location, expanded description, amenities, nightly price, room capacity, and matching card photo are available.
- [x] Location search, date selection, hotel details, and availability are implemented.

## Booking and correctness

- [x] One-room booking collects customer name/email and dates.
- [x] Backend computes authoritative nights and total, returns a unique reference, and snapshots the nightly price.
- [x] Lookup and cancellation require booking reference and email.
- [x] Cancellation is idempotent and releases capacity.
- [x] Past check-in and invalid checkout are rejected.
- [x] Half-open overlapping-stay rule is implemented; adjacent stays are allowed.
- [x] Capacity is checked transactionally under a hotel-row lock.
- [x] API errors have a consistent shape without stack traces.
- [x] Backend integration and concurrency tests pass using H2 in MySQL compatibility mode.
- [x] Hotel comparison supports up to three selections, removal, and clear behavior; pure selection-rule tests are included.
- [x] Hotel imagery is static frontend content and is not stored in MySQL.

## Final checks

- [x] Root README, backend/frontend/database setup instructions, and DESIGN.md are present and updated.
- [x] Desktop catalogue screenshot is present in `docs/screenshots/hotel-catalogue.png`.
- [x] Backend `cd backend; .\mvnw.cmd clean verify` passed after V5 was added: 21 tests, 0 failures, 0 errors, 0 skipped.
- [x] Frontend `cd frontend; npm test` passed: 4 tests, 0 failures; `npm run build` completed successfully.
- [x] `.gitignore` excludes local environment values, credentials, build outputs, and dependencies. Expected paths were confirmed with `git check-ignore`; the tracked-file scan found no credential files or private-key files. The only password-like matches were documented placeholder values in `.env.example` and Maven wrapper variable references.
- [ ] Confirm that every supplied hotel photo is licensed for redistribution and add attribution if required.
- [ ] Run a final browser pass on desktop and mobile after the V5 description migration is applied to the user's MySQL database. Automated screenshot review used an isolated H2 database; the live MySQL backend was not restarted or changed.
- [ ] Review the local commit and explicitly approve before pushing or making the GitHub repository public.

## Known test limits

The simultaneous-last-room test uses H2; it has not been run under real MySQL contention. The user previously confirmed the booking flow works manually, but the browser pass has not been repeated after the latest image/description changes. Image redistribution rights were not verified. The user's MySQL database must apply migration V5 at next backend startup. Time spent was not recorded reliably, so no precise duration is claimed.

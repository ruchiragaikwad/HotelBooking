# Phase Tracker

- [x] Phase 0 - Workspace inspection and plan.
- [x] Phase 1 - Backend and frontend project skeletons.
- [x] Phase 2 - MySQL configuration and persistence.
- [x] Phase 3 - Hotel catalogue and search APIs.
- [x] Phase 4 - Availability and booking logic.
- [x] Phase 5 - React user interface.
- [x] Phase 6 - End-to-end booking, lookup, and cancellation (the user reported the journey works; backend regression tests pass).
- [x] Phase 7 - Compare up to three hotels.
- [x] Phase 8 - Final local verification and submission preparation. Documentation, migrations, images, screenshots, secret-ignore review, automated tests, and production build are complete. Photo reuse rights and the final browser pass against the user's MySQL database remain outstanding. Do not push or change repository visibility without explicit approval.

## Latest automated verification

- Backend: `cd backend; .\\mvnw.cmd clean verify` - PASS, 21 tests, 0 failures, 0 errors, 0 skipped. Flyway applied all five migrations in the isolated test database.
- Frontend: `cd frontend; npm test` - PASS, 4 tests; `npm run build` - PASS, Vite production bundle generated.
- Secret-ignore review: PASS for expected ignore patterns and tracked files. Broad pattern matches were safe example placeholders and Maven wrapper variable references.

The H2 concurrency test has not been repeated against real MySQL under contention. Do not mark manual or database-specific checks as passed unless they have been run.

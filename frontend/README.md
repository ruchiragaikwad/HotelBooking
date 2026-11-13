# Frontend

React and Vite provide the hotel catalogue, location search, date selection, hotel detail, availability and booking estimate screens. The page gets hotel records and availability from the Spring Boot API; it does not keep a second hard-coded hotel catalogue.

## Run locally

1. Start MySQL and the backend by following `../database/README.md` and `../backend/README.md`.
2. From this folder in PowerShell or the VS Code terminal, install dependencies and start Vite:

   ```powershell
   npm install
   npm run dev
   ```

3. Open the local URL printed by Vite (normally `http://localhost:5173`). The Vite development server forwards `/api` requests to `http://localhost:8080`.

`VITE_API_BASE_URL` selects the API base path, and `VITE_API_PROXY_TARGET` selects the development proxy destination (default `http://localhost:8080`). Copy `.env.example` to an untracked `.env.local` to override it for a local setup. The current `.env.example` uses `/api`, which works with the Vite development proxy. The booking form sends the hotel, guest, and stay dates to the backend; confirmation displays the backend reference and total. The booking management form uses the reference and email to retrieve or cancel a booking. The hotel catalogue also includes a comparison panel: select up to three stays to compare their location, amenities, and nightly price. Comparison uses hotel data already loaded from the API and is kept in page state only.

Build the production frontend with `npm run build`. Run the dependency-free comparison logic tests with `npm test`. Hotel photos are static files under `public/images/hotels/`, mapped to catalogue names in `src/App.jsx`; unavailable images fall back to the card background.

# QuickBite web

Vite + React frontend for the QuickBite Spring Boot API.

## Start the frontend

```powershell
cd frontend
corepack pnpm install
corepack pnpm dev
```

Open `http://localhost:5173`.

Vite proxies `/api` to `http://localhost:9909`, so start the backend on port
`9909` for live authentication and restaurant data. When the backend is not
running, the public storefront keeps its local demonstration restaurants so the
UI remains previewable.

To use a different API URL, create `.env`:

```env
VITE_API_BASE_URL=https://your-api.example.com/api
```

## Main routes

- `/` - customer storefront
- `/restaurants/1` - restaurant menu
- `/orders` - customer order tracking
- `/owner` - restaurant-owner operations dashboard
- `/rider` - rider pickup and delivery workspace
- `/admin` - platform administration dashboard

## Checks

```powershell
corepack pnpm lint
corepack pnpm build
```

# QuickBite

QuickBite is a full-stack food-delivery application with customer, restaurant-owner,
rider, and administrator workspaces.

## Project layout

- `frontend/` — React 19 and Vite web application
- `QuickBite/` — Spring Boot 4 API using SQLite
- `render.yaml` — Render Blueprint for the API and static frontend

## Local development

Start the backend:

```powershell
cd QuickBite
$env:JWT_SECRET="quickbite-local-development-secret-key-123456"
$env:PAYSTACK_SECRET_KEY="replace_with_your_paystack_test_key"
.\mvnw.cmd spring-boot:run
```

Start the frontend in a second terminal:

```powershell
cd frontend
corepack pnpm install
corepack pnpm dev
```

Open `http://localhost:5173`.

## Demo accounts

All development accounts use the password `QuickBite123!`.

| Role | Email |
| --- | --- |
| Customer | `customer@quickbite.local` |
| Restaurant owner | `owner@quickbite.local` |
| Rider | `rider@quickbite.local` |
| Administrator | `admin@quickbite.local` |

Demo data can be disabled with `DEMO_DATA_ENABLED=false`.

## Deploy on Render

1. Create a new Render Blueprint from this repository.
2. Render reads `render.yaml` and creates `quickbite-api` and `quickbite-web`.
3. Provide `PAYSTACK_SECRET_KEY` when prompted.
4. The backend deploys on Render's free web-service tier.

The committed `QuickBite/quickbite.db` seeds `/tmp/quickbite.db` when the backend
starts. Free Render services use an ephemeral filesystem, so online database
changes can be lost when the service restarts, spins down, or redeploys.

## Verification

```powershell
cd QuickBite
.\mvnw.cmd test

cd ..\frontend
corepack pnpm lint
corepack pnpm build
```

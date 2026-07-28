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
4. Use a paid backend instance if SQLite changes must survive restarts and deploys,
   because Render persistent disks are not available on free web services.

On the backend's first boot, the committed `QuickBite/quickbite.db` is copied to
the persistent disk. Later deploys preserve the disk database.

## Verification

```powershell
cd QuickBite
.\mvnw.cmd test

cd ..\frontend
corepack pnpm lint
corepack pnpm build
```

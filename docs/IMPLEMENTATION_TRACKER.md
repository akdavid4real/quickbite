# QuickBite implementation tracker

Last updated: 2026-08-05

## Goal

Complete the missing customer, review, restaurant-owner, administrator, rider,
pagination, onboarding-verification, and currency-safety work identified in the
August 2026 repository audit.

## Working agreements

- Changes are delivered in reviewable vertical slices.
- Backend authorization and business rules remain the source of truth; frontend
  role gates are only presentation safeguards.
- Provider onboarding uses a deterministic mock verification adapter for now.
  The domain contract must allow replacing it with a real identity/KYC provider.
- Money is represented with `BigDecimal` in Java and serialized as JSON numbers.
- List endpoints use Spring `Page` responses with explicit page and size limits.
- Existing demo accounts remain usable locally, but production demo access must
  be disabled separately before real users are admitted.

## Planned slices

| Slice | Scope | Status | Verification |
| --- | --- | --- | --- |
| 0 | Audit baseline, tracker, contracts | Complete | Existing 6 backend tests, frontend lint/build pass |
| 1 | Provider verification, admin approval, profile and saved addresses | Complete | Backend compile, frontend lint/build, focused verification test |
| 2 | Customer cancellation, payment retry, contacts, reviews and ratings | Complete | Cancellation/review/retry tests; rendered customer orders |
| 3 | Owner reviews, help, notifications, filters, restaurant/menu editing | Complete | Lint/build and rendered owner workspace |
| 4 | Admin moderation, provider/restaurant approval and order resolution | Complete | Live authenticated API and rendered admin workspace |
| 5 | Rider availability, earnings, navigation/contact, proof and history | Complete | Delivery-proof tests, live summary and rendered rider workspace |
| 6 | Pagination across API and UI | Complete | Live page envelopes and frontend controls across high-growth lists |
| 7 | Currency migration to `BigDecimal` | Complete | Exact `1500.01` to `150001` kobo regression |
| 8 | Full regression, documentation and readiness report | Complete | 16 backend tests, frontend lint/build, API and browser QA |

## Provider verification contract

1. Public registration may create `CUSTOMER`, `RESTAURANT_OWNER`, or `RIDER`
   accounts, but provider accounts begin in `PENDING_APPROVAL` with verification
   status `PENDING`.
2. A provider submits role-appropriate onboarding data to the verification API.
3. The mock adapter returns a deterministic decision and reference. It never
   silently grants administrator access.
4. A successful mock check moves the account to `VERIFIED`; an administrator
   must approve it before protected owner/rider operations are available.
5. Rejection records a reason and permits a corrected resubmission.
6. The adapter is behind an interface so a real provider can replace it without
   rewriting controllers or dashboards.

## Progress log

- 2026-08-04: Goal created from the repository audit.
- 2026-08-04: Re-ran baseline checks: backend tests (6/6), frontend lint, and
  frontend production build all passed. Both configured Render services returned
  HTTP 200.
- 2026-08-04: Added provider/account statuses, saved-address and provider-
  verification persistence, a replaceable verification gateway, and a
  deterministic mock gateway. Identity values containing `REJECT` exercise the
  rejected path; other structurally valid submissions are mock-verified.
- 2026-08-04: Added provider self-service verification endpoints and admin
  approval, suspension, and reactivation endpoints. Protected owner/rider API
  operations now require an active, verified provider account.
- 2026-08-04: Added authenticated profile and saved-address APIs plus a split,
  responsive React account workspace. Owner/rider dashboards direct pending
  providers to onboarding. Backend compilation and frontend build passed.
- 2026-08-04: Added customer cancellation rules, persisted Paystack checkout URLs
  for retry, failed-payment handling, configured callback URLs, order contact
  details, saved-address checkout selection, delivered-only reviews, frontend
  review flows, and restaurant rating recalculation.
- 2026-08-04: Made owner Help, Notifications, order filtering, restaurant editing,
  menu editing/deletion, and review panels functional.
- 2026-08-04: Added admin provider and restaurant approvals, suspension/reactivation,
  restaurant/review moderation, and constrained administrative order resolution.
- 2026-08-04: Added rider availability, earnings summary, delivery history,
  navigation/contact actions, and mandatory URL-based delivery evidence.
- 2026-08-04: Added page response contracts to high-growth restaurant, order,
  review, user, provider-approval, restaurant-approval, and admin list endpoints.
- 2026-08-04: Migrated menu, cart, order, order-item, payment, and API money fields
  from binary floating point to `BigDecimal`; Paystack kobo conversion is exact.
- 2026-08-05: Added interactive pagination controls to public restaurants,
  customer orders, owner orders, admin moderation and approval queues, and rider
  availability/history. Added customer contact to rider deliveries.
- 2026-08-05: Completed the final regression: 16 backend tests, frontend lint,
  production build, authenticated page-envelope checks for all roles, and
  rendered customer, owner, admin, and rider QA with no browser console errors.
- 2026-08-05: Expanded the idempotent demo catalog to three verified restaurants
  and 19 menu items. Added five optimized Nigerian-food images and refreshed the
  tracked SQLite seed so fresh deployments receive the complete catalog.
- 2026-08-05: Expanded the food-media library to 30 images total and connected
  all 15 additions to seeded dishes, bringing the demo catalog to 34 menu items.

## Completion evidence

| Requirement | Evidence |
| --- | --- |
| Customer self-service | Profile/address workspace and checkout selection; rendered cancel, contact and review controls; persisted payment retry URL test |
| Review integrity | Delivered-only service rule, one-review-per-order constraint, aggregate recalculation on create/delete, customer and restaurant UI |
| Owner operations | Rendered order filtering/notifications, support link, restaurant edit, menu edit/delete and reviews |
| Admin operations | Rendered provider/restaurant approval, suspension, content removal and order-resolution controls; authenticated APIs |
| Rider operations | Rendered availability, earnings, navigation, restaurant/customer contact and history; delivery evidence required before completion |
| Scale and money | Spring page envelopes plus UI controls; all currency domain fields use `BigDecimal` and exact Paystack minor-unit conversion |
| Onboarding control | Providers register pending, use the replaceable deterministic mock verification gateway, then require administrator approval |

## Risks and follow-ups

- The production Blueprint currently enables known demo accounts, including an
  administrator. This remains an urgent deployment risk outside the feature
  slices above.
- Render's free ephemeral SQLite database is not suitable for durable production
  customer, order, or payment data.
- Schema changes currently rely on Hibernate `ddl-auto=update`; a migration tool
  is required before production data exists.

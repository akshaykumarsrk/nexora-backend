# NEXORA subscription backend foundation

## Endpoints
- `GET /api/subscriptions/plans` — active plans, public fields only.
- `GET /api/subscriptions/current` — authenticated user's organization's subscription; returns `204 No Content` if none.
- `POST /api/subscriptions/trial` — `{"planCode":"BUSINESS"}`; ADMIN only; starts one 30-day trial for the authenticated organization.

The organization ID is always taken from `OrganizationContext`; clients cannot choose another organization. The trial duration, status, dates, plan price and limits are server controlled. One subscription row per organization prevents starting a second trial through this endpoint.

## Pricing seed data
Starter ₹699/month / ₹6,990/year; Business ₹1,499/month / ₹14,990/year; Growth ₹2,999/month / ₹29,990/year. User/technician/job limits are provisional product decisions and must be validated before production sales. They are stored in the database so admins can later manage plans.

## Migration
Run `database/migrations/V4__subscription_foundation.sql` against the existing MySQL database before using the new endpoints. Existing project currently uses `spring.jpa.hibernate.ddl-auto=update` by default; the SQL migration is provided as the explicit production-oriented schema/seed script. Avoid running both competing schema-management approaches in production without a migration policy.

## Important current limitations
- No payment provider, webhook processing, invoices, cancellation/renewal workflows, or automatic expiry scheduler is implemented yet.
- Plan limits are returned to the UI but not yet enforced by user/technician/job creation services. Do not advertise those limits as enforced until enforcement is added.
- Trial expiration is represented by `trialEndAt`; this foundation does not yet automatically transition status to EXPIRED or block application modules after expiry.
- `GET /current` responds with HTTP 204 when no subscription exists.
- The app's registration flow does not automatically start a trial; the organization admin can start one using the endpoint after signing in.
- Subscription plan prices are proposed seed values, not market-validated pricing.

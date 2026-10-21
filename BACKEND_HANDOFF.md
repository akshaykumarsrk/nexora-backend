# NEXORA Backend handoff

## Changes included in this handoff

- Added the employee invitation workflow: admin creates an invitation, the service stores only a SHA-256 token hash, and the invitee sets their password using a single-use 24-hour link.
- Added a `user_invitations` database migration (`database/migrations/V3__user_invitations.sql`). If `spring.jpa.hibernate.ddl-auto=update` is used locally, Hibernate can create/update the table; apply the migration in environments managed by SQL migrations.
- Removed the legacy `POST /api/users` temporary-password provisioning endpoint. Employee provisioning should use `POST /api/users/invitations` instead.
- Public auth requests now bypass JWT parsing so a stale browser Authorization header cannot block register/login/email-verification/invitation-acceptance requests.
- `ResponseStatusException` is mapped to its intended HTTP status instead of falling through to the generic 500 handler.
- Manual stock movements validate optional job and technician references against the authenticated organization.
- Added `.env.example` with placeholders only. Do not commit real secrets.

## Key endpoints

- `POST /api/users/invitations` — ADMIN only. Body: `email`, `username`, `role` (`MANAGER`, `STAFF`, `TECHNICIAN`), and `technicianId` only for a technician invitation.
- `POST /api/auth/accept-invitation` — public. Body: `token`, `password`, `confirmPassword`.
- Existing `GET /api/auth/verify-email` and registration/login endpoints remain in place.

## Configuration

Provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (at least 32 random bytes for HS256), `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, and `FRONTEND_URL` through the environment. Redis must be reachable for the configured auth rate limiter; it deliberately fails closed for registration/login if Redis is unavailable.

## Build verification limitation

The archive was inspected and internal project imports were statically checked. A Maven compile could not be run in this execution environment because Maven is not installed and the Maven wrapper could not download its distribution. Therefore this handoff does not claim a successful compile or runtime test. Run `mvnw.cmd -DskipTests compile` in the project directory on a machine with Maven wrapper network access; address any compiler output before deployment.


## Subscription backend foundation (2026-10-21)
Added plan catalog and organization subscription persistence, ADMIN-only 30-day trial start, authenticated-organization scoping, and MySQL schema/seed script at `database/migrations/V4__subscription_foundation.sql`. See `SUBSCRIPTION-BACKEND-INTEGRATION.md` for endpoints and limitations. This project does not currently include a migration runner, so apply the SQL migration explicitly before calling these endpoints.

# NEXORA backend security patch notes

## Changes in this patch
- EmployeeService now scopes list/read/update/delete to the authenticated organization.
- Employee creation assigns the current organization from OrganizationContext; client input cannot select an organization.
- EmployeeRepository provides organization-scoped queries.
- Employee entity now has a required Organization relationship.
- Database connection password and JWT signing secret are read from environment variables instead of being committed in application.properties.

## Required before running
Set environment variables:
- DB_PASSWORD
- JWT_SECRET (use a newly generated random secret of at least 32 bytes)
Optional: DB_URL, DB_USERNAME, JPA_DDL_AUTO, JPA_SHOW_SQL, JWT_EXPIRATION, SERVER_PORT.

## Database migration (mandatory)
See database/migrations/V1__employee_tenant_isolation.sql. Back up the database first.
Existing employee records have no tenant owner in the old schema. Assign each record to its verified organization before setting organization_id NOT NULL and adding the foreign key. Do not guess ownership.
The updated Employee entity expects all employee rows to have an organization_id.

## Credential safety
The original application.properties contained a database password and JWT secret. Treat both as exposed: rotate the database password if it is real, generate a new JWT secret, and invalidate old tokens by deploying the new secret.

## Verification
Run `mvn clean test` after applying the migration and setting environment variables. Cross-tenant employee requests should return not found, not another organization's employee.

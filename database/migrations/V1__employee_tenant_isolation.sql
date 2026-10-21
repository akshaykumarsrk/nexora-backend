-- NEXORA: employee tenant isolation migration (MySQL)
-- BACK UP THE DATABASE FIRST.
--
-- Phase 1: add a nullable organization_id so existing records can be mapped safely.
ALTER TABLE employees ADD COLUMN organization_id BIGINT NULL;

-- Phase 2: assign EVERY existing employee to its real organization.
-- Do NOT guess or assign all rows to one organization unless you have verified
-- that every existing employee belongs to that organization.
SELECT id, employee_code, email, name FROM employees;
SELECT id, name FROM organizations;
--
-- After reviewing the results, run an explicit mapping for each known group, e.g.:
-- UPDATE employees SET organization_id = <verified_organization_id>
-- WHERE id IN (<verified_employee_ids>);
--
-- Confirm no rows remain unmapped:
-- SELECT COUNT(*) FROM employees WHERE organization_id IS NULL;
--
-- Phase 3: only after the count above is zero, enforce the relationship:
-- ALTER TABLE employees MODIFY organization_id BIGINT NOT NULL;
-- ALTER TABLE employees
--   ADD CONSTRAINT fk_employees_organization
--   FOREIGN KEY (organization_id) REFERENCES organizations(id);
--
-- Keep this migration staged until the mapping is verified. The updated entity
-- expects organization_id to be populated and non-null for every employee.

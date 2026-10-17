-- Upgrade script for databases created with the ORIGINAL schema.
-- Fresh installs should use LoanApplicationSystem_Database.sql instead.
--
-- Order of steps:
--   1. Run this script.
--   2. Run the Java tool  loan.MigratePasswords  once to hash existing passwords.

USE loan_system;

-- 1. Room for password hashes (old column was VARCHAR(100)).
ALTER TABLE users MODIFY password VARCHAR(255) NOT NULL;

-- 2. Columns for the one-time password reset code.
ALTER TABLE users
    ADD COLUMN reset_token_hash VARCHAR(255) NULL,
    ADD COLUMN reset_token_expiry DATETIME NULL;

-- 3. Stricter column rules (fails if existing rows contain NULLs; fix those first).
ALTER TABLE users
    MODIFY failed_attempts INT NOT NULL DEFAULT 0,
    MODIFY account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER';

-- 4. OPTIONAL: link loan applications to users. Run only if every
--    loan_applications.username still exists in users, otherwise MySQL
--    rejects it. This check should return 0 rows:
--      SELECT l.id FROM loan_applications l
--      LEFT JOIN users u ON u.username = l.username WHERE u.id IS NULL;
--
-- ALTER TABLE loan_applications
--     ADD CONSTRAINT fk_loan_user FOREIGN KEY (username)
--     REFERENCES users (username) ON UPDATE CASCADE ON DELETE RESTRICT;

-- 5. SECURITY NOTE: the old PAN/CIBIL screen replaced customer passwords with
--    the guessable pattern  Loan@<cibil score>.  Any customer who went through
--    that screen should reset their password.

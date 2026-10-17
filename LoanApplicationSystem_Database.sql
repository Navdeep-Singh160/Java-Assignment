CREATE DATABASE IF NOT EXISTS loan_system;

USE loan_system;


-- =========================
-- USERS TABLE
-- =========================
-- password holds a PBKDF2 hash (about 90 characters), never the real password.
-- No default admin is inserted: the application asks you to create the first
-- administrator the first time it starts on an empty database.

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(15) NOT NULL,
    password VARCHAR(255) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    pan VARCHAR(10),
    cibil_score INT,
    reset_token_hash VARCHAR(255) NULL,
    reset_token_expiry DATETIME NULL,
    CONSTRAINT chk_users_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
    CONSTRAINT chk_users_cibil CHECK (cibil_score IS NULL OR cibil_score BETWEEN 300 AND 900)
);


-- =========================
-- LOAN APPLICATIONS TABLE
-- =========================
-- A user who has loan applications cannot be deleted (ON DELETE RESTRICT);
-- deactivate the account instead. Renaming a user updates their applications.

CREATE TABLE IF NOT EXISTS loan_applications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    loan_amount DOUBLE NOT NULL,
    duration INT NOT NULL,
    income DOUBLE NOT NULL,
    purpose VARCHAR(200),
    status VARCHAR(30) NOT NULL DEFAULT 'Pending',
    rejection_reason VARCHAR(200),
    interest_rate DOUBLE,
    emi DOUBLE,
    total_interest DOUBLE,
    total_payable DOUBLE,
    CONSTRAINT fk_loan_user FOREIGN KEY (username)
        REFERENCES users (username)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_loan_amount CHECK (loan_amount > 0),
    CONSTRAINT chk_loan_duration CHECK (duration > 0),
    CONSTRAINT chk_loan_income CHECK (income > 0),
    CONSTRAINT chk_loan_status CHECK (status IN ('Pending', 'Approved', 'Rejected', 'Needs More Info'))
);

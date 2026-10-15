CREATE DATABASE IF NOT EXISTS loan_system;

USE loan_system;


-- =========================
-- USERS TABLE
-- =========================

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(15) NOT NULL,
    password VARCHAR(100) NOT NULL,
    failed_attempts INT DEFAULT 0,
    account_locked BOOLEAN DEFAULT FALSE,
    role VARCHAR(20) DEFAULT 'ROLE_USER',
    pan VARCHAR(10),
    cibil_score INT
);


-- =========================
-- ADMIN ACCOUNT
-- =========================

INSERT INTO users
(username, full_name, email, mobile, password, role)
VALUES
('admin', 'System Admin', 'admin@loan.com', '9999999999', 'Admin@123', 'ROLE_ADMIN');


-- =========================
-- LOAN APPLICATIONS TABLE
-- =========================

CREATE TABLE IF NOT EXISTS loan_applications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    loan_amount DOUBLE NOT NULL,
    duration INT NOT NULL,
    income DOUBLE NOT NULL,
    purpose VARCHAR(200),
    status VARCHAR(30) DEFAULT 'Pending',
    rejection_reason VARCHAR(200),
    interest_rate DOUBLE,
    emi DOUBLE,
    total_interest DOUBLE,
    total_payable DOUBLE
);
# Java MySQL Loan Application System

## Overview
A Java console-based loan application system integrated with MySQL for managing users, loan applications, and administrative loan decisions.

## Technologies
- Java 17+
- MySQL 8.0.16+ (or MariaDB 10.2+; the schema uses CHECK constraints)
- JDBC (MySQL Connector/J)
- Eclipse IDE

## Features
- User registration with validated input and strong-password rules
- Login with hashed passwords and automatic lock after 3 failed attempts
- Password reset with a one-time code (simulated email)
- Admin user management (view, search, edit, activate/deactivate, delete)
- PAN format and CIBIL score validation
- Loan application with a CIBIL-based interest rate
- One admin review workflow: approve (EMI, total interest and total payable calculated), reject with reason, or request more information
- MySQL data persistence
- Console-based notification simulation

## Project layout
```
src/loan/
  Main.java                 menus and central error handling
  Registration, Login, ForgotPassword, Logout     account screens
  CreditProfile.java        PAN and CIBIL submission
  LoanApplication.java      apply / view / admin review
  Admin.java                user management
  LoanType, LoanCalculator, LoanStatus            all loan rules and EMI maths (single source of truth)
  UserDao, LoanDao, Db      all SQL (parameterised) and JDBC helper
  PasswordUtil              PBKDF2 password hashing
  Validator, ConsoleIO      input validation and safe console input
  AppConfig, DBConnection, AppLog, *Exception     configuration, connection, logging
  MigratePasswords          one-time tool for databases created by the old version
test/loan/SelfTest.java     dependency-free tests (no database needed)
```

## Setup (new installation)
1. Install a JDK (17 or newer) and MySQL Server.
2. Run `LoanApplicationSystem_Database.sql` to create the database and tables.
3. Copy `config.properties.example` to `config.properties` and enter your MySQL URL, user and password.
   Alternatively set the environment variables `LOAN_DB_URL`, `LOAN_DB_USER` and `LOAN_DB_PASSWORD`
   (they override the file). `config.properties` is git-ignored; never commit it.
4. Add the MySQL Connector/J jar to the project classpath.
5. Run `loan.TestConnection` to check the settings, then run `loan.Main`.
6. On the first start with an empty database the program asks you to create the first administrator.
   There is no built-in default admin account or password.

Command-line alternative:
```
javac -d bin -cp mysql-connector-j.jar src/loan/*.java
java  -cp bin:mysql-connector-j.jar loan.Main        # use ; instead of : on Windows
```

## Upgrading an existing database (created by the old version)
1. Run `migration_v2_security.sql`.
2. Run `loan.MigratePasswords` once. It converts every stored plain-text password to a hash.
3. Customers who used the old PAN/CIBIL screen had their password replaced by the guessable pattern `Loan@<score>`; ask them to reset it.

## Tests
```
javac -d out src/loan/*.java test/loan/SelfTest.java
java -cp out loan.SelfTest
```
Covers password hashing, every validation rule, interest-rate rules and EMI maths.

## Security notes
- No credentials in source code; settings come from `config.properties` or environment variables.
- Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes (600,000 iterations); plain-text passwords are never stored, printed, logged or emailed.
- All SQL uses prepared statements.
- Technical errors and stack traces go to `loan-system.log`, not the console.
- Login errors do not reveal whether an account exists.

## Limitations
This is an academic prototype.
- Notifications are simulated in the console and are not real email delivery. Because the reset code is shown on the same console, the reset flow only becomes secure once real email is wired in.
- The CIBIL score is entered by the customer and is not verified against a credit bureau; a production system must fetch it from one.
- PAN numbers are stored unencrypted.
- The Connector/J driver and a real MySQL server are required to run the application.

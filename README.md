# Java MySQL Loan Application System

## Overview
A Java console-based loan application system integrated with MySQL for managing users, loan applications, and administrative loan decisions.

## Technologies
- Java
- MySQL
- JDBC
- Eclipse IDE

## Features
- User registration and authentication
- Admin user management
- Loan application submission and review
- PAN format and CIBIL score input validation
- Loan approval and rejection with reasons
- EMI, total interest, and total payable calculation
- MySQL data persistence
- Console-based notification simulation

## Setup
1. Install Java JDK and MySQL Server.
2. Use `LoanApplicationSystem_Database.sql` to create the database and tables.
3. Configure the database connection in `DBConnection.java`.
4. Add the MySQL JDBC driver to the project.
5. Import the project into Eclipse and run `Main.java`.

## Limitations
This is an academic prototype. Notifications are simulated in the console and are not real email delivery.


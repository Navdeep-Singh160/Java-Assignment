# Changelog - review fixes

## 1. Hard-coded database credentials (security)
- Removed the MySQL user and password from `DBConnection.java`.
- Settings now come from environment variables or a git-ignored `config.properties` (`AppConfig`).
- Added `config.properties.example` and a `.gitignore` entry.
- **Action required:** the old password was committed to the repository and remains in git history. Change that MySQL password.

## 2. Plain-text passwords (security)
- Passwords are hashed with salted PBKDF2-HMAC-SHA256 (`PasswordUtil`).
- Existing plain-text accounts still log in once and are upgraded to a hash automatically; `MigratePasswords` converts all of them in one go.
- The default admin (`admin` / `Admin@123`) is no longer seeded in the SQL. The first administrator is created on first start.
- Password column widened to `VARCHAR(255)`.
- Related security holes closed while touching this code:
  - The PAN/CIBIL screen overwrote the customer's username and password with generated values and printed/emailed the password. Removed.
  - "Forgot password" let anyone who knew an email address set a new password. Now needs a one-time, expiring, hashed code with limited attempts.
  - Login/reset messages no longer reveal whether an account exists.
  - Users can no longer apply for loans or submit PAN/CIBIL under someone else's username; the logged-in account is used.

## 3. Insufficient input validation
- New `Validator` (username, name, email, mobile, password strength, PAN, purpose, reason) and `ConsoleIO` (shared Scanner, re-prompts, retry limit, range-checked numbers).
- Fixed crashes from `nextInt()`/`nextDouble()` on non-numeric input, and from `parseInt` in the post-login menus.
- Loan amount, duration and income now have limits (`LoanCalculator` constants).
- Admin edit keeps the current value when a field is left blank.
- Passwords are not echoed when run from a real terminal.
- Database: CHECK constraints, NOT NULL columns, and a foreign key from loans to users.

## 4. Duplicated / inconsistent business logic
- **Two interest-rate tables** (flat per type in the application form, CIBIL-based in the PAN step) replaced by `LoanType.rateFor(cibil)`.
- **Two admin review paths** (`LoanApplication.reviewApplication` and `LoanApproval.reviewLoan`) merged into one. Approving through the old first path left EMI, interest and total payable empty; approval now always calculates them. `LoanApproval` was removed.
- Decisions are final: an approved/rejected application can no longer be re-decided (guarded in SQL as well).
- Re-activating a user now clears the failed-attempt counter (they used to be one wrong password from re-locking).
- Failed-login counting is a single atomic SQL statement.
- "Find the user's email and send a notification" (copied five times) is `EmailNotification.sendToUser`.
- Connection/statement/close boilerplate (copied in every method) is `Db` plus `UserDao`/`LoanDao`.
- EMI maths in one place (`LoanCalculator`), rounded to 2 decimals, handles 0% interest, and rejects 0 months (previously divide-by-zero).
- Interest rate now requires PAN/CIBIL on file before applying, so every loan is priced the same way.

## 5. Exception handling
- No more `catch (Exception) { e.printStackTrace(); }`. DAO failures become `DataAccessException` with a user-safe message; the SQL state and stack trace go to `loan-system.log`.
- `DBConnection` no longer returns `null` on failure (which caused `NullPointerException`s later); it throws.
- Every JDBC resource uses try-with-resources (previously leaked on any exception).
- One central handler in `Main` keeps the program running after a failed action; unexpected errors are logged.
- Closed input (Ctrl+D / Ctrl+Z) exits cleanly; repeated invalid input cancels the action instead of looping or crashing.
- Duplicate username/email and delete-with-loans are reported in plain language.

## Behaviour changes to be aware of
- User menu: `1 Submit PAN and CIBIL`, `2 Apply for Loan`, `3 My Loan Applications`, `4 Logout` (order changed; "My Loan Applications" is new).
- Admin menu: the two loan-review items are merged, so Logout is now option 8 (was 9).
- Menus now loop until logout, and logout returns to the start menu (previously the program ended after one action).
- Admins cannot delete or deactivate themselves, or delete the last admin.

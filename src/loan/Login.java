package loan;

import java.util.Optional;
import java.util.logging.Level;

public final class Login {

    public static final int MAX_FAILED_ATTEMPTS = 3;

    private Login() { }

    /**
     * @return the logged-in user, or null if the login failed (a message has been printed)
     */
    public static User loginUser() {

        String usernameOrEmail = ConsoleIO.readLine("Enter username or email: ");
        String password = ConsoleIO.readPassword("Enter password: ");

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            System.out.println("Username/email and password are required.");
            return null;
        }

        Optional<User> found = UserDao.findByLogin(usernameOrEmail);

        if (found.isEmpty()) {
            PasswordUtil.burnTime(password);
            System.out.println("Invalid username/email or password.");
            return null;
        }

        User user = found.get();

        if (!passwordMatches(user, password)) {
            handleWrongPassword(user);
            return null;
        }

        if (user.isAccountLocked()) {
            System.out.println("This account is locked or deactivated. "
                    + "Please contact an administrator.");
            return null;
        }

        if (user.getFailedAttempts() > 0) {
            UserDao.resetFailedAttempts(user.getId());
        }

        System.out.println("Login successful!");
        System.out.println("Welcome, " + user.getFullName() + ".");

        return user;
    }

    private static void handleWrongPassword(User user) {

        // A locked account stays locked; do not keep counting.
        if (user.isAccountLocked()) {
            System.out.println("Invalid username/email or password.");
            return;
        }

        UserDao.AttemptState state =
                UserDao.recordFailedLogin(user.getId(), MAX_FAILED_ATTEMPTS);

        System.out.println("Invalid username/email or password.");

        if (state.locked()) {
            System.out.println("Account locked after " + MAX_FAILED_ATTEMPTS
                    + " failed attempts. Please contact an administrator.");
        }
    }

    /**
     * Checks the password against the stored hash. Accounts created before
     * hashing was added still hold plain text; those are accepted once and
     * immediately converted to a hash.
     */
    private static boolean passwordMatches(User user, String password) {

        String stored = user.getPasswordHash();

        if (PasswordUtil.isHashed(stored)) {

            if (!PasswordUtil.verify(password, stored)) {
                return false;
            }

            if (PasswordUtil.needsRehash(stored)) {
                upgradeHash(user, password);
            }

            return true;
        }

        if (PasswordUtil.matchesLegacyPlaintext(password, stored)) {
            upgradeHash(user, password);
            return true;
        }

        PasswordUtil.burnTime(password);
        return false;
    }

    private static void upgradeHash(User user, String password) {

        try {
            UserDao.updatePasswordHash(user.getId(), PasswordUtil.hash(password));

        } catch (DataAccessException e) {
            // Login is still valid; the upgrade will be retried next time.
            AppLog.get(Login.class).log(Level.WARNING,
                    "Could not upgrade password hash for user id " + user.getId(), e);
        }
    }
}

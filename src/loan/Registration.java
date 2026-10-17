package loan;

import java.util.Locale;

public final class Registration {

    private Registration() { }

    /** Self-registration of a normal customer account. */
    public static void registerUser() {
        createAccount(User.ROLE_USER);
    }

    /**
     * Collects and validates every field, hashes the password and saves the account.
     * Also used to create the first administrator on a fresh database.
     */
    public static void createAccount(String role) {

        String username = ConsoleIO.readValidated("Enter username: ", Validator::username);
        String fullName = ConsoleIO.readValidated("Enter full name: ", Validator::fullName);
        String email = ConsoleIO.readValidated("Enter email: ", Validator::email)
                .toLowerCase(Locale.ROOT);
        String mobile = ConsoleIO.readValidated("Enter mobile number: ", Validator::mobile);
        String password = ConsoleIO.readNewPassword("Enter password: ");

        try {
            UserDao.create(username, fullName, email, mobile,
                    PasswordUtil.hash(password), role);

            System.out.println("Registration successful!");

        } catch (DataAccessException e) {

            if (e.isDuplicate()) {
                System.out.println("That username or email is already registered.");
            } else {
                throw e;
            }
        }
    }
}

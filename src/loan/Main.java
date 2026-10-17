package loan;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = AppLog.get(Main.class);

    public static void main(String[] args) {

        AppLog.init();

        try {
            runApplication();

        } catch (ConsoleIO.InputClosedException e) {
            System.out.println();
            System.out.println("Input closed. Goodbye.");
        }
    }

    private static void runApplication() {

        if (!prepareDatabase()) {
            return;
        }

        // After logout the program returns to the start menu instead of exiting.
        while (true) {

            User user = guestMenu();

            if (user == null) {
                System.out.println("Thank you for using the Loan Application System.");
                return;
            }

            if (user.isAdmin()) {
                adminMenu(user);
            } else {
                userMenu(user);
            }
        }
    }

    /**
     * Checks the database is reachable and, on a fresh database, creates the
     * first administrator. There is no built-in default admin password any more.
     *
     * @return false if the application cannot continue
     */
    private static boolean prepareDatabase() {

        try {
            if (UserDao.countAdmins() == 0) {

                System.out.println("No administrator account exists yet. Let's create one.");

                run(() -> Registration.createAccount(User.ROLE_ADMIN));
            }

            return true;

        } catch (DataAccessException | ConfigException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // ---------- menus ----------

    /** @return the user who logged in, or null if the user chose Exit */
    private static User guestMenu() {

        while (true) {

            System.out.println();
            System.out.println("===== Loan Application System =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Forgot Password");
            System.out.println("4. Exit");

            switch (ConsoleIO.readMenuChoice()) {

                case 1 -> run(Registration::registerUser);

                case 2 -> {
                    User user = guard(Login::loginUser);

                    if (user != null) {
                        return user;
                    }
                }

                case 3 -> run(ForgotPassword::resetPassword);

                case 4 -> {
                    return null;
                }

                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void userMenu(User user) {

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println();
            System.out.println("----- User Menu -----");
            System.out.println("1. Submit PAN and CIBIL");
            System.out.println("2. Apply for Loan");
            System.out.println("3. My Loan Applications");
            System.out.println("4. Logout");

            switch (ConsoleIO.readMenuChoice()) {

                case 1 -> run(() -> CreditProfile.submit(user));
                case 2 -> run(() -> LoanApplication.applyForLoan(user));
                case 3 -> run(() -> LoanApplication.viewMyApplications(user));

                case 4 -> {
                    Logout.logoutUser();
                    loggedIn = false;
                }

                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void adminMenu(User admin) {

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println();
            System.out.println("----- Admin Menu -----");
            System.out.println("1. View Users");
            System.out.println("2. Search User");
            System.out.println("3. Edit User");
            System.out.println("4. Activate/Deactivate User");
            System.out.println("5. Delete User");
            System.out.println("6. View Loan Applications");
            System.out.println("7. Review Loan Application (Approve/Reject/More Info)");
            System.out.println("8. Logout");

            switch (ConsoleIO.readMenuChoice()) {

                case 1 -> run(Admin::viewAllUsers);
                case 2 -> run(Admin::searchUser);
                case 3 -> run(Admin::editUser);
                case 4 -> run(() -> Admin.changeUserStatus(admin));
                case 5 -> run(() -> Admin.deleteUser(admin));
                case 6 -> run(LoanApplication::viewApplications);
                case 7 -> run(LoanApplication::reviewApplication);

                case 8 -> {
                    Logout.logoutUser();
                    loggedIn = false;
                }

                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ---------- central error handling ----------

    private static void run(Runnable action) {

        guard(() -> {
            action.run();
            return null;
        });
    }

    /**
     * Runs one menu action. Expected problems become a short message; anything
     * unexpected is logged with its stack trace and the program keeps running,
     * so one failed action cannot crash the whole session.
     *
     * @return the action's result, or null if it failed
     */
    private static <T> T guard(Supplier<T> action) {

        try {
            return action.get();

        } catch (ConsoleIO.InputClosedException e) {
            throw e;   // let main() end the program cleanly

        } catch (ConsoleIO.InputAbortedException e) {
            System.out.println("Too many invalid entries. Returning to the menu.");

        } catch (DataAccessException e) {
            System.out.println(e.getMessage());

        } catch (ConfigException e) {
            System.out.println("Configuration problem: " + e.getMessage());

        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Unexpected error", e);
            System.out.println("An unexpected error occurred. "
                    + "Details were written to loan-system.log.");
        }

        return null;
    }
}

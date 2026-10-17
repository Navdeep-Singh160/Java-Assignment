package loan;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Administrator screens for managing users. */
public final class Admin {

    private Admin() { }

    public static void viewAllUsers() {

        List<User> users = UserDao.findAll();

        System.out.println("----- All Users -----");

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        for (User user : users) {
            printUser(user);
        }
    }

    public static void searchUser() {

        String query = ConsoleIO.readLine("Enter username or email to search: ");

        if (query.isEmpty()) {
            System.out.println("Please enter a username or email.");
            return;
        }

        Optional<User> found = UserDao.findByLogin(query);

        if (found.isPresent()) {
            System.out.println("----- User Found -----");
            printUser(found.get());
        } else {
            System.out.println("User not found.");
        }
    }

    /** Blank answers keep the current value, so a field can no longer be wiped by accident. */
    public static void editUser() {

        String username = ConsoleIO.readLine("Enter username to edit: ");

        Optional<User> found = UserDao.findByUsername(username);

        if (found.isEmpty()) {
            System.out.println("User not found.");
            return;
        }

        User user = found.get();

        String fullName = optionalInput("Enter new full name", user.getFullName(), Validator::fullName);
        String email = optionalInput("Enter new email", user.getEmail(), Validator::email)
                .toLowerCase(Locale.ROOT);
        String mobile = optionalInput("Enter new mobile number", user.getMobile(), Validator::mobile);

        try {
            if (UserDao.updateProfile(user.getUsername(), fullName, email, mobile)) {
                System.out.println("User details updated successfully!");
            } else {
                System.out.println("User not found.");
            }

        } catch (DataAccessException e) {

            if (e.isDuplicate()) {
                System.out.println("That email is already used by another account.");
            } else {
                throw e;
            }
        }
    }

    /** Activate (unlock) or deactivate (lock) an account. */
    public static void changeUserStatus(User actor) {

        String username = ConsoleIO.readLine("Enter username: ");

        Optional<User> found = UserDao.findByUsername(username);

        if (found.isEmpty()) {
            System.out.println("User not found.");
            return;
        }

        User target = found.get();

        System.out.println("1. Activate");
        System.out.println("2. Deactivate");

        boolean lock = ConsoleIO.readInt("Enter choice: ", 1, 2) == 2;

        if (lock && target.getId() == actor.getId()) {
            System.out.println("You cannot deactivate your own account.");
            return;
        }

        if (!UserDao.setLocked(target.getUsername(), lock)) {
            System.out.println("User not found.");
            return;
        }

        System.out.println(lock ? "User deactivated successfully!" : "User activated successfully!");

        EmailNotification.sendEmail(target.getEmail(),
                lock ? "Your account has been deactivated by the administrator."
                     : "Your account has been activated by the administrator.");
    }

    public static void deleteUser(User actor) {

        String username = ConsoleIO.readLine("Enter username to delete: ");

        Optional<User> found = UserDao.findByUsername(username);

        if (found.isEmpty()) {
            System.out.println("User not found.");
            return;
        }

        User target = found.get();

        if (target.getId() == actor.getId()) {
            System.out.println("You cannot delete your own account.");
            return;
        }

        if (target.isAdmin() && UserDao.countAdmins() <= 1) {
            System.out.println("The last administrator account cannot be deleted.");
            return;
        }

        if (!ConsoleIO.confirm("Permanently delete '" + target.getUsername() + "'?")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        try {
            if (UserDao.delete(target.getUsername())) {
                System.out.println("User deleted successfully!");
            } else {
                System.out.println("User not found.");
            }

        } catch (DataAccessException e) {

            if (e.isConstraintViolation()) {
                System.out.println("This user has loan applications on record and cannot be "
                        + "deleted. Deactivate the account instead.");
            } else {
                throw e;
            }
        }
    }

    // ---------- helpers ----------

    /** Prompts with the current value; blank input keeps it, anything else must validate. */
    private static String optionalInput(String label, String current,
                                        java.util.function.Function<String, String> validator) {

        String entered = ConsoleIO.readValidated(
                label + " [" + current + "] (blank to keep): ",
                value -> value.isEmpty() ? null : validator.apply(value));

        return entered.isEmpty() ? current : entered;
    }

    private static void printUser(User user) {

        System.out.println("ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Name: " + user.getFullName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Mobile: " + user.getMobile());
        System.out.println("Role: " + user.getRole());
        System.out.println("Account Locked: " + user.isAccountLocked());
        System.out.println("---------------------");
    }
}

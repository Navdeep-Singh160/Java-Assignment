package loan;

import java.util.logging.Level;

/**
 * Simulated email delivery (prints to the console).
 * Never put passwords in a message; reset codes are the only secret sent.
 */
public final class EmailNotification {

    private EmailNotification() { }

    public static void sendEmail(String email, String message) {

        System.out.println("----- Email Notification -----");
        System.out.println("To: " + email);
        System.out.println("Message: " + message);
        System.out.println("------------------------------");
    }

    /**
     * Looks up the user's email and notifies them. A notification problem must
     * never undo or hide the action that triggered it, so errors are logged only.
     * (Replaces the same "find email, then send" code that was copied five times.)
     */
    public static void sendToUser(String username, String message) {

        try {
            UserDao.findByUsername(username)
                    .ifPresent(user -> sendEmail(user.getEmail(), message));

        } catch (DataAccessException e) {
            AppLog.get(EmailNotification.class).log(Level.WARNING,
                    "Could not look up email for notification", e);
            System.out.println("(The user could not be notified by email.)");
        }
    }
}

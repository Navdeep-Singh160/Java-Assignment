package loan;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

/**
 * Password reset with a one-time code.
 *
 * The old version let anyone who knew a registered email address set a new
 * password for that account. Now a random 6-digit code is "emailed" (printed by
 * the simulated {@link EmailNotification}), is stored only as a hash, expires
 * after {@link #CODE_VALID_MINUTES} minutes and allows
 * {@link ConsoleIO#MAX_ATTEMPTS} guesses.
 *
 * The same message is shown whether or not the email is registered, so the
 * screen cannot be used to find out who has an account.
 */
public final class ForgotPassword {

    private static final int CODE_VALID_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ForgotPassword() { }

    public static void resetPassword() {

        String email = ConsoleIO.readValidated("Enter registered email: ", Validator::email)
                .toLowerCase(Locale.ROOT);

        Optional<User> found = UserDao.findByEmail(email);

        if (found.isPresent()) {
            issueCode(found.get(), email);
        }

        System.out.println("If that email is registered, a reset code has been sent to it.");

        if (!codeAccepted(found)) {
            System.out.println("Invalid or expired reset code.");
            found.ifPresent(user -> UserDao.clearResetToken(user.getId()));
            return;
        }

        User user = found.get();

        String newPassword = ConsoleIO.readNewPassword("Enter new password: ");

        UserDao.completePasswordReset(user.getId(), PasswordUtil.hash(newPassword));

        System.out.println("Password changed successfully! You can now log in.");
    }

    private static void issueCode(User user, String email) {

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        UserDao.saveResetToken(user.getId(), PasswordUtil.hash(code), CODE_VALID_MINUTES);

        EmailNotification.sendEmail(email,
                "Your password reset code is " + code
                + ". It is valid for " + CODE_VALID_MINUTES + " minutes. "
                + "If you did not request this, ignore this message.");
    }

    private static boolean codeAccepted(Optional<User> user) {

        for (int attempt = 1; attempt <= ConsoleIO.MAX_ATTEMPTS; attempt++) {

            String entered = ConsoleIO.readLine("Enter reset code: ");

            if (user.isEmpty()) {
                // same delay and same prompts as a real account
                PasswordUtil.burnTime(entered);
                continue;
            }

            Optional<String> tokenHash = UserDao.findValidResetTokenHash(user.get().getId());

            if (tokenHash.isPresent() && PasswordUtil.verify(entered, tokenHash.get())) {
                return true;
            }

            if (tokenHash.isEmpty()) {
                return false;   // expired or already used
            }

            System.out.println("Incorrect code.");
        }

        return false;
    }
}

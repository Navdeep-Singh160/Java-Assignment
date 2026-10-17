package loan;

import java.util.Locale;
import java.util.Optional;

/**
 * "Submit PAN and CIBIL" screen (formerly LoanCalculator.processLoan()).
 *
 * The old version also replaced the customer's username and password with
 * generated ones ("loan<timestamp>" / "Loan@<cibil>") and printed and emailed
 * the password in plain text. That broke the link to the customer's existing
 * loan applications and created guessable passwords, so it has been removed:
 * this step now only stores PAN and CIBIL on the logged-in account.
 */
public final class CreditProfile {

    private CreditProfile() { }

    public static void submit(User sessionUser) {

        String pan = ConsoleIO.readValidated("Enter PAN number: ", Validator::pan)
                .toUpperCase(Locale.ROOT);

        int cibil = ConsoleIO.readInt(
                "Enter CIBIL score (" + LoanCalculator.MIN_CIBIL + "-"
                        + LoanCalculator.MAX_CIBIL + "): ",
                LoanCalculator.MIN_CIBIL, LoanCalculator.MAX_CIBIL);

        // The account is always the logged-in one; users can no longer type someone else's username.
        Optional<User> current = UserDao.findByUsername(sessionUser.getUsername());

        if (current.isEmpty()) {
            System.out.println("Your account could not be found. Please log in again.");
            return;
        }

        UserDao.saveCreditProfile(current.get().getId(), pan, cibil);

        System.out.println();
        System.out.println("PAN and CIBIL details saved successfully!");
        System.out.println("PAN: " + pan);
        System.out.println("CIBIL Score: " + cibil);
        System.out.println();
        System.out.println("Interest rates available to you:");

        for (LoanType type : LoanType.values()) {
            System.out.println("  " + type.displayName() + ": " + type.rateFor(cibil) + "% p.a.");
        }
    }
}

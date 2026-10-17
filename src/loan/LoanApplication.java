package loan;

import java.util.List;
import java.util.Optional;

/**
 * Loan application screens for customers and administrators.
 *
 * This class now owns the whole review workflow. Before, two admin menu items
 * (LoanApplication.reviewApplication and LoanApproval.reviewLoan) both changed
 * loan status with different rules; approving through the first one left EMI,
 * interest and total payable empty. There is now one path, and approval always
 * calculates the EMI through {@link LoanCalculator}.
 */
public final class LoanApplication {

    private LoanApplication() { }

    // ---------- customer ----------

    public static void applyForLoan(User sessionUser) {

        // Re-read the account so PAN/CIBIL submitted earlier in this session are seen.
        Optional<User> current = UserDao.findByUsername(sessionUser.getUsername());

        if (current.isEmpty()) {
            System.out.println("Your account could not be found. Please log in again.");
            return;
        }

        User user = current.get();

        if (user.getPan() == null || user.getCibilScore() == null) {
            System.out.println("Please submit your PAN and CIBIL score first "
                    + "(User Menu option 1). Your interest rate depends on it.");
            return;
        }

        int cibil = user.getCibilScore();
        LoanType[] types = LoanType.values();

        System.out.println();
        for (int i = 0; i < types.length; i++) {
            System.out.println((i + 1) + ". " + types[i].displayName()
                    + " (" + types[i].rateFor(cibil) + "% p.a.)");
        }

        LoanType type = types[ConsoleIO.readInt("Enter loan type: ", 1, types.length) - 1];

        double amount = ConsoleIO.readMoney("Enter loan amount: ",
                LoanCalculator.MIN_AMOUNT, LoanCalculator.MAX_AMOUNT);

        int months = ConsoleIO.readInt(
                "Enter duration in months (" + LoanCalculator.MIN_MONTHS + "-"
                        + LoanCalculator.MAX_MONTHS + "): ",
                LoanCalculator.MIN_MONTHS, LoanCalculator.MAX_MONTHS);

        double income = ConsoleIO.readMoney("Enter monthly income: ",
                LoanCalculator.MIN_INCOME, LoanCalculator.MAX_INCOME);

        String purpose = ConsoleIO.readValidated("Enter purpose of loan: ", Validator::purpose);

        double rate = type.rateFor(cibil);

        LoanCalculator.EmiResult estimate = LoanCalculator.calculate(amount, rate, months);

        int id = LoanDao.create(user.getUsername(), type, amount, months, income, purpose, rate);

        System.out.println();
        System.out.println("Loan application #" + id + " submitted successfully!");
        System.out.println("Loan Type: " + type.displayName());
        System.out.println("Interest Rate: " + rate + "%");
        System.out.println("Estimated EMI: " + money(estimate.emi()));
        System.out.println("Application status: " + LoanStatus.PENDING.dbValue());
    }

    public static void viewMyApplications(User sessionUser) {

        List<Loan> loans = LoanDao.findByUsername(sessionUser.getUsername());

        printLoans("Your Loan Applications", loans);
    }

    // ---------- admin ----------

    public static void viewApplications() {

        printLoans("Loan Applications", LoanDao.findAll());
    }

    /** Approve (with EMI), reject (with reason) or request more information. */
    public static void reviewApplication() {

        int id = ConsoleIO.readInt("Enter application ID: ", 1, Integer.MAX_VALUE);

        Optional<Loan> found = LoanDao.findById(id);

        if (found.isEmpty()) {
            System.out.println("Application not found.");
            return;
        }

        Loan loan = found.get();

        printLoan(loan);

        if (loan.isFinalised()) {
            System.out.println("This application is already " + loan.status()
                    + " and cannot be changed.");
            return;
        }

        System.out.println("1. Approve (calculates EMI)");
        System.out.println("2. Reject");
        System.out.println("3. Needs More Info");
        System.out.println("4. Cancel");

        switch (ConsoleIO.readInt("Enter choice: ", 1, 4)) {
            case 1 -> approve(loan);
            case 2 -> reject(loan);
            case 3 -> requestMoreInfo(loan);
            default -> System.out.println("No changes made.");
        }
    }

    private static void approve(Loan loan) {

        if (loan.interestRate() <= 0) {
            System.out.println("This application has no valid interest rate and cannot be approved.");
            return;
        }

        LoanCalculator.EmiResult result =
                LoanCalculator.calculate(loan.loanAmount(), loan.interestRate(), loan.duration());

        if (!LoanDao.markApproved(loan.id(), result.emi(),
                result.totalInterest(), result.totalPayable())) {
            System.out.println("This application was changed by someone else. "
                    + "Please open it again.");
            return;
        }

        System.out.println();
        System.out.println("Loan approved successfully!");
        System.out.println("Interest Rate: " + loan.interestRate() + "%");
        System.out.println("Monthly EMI: " + money(result.emi()));
        System.out.println("Total Interest: " + money(result.totalInterest()));
        System.out.println("Total Payable: " + money(result.totalPayable()));

        EmailNotification.sendToUser(loan.username(),
                "Your loan application #" + loan.id() + " has been approved. "
                + "Monthly EMI: " + money(result.emi())
                + ", Total Interest: " + money(result.totalInterest())
                + ", Total Payable: " + money(result.totalPayable()));
    }

    private static void reject(Loan loan) {

        String reason = ConsoleIO.readValidated("Enter rejection reason: ", Validator::reason);

        if (!LoanDao.markRejected(loan.id(), reason)) {
            System.out.println("This application was changed by someone else. "
                    + "Please open it again.");
            return;
        }

        System.out.println("Loan rejected successfully!");

        EmailNotification.sendToUser(loan.username(),
                "Your loan application #" + loan.id() + " has been rejected. Reason: " + reason);
    }

    private static void requestMoreInfo(Loan loan) {

        if (!LoanDao.markNeedsMoreInfo(loan.id())) {
            System.out.println("This application was changed by someone else. "
                    + "Please open it again.");
            return;
        }

        System.out.println("Application marked as '" + LoanStatus.NEEDS_MORE_INFO.dbValue() + "'.");

        EmailNotification.sendToUser(loan.username(),
                "More information is required for your loan application #" + loan.id() + ".");
    }

    // ---------- shared display ----------

    private static void printLoans(String title, List<Loan> loans) {

        System.out.println("----- " + title + " -----");

        if (loans.isEmpty()) {
            System.out.println("No applications found.");
            return;
        }

        for (Loan loan : loans) {
            printLoan(loan);
        }
    }

    private static void printLoan(Loan loan) {

        System.out.println("Application ID: " + loan.id());
        System.out.println("Username: " + loan.username());
        System.out.println("Loan Type: " + loan.loanType());
        System.out.println("Loan Amount: " + money(loan.loanAmount()));
        System.out.println("Duration: " + loan.duration() + " months");
        System.out.println("Income: " + money(loan.income()));
        System.out.println("Purpose: " + loan.purpose());
        System.out.println("Interest Rate: " + loan.interestRate() + "%");
        System.out.println("Status: " + loan.status());

        if (loan.rejectionReason() != null && !loan.rejectionReason().isBlank()) {
            System.out.println("Rejection Reason: " + loan.rejectionReason());
        }

        if (loan.emi() != null) {
            System.out.println("EMI: " + money(loan.emi()));
            System.out.println("Total Interest: " + money(loan.totalInterest()));
            System.out.println("Total Payable: " + money(loan.totalPayable()));
        }

        System.out.println("-----------------------------");
    }

    private static String money(double value) {
        return String.format("%,.2f", value);
    }
}

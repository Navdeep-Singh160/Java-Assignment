package loan;

/**
 * The single place where loan products and their interest rates are defined.
 *
 * Previously the application form used a flat rate per type while the PAN/CIBIL
 * step used CIBIL-based rates, so the same customer saw two different rates.
 * Every screen now calls {@link #rateFor(int)}.
 */
public enum LoanType {

    //            display name     rate if CIBIL >= 750   rate otherwise
    PERSONAL_LOAN("Personal Loan", 10.0, 12.0),
    HOME_LOAN("Home Loan", 7.5, 9.0),
    CAR_LOAN("Car Loan", 8.5, 10.0);

    private final String displayName;
    private final double preferredRate;
    private final double standardRate;

    LoanType(String displayName, double preferredRate, double standardRate) {
        this.displayName = displayName;
        this.preferredRate = preferredRate;
        this.standardRate = standardRate;
    }

    public String displayName() {
        return displayName;
    }

    /** Annual interest rate (percent) for a customer with the given CIBIL score. */
    public double rateFor(int cibilScore) {
        return cibilScore >= LoanCalculator.PREFERRED_CIBIL ? preferredRate : standardRate;
    }
}

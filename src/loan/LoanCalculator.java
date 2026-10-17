package loan;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure loan maths and business limits. No database or console access, so it
 * can be tested on its own and is shared by every screen that needs EMI figures.
 */
public final class LoanCalculator {

    // ----- business limits (used for input validation everywhere) -----
    public static final int MIN_CIBIL = 600;
    public static final int MAX_CIBIL = 900;
    public static final int PREFERRED_CIBIL = 750;

    public static final double MIN_AMOUNT = 1_000;
    public static final double MAX_AMOUNT = 100_000_000;

    public static final int MIN_MONTHS = 1;
    public static final int MAX_MONTHS = 360;

    public static final double MIN_INCOME = 1;
    public static final double MAX_INCOME = 100_000_000;

    /** EMI, total payable and total interest, rounded to 2 decimals. */
    public record EmiResult(double emi, double totalPayable, double totalInterest) { }

    private LoanCalculator() { }

    /**
     * Standard reducing-balance EMI:  P * r * (1+r)^n / ((1+r)^n - 1)
     *
     * @param principal         loan amount (must be &gt; 0)
     * @param annualRatePercent yearly interest rate in percent (0 is allowed)
     * @param months            duration in months (must be &gt; 0)
     */
    public static EmiResult calculate(double principal, double annualRatePercent, int months) {

        if (!Double.isFinite(principal) || principal <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero.");
        }

        if (!Double.isFinite(annualRatePercent) || annualRatePercent < 0) {
            throw new IllegalArgumentException("Interest rate must not be negative.");
        }

        if (months <= 0) {
            throw new IllegalArgumentException("Duration must be at least one month.");
        }

        double rawEmi;

        if (annualRatePercent == 0) {

            rawEmi = principal / months;

        } else {

            double monthlyRate = annualRatePercent / 12 / 100;
            double growth = Math.pow(1 + monthlyRate, months);

            rawEmi = principal * monthlyRate * growth / (growth - 1);
        }

        BigDecimal emi = BigDecimal.valueOf(rawEmi).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPayable = emi.multiply(BigDecimal.valueOf(months));
        BigDecimal totalInterest = totalPayable
                .subtract(BigDecimal.valueOf(principal))
                .setScale(2, RoundingMode.HALF_UP);

        return new EmiResult(
                emi.doubleValue(),
                totalPayable.doubleValue(),
                totalInterest.doubleValue());
    }
}

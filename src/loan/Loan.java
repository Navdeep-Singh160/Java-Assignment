package loan;

/** A row of the loan_applications table. Money columns that are only filled on approval are nullable. */
public record Loan(
        int id,
        String username,
        String loanType,
        double loanAmount,
        int duration,
        double income,
        String purpose,
        String status,
        String rejectionReason,
        double interestRate,
        Double emi,
        Double totalInterest,
        Double totalPayable) {

    public boolean isFinalised() {
        return LoanStatus.APPROVED.dbValue().equals(status)
                || LoanStatus.REJECTED.dbValue().equals(status);
    }
}

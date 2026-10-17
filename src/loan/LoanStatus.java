package loan;

/** Allowed values of loan_applications.status (same text as stored in the database). */
public enum LoanStatus {

    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    NEEDS_MORE_INFO("Needs More Info");

    private final String dbValue;

    LoanStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    /** Approved and rejected decisions are final and cannot be changed. */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
}

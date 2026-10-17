package loan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** All SQL that touches the loan_applications table. Every query is parameterised. */
public final class LoanDao {

    private static final String COLUMNS =
            "id, username, loan_type, loan_amount, duration, income, purpose, status, "
            + "rejection_reason, interest_rate, emi, total_interest, total_payable";

    // A decision can only be made on applications that are still open.
    private static final String OPEN_STATES_GUARD = "AND status IN ('Pending', 'Needs More Info')";

    private LoanDao() { }

    private static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Loan map(ResultSet rs) throws SQLException {
        return new Loan(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("loan_type"),
                rs.getDouble("loan_amount"),
                rs.getInt("duration"),
                rs.getDouble("income"),
                rs.getString("purpose"),
                rs.getString("status"),
                rs.getString("rejection_reason"),
                rs.getDouble("interest_rate"),
                nullableDouble(rs, "emi"),
                nullableDouble(rs, "total_interest"),
                nullableDouble(rs, "total_payable"));
    }

    /** @return the id of the new application */
    public static int create(String username, LoanType type, double amount, int months,
                             double income, String purpose, double interestRate) {

        return Db.insertReturningId(
                "INSERT INTO loan_applications "
                + "(username, loan_type, loan_amount, duration, income, purpose, interest_rate) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                "submitting the loan application",
                ps -> {
                    ps.setString(1, username);
                    ps.setString(2, type.displayName());
                    ps.setDouble(3, amount);
                    ps.setInt(4, months);
                    ps.setDouble(5, income);
                    ps.setString(6, purpose);
                    ps.setDouble(7, interestRate);
                });
    }

    public static List<Loan> findAll() {
        return Db.query(
                "SELECT " + COLUMNS + " FROM loan_applications ORDER BY id",
                "listing loan applications",
                Db.NO_PARAMS,
                LoanDao::map);
    }

    public static List<Loan> findByUsername(String username) {
        return Db.query(
                "SELECT " + COLUMNS + " FROM loan_applications WHERE username = ? ORDER BY id",
                "listing your loan applications",
                ps -> ps.setString(1, username),
                LoanDao::map);
    }

    public static Optional<Loan> findById(int id) {
        return Db.queryOne(
                "SELECT " + COLUMNS + " FROM loan_applications WHERE id = ?",
                "looking up the loan application",
                ps -> ps.setInt(1, id),
                LoanDao::map);
    }

    /** @return false if the application is not open any more (already decided, or deleted) */
    public static boolean markApproved(int id, double emi, double totalInterest, double totalPayable) {
        return Db.update(
                "UPDATE loan_applications SET status = 'Approved', rejection_reason = NULL, "
                + "emi = ?, total_interest = ?, total_payable = ? WHERE id = ? " + OPEN_STATES_GUARD,
                "approving the loan",
                ps -> {
                    ps.setDouble(1, emi);
                    ps.setDouble(2, totalInterest);
                    ps.setDouble(3, totalPayable);
                    ps.setInt(4, id);
                }) > 0;
    }

    /** @return false if the application is not open any more */
    public static boolean markRejected(int id, String reason) {
        return Db.update(
                "UPDATE loan_applications SET status = 'Rejected', rejection_reason = ?, "
                + "emi = NULL, total_interest = NULL, total_payable = NULL WHERE id = ? "
                + OPEN_STATES_GUARD,
                "rejecting the loan",
                ps -> {
                    ps.setString(1, reason);
                    ps.setInt(2, id);
                }) > 0;
    }

    /** @return false if the application is not open any more */
    public static boolean markNeedsMoreInfo(int id) {
        return Db.update(
                "UPDATE loan_applications SET status = 'Needs More Info' WHERE id = ? "
                + OPEN_STATES_GUARD,
                "updating the loan status",
                ps -> ps.setInt(1, id)) > 0;
    }
}

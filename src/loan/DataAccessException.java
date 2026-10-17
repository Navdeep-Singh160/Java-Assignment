package loan;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Unchecked wrapper for database failures.
 *
 * The message is safe to show to the user. The technical details (SQL state,
 * vendor code, stack trace) are written to the log file instead of the console.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    private final String sqlState;
    private final int vendorCode;

    public DataAccessException(String message, Throwable cause, String sqlState, int vendorCode) {
        super(message, cause);
        this.sqlState = sqlState;
        this.vendorCode = vendorCode;
    }

    public DataAccessException(String message, Throwable cause) {
        this(message, cause, null, 0);
    }

    /** Logs the technical details and returns a user-friendly exception. */
    public static DataAccessException of(String action, SQLException e) {

        AppLog.get(DataAccessException.class).log(Level.SEVERE,
                "Database error while " + action
                        + " [SQLState=" + e.getSQLState()
                        + ", code=" + e.getErrorCode() + "]", e);

        return new DataAccessException(
                "A database error occurred while " + action
                        + ". Please try again or contact support.",
                e, e.getSQLState(), e.getErrorCode());
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getVendorCode() {
        return vendorCode;
    }

    /** True when a UNIQUE constraint (for example username or email) was violated. */
    public boolean isDuplicate() {
        return vendorCode == MYSQL_DUPLICATE_ENTRY;
    }

    /** True for any integrity constraint violation (duplicate key, foreign key, check). */
    public boolean isConstraintViolation() {
        return sqlState != null && sqlState.startsWith("23");
    }
}

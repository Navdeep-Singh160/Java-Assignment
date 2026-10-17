package loan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** All SQL that touches the users table lives here. Every query is parameterised. */
public final class UserDao {

    private static final String COLUMNS =
            "id, username, full_name, email, mobile, password, role, "
            + "failed_attempts, account_locked, pan, cibil_score";

    /** Result of recording a failed login. */
    public record AttemptState(int failedAttempts, boolean locked) { }

    private UserDao() { }

    private static User map(ResultSet rs) throws SQLException {

        int cibil = rs.getInt("cibil_score");
        Integer cibilScore = rs.wasNull() ? null : cibil;

        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("mobile"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getInt("failed_attempts"),
                rs.getBoolean("account_locked"),
                rs.getString("pan"),
                cibilScore);
    }

    // ---------- lookups ----------

    public static Optional<User> findByLogin(String usernameOrEmail) {
        return Db.queryOne(
                "SELECT " + COLUMNS + " FROM users WHERE username = ? OR email = ?",
                "looking up the account",
                ps -> {
                    ps.setString(1, usernameOrEmail);
                    ps.setString(2, usernameOrEmail);
                },
                UserDao::map);
    }

    public static Optional<User> findByUsername(String username) {
        return Db.queryOne(
                "SELECT " + COLUMNS + " FROM users WHERE username = ?",
                "looking up the user",
                ps -> ps.setString(1, username),
                UserDao::map);
    }

    public static Optional<User> findByEmail(String email) {
        return Db.queryOne(
                "SELECT " + COLUMNS + " FROM users WHERE email = ?",
                "looking up the email address",
                ps -> ps.setString(1, email),
                UserDao::map);
    }

    public static List<User> findAll() {
        return Db.query(
                "SELECT " + COLUMNS + " FROM users ORDER BY id",
                "listing users",
                Db.NO_PARAMS,
                UserDao::map);
    }

    public static int countAdmins() {
        return Db.query(
                "SELECT COUNT(*) FROM users WHERE role = ?",
                "counting administrators",
                ps -> ps.setString(1, User.ROLE_ADMIN),
                rs -> rs.getInt(1)).get(0);
    }

    // ---------- create / update / delete ----------

    /** @throws DataAccessException with isDuplicate() true if username or email already exists */
    public static void create(String username, String fullName, String email,
                              String mobile, String passwordHash, String role) {

        Db.update(
                "INSERT INTO users (username, full_name, email, mobile, password, role) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                "creating the account",
                ps -> {
                    ps.setString(1, username);
                    ps.setString(2, fullName);
                    ps.setString(3, email);
                    ps.setString(4, mobile);
                    ps.setString(5, passwordHash);
                    ps.setString(6, role);
                });
    }

    public static boolean updateProfile(String username, String fullName,
                                        String email, String mobile) {

        return Db.update(
                "UPDATE users SET full_name = ?, email = ?, mobile = ? WHERE username = ?",
                "updating the user",
                ps -> {
                    ps.setString(1, fullName);
                    ps.setString(2, email);
                    ps.setString(3, mobile);
                    ps.setString(4, username);
                }) > 0;
    }

    /**
     * Locks (deactivates) or unlocks (activates) an account.
     * Unlocking also clears the failed-attempt counter; before, a re-activated
     * user stayed one wrong password away from being locked again.
     */
    public static boolean setLocked(String username, boolean locked) {

        String sql = locked
                ? "UPDATE users SET account_locked = TRUE WHERE username = ?"
                : "UPDATE users SET account_locked = FALSE, failed_attempts = 0 WHERE username = ?";

        return Db.update(sql, "changing the account status",
                ps -> ps.setString(1, username)) > 0;
    }

    public static boolean delete(String username) {
        return Db.update(
                "DELETE FROM users WHERE username = ?",
                "deleting the user",
                ps -> ps.setString(1, username)) > 0;
    }

    public static void saveCreditProfile(int userId, String pan, int cibilScore) {
        Db.update(
                "UPDATE users SET pan = ?, cibil_score = ? WHERE id = ?",
                "saving PAN and CIBIL details",
                ps -> {
                    ps.setString(1, pan);
                    ps.setInt(2, cibilScore);
                    ps.setInt(3, userId);
                });
    }

    // ---------- login attempts ----------

    /**
     * Counts a failed login in one atomic statement (no read-then-write race)
     * and locks the account once the limit is reached.
     * In MySQL the SET assignments run left to right, so account_locked is
     * evaluated against the counter value from before the increment.
     */
    public static AttemptState recordFailedLogin(int userId, int maxAttempts) {

        Db.update(
                "UPDATE users SET account_locked = (account_locked OR failed_attempts + 1 >= ?), "
                + "failed_attempts = failed_attempts + 1 WHERE id = ?",
                "recording the failed login",
                ps -> {
                    ps.setInt(1, maxAttempts);
                    ps.setInt(2, userId);
                });

        return Db.queryOne(
                "SELECT failed_attempts, account_locked FROM users WHERE id = ?",
                "recording the failed login",
                ps -> ps.setInt(1, userId),
                rs -> new AttemptState(rs.getInt(1), rs.getBoolean(2)))
                .orElse(new AttemptState(maxAttempts, true));
    }

    public static void resetFailedAttempts(int userId) {
        Db.update(
                "UPDATE users SET failed_attempts = 0 WHERE id = ?",
                "resetting the login counter",
                ps -> ps.setInt(1, userId));
    }

    // ---------- passwords ----------

    /** Replaces the stored password hash (also used to upgrade old plain-text passwords). */
    public static void updatePasswordHash(int userId, String passwordHash) {
        Db.update(
                "UPDATE users SET password = ? WHERE id = ?",
                "updating the password",
                ps -> {
                    ps.setString(1, passwordHash);
                    ps.setInt(2, userId);
                });
    }

    /** Sets a new password after a successful reset and invalidates the reset code. */
    public static void completePasswordReset(int userId, String passwordHash) {
        Db.update(
                "UPDATE users SET password = ?, failed_attempts = 0, "
                + "reset_token_hash = NULL, reset_token_expiry = NULL WHERE id = ?",
                "resetting the password",
                ps -> {
                    ps.setString(1, passwordHash);
                    ps.setInt(2, userId);
                });
    }

    public static void saveResetToken(int userId, String tokenHash, int validMinutes) {
        Db.update(
                "UPDATE users SET reset_token_hash = ?, "
                + "reset_token_expiry = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?",
                "creating a reset code",
                ps -> {
                    ps.setString(1, tokenHash);
                    ps.setInt(2, validMinutes);
                    ps.setInt(3, userId);
                });
    }

    /** Hash of the reset code, only if it has not expired. */
    public static Optional<String> findValidResetTokenHash(int userId) {
        return Db.queryOne(
                "SELECT reset_token_hash FROM users WHERE id = ? "
                + "AND reset_token_hash IS NOT NULL AND reset_token_expiry > NOW()",
                "checking the reset code",
                ps -> ps.setInt(1, userId),
                rs -> rs.getString(1));
    }

    public static void clearResetToken(int userId) {
        Db.update(
                "UPDATE users SET reset_token_hash = NULL, reset_token_expiry = NULL WHERE id = ?",
                "clearing the reset code",
                ps -> ps.setInt(1, userId));
    }

    /**
     * One-time migration: hashes every password that is still stored as plain text.
     * @return number of passwords converted
     */
    public static int migratePlaintextPasswords() {

        List<String[]> legacy = Db.query(
                "SELECT id, password FROM users WHERE LEFT(password, 14) <> 'pbkdf2_sha256$'",
                "finding plain-text passwords",
                Db.NO_PARAMS,
                rs -> new String[] { String.valueOf(rs.getInt(1)), rs.getString(2) });

        for (String[] row : legacy) {
            updatePasswordHash(Integer.parseInt(row[0]), PasswordUtil.hash(row[1]));
        }

        return legacy.size();
    }
}

package loan;

/**
 * One-time tool for databases created with the old version: converts every
 * plain-text password to a PBKDF2 hash. Safe to run more than once.
 *
 * Run:  java -cp bin:mysql-connector-j.jar loan.MigratePasswords
 */
public class MigratePasswords {

    public static void main(String[] args) {

        AppLog.init();

        try {
            int converted = UserDao.migratePlaintextPasswords();

            System.out.println(converted + " plain-text password(s) converted to hashes.");

        } catch (DataAccessException | ConfigException e) {

            System.out.println("Migration failed: " + e.getMessage());
        }
    }
}

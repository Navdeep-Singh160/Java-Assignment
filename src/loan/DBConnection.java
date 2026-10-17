package loan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Opens database connections using settings from {@link AppConfig}.
 * Callers must close the connection (use try-with-resources).
 */
public final class DBConnection {

    private DBConnection() { }

    /**
     * @return an open connection; never null
     * @throws ConfigException      if the connection settings are missing
     * @throws DataAccessException  if the database cannot be reached
     */
    public static Connection getConnection() {

        String url = AppConfig.dbUrl();
        String user = AppConfig.dbUser();
        String password = AppConfig.dbPassword();

        try {
            return DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {

            AppLog.get(DBConnection.class).log(Level.SEVERE,
                    "Database connection failed [SQLState=" + e.getSQLState() + "]", e);

            throw new DataAccessException(
                    "Could not connect to the database. Check your settings in "
                    + "config.properties, that MySQL is running, and that the "
                    + "MySQL JDBC driver is on the classpath.", e);
        }
    }
}

package loan;

import java.sql.Connection;
import java.sql.SQLException;

/** Run this class to check that config.properties and the MySQL driver are set up correctly. */
public class TestConnection {

    public static void main(String[] args) {

        AppLog.init();

        try (Connection con = DBConnection.getConnection()) {

            System.out.println(con.isValid(5)
                    ? "Connection test successful!"
                    : "Connection opened but did not respond.");

        } catch (DataAccessException | ConfigException | SQLException e) {

            System.out.println("Connection test failed: " + e.getMessage());
        }
    }
}

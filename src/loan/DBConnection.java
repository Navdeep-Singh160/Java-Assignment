package loan;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {

        Connection con = null;

        try {
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/loan_system",
                "root",
                "Navdeep@2006"
            );

            System.out.println("Database connected successfully!");

        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }

        return con;
    }
}


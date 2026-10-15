package loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Login {

    public static String loginUser() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username or email: ");
        String usernameOrEmail = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                boolean locked = rs.getBoolean("account_locked");
                int failedAttempts = rs.getInt("failed_attempts");
                String role = rs.getString("role");

                if (locked) {

                    System.out.println("Account is locked.");
                    return null;

                } else if (rs.getString("password").equals(password)) {

                    System.out.println("Login successful!");
                    System.out.println("Role: " + role);

                    String resetSql =
                            "UPDATE users SET failed_attempts = 0 WHERE id = ?";

                    PreparedStatement resetPs =
                            con.prepareStatement(resetSql);

                    resetPs.setInt(1, rs.getInt("id"));
                    resetPs.executeUpdate();

                    resetPs.close();
                    rs.close();
                    ps.close();
                    con.close();

                    return role;

                } else {

                    failedAttempts++;

                    if (failedAttempts >= 3) {

                        String lockSql =
                                "UPDATE users SET failed_attempts = ?, account_locked = TRUE WHERE id = ?";

                        PreparedStatement lockPs =
                                con.prepareStatement(lockSql);

                        lockPs.setInt(1, failedAttempts);
                        lockPs.setInt(2, rs.getInt("id"));

                        lockPs.executeUpdate();
                        lockPs.close();

                        System.out.println("Wrong password.");
                        System.out.println("Account locked after 3 failed attempts.");

                    } else {

                        String updateSql =
                                "UPDATE users SET failed_attempts = ? WHERE id = ?";

                        PreparedStatement updatePs =
                                con.prepareStatement(updateSql);

                        updatePs.setInt(1, failedAttempts);
                        updatePs.setInt(2, rs.getInt("id"));

                        updatePs.executeUpdate();
                        updatePs.close();

                        System.out.println("Wrong password.");
                        System.out.println("Failed attempts: " + failedAttempts);
                    }
                }

            } else {

                System.out.println("User not found.");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Login failed.");
            e.printStackTrace();
        }

        return null;
    }
}







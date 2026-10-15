package loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class ForgotPassword {

    public static void resetPassword() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter registered email: ");
        String email = sc.nextLine();

        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine();

        try {
            Connection con = DBConnection.getConnection();

            String checkSql = "SELECT * FROM users WHERE email = ?";

            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, email);

            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {

                String updateSql =
                        "UPDATE users SET password = ? WHERE email = ?";

                PreparedStatement updatePs =
                        con.prepareStatement(updateSql);

                updatePs.setString(1, newPassword);
                updatePs.setString(2, email);

                updatePs.executeUpdate();

                System.out.println("Password changed successfully!");

                updatePs.close();

            } else {
                System.out.println("Email not registered.");
            }

            rs.close();
            checkPs.close();
            con.close();

        } catch (Exception e) {
            System.out.println("Password reset failed.");
            e.printStackTrace();
        }
    }
}



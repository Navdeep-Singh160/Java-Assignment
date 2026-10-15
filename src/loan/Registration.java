package loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class Registration {

    public static void registerUser() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter full name: ");
        String fullName = sc.nextLine();

        System.out.print("Enter email: ");
        String email = sc.nextLine();

        System.out.print("Enter mobile number: ");
        String mobile = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "INSERT INTO users "
                   + "(username, full_name, email, mobile, password) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setString(4, mobile);
            ps.setString(5, password);

            ps.executeUpdate();

            System.out.println("Registration successful!");

            ps.close();
            con.close();

        } catch (Exception e) {
            System.out.println("Registration failed!");
            e.printStackTrace();
        }
    }
}



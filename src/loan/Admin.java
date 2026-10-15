package loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Admin {

    // View all users
    public static void viewAllUsers() {

        String sql = "SELECT id, username, full_name, email, mobile, role, account_locked FROM users";

        try {

            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            System.out.println("----- All Users -----");

            while (rs.next()) {

                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Name: " + rs.getString("full_name"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Mobile: " + rs.getString("mobile"));
                System.out.println("Role: " + rs.getString("role"));
                System.out.println("Account Locked: " + rs.getBoolean("account_locked"));
                System.out.println("---------------------");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to view users.");
            e.printStackTrace();
        }
    }


    // Search user
    public static void searchUser() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username to search: ");
        String username = sc.nextLine();

        String sql = "SELECT * FROM users WHERE username = ?";

        try {

            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println("----- User Found -----");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Name: " + rs.getString("full_name"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Mobile: " + rs.getString("mobile"));
                System.out.println("Role: " + rs.getString("role"));
                System.out.println("Account Locked: " + rs.getBoolean("account_locked"));

            } else {

                System.out.println("User not found.");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to search user.");
            e.printStackTrace();
        }
    }


    // Edit user
    public static void editUser() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username to edit: ");
        String username = sc.nextLine();

        System.out.print("Enter new full name: ");
        String fullName = sc.nextLine();

        System.out.print("Enter new email: ");
        String email = sc.nextLine();

        System.out.print("Enter new mobile number: ");
        String mobile = sc.nextLine();

        String sql = "UPDATE users SET full_name = ?, email = ?, mobile = ? WHERE username = ?";

        try {

            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, mobile);
            ps.setString(4, username);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("User details updated successfully!");

            } else {

                System.out.println("User not found.");
            }

            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to edit user.");
            e.printStackTrace();
        }
    }


    // Activate or deactivate user
    public static void changeUserStatus() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter 1 to Activate or 2 to Deactivate: ");
        int choice = sc.nextInt();

        boolean locked;

        if (choice == 1) {

            locked = false;

        } else if (choice == 2) {

            locked = true;

        } else {

            System.out.println("Invalid choice.");
            return;
        }

        String sql = "UPDATE users SET account_locked = ? WHERE username = ?";

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setBoolean(1, locked);
            ps.setString(2, username);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                // Get user's email
                String emailSql = "SELECT email FROM users WHERE username = ?";

                PreparedStatement emailPs = con.prepareStatement(emailSql);

                emailPs.setString(1, username);

                ResultSet emailRs = emailPs.executeQuery();

                if (emailRs.next()) {

                    String email = emailRs.getString("email");

                    if (locked) {

                        System.out.println("User deactivated successfully!");

                        EmailNotification.sendEmail(
                            email,
                            "Your account has been deactivated by the administrator."
                        );

                    } else {

                        System.out.println("User activated successfully!");

                        EmailNotification.sendEmail(
                            email,
                            "Your account has been activated by the administrator."
                        );
                    }
                }

                emailRs.close();
                emailPs.close();

            } else {

                System.out.println("User not found.");
            }

            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to change user status.");
            e.printStackTrace();
        }
    }


    // Delete user
    public static void deleteUser() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username to delete: ");
        String username = sc.nextLine();

        String sql = "DELETE FROM users WHERE username = ?";

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, username);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("User deleted successfully!");

            } else {

                System.out.println("User not found.");
            }

            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to delete user.");
            e.printStackTrace();
        }
    }
}










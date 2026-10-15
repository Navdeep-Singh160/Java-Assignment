package loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class LoanApplication {

    // Apply for loan
    public static void applyForLoan() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.println("1. Personal Loan");
        System.out.println("2. Home Loan");
        System.out.println("3. Car Loan");

        System.out.print("Enter loan type: ");
        int choice = sc.nextInt();

        String loanType;
        double interestRate;

        if (choice == 1) {

            loanType = "Personal Loan";
            interestRate = 10.0;

        } else if (choice == 2) {

            loanType = "Home Loan";
            interestRate = 7.5;

        } else if (choice == 3) {

            loanType = "Car Loan";
            interestRate = 8.5;

        } else {

            System.out.println("Invalid loan type.");
            return;
        }

        System.out.print("Enter loan amount: ");
        double loanAmount = sc.nextDouble();

        System.out.print("Enter duration in months: ");
        int duration = sc.nextInt();

        System.out.print("Enter monthly income: ");
        double income = sc.nextDouble();

        sc.nextLine();

        System.out.print("Enter purpose of loan: ");
        String purpose = sc.nextLine();


        String sql = "INSERT INTO loan_applications "
                   + "(username, loan_type, loan_amount, duration, income, purpose, interest_rate) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";


        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, loanType);
            ps.setDouble(3, loanAmount);
            ps.setInt(4, duration);
            ps.setDouble(5, income);
            ps.setString(6, purpose);
            ps.setDouble(7, interestRate);

            ps.executeUpdate();

            System.out.println();
            System.out.println("Loan application submitted successfully!");
            System.out.println("Loan Type: " + loanType);
            System.out.println("Interest Rate: " + interestRate + "%");
            System.out.println("Application status: Pending");

            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to submit loan application.");
            e.printStackTrace();
        }
    }


    // View all loan applications
    public static void viewApplications() {

        String sql = "SELECT * FROM loan_applications";

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            System.out.println("----- Loan Applications -----");

            while (rs.next()) {

                System.out.println("Application ID: " + rs.getInt("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Loan Type: " + rs.getString("loan_type"));
                System.out.println("Loan Amount: " + rs.getDouble("loan_amount"));
                System.out.println("Duration: " + rs.getInt("duration") + " months");
                System.out.println("Income: " + rs.getDouble("income"));
                System.out.println("Purpose: " + rs.getString("purpose"));
                System.out.println("Interest Rate: " + rs.getDouble("interest_rate") + "%");
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Rejection Reason: " + rs.getString("rejection_reason"));
                System.out.println("EMI: " + rs.getDouble("emi"));
                System.out.println("Total Interest: " + rs.getDouble("total_interest"));
                System.out.println("Total Payable: " + rs.getDouble("total_payable"));
                System.out.println("-----------------------------");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to view applications.");
            e.printStackTrace();
        }
    }


    // Review loan application
    public static void reviewApplication() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter application ID: ");
        int id = sc.nextInt();

        System.out.println("1. Approve");
        System.out.println("2. Reject");
        System.out.println("3. Needs More Info");

        System.out.print("Enter choice: ");
        int choice = sc.nextInt();

        sc.nextLine();

        String status = "";
        String reason = "";

        if (choice == 1) {

            status = "Approved";

        } else if (choice == 2) {

            status = "Rejected";

            System.out.print("Enter rejection reason: ");
            reason = sc.nextLine();

        } else if (choice == 3) {

            status = "Needs More Info";

        } else {

            System.out.println("Invalid choice.");
            return;
        }


        String sql = "UPDATE loan_applications "
                   + "SET status = ?, rejection_reason = ? "
                   + "WHERE id = ?";


        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, status);
            ps.setString(2, reason);
            ps.setInt(3, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println("Loan application updated successfully!");

                // Get username
                String userSql =
                        "SELECT username FROM loan_applications WHERE id = ?";

                PreparedStatement userPs =
                        con.prepareStatement(userSql);

                userPs.setInt(1, id);

                ResultSet rs = userPs.executeQuery();

                if (rs.next()) {

                    String username = rs.getString("username");

                    // Get email
                    String emailSql =
                            "SELECT email FROM users WHERE username = ?";

                    PreparedStatement emailPs =
                            con.prepareStatement(emailSql);

                    emailPs.setString(1, username);

                    ResultSet emailRs = emailPs.executeQuery();

                    if (emailRs.next()) {

                        String email = emailRs.getString("email");

                        if (status.equals("Approved")) {

                            EmailNotification.sendEmail(
                                email,
                                "Your loan application has been approved."
                            );

                        } else if (status.equals("Rejected")) {

                            EmailNotification.sendEmail(
                                email,
                                "Your loan application has been rejected. Reason: "
                                + reason
                            );

                        } else {

                            EmailNotification.sendEmail(
                                email,
                                "More information is required for your loan application."
                            );
                        }
                    }

                    emailRs.close();
                    emailPs.close();
                }

                rs.close();
                userPs.close();

            } else {

                System.out.println("Application not found.");
            }

            ps.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to review application.");
            e.printStackTrace();
        }
    }
}







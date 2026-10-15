package loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class LoanApproval {

    public static void reviewLoan() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter application ID: ");
        int id = sc.nextInt();

        System.out.println("1. Approve Loan");
        System.out.println("2. Reject Loan");

        System.out.print("Enter choice: ");
        int choice = sc.nextInt();

        sc.nextLine();

        try {

            Connection con = DBConnection.getConnection();

            // Get loan details
            String selectSql =
                    "SELECT * FROM loan_applications WHERE id = ?";

            PreparedStatement selectPs =
                    con.prepareStatement(selectSql);

            selectPs.setInt(1, id);

            ResultSet rs = selectPs.executeQuery();

            if (rs.next()) {

                double loanAmount = rs.getDouble("loan_amount");
                int duration = rs.getInt("duration");
                String username = rs.getString("username");

                if (choice == 1) {

                    // Get interest rate
                    double interestRate = rs.getDouble("interest_rate");

                    // Convert annual interest rate to monthly rate
                    double monthlyRate =
                            interestRate / 12 / 100;

                    // EMI formula
                    double emi =
                            (loanAmount * monthlyRate
                            * Math.pow(1 + monthlyRate, duration))
                            /
                            (Math.pow(1 + monthlyRate, duration) - 1);

                    double totalPayable = emi * duration;

                    double totalInterest =
                            totalPayable - loanAmount;


                    // Update loan details
                    String updateSql =
                            "UPDATE loan_applications "
                            + "SET status = 'Approved', "
                            + "emi = ?, "
                            + "total_interest = ?, "
                            + "total_payable = ? "
                            + "WHERE id = ?";

                    PreparedStatement updatePs =
                            con.prepareStatement(updateSql);

                    updatePs.setDouble(1, emi);
                    updatePs.setDouble(2, totalInterest);
                    updatePs.setDouble(3, totalPayable);
                    updatePs.setInt(4, id);

                    updatePs.executeUpdate();


                    System.out.println();
                    System.out.println("Loan approved successfully!");
                    System.out.println("Interest Rate: "
                            + interestRate + "%");
                    System.out.println("Monthly EMI: "
                            + String.format("%.2f", emi));
                    System.out.println("Total Interest: "
                            + String.format("%.2f", totalInterest));
                    System.out.println("Total Payable: "
                            + String.format("%.2f", totalPayable));


                    // Get user email
                    String emailSql =
                            "SELECT email FROM users WHERE username = ?";

                    PreparedStatement emailPs =
                            con.prepareStatement(emailSql);

                    emailPs.setString(1, username);

                    ResultSet emailRs =
                            emailPs.executeQuery();

                    if (emailRs.next()) {

                        String email =
                                emailRs.getString("email");

                        EmailNotification.sendEmail(
                            email,
                            "Your loan has been approved. "
                            + "Monthly EMI: "
                            + String.format("%.2f", emi)
                            + ", Total Interest: "
                            + String.format("%.2f", totalInterest)
                            + ", Total Payable: "
                            + String.format("%.2f", totalPayable)
                        );
                    }

                    emailRs.close();
                    emailPs.close();
                    updatePs.close();

                } else if (choice == 2) {

                    System.out.print("Enter rejection reason: ");
                    String reason = sc.nextLine();


                    String updateSql =
                            "UPDATE loan_applications "
                            + "SET status = 'Rejected', "
                            + "rejection_reason = ? "
                            + "WHERE id = ?";

                    PreparedStatement updatePs =
                            con.prepareStatement(updateSql);

                    updatePs.setString(1, reason);
                    updatePs.setInt(2, id);

                    updatePs.executeUpdate();


                    System.out.println();
                    System.out.println("Loan rejected successfully!");


                    // Get user email
                    String emailSql =
                            "SELECT email FROM users WHERE username = ?";

                    PreparedStatement emailPs =
                            con.prepareStatement(emailSql);

                    emailPs.setString(1, username);

                    ResultSet emailRs =
                            emailPs.executeQuery();

                    if (emailRs.next()) {

                        String email =
                                emailRs.getString("email");

                        EmailNotification.sendEmail(
                            email,
                            "Your loan has been rejected. Reason: "
                            + reason
                        );
                    }

                    emailRs.close();
                    emailPs.close();
                    updatePs.close();

                } else {

                    System.out.println("Invalid choice.");
                }

            } else {

                System.out.println("Application not found.");
            }

            rs.close();
            selectPs.close();
            con.close();

        } catch (Exception e) {

            System.out.println("Unable to process loan approval.");
            e.printStackTrace();
        }
    }
}


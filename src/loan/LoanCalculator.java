package loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class LoanCalculator {

    public static void processLoan() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter PAN number: ");
        String pan = sc.nextLine();

        // PAN validation
        if (!pan.matches("[A-Z]{5}[0-9]{4}[A-Z]")) {

            System.out.println("Invalid PAN number.");
            return;
        }

        System.out.print("Enter CIBIL score: ");
        int cibil = sc.nextInt();

        // CIBIL validation
        if (cibil < 600 || cibil > 900) {

            System.out.println("CIBIL score must be between 600 and 900.");
            return;
        }

        sc.nextLine();

        System.out.println("1. Personal Loan");
        System.out.println("2. Home Loan");
        System.out.println("3. Car Loan");

        System.out.print("Enter loan type: ");
        int choice = sc.nextInt();

        LoanType loanType;

        if (choice == 1) {

            loanType = LoanType.PERSONAL_LOAN;

        } else if (choice == 2) {

            loanType = LoanType.HOME_LOAN;

        } else if (choice == 3) {

            loanType = LoanType.CAR_LOAN;

        } else {

            System.out.println("Invalid loan type.");
            return;
        }


        double interestRate;


        // Personal Loan
        if (loanType == LoanType.PERSONAL_LOAN) {

            if (cibil >= 750) {
                interestRate = 10.0;
            } else {
                interestRate = 12.0;
            }

        }

        // Home Loan
        else if (loanType == LoanType.HOME_LOAN) {

            if (cibil >= 750) {
                interestRate = 7.5;
            } else {
                interestRate = 9.0;
            }

        }

        // Car Loan
        else {

            if (cibil >= 750) {
                interestRate = 8.5;
            } else {
                interestRate = 10.0;
            }
        }


        // Generate username and password
        String newUsername = "loan" + System.currentTimeMillis();
        String newPassword = "Loan@" + cibil;


        String sql =
                "UPDATE users SET pan = ?, cibil_score = ?, "
                + "username = ?, password = ? WHERE username = ?";


        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, pan);
            ps.setInt(2, cibil);
            ps.setString(3, newUsername);
            ps.setString(4, newPassword);
            ps.setString(5, username);

            int rows = ps.executeUpdate();


            if (rows > 0) {

                String displayLoanType = "";

                if (loanType == LoanType.PERSONAL_LOAN) {
                    displayLoanType = "Personal Loan";
                } else if (loanType == LoanType.HOME_LOAN) {
                    displayLoanType = "Home Loan";
                } else {
                    displayLoanType = "Car Loan";
                }


                System.out.println();
                System.out.println("Loan information saved successfully!");
                System.out.println("PAN: " + pan);
                System.out.println("CIBIL Score: " + cibil);
                System.out.println("Loan Type: " + displayLoanType);
                System.out.println("Interest Rate: " + interestRate + "%");


                System.out.println();
                System.out.println("----- Generated Credentials -----");
                System.out.println("Username: " + newUsername);
                System.out.println("Password: " + newPassword);


                // Send email notification
                String emailSql =
                        "SELECT email FROM users WHERE username = ?";

                PreparedStatement emailPs =
                        con.prepareStatement(emailSql);

                emailPs.setString(1, newUsername);

                ResultSet rs = emailPs.executeQuery();


                if (rs.next()) {

                    String email = rs.getString("email");

                    EmailNotification.sendEmail(
                        email,
                        "Your loan credentials are Username: "
                        + newUsername
                        + " Password: "
                        + newPassword
                    );
                }


                rs.close();
                emailPs.close();

            } else {

                System.out.println("User not found.");
            }


            ps.close();
            con.close();


        } catch (Exception e) {

            System.out.println("Unable to process loan information.");
            e.printStackTrace();
        }
    }
}




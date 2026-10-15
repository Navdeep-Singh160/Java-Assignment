
package loan;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String role = null;

        // MAIN MENU
        while (role == null) {

            System.out.println();
            System.out.println("===== Loan Application System =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Forgot Password");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {

                case 1:
                    Registration.registerUser();
                    break;

                case 2:
                    role = Login.loginUser();

                    if (role == null) {
                        System.out.println("Login failed. Please try again.");
                    }
                    break;

                case 3:
                    ForgotPassword.resetPassword();
                    break;

                case 4:
                    System.out.println("Thank you for using the Loan Application System.");
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        // USER MENU
        if (role.equals("ROLE_USER")) {

            System.out.println();
            System.out.println("----- User Menu -----");
            System.out.println("1. Apply for Loan");
            System.out.println("2. Process PAN and CIBIL");
            System.out.println("3. Logout");
            System.out.print("Enter choice: ");

            int choice = Integer.parseInt(sc.nextLine().trim());

            if (choice == 1) {
                LoanApplication.applyForLoan();

            } else if (choice == 2) {
                LoanCalculator.processLoan();

            } else if (choice == 3) {
                Logout.logoutUser();

            } else {
                System.out.println("Invalid choice.");
            }
        }

        // ADMIN MENU
        else if (role.equals("ROLE_ADMIN")) {

            System.out.println();
            System.out.println("----- Admin Menu -----");
            System.out.println("1. View Users");
            System.out.println("2. Search User");
            System.out.println("3. Edit User");
            System.out.println("4. Activate/Deactivate User");
            System.out.println("5. Delete User");
            System.out.println("6. View Loan Applications");
            System.out.println("7. Review Loan Application");
            System.out.println("8. Approve/Reject Loan with EMI");
            System.out.println("9. Logout");
            System.out.print("Enter choice: ");

            int choice = Integer.parseInt(sc.nextLine().trim());

            if (choice == 1) {
                Admin.viewAllUsers();

            } else if (choice == 2) {
                Admin.searchUser();

            } else if (choice == 3) {
                Admin.editUser();

            } else if (choice == 4) {
                Admin.changeUserStatus();

            } else if (choice == 5) {
                Admin.deleteUser();

            } else if (choice == 6) {
                LoanApplication.viewApplications();

            } else if (choice == 7) {
                LoanApplication.reviewApplication();

            } else if (choice == 8) {
                LoanApproval.reviewLoan();

            } else if (choice == 9) {
                Logout.logoutUser();

            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
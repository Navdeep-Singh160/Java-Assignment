package loan;

public class EmailNotification {

    public static void sendEmail(String email, String message) {

        System.out.println("----- Email Notification -----");
        System.out.println("To: " + email);
        System.out.println("Message: " + message);
        System.out.println("------------------------------");

    }
}




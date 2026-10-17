package loan;

import java.io.Console;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * All keyboard input goes through here.
 *
 * - One shared Scanner (the old code created a new Scanner per method, which
 *   can swallow buffered input).
 * - Every prompt is validated; invalid input is re-asked, and after
 *   {@link #MAX_ATTEMPTS} bad tries the action is aborted instead of looping forever.
 * - No more InputMismatchException from nextInt()/nextDouble().
 * - Passwords are not echoed when a real console is available.
 */
public final class ConsoleIO {

    public static final int MAX_ATTEMPTS = 3;

    private static final Scanner IN = new Scanner(System.in);

    private static final Pattern MONEY = Pattern.compile("^\\d{1,12}(\\.\\d{1,2})?$");

    /** Input ended (Ctrl+D / Ctrl+Z or closed pipe). The program should exit. */
    public static class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InputClosedException() {
            super("Input stream closed.");
        }
    }

    /** Too many invalid entries; the current action should be cancelled. */
    public static class InputAbortedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public InputAbortedException() {
            super("Too many invalid entries.");
        }
    }

    private ConsoleIO() { }

    // ---------- text ----------

    private static String readRaw(String prompt) {

        System.out.print(prompt);
        System.out.flush();

        if (!IN.hasNextLine()) {
            throw new InputClosedException();
        }

        return IN.nextLine();
    }

    public static String readLine(String prompt) {
        return readRaw(prompt).trim();
    }

    /** Re-asks until the validator returns null; aborts after MAX_ATTEMPTS. */
    public static String readValidated(String prompt, Function<String, String> validator) {

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            String value = readLine(prompt);
            String error = validator.apply(value);

            if (error == null) {
                return value;
            }

            System.out.println(error);
        }

        throw new InputAbortedException();
    }

    // ---------- numbers ----------

    public static int readInt(String prompt, int min, int max) {

        String text = readValidated(prompt, value -> {
            try {
                int number = Integer.parseInt(value);
                return (number < min || number > max)
                        ? "Please enter a number between " + min + " and " + max + "."
                        : null;
            } catch (NumberFormatException e) {
                return "Please enter a valid whole number.";
            }
        });

        return Integer.parseInt(text);
    }

    /** Reads a money amount with at most 2 decimals, e.g. 250000 or 250000.50. */
    public static double readMoney(String prompt, double min, double max) {

        String text = readValidated(prompt, value -> {

            if (!MONEY.matcher(value).matches()) {
                return "Enter an amount with up to 2 decimals (for example 250000 or 250000.50).";
            }

            double amount = new BigDecimal(value).doubleValue();

            if (amount < min || amount > max) {
                return String.format("Amount must be between %,.2f and %,.2f.", min, max);
            }

            return null;
        });

        return new BigDecimal(text).doubleValue();
    }

    /** For menus: returns the number entered, or -1 if the input is not a number. */
    public static int readMenuChoice() {

        String text = readLine("Enter choice: ");

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean confirm(String prompt) {

        String answer = readLine(prompt + " (yes/no): ");

        return answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y");
    }

    // ---------- passwords ----------

    /** Reads a password without echo when possible. Never trimmed. */
    public static String readPassword(String prompt) {

        Console console = System.console();

        if (console == null) {
            return readRaw(prompt);
        }

        char[] chars = console.readPassword("%s", prompt);

        if (chars == null) {
            throw new InputClosedException();
        }

        String password = new String(chars);
        Arrays.fill(chars, ' ');

        return password;
    }

    /** Asks for a new password twice and checks the strength rules. */
    public static String readNewPassword(String prompt) {

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            String password = readPassword(prompt);
            String error = Validator.password(password);

            if (error != null) {
                System.out.println(error);
                continue;
            }

            String confirmation = readPassword("Confirm password: ");

            if (!password.equals(confirmation)) {
                System.out.println("Passwords do not match.");
                continue;
            }

            return password;
        }

        throw new InputAbortedException();
    }
}

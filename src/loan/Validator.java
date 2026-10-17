package loan;

import java.util.regex.Pattern;

/**
 * Central input validation. Every method returns null when the value is
 * valid, otherwise a message explaining what is wrong. Validators never throw.
 */
public final class Validator {

    private static final Pattern USERNAME =
            Pattern.compile("^[A-Za-z][A-Za-z0-9_.]{3,29}$");

    private static final Pattern FULL_NAME =
            Pattern.compile("^\\p{L}[\\p{L} .'-]{1,99}$");

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    // Indian mobile numbers: 10 digits starting with 6-9
    private static final Pattern MOBILE = Pattern.compile("^[6-9][0-9]{9}$");

    private static final Pattern PAN =
            Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$", Pattern.CASE_INSENSITIVE);

    private static final int MAX_EMAIL_LENGTH = 100;

    private Validator() { }

    public static String username(String value) {

        if (!USERNAME.matcher(nullToEmpty(value)).matches()) {
            return "Username must be 4-30 characters, start with a letter, "
                    + "and contain only letters, digits, '_' or '.'.";
        }

        return null;
    }

    public static String fullName(String value) {

        if (!FULL_NAME.matcher(nullToEmpty(value)).matches()) {
            return "Name must be 2-100 characters and contain only letters, "
                    + "spaces, '.', '-' or an apostrophe.";
        }

        return null;
    }

    public static String email(String value) {

        String v = nullToEmpty(value);

        if (v.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(v).matches()) {
            return "Please enter a valid email address (for example name@example.com).";
        }

        return null;
    }

    public static String mobile(String value) {

        if (!MOBILE.matcher(nullToEmpty(value)).matches()) {
            return "Mobile number must be 10 digits and start with 6, 7, 8 or 9.";
        }

        return null;
    }

    public static String password(String value) {

        if (value == null || value.length() < 8 || value.length() > 64) {
            return "Password must be 8-64 characters long.";
        }

        if (value.chars().anyMatch(Character::isWhitespace)) {
            return "Password must not contain spaces.";
        }

        boolean hasUpper = value.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = value.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = value.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
            return "Password must include an uppercase letter, a lowercase letter, "
                    + "a digit and a special character.";
        }

        return null;
    }

    public static String pan(String value) {

        if (!PAN.matcher(nullToEmpty(value)).matches()) {
            return "Invalid PAN. Expected format: 5 letters, 4 digits, 1 letter (e.g. ABCDE1234F).";
        }

        return null;
    }

    public static String purpose(String value) {
        return text(value, 3, 200, "Purpose");
    }

    public static String reason(String value) {
        return text(value, 3, 200, "Reason");
    }

    private static String text(String value, int min, int max, String label) {

        int length = nullToEmpty(value).length();

        if (length < min || length > max) {
            return label + " must be between " + min + " and " + max + " characters.";
        }

        return null;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

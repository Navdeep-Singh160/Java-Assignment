package loan;

/**
 * Dependency-free tests for the logic that does not need a database:
 * password hashing, validation rules and the EMI / interest-rate rules.
 *
 * Run (from the project root):
 *   javac -d out src/loan/*.java test/loan/SelfTest.java
 *   java -cp out loan.SelfTest
 * Exit code is 0 when everything passes, 1 otherwise.
 */
public class SelfTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {

        passwordHashing();
        validation();
        interestRates();
        emiCalculation();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");

        System.exit(failed == 0 ? 0 : 1);
    }

    // ---------- password hashing ----------

    private static void passwordHashing() {

        String hash = PasswordUtil.hash("Str0ng@Pass");

        check("hash is not the plain password", !hash.contains("Str0ng@Pass"));
        check("hash has expected prefix", hash.startsWith("pbkdf2_sha256$"));
        check("hash fits VARCHAR(255)", hash.length() < 255);
        check("correct password verifies", PasswordUtil.verify("Str0ng@Pass", hash));
        check("wrong password is rejected", !PasswordUtil.verify("Str0ng@Pas", hash));
        check("same password gives different hash (random salt)",
                !hash.equals(PasswordUtil.hash("Str0ng@Pass")));
        check("fresh hash does not need rehash", !PasswordUtil.needsRehash(hash));
        check("plain text is detected as not hashed", !PasswordUtil.isHashed("Admin@123"));
        check("plain text never verifies as a hash", !PasswordUtil.verify("Admin@123", "Admin@123"));
        check("garbage hash is rejected, not thrown",
                !PasswordUtil.verify("x", "pbkdf2_sha256$abc$!!$??"));
        check("legacy plain-text comparison works",
                PasswordUtil.matchesLegacyPlaintext("Admin@123", "Admin@123"));
        check("legacy plain-text mismatch rejected",
                !PasswordUtil.matchesLegacyPlaintext("admin@123", "Admin@123"));
    }

    // ---------- validation ----------

    private static void validation() {

        check("valid username", Validator.username("navdeep_s") == null);
        check("username too short", Validator.username("abc") != null);
        check("username starting with digit", Validator.username("1abcd") != null);
        check("username with spaces/symbols", Validator.username("bad name!") != null);
        check("SQL-like username rejected", Validator.username("x' OR '1'='1") != null);

        check("valid name", Validator.fullName("Navdeep Singh") == null);
        check("name with apostrophe", Validator.fullName("D'Souza") == null);
        check("name with digits rejected", Validator.fullName("Nav123") != null);

        check("valid email", Validator.email("user.name+tag@mail.example.com") == null);
        check("email without @", Validator.email("user.example.com") != null);
        check("email without domain dot", Validator.email("user@example") != null);
        check("email too long", Validator.email("a".repeat(95) + "@x.com") != null);

        check("valid mobile", Validator.mobile("9876543210") == null);
        check("mobile starting with 5", Validator.mobile("5876543210") != null);
        check("mobile too short", Validator.mobile("98765") != null);
        check("mobile with letters", Validator.mobile("98765abcde") != null);

        check("strong password accepted", Validator.password("Admin@123") == null);
        check("short password rejected", Validator.password("Ab@1") != null);
        check("no uppercase rejected", Validator.password("admin@1234") != null);
        check("no digit rejected", Validator.password("Admin@abcd") != null);
        check("no special rejected", Validator.password("Admin12345") != null);
        check("password with space rejected", Validator.password("Admin @123") != null);

        check("valid PAN", Validator.pan("ABCDE1234F") == null);
        check("lowercase PAN accepted (caller upper-cases)", Validator.pan("abcde1234f") == null);
        check("PAN wrong shape", Validator.pan("ABCD12345F") != null);
        check("empty PAN", Validator.pan("") != null);
        check("null PAN does not throw", Validator.pan(null) != null);

        check("purpose ok", Validator.purpose("Home renovation") == null);
        check("purpose too short", Validator.purpose("ab") != null);
        check("purpose too long", Validator.purpose("x".repeat(201)) != null);
    }

    // ---------- one set of interest rules ----------

    private static void interestRates() {

        check("personal, good CIBIL", LoanType.PERSONAL_LOAN.rateFor(750) == 10.0);
        check("personal, lower CIBIL", LoanType.PERSONAL_LOAN.rateFor(749) == 12.0);
        check("home, good CIBIL", LoanType.HOME_LOAN.rateFor(800) == 7.5);
        check("home, lower CIBIL", LoanType.HOME_LOAN.rateFor(650) == 9.0);
        check("car, good CIBIL", LoanType.CAR_LOAN.rateFor(750) == 8.5);
        check("car, lower CIBIL", LoanType.CAR_LOAN.rateFor(600) == 10.0);
    }

    // ---------- EMI ----------

    private static void emiCalculation() {

        // Standard textbook case: 100,000 at 12% p.a. for 12 months -> EMI 8,884.88
        LoanCalculator.EmiResult r = LoanCalculator.calculate(100_000, 12, 12);

        check("EMI 100000 @12% x12 = 8884.88", r.emi() == 8884.88);
        check("total payable = emi * months", r.totalPayable() == 106_618.56);
        check("total interest = payable - principal", r.totalInterest() == 6_618.56);

        LoanCalculator.EmiResult zero = LoanCalculator.calculate(12_000, 0, 12);
        check("0% interest splits evenly", zero.emi() == 1_000.00 && zero.totalInterest() == 0.0);

        check("zero principal rejected", throwsIllegalArgument(() -> LoanCalculator.calculate(0, 10, 12)));
        check("zero months rejected (was divide-by-zero/NaN)",
                throwsIllegalArgument(() -> LoanCalculator.calculate(1000, 10, 0)));
        check("negative rate rejected", throwsIllegalArgument(() -> LoanCalculator.calculate(1000, -1, 12)));
        check("NaN principal rejected",
                throwsIllegalArgument(() -> LoanCalculator.calculate(Double.NaN, 10, 12)));
    }

    // ---------- tiny test helpers ----------

    private static boolean throwsIllegalArgument(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void check(String name, boolean condition) {

        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }
}

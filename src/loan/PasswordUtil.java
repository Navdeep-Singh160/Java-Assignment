package loan;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing with PBKDF2-HMAC-SHA256 (built into the JDK, no extra
 * libraries) using a random salt per password.
 *
 * Stored format:  pbkdf2_sha256$<iterations>$<base64 salt>$<base64 hash>
 * The iteration count is stored with the hash so it can be raised later
 * without breaking existing passwords.
 */
public final class PasswordUtil {

    private static final String PREFIX = "pbkdf2_sha256";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int MAX_ACCEPTED_ITERATIONS = 10_000_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static String dummyHash;

    private PasswordUtil() { }

    public static String hash(String password) {

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] key = derive(password, salt, ITERATIONS, KEY_BITS);

        Base64.Encoder encoder = Base64.getEncoder();

        return PREFIX + "$" + ITERATIONS
                + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(key);
    }

    /** True if the stored value is in the hashed format (not legacy plain text). */
    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX + "$");
    }

    public static boolean verify(String password, String stored) {

        if (password == null || !isHashed(stored)) {
            return false;
        }

        String[] parts = stored.split("\\$");

        if (parts.length != 4) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);

            if (iterations < 1 || iterations > MAX_ACCEPTED_ITERATIONS) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);

            byte[] actual = derive(password, salt, iterations, expected.length * 8);

            return MessageDigest.isEqual(expected, actual);

        } catch (IllegalArgumentException e) {
            // malformed stored value (bad number, bad base64, empty key)
            return false;
        }
    }

    /** True if the hash uses fewer iterations than the current standard. */
    public static boolean needsRehash(String stored) {

        if (!isHashed(stored)) {
            return true;
        }

        String[] parts = stored.split("\\$");

        try {
            return parts.length != 4 || Integer.parseInt(parts[1]) < ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Compares against an old plain-text password in constant time.
     * Only used to log in accounts created before hashing was introduced;
     * the password is upgraded to a hash straight after a successful login.
     */
    public static boolean matchesLegacyPlaintext(String password, String stored) {

        if (password == null || stored == null) {
            return false;
        }

        return MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Spends roughly the same time as a real check. Called when a login
     * names an unknown account so response time does not reveal which
     * usernames exist.
     */
    public static synchronized void burnTime(String password) {

        if (dummyHash == null) {
            dummyHash = hash("timing-equalisation-only");
        }

        verify(password == null ? "" : password, dummyHash);
    }

    private static byte[] derive(String password, byte[] salt, int iterations, int keyBits) {

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBits);

        try {
            return SecretKeyFactory.getInstance(ALGORITHM)
                    .generateSecret(spec)
                    .getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing is not available on this JVM.", e);

        } finally {
            spec.clearPassword();
        }
    }
}

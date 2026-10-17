package loan;

/** A row of the users table. Immutable; also used as the logged-in session user. */
public final class User {

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final int id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String mobile;
    private final String passwordHash;
    private final String role;
    private final int failedAttempts;
    private final boolean accountLocked;
    private final String pan;
    private final Integer cibilScore;

    public User(int id, String username, String fullName, String email, String mobile,
                String passwordHash, String role, int failedAttempts,
                boolean accountLocked, String pan, Integer cibilScore) {

        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.passwordHash = passwordHash;
        this.role = role;
        this.failedAttempts = failedAttempts;
        this.accountLocked = accountLocked;
        this.pan = pan;
        this.cibilScore = cibilScore;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getRole() { return role; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isAccountLocked() { return accountLocked; }
    public String getPan() { return pan; }

    /** Null until the customer has submitted PAN and CIBIL. */
    public Integer getCibilScore() { return cibilScore; }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    /** Only for authentication code. Never print or log this. */
    String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        // deliberately excludes the password hash
        return "User{id=" + id + ", username=" + username + ", role=" + role + "}";
    }
}

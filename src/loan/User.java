package loan;

public class User {

    private int id;
    private String username;
    private String fullName;
    private String email;
    private String mobile;
    private String password;
    private int failedAttempts;
    private boolean accountLocked;

    public User(String username, String fullName, String email,
                String mobile, String password) {

        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.failedAttempts = 0;
        this.accountLocked = false;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
}



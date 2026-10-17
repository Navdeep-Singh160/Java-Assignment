package loan;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Central logging setup. Technical errors go to loan-system.log so that
 * stack traces are never shown to console users. Passwords, reset codes and
 * other secrets must never be logged.
 */
public final class AppLog {

    private static final String LOG_FILE = "loan-system.log";
    private static boolean initialised;

    private AppLog() { }

    public static synchronized void init() {

        if (initialised) {
            return;
        }
        initialised = true;

        Logger root = Logger.getLogger("");

        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }

        try {
            FileHandler fileHandler = new FileHandler(LOG_FILE, 1_000_000, 1, true);
            fileHandler.setFormatter(new SimpleFormatter());
            root.addHandler(fileHandler);

        } catch (IOException | SecurityException e) {
            System.out.println("Warning: log file could not be opened; "
                    + "errors will not be recorded.");
        }

        root.setLevel(Level.INFO);
    }

    public static Logger get(Class<?> type) {
        return Logger.getLogger(type.getName());
    }
}

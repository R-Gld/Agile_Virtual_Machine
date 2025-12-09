package fr.ufrst.m1info.gl.groupe7.memoire.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Custom Logback appender that redirects log messages to a JavaFX ConsoleOutput widget.
 * This appender is designed to work with the GUI module without creating a circular dependency.
 *
 * Thread-safe: Uses JavaFX Platform.runLater() for GUI updates.
 * Headless-friendly: Gracefully handles absence of GUI (CLI mode).
 */
public class GuiAppender extends AppenderBase<ILoggingEvent> {

    // Static reference to the GUI console widget (set by GUI module at startup)
    private static volatile Object guiConsole = null;

    // Cached reflection methods for performance
    private static Method printMessageMethod = null;
    private static Method platformRunLaterMethod = null;

    // Date formatter for log timestamps
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

    /**
     * Register the GUI console widget to receive log messages.
     * Called by the GUI module at application startup.
     *
     * @param console the ConsoleOutput widget instance
     */
    public static void setGuiConsole(Object console) {
        guiConsole = console;

        // Pre-cache reflection methods for better performance
        if (console != null) {
            try {
                // Cache printMessage method from ConsoleOutput
                printMessageMethod = console.getClass().getMethod("printMessage", String.class);

                // Cache Platform.runLater method from JavaFX
                Class<?> platformClass = Class.forName("javafx.application.Platform");
                platformRunLaterMethod = platformClass.getMethod("runLater", Runnable.class);
            } catch (Exception e) {
                // Reflection failed - will fallback to runtime reflection
                printMessageMethod = null;
                platformRunLaterMethod = null;
            }
        }
    }

    /**
     * Unregister the GUI console (e.g., when GUI is closing).
     */
    public static void unsetGuiConsole() {
        guiConsole = null;
        printMessageMethod = null;
        platformRunLaterMethod = null;
    }

    /**
     * Appends a log event to the GUI console.
     * Called by Logback for each log statement.
     *
     * @param event the logging event
     */
    @Override
    protected void append(ILoggingEvent event) {
        // Ignore if no GUI console is registered (headless mode)
        if (guiConsole == null) {
            return;
        }

        // Format the log message
        String formattedMessage = formatMessage(event);

        // Send to GUI on JavaFX application thread
        try {
            sendToGui(formattedMessage);
        } catch (Exception e) {
            // Silent fail - don't disrupt logging if GUI has issues
            // The CONSOLE and FILE appenders will still work
        }
    }

    /**
     * Formats a log event into a human-readable string.
     * Format: "HH:mm:ss [LEVEL] LoggerName - Message"
     *
     * @param event the logging event
     * @return formatted log message
     */
    private String formatMessage(ILoggingEvent event) {
        String timestamp = timeFormatter.format(new Date(event.getTimeStamp()));
        String level = String.format("%-5s", event.getLevel().toString());

        // Shorten logger name for readability (keep last 2 segments)
        String loggerName = event.getLoggerName();
        String[] parts = loggerName.split("\\.");
        if (parts.length > 2) {
            loggerName = parts[parts.length - 2] + "." + parts[parts.length - 1];
        }

        return String.format("%s [%s] %s - %s",
            timestamp,
            level,
            loggerName,
            event.getFormattedMessage()
        );
    }

    /**
     * Sends a message to the GUI console using JavaFX Platform.runLater().
     * Uses reflection to avoid compile-time dependency on JavaFX.
     *
     * @param message the formatted log message
     * @throws Exception if reflection fails
     */
    private void sendToGui(String message) throws Exception {
        if (platformRunLaterMethod != null && printMessageMethod != null) {
            // Fast path: use cached reflection methods
            Runnable task = () -> {
                try {
                    printMessageMethod.invoke(guiConsole, message);
                } catch (Exception e) {
                    // Ignore GUI errors
                }
            };
            platformRunLaterMethod.invoke(null, task);
        } else {
            // Slow path: runtime reflection
            Class<?> platformClass = Class.forName("javafx.application.Platform");
            Method runLater = platformClass.getMethod("runLater", Runnable.class);

            Runnable task = () -> {
                try {
                    Method printMessage = guiConsole.getClass()
                        .getMethod("printMessage", String.class);
                    printMessage.invoke(guiConsole, message);
                } catch (Exception e) {
                    // Ignore GUI errors
                }
            };

            runLater.invoke(null, task);
        }
    }
}

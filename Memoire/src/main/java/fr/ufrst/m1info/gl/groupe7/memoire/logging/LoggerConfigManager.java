package fr.ufrst.m1info.gl.groupe7.memoire.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager for dynamic log level configuration.
 * Allows the GUI to change log levels at runtime without restarting the application.
 * <p>
 * This class provides programmatic control over Logback's logging configuration,
 * enabling users to adjust verbosity on-the-fly from the GUI interface.
 */
public class LoggerConfigManager {

    /**
     * Sets the global (root) log level for the entire application.
     * This affects all loggers unless they have a specific level configured.
     *
     * @param level the new root log level (TRACE, DEBUG, INFO, WARN, ERROR, OFF)
     */
    public static void setRootLevel(Level level) {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(level);
    }

    /**
     * Sets the log level for a specific logger by name.
     * Useful for controlling verbosity of individual packages or classes.
     * <p>
     * Examples:
     * - "fr.ufrst.m1info.gl.groupe7.memoire" - all classes in Memoire module
     * - "fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor" - specific class
     *
     * @param loggerName the fully qualified logger name
     * @param level the new log level for this logger
     */
    public static void setLoggerLevel(String loggerName, Level level) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.setLevel(level);
    }

    /**
     * Gets the current log level for a specific logger.
     *
     * @param loggerName the logger name
     * @return the effective log level (may be inherited from parent if not explicitly set)
     */
    public static Level getLoggerLevel(String loggerName) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        return logger.getEffectiveLevel();
    }

    /**
     * Gets the current root (global) log level.
     *
     * @return the root logger's level
     */
    public static Level getRootLevel() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        return rootLogger.getLevel();
    }

    /**
     * Returns a map of all configured loggers and their levels.
     * Useful for displaying current configuration in the GUI.
     *
     * @return map of logger name -> level
     */
    public static Map<String, Level> getAllLoggers() {
        Map<String, Level> loggers = new HashMap<>();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        for (Logger logger : loggerContext.getLoggerList()) {
            if (logger.getLevel() != null) { // Only include loggers with explicit level
                loggers.put(logger.getName(), logger.getLevel());
            }
        }

        return loggers;
    }

    /**
     * Resets all loggers to their default configuration.
     * Re-reads the logback.xml configuration file.
     */
    public static void resetToDefaults() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        try {
            // Reload configuration from logback.xml
            ch.qos.logback.classic.util.ContextInitializer ci =
                new ch.qos.logback.classic.util.ContextInitializer(loggerContext);
            ci.autoConfig();
        } catch (Exception e) {
            // If reload fails, set sensible defaults
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.INFO);
        }
    }

    /**
     * Preset configurations for common use cases.
     */
    public static class Presets {

        /**
         * Production mode: minimal logging (INFO and above).
         */
        public static void applyProductionMode() {
            setRootLevel(Level.INFO);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.memoire", Level.INFO);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.lexerparser", Level.INFO);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.compiler", Level.INFO);
        }

        /**
         * Development mode: detailed logging (DEBUG and above).
         */
        public static void applyDevelopmentMode() {
            setRootLevel(Level.DEBUG);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.memoire", Level.DEBUG);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.lexerparser", Level.DEBUG);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.compiler", Level.DEBUG);
        }

        /**
         * Verbose mode: maximum logging including trace (TRACE and above).
         */
        public static void applyVerboseMode() {
            setRootLevel(Level.TRACE);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.memoire", Level.TRACE);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.lexerparser", Level.DEBUG);
            setLoggerLevel("fr.ufrst.m1info.gl.groupe7.compiler", Level.INFO);
        }

        /**
         * Silent mode: only errors (ERROR and above).
         */
        public static void applySilentMode() {
            setRootLevel(Level.ERROR);
        }

        /**
         * Off mode: no logging at all.
         */
        public static void applyOffMode() {
            setRootLevel(Level.OFF);
        }
    }
}

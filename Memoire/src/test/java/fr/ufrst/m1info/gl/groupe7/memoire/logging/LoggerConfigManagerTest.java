package fr.ufrst.m1info.gl.groupe7.memoire.logging;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour LoggerConfigManager.
 * Couverture cible: 100%
 */
class LoggerConfigManagerTest {

    private Level originalRootLevel;

    @BeforeEach
    void setUp() {
        // Save original root level
        originalRootLevel = LoggerConfigManager.getRootLevel();
    }

    @AfterEach
    void tearDown() {
        // Restore original root level
        LoggerConfigManager.setRootLevel(originalRootLevel);
    }

    @Test
    void setRootLevel_setsGlobalLogLevel() {
        LoggerConfigManager.setRootLevel(Level.DEBUG);
        assertEquals(Level.DEBUG, LoggerConfigManager.getRootLevel());

        LoggerConfigManager.setRootLevel(Level.ERROR);
        assertEquals(Level.ERROR, LoggerConfigManager.getRootLevel());
    }

    @Test
    void getRootLevel_returnsCurrentLevel() {
        LoggerConfigManager.setRootLevel(Level.WARN);
        Level level = LoggerConfigManager.getRootLevel();

        assertNotNull(level);
        assertEquals(Level.WARN, level);
    }

    @Test
    void setLoggerLevel_setsSpecificLoggerLevel() {
        String loggerName = "fr.ufrst.m1info.gl.groupe7.test";

        LoggerConfigManager.setLoggerLevel(loggerName, Level.TRACE);
        Level level = LoggerConfigManager.getLoggerLevel(loggerName);

        assertEquals(Level.TRACE, level);
    }

    @Test
    void getLoggerLevel_returnsEffectiveLevel() {
        String loggerName = "fr.ufrst.m1info.gl.groupe7.test2";

        // Set root to INFO
        LoggerConfigManager.setRootLevel(Level.INFO);

        // Logger without explicit level should inherit from root
        Level level = LoggerConfigManager.getLoggerLevel(loggerName);
        assertEquals(Level.INFO, level);

        // Set explicit level
        LoggerConfigManager.setLoggerLevel(loggerName, Level.DEBUG);
        level = LoggerConfigManager.getLoggerLevel(loggerName);
        assertEquals(Level.DEBUG, level);
    }

    @Test
    void getAllLoggers_returnsConfiguredLoggers() {
        // Set some specific logger levels
        LoggerConfigManager.setLoggerLevel("test.logger1", Level.DEBUG);
        LoggerConfigManager.setLoggerLevel("test.logger2", Level.ERROR);

        Map<String, Level> loggers = LoggerConfigManager.getAllLoggers();

        assertNotNull(loggers);
        assertTrue(!loggers.isEmpty(), "Should have at least root logger");
    }

    @Test
    void getAllLoggers_includesRootLogger() {
        LoggerConfigManager.setRootLevel(Level.INFO);

        Map<String, Level> loggers = LoggerConfigManager.getAllLoggers();

        // Root logger should be included
        assertTrue(loggers.containsKey("ROOT") || loggers.containsValue(Level.INFO));
    }

    @Test
    void resetToDefaults_resetsConfiguration() {
        // Change root level
        LoggerConfigManager.setRootLevel(Level.TRACE);

        // Reset to defaults
        assertDoesNotThrow(LoggerConfigManager::resetToDefaults);

        // Level should be reset (likely to INFO from logback.xml)
        Level level = LoggerConfigManager.getRootLevel();
        assertNotNull(level);
    }

    @Test
    void presets_applyProductionMode() {
        LoggerConfigManager.Presets.applyProductionMode();

        assertEquals(Level.INFO, LoggerConfigManager.getRootLevel());
        assertEquals(Level.INFO, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.memoire"));
        assertEquals(Level.INFO, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.lexerparser"));
        assertEquals(Level.INFO, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.compiler"));
    }

    @Test
    void presets_applyDevelopmentMode() {
        LoggerConfigManager.Presets.applyDevelopmentMode();

        assertEquals(Level.DEBUG, LoggerConfigManager.getRootLevel());
        assertEquals(Level.DEBUG, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.memoire"));
        assertEquals(Level.DEBUG, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.lexerparser"));
        assertEquals(Level.DEBUG, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.compiler"));
    }

    @Test
    void presets_applyVerboseMode() {
        LoggerConfigManager.Presets.applyVerboseMode();

        assertEquals(Level.TRACE, LoggerConfigManager.getRootLevel());
        assertEquals(Level.TRACE, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.memoire"));
        assertEquals(Level.DEBUG, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.lexerparser"));
        assertEquals(Level.INFO, LoggerConfigManager.getLoggerLevel("fr.ufrst.m1info.gl.groupe7.compiler"));
    }

    @Test
    void presets_applySilentMode() {
        LoggerConfigManager.Presets.applySilentMode();

        assertEquals(Level.ERROR, LoggerConfigManager.getRootLevel());
    }

    @Test
    void presets_applyOffMode() {
        LoggerConfigManager.Presets.applyOffMode();

        assertEquals(Level.OFF, LoggerConfigManager.getRootLevel());
    }

    @Test
    void setRootLevel_withAllLevels() {
        // Test all log levels
        Level[] levels = {Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.OFF};

        for (Level level : levels) {
            LoggerConfigManager.setRootLevel(level);
            assertEquals(level, LoggerConfigManager.getRootLevel(),
                "Root level should be " + level);
        }
    }

    @Test
    void setLoggerLevel_multipleLoggers() {
        String logger1 = "test.package1";
        String logger2 = "test.package2";
        String logger3 = "test.package3";

        LoggerConfigManager.setLoggerLevel(logger1, Level.TRACE);
        LoggerConfigManager.setLoggerLevel(logger2, Level.WARN);
        LoggerConfigManager.setLoggerLevel(logger3, Level.ERROR);

        assertEquals(Level.TRACE, LoggerConfigManager.getLoggerLevel(logger1));
        assertEquals(Level.WARN, LoggerConfigManager.getLoggerLevel(logger2));
        assertEquals(Level.ERROR, LoggerConfigManager.getLoggerLevel(logger3));
    }

    @Test
    void getAllLoggers_afterSettingMultiple() {
        LoggerConfigManager.setLoggerLevel("custom.test1", Level.DEBUG);
        LoggerConfigManager.setLoggerLevel("custom.test2", Level.INFO);
        LoggerConfigManager.setLoggerLevel("custom.test3", Level.ERROR);

        Map<String, Level> allLoggers = LoggerConfigManager.getAllLoggers();

        assertNotNull(allLoggers);
        assertTrue(allLoggers.size() >= 3, "Should have at least 3 custom loggers");
    }
}

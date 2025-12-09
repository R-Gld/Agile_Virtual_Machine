package fr.ufrst.m1info.gl.groupe7.gui;

import ch.qos.logback.classic.Level;
import fr.ufrst.m1info.gl.groupe7.memoire.logging.LoggerConfigManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour LogControlPanel.
 * Couverture cible: 100%
 */
@ExtendWith(ApplicationExtension.class)
class TestLogControlPanel {

    private LogControlPanel logControlPanel;
    private Level originalLevel;

    @Start
    void start(Stage stage) {
        // Save original log level
        originalLevel = LoggerConfigManager.getRootLevel();

        logControlPanel = new LogControlPanel();
        stage.setScene(new Scene(new StackPane(logControlPanel), 400, 100));
        stage.show();
    }

    private void runOnFxThread(Runnable action) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void constructor_initializesComboBoxWithLevels(FxRobot robot) {
        runOnFxThread(() -> {
            ComboBox<String> comboBox = (ComboBox<String>) logControlPanel.lookup(".combo-box");
            assertNotNull(comboBox, "ComboBox should be initialized");

            // Verify all levels are present
            assertTrue(comboBox.getItems().contains("TRACE"));
            assertTrue(comboBox.getItems().contains("DEBUG"));
            assertTrue(comboBox.getItems().contains("INFO"));
            assertTrue(comboBox.getItems().contains("WARN"));
            assertTrue(comboBox.getItems().contains("ERROR"));
            assertTrue(comboBox.getItems().contains("OFF"));

            assertEquals(6, comboBox.getItems().size(), "Should have exactly 6 log levels");
        });
    }

    @Test
    void constructor_setsDefaultValue(FxRobot robot) {
        runOnFxThread(() -> {
            ComboBox<String> comboBox = (ComboBox<String>) logControlPanel.lookup(".combo-box");
            assertEquals("INFO", comboBox.getValue(), "Default value should be INFO");
        });
    }

    @Test
    void selectLevel_updatesRootLogger(FxRobot robot) {
        // Click on ComboBox to open dropdown
        robot.clickOn(".combo-box");
        WaitForAsyncUtils.waitForFxEvents();

        // Select DEBUG
        robot.clickOn("DEBUG");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify root logger was updated
        runOnFxThread(() -> {
            assertEquals(Level.DEBUG, LoggerConfigManager.getRootLevel(),
                "Root logger level should be updated to DEBUG");
        });

        // Restore original level
        LoggerConfigManager.setRootLevel(originalLevel);
    }

    @Test
    void selectLevel_trace_updatesLogger(FxRobot robot) {
        robot.clickOn(".combo-box");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("TRACE");
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            assertEquals(Level.TRACE, LoggerConfigManager.getRootLevel());
        });

        LoggerConfigManager.setRootLevel(originalLevel);
    }

    @Test
    void selectLevel_warn_updatesLogger(FxRobot robot) {
        robot.clickOn(".combo-box");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("WARN");
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            assertEquals(Level.WARN, LoggerConfigManager.getRootLevel());
        });

        LoggerConfigManager.setRootLevel(originalLevel);
    }

    @Test
    void selectLevel_error_updatesLogger(FxRobot robot) {
        robot.clickOn(".combo-box");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("ERROR");
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            assertEquals(Level.ERROR, LoggerConfigManager.getRootLevel());
        });

        LoggerConfigManager.setRootLevel(originalLevel);
    }

    @Test
    void selectLevel_off_updatesLogger(FxRobot robot) {
        robot.clickOn(".combo-box");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("OFF");
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            assertEquals(Level.OFF, LoggerConfigManager.getRootLevel());
        });

        LoggerConfigManager.setRootLevel(originalLevel);
    }

    @Test
    void panel_hasCorrectStyling(FxRobot robot) {
        runOnFxThread(() -> {
            assertNotNull(logControlPanel.getStyle());
            assertTrue(logControlPanel.getStyle().contains("rgba(50, 50, 50, 0.3)"),
                "Panel should have semi-transparent background");
        });
    }

    @Test
    void panel_containsLabel(FxRobot robot) {
        runOnFxThread(() -> {
            assertNotNull(logControlPanel.lookup(".label"),
                "Panel should contain a label");
        });
    }

    @Test
    void comboBox_hasPrefWidth(FxRobot robot) {
        runOnFxThread(() -> {
            ComboBox<String> comboBox = (ComboBox<String>) logControlPanel.lookup(".combo-box");
            assertEquals(100.0, comboBox.getPrefWidth(), 0.1,
                "ComboBox should have preferred width of 100");
        });
    }

    @Test
    void selectLevel_multipleChanges_allApplied(FxRobot robot) {
        // Change to DEBUG
        robot.clickOn(".combo-box");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("DEBUG");
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            assertEquals(Level.DEBUG, LoggerConfigManager.getRootLevel());
        });

        // Change to ERROR
        robot.clickOn(".combo-box");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("ERROR");
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            assertEquals(Level.ERROR, LoggerConfigManager.getRootLevel());
        });

        // Change back to INFO
        robot.clickOn(".combo-box");
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("INFO");
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            assertEquals(Level.INFO, LoggerConfigManager.getRootLevel());
        });

        LoggerConfigManager.setRootLevel(originalLevel);
    }
}

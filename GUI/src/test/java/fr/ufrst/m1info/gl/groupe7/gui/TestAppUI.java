package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for App UI components (titlebar, dark mode) to improve code
 * coverage
 */
@ExtendWith(ApplicationExtension.class)
public class TestAppUI {

    private App app;
    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        app = new App();
        app.start(stage);
    }

    /**
     * Test dark mode toggle exists
     */
    @Test
    void testDarkModeToggleExists(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        ToggleButton darkModeButton = topMenu.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> (ToggleButton) node)
                .findFirst()
                .orElse(null);

        assertNotNull(darkModeButton, "Dark mode toggle should exist");
        assertNotNull(darkModeButton.getGraphic(), "Dark mode button should have an icon");
    }

    /**
     * Test dark mode can be toggled on
     */
    @Test
    void testDarkModeCanBeToggledOn(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        ToggleButton darkModeButton = topMenu.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> (ToggleButton) node)
                .findFirst()
                .orElse(null);

        assertNotNull(darkModeButton);
        assertFalse(darkModeButton.isSelected(), "Dark mode should be off initially");

        // Toggle dark mode on
        runOnFxThread(() -> darkModeButton.fire());
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(darkModeButton.isSelected(), "Dark mode should be on after toggle");

        // Check if dark theme CSS is applied
        boolean hasDarkCss = stage.getScene().getStylesheets().stream()
                .anyMatch(css -> css.contains("dark.css"));
        assertTrue(hasDarkCss, "Dark theme CSS should be applied");
    }

    /**
     * Test dark mode can be toggled off
     */
    @Test
    void testDarkModeCanBeToggledOff(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        ToggleButton darkModeButton = topMenu.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> (ToggleButton) node)
                .findFirst()
                .orElse(null);

        // Toggle on then off
        runOnFxThread(() -> {
            darkModeButton.fire(); // On
        });
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            darkModeButton.fire(); // Off
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(darkModeButton.isSelected(), "Dark mode should be off");

        // Check if light theme CSS is applied
        boolean hasLightCss = stage.getScene().getStylesheets().stream()
                .anyMatch(css -> css.contains("light.css"));
        assertTrue(hasLightCss, "Light theme CSS should be applied");
    }

    /**
     * Test title bar exists
     */
    @Test
    void testTitleBarExists(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        assertNotNull(titleBar, "Title bar should exist");
    }

    /**
     * Test title label exists and has correct text
     */
    @Test
    void testTitleLabelExists(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        Label titleLabel = titleBar.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .findFirst()
                .orElse(null);

        assertNotNull(titleLabel, "Title label should exist");
        assertEquals("MiniJaja IDE", titleLabel.getText(), "Title should be 'MiniJaja IDE'");
    }

    /**
     * Test title bar has window control buttons
     */
    @Test
    void testTitleBarHasWindowControls(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        long buttonCount = titleBar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .count();

        assertEquals(3, buttonCount, "Title bar should have 3 window control buttons");
    }

    /**
     * Test minimize button exists
     */
    @Test
    void testMinimizeButtonExists(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        java.util.List<Button> buttons = titleBar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(buttons.size() >= 3, "Should have at least 3 window buttons");
        Button minButton = buttons.get(0);
        assertEquals("-", minButton.getText(), "First button should be minimize with '-' text");
    }

    /**
     * Test maximize button exists
     */
    @Test
    void testMaximizeButtonExists(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        java.util.List<Button> buttons = titleBar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        Button maxButton = buttons.get(1);
        assertEquals("□", maxButton.getText(), "Second button should be maximize with '□' text");
    }

    /**
     * Test close button exists
     */
    @Test
    void testCloseButtonExists(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        java.util.List<Button> buttons = titleBar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        Button closeButton = buttons.get(2);
        assertEquals("X", closeButton.getText(), "Third button should be close with 'X' text");
    }

    /**
     * Test toolbar has all required buttons
     */
    @Test
    void testToolbarHasAllButtons(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        long buttonCount = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .count();

        assertTrue(buttonCount >= 5, "Toolbar should have at least 5 buttons (build, run, debug, step/continue, stop)");
    }

    /**
     * Test all toolbar buttons have tooltips
     */
    @Test
    void testToolbarButtonsHaveTooltips(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        for (Button button : buttons) {
            assertNotNull(button.getTooltip(), "Each toolbar button should have a tooltip: " + button.getId());
        }
    }

    /**
     * Test all toolbar buttons have graphics/icons
     */
    @Test
    void testToolbarButtonsHaveGraphics(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        for (Button button : buttons) {
            assertNotNull(button.getGraphic(), "Each toolbar button should have an icon/graphic");
        }
    }

    /**
     * Test theme switching doesn't crash
     */
    @Test
    void testThemeSwitchingMultipleTimes(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        ToggleButton darkModeButton = topMenu.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> (ToggleButton) node)
                .findFirst()
                .orElse(null);

        // Toggle multiple times to ensure no crashes
        runOnFxThread(() -> darkModeButton.fire()); // dark
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> darkModeButton.fire()); // light
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> darkModeButton.fire()); // dark again
        WaitForAsyncUtils.waitForFxEvents();

        // Should not crash and should be in dark mode
        assertTrue(darkModeButton.isSelected(), "Dark mode should be selected after 3 toggles");
    }

    /**
     * Test window controls don't throw exceptions
     */
    @Test
    void testWindowControlsAreAccessible(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        java.util.List<Button> buttons = titleBar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        // Just verify all buttons are accessible and not null
        for (Button button : buttons) {
            assertNotNull(button, "Window control button should not be null");
            assertNotNull(button.getOnAction(), "Window control button should have action handler");
        }
    }

    /**
     * Helper method to run code on JavaFX thread
     */
    private void runOnFxThread(Runnable runnable) {
        javafx.application.Platform.runLater(runnable);
    }
}

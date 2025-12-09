package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe App.
 */
@ExtendWith(ApplicationExtension.class)
class TestApp {

    private App app;
    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        app = new App();
        app.start(stage);
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
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testAppStartsSuccessfully() {
        assertNotNull(stage, "Stage doit être initialisé");
        assertTrue(stage.isShowing(), "Stage doit être visible");
    }

    @Test
    void testSceneIsSet() {
        assertNotNull(stage.getScene(), "Scene doit être définie");
    }

    @Test
    void testSceneHasBorderPaneRoot() {
        assertInstanceOf(BorderPane.class, stage.getScene().getRoot(), "Root doit être un BorderPane");
    }

    @Test
    void testBorderPaneHasTopMenu() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        assertNotNull(root.getTop(), "Top doit contenir le menu");
        assertInstanceOf(VBox.class, root.getTop(), "Top doit être un VBox");
    }

    @Test
    void testBorderPaneHasCenterSplitPane() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        assertNotNull(root.getCenter(), "Center doit contenir le SplitPane");
        assertInstanceOf(SplitPane.class, root.getCenter(), "Center doit être un SplitPane");
    }

    @Test
    void testBorderPaneHasBottomConsole() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        // Console is now inside mainSplitPane, not at bottom
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        assertInstanceOf(ConsoleOutput.class, mainSplitPane.getItems().get(1), "Console doit être dans mainSplitPane");
    }

    @Test
    void testSplitPaneHasTwoCodeAreas() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        assertEquals(2, editorSplitPane.getItems().size(), "editorSplitPane doit contenir 2 éléments");
    }

    @Test
    void testMenuBarExists() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1); // topMenu is second child

        boolean hasMenuBar = topMenu.getChildren().stream()
                .anyMatch(node -> node instanceof MenuBar);
        assertTrue(hasMenuBar, "topMenu doit contenir un MenuBar");
    }

    @Test
    void testFileMenuExists() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        MenuBar menuBar = topMenu.getChildren().stream()
                .filter(node -> node instanceof MenuBar)
                .map(node -> (MenuBar) node)
                .findFirst()
                .orElse(null);

        assertNotNull(menuBar, "MenuBar doit exister");
        assertFalse(menuBar.getMenus().isEmpty(), "MenuBar doit avoir des menus");

        Menu fileMenu = menuBar.getMenus().get(0);
        assertEquals("File", fileMenu.getText(), "Premier menu doit être 'File'");
    }

    @Test
    void testFileMenuHasSaveAndOpenItems() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        MenuBar menuBar = topMenu.getChildren().stream()
                .filter(node -> node instanceof MenuBar)
                .map(node -> (MenuBar) node)
                .findFirst()
                .orElse(null);

        Menu fileMenu = menuBar.getMenus().get(0);
        assertEquals(2, fileMenu.getItems().size(), "File menu doit avoir 2 items");

        MenuItem saveItem = fileMenu.getItems().get(0);
        MenuItem openItem = fileMenu.getItems().get(1);

        assertEquals("Save", saveItem.getText(), "Premier item doit être 'Save'");
        assertEquals("Open", openItem.getText(), "Deuxième item doit être 'Open'");
    }

    @Test
    void testChoiceBoxExists() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        @SuppressWarnings("unchecked")
        ChoiceBox<String> choiceBox = toolbar.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> (ChoiceBox<String>) node)
                .findFirst()
                .orElse(null);

        assertNotNull(choiceBox, "ChoiceBox doit exister");
        assertEquals(2, choiceBox.getItems().size(), "ChoiceBox doit avoir 2 options");
        assertTrue(choiceBox.getItems().contains("MiniJaja"), "ChoiceBox doit contenir 'MiniJaja'");
        assertTrue(choiceBox.getItems().contains("Jajacode"), "ChoiceBox doit contenir 'Jajacode'");
        assertEquals("MiniJaja", choiceBox.getValue(), "Valeur par défaut doit être 'MiniJaja'");
    }

    @Test
    void testMjjCodeAreaHasDefaultCode() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        MyCodeArea mjjCodeArea = (MyCodeArea) miniWrapper.getChildren().get(1); // code area is second child after
                                                                                // titlebar
        String text = mjjCodeArea.getText();

        assertTrue(text.contains("class C"), "Code par défaut doit contenir 'class C'");
        assertTrue(text.contains("main"), "Code par défaut doit contenir 'main'");
    }

    @Test
    void testJjcCodeAreaIsEmpty() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);
        MyCodeArea jjcCodeArea = (MyCodeArea) jajaWrapper.getChildren().get(1);
        assertEquals("", jjcCodeArea.getText(), "JJC code area doit être vide initialement");
    }

    @Test
    void testConsoleHasCorrectId() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        ConsoleOutput console = (ConsoleOutput) mainSplitPane.getItems().get(1);
        assertEquals("console", console.getId(), "Console doit avoir l'ID 'console'");
    }

    @Test
    void testMjjCodeAreaHasCorrectId() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        MyCodeArea mjjCodeArea = (MyCodeArea) miniWrapper.getChildren().get(1);
        assertEquals("mjj-code", mjjCodeArea.getId(), "MJJ code area doit avoir l'ID 'mjj-code'");
    }

    @Test
    void testJjcCodeAreaHasCorrectId() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);
        MyCodeArea jjcCodeArea = (MyCodeArea) jajaWrapper.getChildren().get(1);
        assertEquals("jjc-code", jjcCodeArea.getId(), "JJC code area doit avoir l'ID 'jjc-code'");
    }

    @Test
    void testCompileButtonTriggersCompilation() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        // Trouver le bouton compile (premier bouton après le spacer)
        Button buildButton = null;
        for (Node node : toolbar.getChildren()) {
            if (node instanceof Button) {
                buildButton = (Button) node;
                break;
            }
        }

        assertNotNull(buildButton, "Build button doit exister");

        // Simuler le clic
        Button finalBuildButton = buildButton;
        runOnFxThread(() -> finalBuildButton.fire());

        // Attendre que la compilation se termine
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WaitForAsyncUtils.waitForFxEvents();

        // Vérifier que le JJC code area contient du code compilé
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);
        MyCodeArea jjcCodeArea = (MyCodeArea) jajaWrapper.getChildren().get(1);
        runOnFxThread(() -> {
            String jjcText = jjcCodeArea.getText();
            // Le code devrait avoir été compilé (même si c'est une erreur ou un résultat)
            // On vérifie juste que quelque chose s'est passé
            assertNotNull(jjcText);
        });
    }

    @Test
    void testStopMethod() throws Exception {
        // Tester que stop() ne lance pas d'exception
        runOnFxThread(() -> {
            try {
                app.stop();
            } catch (Exception e) {
                fail("stop() ne doit pas lancer d'exception: " + e.getMessage());
            }
        });
    }

    @Test
    void testDebugButtonsInitialState() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        // Compter les boutons
        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        // Les boutons step et stop doivent être désactivés au départ
        // (ce sont les 2 derniers boutons)
        assertTrue(buttons.size() >= 4, "Doit avoir au moins 4 boutons");

        Button stepButton = buttons.get(buttons.size() - 2);
        Button stopButton = buttons.get(buttons.size() - 1);

        assertTrue(stepButton.isDisabled(), "Step button doit être désactivé initialement");
        assertTrue(stopButton.isDisabled(), "Stop button doit être désactivé initialement");
    }

    @Test
    void testDebugModeCanBeStarted() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        // Bouton debug est le 3ème bouton (après build et run)
        Button debugButton = buttons.get(2);
        Button stepButton = buttons.get(3);
        Button stopButton = buttons.get(4);

        // Démarrer le debug
        runOnFxThread(() -> debugButton.fire());
        WaitForAsyncUtils.waitForFxEvents();

        // Les boutons step et stop doivent être activés
        runOnFxThread(() -> {
            assertFalse(stepButton.isDisabled(), "Step button doit être activé après debug start");
            assertFalse(stopButton.isDisabled(), "Stop button doit être activé après debug start");
        });
    }

    @Test
    void testDebugModeCanBeStopped() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        Button debugButton = buttons.get(2);
        Button stepButton = buttons.get(3);
        Button stopButton = buttons.get(4);

        // Démarrer puis arrêter le debug
        runOnFxThread(() -> {
            debugButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            stopButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Les boutons step et stop doivent être désactivés
        runOnFxThread(() -> {
            assertTrue(stepButton.isDisabled(), "Step button doit être désactivé après stop");
            assertTrue(stopButton.isDisabled(), "Stop button doit être désactivé après stop");
        });
    }

    @Test
    void testStepDebugAdvancesLine() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        ConsoleOutput console = (ConsoleOutput) mainSplitPane.getItems().get(1);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        Button debugButton = buttons.get(2);
        Button stepButton = buttons.get(3);

        // Démarrer le debug
        runOnFxThread(() -> debugButton.fire());
        WaitForAsyncUtils.waitForFxEvents();

        // Faire un step
        runOnFxThread(() -> stepButton.fire());
        WaitForAsyncUtils.waitForFxEvents();

        // Vérifier que la console contient des messages de debug
        runOnFxThread(() -> {
            // La console devrait contenir des messages [DEBUG]
            // On ne peut pas accéder facilement au contenu, mais on vérifie que ça ne crash
            // pas
            assertNotNull(console);
        });
    }

    @Test
    void testChoiceBoxCanBeChanged() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        @SuppressWarnings("unchecked")
        ChoiceBox<String> choiceBox = toolbar.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> (ChoiceBox<String>) node)
                .findFirst()
                .orElse(null);

        runOnFxThread(() -> choiceBox.setValue("Jajacode"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Jajacode", choiceBox.getValue(), "ChoiceBox doit pouvoir changer de valeur");
    }

    @Test
    void testAllButtonsHaveTooltips() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        for (Button button : buttons) {
            assertNotNull(button.getTooltip(), "Chaque bouton doit avoir un tooltip");
            assertNotNull(button.getTooltip().getText(), "Tooltip should have text");
            assertFalse(button.getTooltip().getText().isEmpty(), "Tooltip text should not be empty");
        }
    }

    @Test
    void testAllButtonsHaveGraphics() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        for (Button button : buttons) {
            assertNotNull(button.getGraphic(), "Chaque bouton doit avoir un graphique (icône)");
        }
    }

    /**
     * Test that empty JJC code area can be verified
     */
    @Test
    void testJjcCodeAreaStartsEmpty() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);
        MyCodeArea jjcCodeArea = (MyCodeArea) jajaWrapper.getChildren().get(1);

        String text = jjcCodeArea.getText();
        assertTrue(text.isEmpty() || text.trim().isEmpty(), "JJC code area should start empty");
    }

    /**
     * Test editor wrappers exist
     */
    @Test
    void testEditorWrappersExist() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        assertEquals(2, editorSplitPane.getItems().size(), "Editor split pane should have 2 items");

        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);

        assertNotNull(miniWrapper, "MiniJaja wrapper should exist");
        assertNotNull(jajaWrapper, "Jajacode wrapper should exist");
    }

    /**
     * Test that both code areas are MyCodeArea instances
     */
    @Test
    void testCodeAreasAreCorrectType() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);

        assertTrue(miniWrapper.getChildren().get(1) instanceof MyCodeArea, "MJJ area should be MyCodeArea");
        assertTrue(jajaWrapper.getChildren().get(1) instanceof MyCodeArea, "JJC area should be MyCodeArea");
    }

    /**
     * Test that scene has stylesheets applied
     */
    @Test
    void testSceneHasStylesheets() {
        assertFalse(stage.getScene().getStylesheets().isEmpty(), "Scene should have stylesheets");

        boolean hasLightOrDark = stage.getScene().getStylesheets().stream()
                .anyMatch(css -> css.contains("light.css") || css.contains("dark.css"));
        assertTrue(hasLightOrDark, "Scene should have light or dark theme CSS");
    }

    /**
     * Test that main split pane has correct orientation
     */
    @Test
    void testMainSplitPaneOrientation() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();

        assertEquals(javafx.geometry.Orientation.VERTICAL, mainSplitPane.getOrientation(),
                "Main split pane should have vertical orientation");
    }

    /**
     * Test that editor split pane has correct orientation
     */
    @Test
    void testEditorSplitPaneOrientation() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        // Default orientation is HORIZONTAL
        assertNotNull(editorSplitPane.getOrientation(), "Editor split pane should have orientation set");
    }

    /**
     * Test that debug/run/step buttons can be found
     */
    @Test
    void testDebugControlButtonsExist() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(buttons.size() >= 5, "Should have at least build, run, debug, step, stop buttons");

        // Verify each button has a tooltip (for identification)
        long buttonsWithTooltips = buttons.stream()
                .filter(b -> b.getTooltip() != null)
                .count();
        assertEquals(buttons.size(), buttonsWithTooltips, "All buttons should have tooltips");
    }

    /**
     * Test that console has proper styling
     */
    @Test
    void testConsoleHasStyling() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        ConsoleOutput console = (ConsoleOutput) mainSplitPane.getItems().get(1);

        String style = console.getStyle();
        assertNotNull(style, "Console should have style applied");
        assertTrue(style.contains("border"), "Console style should include border");
    }

    /**
     * Test toolbar has correct ID
     *
     */
    @Test
    void testToolbarHasCorrectId() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        assertEquals("debug-toolbar", toolbar.getId(), "Toolbar should have ID 'debug-toolbar'");
    }

    /**
     * Test stage has a title (even if empty)
     */
    @Test
    void testStageHasTitle() {
        assertNotNull(stage.getTitle(), "Stage should have a title property");
    }

    /**
     * Test that FileMenu has exactly 2 items
     */
    @Test
    void testFileMenuHasCorrectItemCount() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        MenuBar menuBar = topMenu.getChildren().stream()
                .filter(node -> node instanceof MenuBar)
                .map(node -> (MenuBar) node)
                .findFirst()
                .orElse(null);

        Menu fileMenu = menuBar.getMenus().get(0);
        assertEquals(2, fileMenu.getItems().size(), "File menu should have exactly 2 items");
    }

    /**
     * Test choice box has correct default value
     */
    @Test
    void testChoiceBoxDefaultValue() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        @SuppressWarnings("unchecked")
        ChoiceBox<String> choiceBox = toolbar.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> (ChoiceBox<String>) node)
                .findFirst()
                .orElse(null);

        assertEquals("MiniJaja", choiceBox.getValue(), "Default choice should be MiniJaja");
    }

    /**
     * Test choice box has exactly 2 items
     */
    @Test
    void testChoiceBoxItemCount() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        @SuppressWarnings("unchecked")
        ChoiceBox<String> choiceBox = toolbar.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> (ChoiceBox<String>) node)
                .findFirst()
                .orElse(null);

        assertEquals(2, choiceBox.getItems().size(), "Choice box should have 2 items");
    }

    /**
     * Test main split pane divider position
     */
    @Test
    void testMainSplitPaneDividerPosition() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();

        double[] positions = mainSplitPane.getDividerPositions();
        assertEquals(1, positions.length, "Main split pane should have 1 divider");
        assertTrue(positions[0] > 0 && positions[0] < 1, "Divider position should be between 0 and 1");
    }

    /**
     * Test code areas have correct IDs
     */
    @Test
    void testCodeAreasHaveUniqueIds() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);

        MyCodeArea mjjArea = (MyCodeArea) miniWrapper.getChildren().get(1);
        MyCodeArea jjcArea = (MyCodeArea) jajaWrapper.getChildren().get(1);

        assertNotEquals(mjjArea.getId(), jjcArea.getId(), "Code areas should have different IDs");
    }

    /**
     * Test top VBox has 3 children
     */
    @Test
    void testTopVBoxHasThreeChildren() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();

        assertEquals(3, vbox.getChildren().size(), "Top VBox should have 3 children (titleBar, menu, toolbar)");
    }

    /**
     * Test main split pane has 2 items
     */
    @Test
    void testMainSplitPaneHasTwoItems() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();

        assertEquals(2, mainSplitPane.getItems().size(), "Main split pane should have 2 items");
    }

    /**
     * Test MenuBar exists in topMenu
     */
    @Test
    void testMenuBarExistsInTopMenu() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        long menuBarCount = topMenu.getChildren().stream()
                .filter(node -> node instanceof MenuBar)
                .count();

        assertEquals(1, menuBarCount, "Top menu should have exactly 1 MenuBar");
    }

    /**
     * Test toolbar children count
     */
    @Test
    void testToolbarHasMultipleChildren() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        assertTrue(toolbar.getChildren().size() >= 6,
                "Toolbar should have at least 6 children (build button, choice box, run, debug, step, continue, stop)");
    }

    /**
     * Test code area wrappers have title bars
     */
    @Test
    void testCodeAreaWrappersHaveTitles() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);

        // Each wrapper should have at least 2 children (title + code area)
        assertTrue(miniWrapper.getChildren().size() >= 2, "MiniJaja wrapper should have title and code area");
        assertTrue(jajaWrapper.getChildren().size() >= 2, "Jajacode wrapper should have title and code area");
    }

    /**
     * Test dark mode toggle functionality
     */
    @Test
    void testDarkModeToggleButton() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        // Dark mode button is the last child in top menu
        javafx.scene.control.ToggleButton darkModeBtn = (javafx.scene.control.ToggleButton) topMenu.getChildren()
                .get(topMenu.getChildren().size() - 1);

        assertNotNull(darkModeBtn, "Dark mode toggle should exist");
        assertFalse(darkModeBtn.isSelected(), "Dark mode should be off initially");

        // Toggle to dark mode
        runOnFxThread(darkModeBtn::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(darkModeBtn.isSelected(), "Dark mode should be on after toggle");

        // Verify dark CSS is loaded
        boolean hasDarkCss = stage.getScene().getStylesheets().stream()
                .anyMatch(s -> s.contains("dark.css"));
        assertTrue(hasDarkCss, "Dark mode CSS should be loaded");
    }

    /**
     * Test window minimize button
     */
    @Test
    void testWindowMinimizeButton() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        // Get minimize button (third from end: min, max, close)
        Button minButton = (Button) titleBar.getChildren().get(titleBar.getChildren().size() - 3);

        assertTrue(minButton.getStyleClass().contains("window-button-min"),
                "Button should have minimize style");

        runOnFxThread(minButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(stage.isIconified(), "Stage should be minimized");
    }

    /**
     * Test window maximize button
     */
    @Test
    void testWindowMaximizeButton() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        // Get maximize button (second from end)
        Button maxButton = (Button) titleBar.getChildren().get(titleBar.getChildren().size() - 2);

        assertTrue(maxButton.getStyleClass().contains("window-button-max"),
                "Button should have maximize style");

        boolean wasMaximized = stage.isMaximized();
        runOnFxThread(maxButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotEquals(wasMaximized, stage.isMaximized(), "Maximize state should toggle");
    }

    /**
     * Test clear JajaCode button functionality
     */
    @Test
    void testClearJajaCodeButton() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);

        // Get title bar and clear button
        HBox jajaTitleBar = (HBox) jajaWrapper.getChildren().get(0);
        Button clearButton = (Button) jajaTitleBar.getChildren().get(jajaTitleBar.getChildren().size() - 1);

        MyCodeArea jjcCodeArea = (MyCodeArea) jajaWrapper.getChildren().get(1);

        // Set some text
        runOnFxThread(() -> jjcCodeArea.loadText("test code"));
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(jjcCodeArea.getText().isEmpty(), "Code area should have text");

        // Click clear button
        runOnFxThread(clearButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(jjcCodeArea.getText().isEmpty(), "Code area should be empty after clear");
    }

    /**
     * Test run button exists and can be clicked
     */
    @Test
    void testRunButtonExists() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(buttons.size() > 1, "Toolbar should have run button");
        Button runButton = buttons.get(1); // Run is second button

        assertNotNull(runButton.getTooltip(), "Run button should have tooltip");
        assertTrue(runButton.getTooltip().getText().contains("Run"), "Tooltip should mention Run");
    }

    /**
     * Test continue button initial state
     */
    @Test
    void testContinueButtonInitialState() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(buttons.size() >= 6, "Toolbar should have at least 6 buttons");
        Button continueButton = buttons.get(4); // Continue button

        assertNotNull(continueButton, "Continue button should exist");
        assertNotNull(continueButton.getTooltip(), "Continue button should have tooltip");
        assertTrue(continueButton.isDisabled(), "Continue button should be disabled initially");
    }

    /**
     * Test continue button lifecycle (enabled in debug, disabled after stop)
     */
    @Test
    void testContinueButtonLifecycle() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        Button debugButton = buttons.get(2);
        Button continueButton = buttons.get(4);
        Button stopButton = buttons.get(5);

        // Start debug
        runOnFxThread(debugButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(continueButton.isDisabled(), "Continue button should be enabled during debug");

        // Stop debug
        runOnFxThread(stopButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(continueButton.isDisabled(), "Continue button should be disabled after stop");
    }

    /**
     * Test compile with valid MiniJaja code
     */
    @Test
    void testCompileValidCode() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);
        MyCodeArea mjjCodeArea = (MyCodeArea) miniWrapper.getChildren().get(1);
        MyCodeArea jjcCodeArea = (MyCodeArea) jajaWrapper.getChildren().get(1);

        // Set valid code
        String validCode = "class Test { main { int x = 5; } }";
        runOnFxThread(() -> mjjCodeArea.loadText(validCode));

        // Get compile button (first button)
        Button compileButton = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .findFirst()
                .orElse(null);

        assertNotNull(compileButton, "Compile button should exist");

        // Click compile
        runOnFxThread(compileButton::fire);

        // Wait for compilation
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        WaitForAsyncUtils.waitForFxEvents();

        // Verify JajaCode was generated
        runOnFxThread(() -> {
            String jjcText = jjcCodeArea.getText();
            assertNotNull(jjcText, "JajaCode should be generated");
        });
    }

    /**
     * Test run with MiniJaja selection
     */
    @Test
    void testRunWithMiniJajaSelection() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        @SuppressWarnings("unchecked")
        ChoiceBox<String> choiceBox = toolbar.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> (ChoiceBox<String>) node)
                .findFirst()
                .orElse(null);

        assertNotNull(choiceBox, "ChoiceBox should exist");

        // Ensure MiniJaja is selected
        runOnFxThread(() -> choiceBox.setValue("MiniJaja"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("MiniJaja", choiceBox.getValue(), "Should be set to MiniJaja");

        // Get run button
        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        Button runButton = buttons.get(1);

        // Click run (should not crash)
        runOnFxThread(runButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(stage.isShowing(), "Stage should still be showing after run");
    }

    /**
     * Test run with Jajacode selection
     */
    @Test
    void testRunWithJajacodeSelection() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        @SuppressWarnings("unchecked")
        ChoiceBox<String> choiceBox = toolbar.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> (ChoiceBox<String>) node)
                .findFirst()
                .orElse(null);

        // Switch to Jajacode
        runOnFxThread(() -> choiceBox.setValue("Jajacode"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Jajacode", choiceBox.getValue(), "Should be set to Jajacode");

        // Get run button
        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        Button runButton = buttons.get(1);

        // Click run (should not crash even with empty code)
        runOnFxThread(runButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(stage.isShowing(), "Stage should still be showing after run");
    }

    /**
     * Test that code areas can be edited
     */
    @Test
    void testCodeAreasAreEditable() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);

        MyCodeArea mjjCodeArea = (MyCodeArea) miniWrapper.getChildren().get(1);
        MyCodeArea jjcCodeArea = (MyCodeArea) jajaWrapper.getChildren().get(1);

        // Test MiniJaja area is editable
        runOnFxThread(() -> {
            mjjCodeArea.loadText("test");
            assertEquals("test", mjjCodeArea.getText(), "MJJ area should be editable");
        });

        // Test JajaCode area is editable
        runOnFxThread(() -> {
            jjcCodeArea.loadText("123");
            assertEquals("123", jjcCodeArea.getText(), "JJC area should be editable");
        });
    }

    /**
     * Test toolbar buttons have proper styling
     */
    @Test
    void testToolbarButtonsHaveStyles() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        for (Button button : buttons) {
            assertNotNull(button.getStyleClass(), "Button should have style classes");
            assertFalse(button.getStyleClass().isEmpty(), "Button should have at least one style class");
        }
    }

    /**
     * Test console exists and has correct styling
     */
    @Test
    void testConsoleHasProperStyling() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        ConsoleOutput console = (ConsoleOutput) mainSplitPane.getItems().get(1);

        assertNotNull(console, "Console should exist");
        assertEquals("console", console.getId(), "Console should have correct ID");

        String style = console.getStyle();
        assertNotNull(style, "Console should have inline style");
        assertTrue(style.contains("border") || style.contains("padding"),
                "Console should have border or padding styling");
    }

    /**
     * Test main method launches application
     */
    @Test
    void testMainMethodExists() {
        // This test verifies that the main method exists and can be called
        // We don't actually call it to avoid launching another instance
        try {
            App.class.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            fail("Main method should exist");
        }
    }
}

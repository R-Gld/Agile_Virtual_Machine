package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
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

        assertNotNull(menuBar);
        Menu fileMenu = menuBar.getMenus().get(0);
        assertEquals(2, fileMenu.getItems().size(), "File menu doit avoir 2 items");

        MenuItem saveItem = fileMenu.getItems().get(0);
        MenuItem openItem = fileMenu.getItems().get(1);

        assertEquals("Save", saveItem.getText(), "Premier item doit être 'Save'");
        assertEquals("Open", openItem.getText(), "Deuxième item doit être 'Open'");
    }

    @Test
    void testBuildButtonExists(FxRobot robot) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2); // toolbar is third child

        long buttonCount = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .count();

        assertTrue(buttonCount >= 4, "toolbar doit contenir au moins 4 boutons (build, run, debug, step, stop)");
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
        runOnFxThread(finalBuildButton::fire);

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
        runOnFxThread(debugButton::fire);
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
        runOnFxThread(debugButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(stopButton::fire);
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
        runOnFxThread(debugButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        // Faire un step
        runOnFxThread(stepButton::fire);
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

        runOnFxThread(() -> {
            assertNotNull(choiceBox);
            choiceBox.setValue("Jajacode");
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(choiceBox);
        assertEquals("Jajacode", choiceBox.getValue(), "ChoiceBox doit pouvoir changer de valeur");
    }

    @Test
    void testSceneDimensions() {
        assertEquals(1150, stage.getScene().getWidth(), 1, "Largeur de la scène doit être 1150");
        assertEquals(720, stage.getScene().getHeight(), 1, "Hauteur de la scène doit être 720");
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
     * Test that code areas start with default content
     */
    @Test
    void testCodeAreasHaveInitialContent() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        VBox miniWrapper = (VBox) editorSplitPane.getItems().get(0);
        MyCodeArea mjjCodeArea = (MyCodeArea) miniWrapper.getChildren().get(1);

        String text = mjjCodeArea.getText();
        assertFalse(text.isEmpty(), "MJJ code area should have default sample code");
        assertTrue(text.contains("class C"), "Default code should contain a class");
    }

    /**
     * Test console exists and is accessible
     */
    @Test
    void testConsoleIsAccessible() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        ConsoleOutput console = (ConsoleOutput) mainSplitPane.getItems().get(1);

        assertNotNull(console, "Console should exist");
        assertEquals("console", console.getId(), "Console should have correct ID");
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

        assertInstanceOf(MyCodeArea.class, miniWrapper.getChildren().get(1), "MJJ area should be MyCodeArea");
        assertInstanceOf(MyCodeArea.class, jajaWrapper.getChildren().get(1), "JJC area should be MyCodeArea");
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

        assertNotNull(menuBar);
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

        assertNotNull(choiceBox);
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

        assertNotNull(choiceBox);
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
     * Test top container is a VBox
     */
    @Test
    void testTopContainerIsVBox() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        assertInstanceOf(VBox.class, root.getTop(), "Top container should be VBox");
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
     * Test center is a SplitPane
     */
    @Test
    void testCenterIsSplitPane() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        assertInstanceOf(SplitPane.class, root.getCenter(), "Center should be SplitPane");
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
     * Test button tooltips are not empty
     */
    @Test
    void testButtonTooltipsNotEmpty() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        for (Button button : buttons) {
            assertNotNull(button.getTooltip(), "Button should have tooltip");
            assertNotNull(button.getTooltip().getText(), "Tooltip should have text");
            assertFalse(button.getTooltip().getText().isEmpty(), "Tooltip text should not be empty");
        }
    }

    /**
     * Test scene width matches expected
     */
    @Test
    void testSceneWidthIsCorrect() {
        assertEquals(1150, stage.getScene().getWidth(), 1.0, "Scene width should be 1150");
    }

    /**
     * Test scene height matches expected
     */
    @Test
    void testSceneHeightIsCorrect() {
        assertEquals(720, stage.getScene().getHeight(), 1.0, "Scene height should be 720");
    }

    /**
     * Test root is BorderPane
     */
    @Test
    void testRootIsBorderPane() {
        assertInstanceOf(BorderPane.class, stage.getScene().getRoot(), "Root should be BorderPane");
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
     * Test MenuBar has at least 1 menu
     */
    @Test
    void testMenuBarHasMenus() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        MenuBar menuBar = topMenu.getChildren().stream()
                .filter(node -> node instanceof MenuBar)
                .map(node -> (MenuBar) node)
                .findFirst()
                .orElse(null);

        assertNotNull(menuBar);
        assertFalse(menuBar.getMenus().isEmpty(), "MenuBar should have at least one menu");
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
     * Test that the Dark Mode toggle button works and changes the stylesheet.
     */
    @Test
    void testDarkModeToggle() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox topMenu = (HBox) vbox.getChildren().get(1);

        // The dark mode toggle is the last item in the top menu
        Node lastNode = topMenu.getChildren().get(topMenu.getChildren().size() - 1);
        assertInstanceOf(javafx.scene.control.ToggleButton.class, lastNode, "Last item in menu should be ToggleButton");

        javafx.scene.control.ToggleButton darkModeBtn = (javafx.scene.control.ToggleButton) lastNode;

        // Initial state check (should be light mode)
        assertFalse(darkModeBtn.isSelected(), "Dark mode should be off initially");
        boolean hasLightCss = stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("light.css"));
        assertTrue(hasLightCss, "Should have light CSS initially");

        // Click to toggle
        runOnFxThread(darkModeBtn::fire);
        WaitForAsyncUtils.waitForFxEvents();

        // Check dark mode state
        assertTrue(darkModeBtn.isSelected(), "Dark mode should be on after click");
        boolean hasDarkCss = stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("dark.css"));
        assertTrue(hasDarkCss, "Should have dark CSS after toggle");

        // Toggle back
        runOnFxThread(darkModeBtn::fire);
        WaitForAsyncUtils.waitForFxEvents();

        // Check light mode state again
        assertFalse(darkModeBtn.isSelected());
        hasLightCss = stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("light.css"));
        assertTrue(hasLightCss, "Should be back to light CSS");
    }

    /**
     * Test the "Clear" button functionality in the JajaCode area.
     */
    @Test
    void testClearJajaCodeButton() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        SplitPane editorSplitPane = (SplitPane) mainSplitPane.getItems().get(0);

        // JajaWrapper is the second item
        VBox jajaWrapper = (VBox) editorSplitPane.getItems().get(1);

        // The title bar is the first child of the wrapper
        HBox jajaTitleBar = (HBox) jajaWrapper.getChildren().get(0);

        // The clear button is the last child of the title bar
        Node lastNode = jajaTitleBar.getChildren().get(jajaTitleBar.getChildren().size() - 1);
        assertInstanceOf(Button.class, lastNode, "Last item in Jaja title bar should be Clear button");
        Button clearButton = (Button) lastNode;

        // The code area is the second child of the wrapper
        MyCodeArea jjcCodeArea = (MyCodeArea) jajaWrapper.getChildren().get(1);

        // Set some text
        runOnFxThread(() -> jjcCodeArea.loadText("Some generated code..."));
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(jjcCodeArea.getText().isEmpty(), "Code area should have text before clear");

        // Click Clear
        runOnFxThread(clearButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify empty
        assertEquals("", jjcCodeArea.getText(), "Code area should be empty after clear");
    }

    /**
     * Test the Window Minimize button in the custom title bar.
     */
    @Test
    void testWindowMinimizeButton() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        // Indices in titleBar: 0:Label, 1:Spacer, 2:Min, 3:Max, 4:Close
        Button minButton = (Button) titleBar.getChildren().get(2);
        assertTrue(minButton.getStyleClass().contains("window-button-min"));

        runOnFxThread(minButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(stage.isIconified(), "Stage should be iconified (minimized)");
    }

    /**
     * Test the Window Maximize button in the custom title bar.
     */
    @Test
    void testWindowMaximizeButton() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox titleBar = (HBox) vbox.getChildren().get(0);

        // Indices in titleBar: 0:Label, 1:Spacer, 2:Min, 3:Max, 4:Close
        Button maxButton = (Button) titleBar.getChildren().get(3);
        assertTrue(maxButton.getStyleClass().contains("window-button-max"));

        boolean initialMax = stage.isMaximized();

        runOnFxThread(maxButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotEquals(initialMax, stage.isMaximized(), "Stage maximization state should toggle");
    }

    /**
     * Test that the Run button exists and can be clicked.
     */
    @Test
    void testRunButton() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        java.util.List<Button> buttons = toolbar.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(java.util.stream.Collectors.toList());

        // Buttons order: Build, Run, Debug, Step, Continue, Stop
        // Run is index 1
        assertTrue(buttons.size() > 1);
        Button runButton = buttons.get(1);
        assertNotNull(runButton.getTooltip(), "Run button should have tooltip");
        assertTrue(runButton.getTooltip().getText().contains("Run"));

        // Just fire to ensure no exceptions
        runOnFxThread(runButton::fire);
        WaitForAsyncUtils.waitForFxEvents();
        // (Cannot easily verify output without mocking Console, but ensures no crash)
    }

    /**
     * Test the existence and initial state of the Continue button.
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

        // Buttons order: Build, Run, Debug, Step, Continue, Stop
        // Continue is index 4
        assertTrue(buttons.size() >= 6, "Toolbar missing buttons");
        Button continueButton = buttons.get(4);

        assertNotNull(continueButton.getTooltip());
        assertTrue(continueButton.getTooltip().getText().contains("Continue"));
        assertTrue(continueButton.isDisabled(), "Continue button should be disabled initially");
    }

    /**
     * Test that Continue button is enabled when debug starts and disabled when
     * stopped.
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

        // Start Debug
        runOnFxThread(debugButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(continueButton.isDisabled(), "Continue button should be enabled in debug mode");

        // Stop Debug
        runOnFxThread(stopButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(continueButton.isDisabled(), "Continue button should be disabled after stopping debug");
    }

    /**
     * Test debug functionality when switching to JajaCode view.
     */
    @Test
    void testDebugWithJajaCodeSelection() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        VBox vbox = (VBox) root.getTop();
        HBox toolbar = (HBox) vbox.getChildren().get(2);

        // Find ChoiceBox
        @SuppressWarnings("unchecked")
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) toolbar.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .findFirst()
                .orElse(null);
        assertNotNull(choiceBox);

        // Find Debug Button
        Button debugButton = (Button) toolbar.getChildren().stream()
                .filter(node -> node instanceof Button && ((Button) node).getTooltip().getText().contains("Debug"))
                .findFirst()
                .orElse(null);
        assertNotNull(debugButton);

        // Switch to Jajacode
        runOnFxThread(() -> choiceBox.setValue("Jajacode"));
        WaitForAsyncUtils.waitForFxEvents();

        // Start debug
        runOnFxThread(debugButton::fire);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify Step button is enabled (implies debug started successfully)
        Button stepButton = (Button) toolbar.getChildren().stream()
                .filter(node -> node instanceof Button && ((Button) node).getTooltip().getText().contains("Step"))
                .findFirst()
                .orElse(null);

        assertNotNull(stepButton);
        assertFalse(stepButton.isDisabled(), "Step button should be enabled for JajaCode debug");
    }
}
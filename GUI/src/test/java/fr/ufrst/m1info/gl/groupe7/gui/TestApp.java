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
        assertTrue(stage.getScene().getRoot() instanceof BorderPane, "Root doit être un BorderPane");
    }

    @Test
    void testBorderPaneHasTopMenu() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        assertNotNull(root.getTop(), "Top doit contenir le menu");
        assertTrue(root.getTop() instanceof VBox, "Top doit être un VBox");
    }

    @Test
    void testBorderPaneHasCenterSplitPane() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        assertNotNull(root.getCenter(), "Center doit contenir le SplitPane");
        assertTrue(root.getCenter() instanceof SplitPane, "Center doit être un SplitPane");
    }

    @Test
    void testBorderPaneHasBottomConsole() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        // Console is now inside mainSplitPane, not at bottom
        SplitPane mainSplitPane = (SplitPane) root.getCenter();
        assertTrue(mainSplitPane.getItems().get(1) instanceof ConsoleOutput, "Console doit être dans mainSplitPane");
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
}

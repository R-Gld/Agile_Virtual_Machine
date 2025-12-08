package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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

import javafx.application.Platform;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe ConsoleOutput.
 */
@ExtendWith(ApplicationExtension.class)
class TestConsoleOutput {

    private ConsoleOutput consoleOutput;

    @Start
    void start(Stage stage) {
        consoleOutput = new ConsoleOutput("test-console");
        stage.setScene(new Scene(new StackPane(consoleOutput), 400, 200));
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
    void testConsoleOutputHasCorrectId() {
        assertEquals("test-console", consoleOutput.getId(), "L'ID du composant doit être correct");
    }

    @Test
    void testConsoleOutputInitiallyEmpty() {
        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().isEmpty(), "La console doit être vide initialement");
        });
    }

    @Test
    void testPrintMessageAddsText() {
        runOnFxThread(() -> consoleOutput.printMessage("Hello World"));
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().contains("Hello World"), "Le message doit être affiché");
        });
    }

    @Test
    void testPrintMessageAddsNewline() {
        runOnFxThread(() -> consoleOutput.printMessage("Line1"));
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().endsWith("\n"), "Le message doit se terminer par un saut de ligne");
        });
    }

    @Test
    void testPrintMultipleMessages() {
        runOnFxThread(() -> {
            consoleOutput.printMessage("First");
            consoleOutput.printMessage("Second");
            consoleOutput.printMessage("Third");
        });
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            String content = textArea.getText();
            assertTrue(content.contains("First"), "Premier message manquant");
            assertTrue(content.contains("Second"), "Deuxième message manquant");
            assertTrue(content.contains("Third"), "Troisième message manquant");
        });
    }

    @Test
    void testPrintNullMessageDoesNothing() {
        runOnFxThread(() -> consoleOutput.printMessage(null));
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().isEmpty(), "Un message null ne doit rien afficher");
        });
    }

    @Test
    void testClearRemovesAllContent() {
        runOnFxThread(() -> {
            consoleOutput.printMessage("Some content");
            consoleOutput.printMessage("More content");
        });
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> consoleOutput.clear());
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().isEmpty(), "La console doit être vide après clear()");
        });
    }

    @Test
    void testClearOnEmptyConsole() {
        runOnFxThread(() -> consoleOutput.clear());
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().isEmpty(), "Clear sur console vide ne doit pas poser de problème");
        });
    }

    @Test
    void testTextAreaIsNotEditable() {
        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertFalse(textArea.isEditable(), "La console ne doit pas être éditable");
        });
    }

    @Test
    void testClearButtonExists() {
        runOnFxThread(() -> {
            Button clearButton = getClearButton();
            assertNotNull(clearButton, "Le bouton clear doit exister");
            assertEquals("Clear", clearButton.getText(), "Le texte du bouton doit être 'Clear'");
        });
    }

    @Test
    void testClearButtonClears(FxRobot robot) {
        runOnFxThread(() -> consoleOutput.printMessage("Test message"));
        WaitForAsyncUtils.waitForFxEvents();

        // Simuler le clic sur le bouton clear via appel direct (plus fiable en
        // headless)
        runOnFxThread(() -> {
            Button clearButton = getClearButton();
            clearButton.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().isEmpty(), "Le bouton clear doit vider la console");
        });
    }

    @Test
    void testPrintEmptyStringAddsNewline() {
        runOnFxThread(() -> consoleOutput.printMessage(""));
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertEquals("\n", textArea.getText(), "Un message vide doit ajouter seulement un saut de ligne");
        });
    }

    @Test
    void testPrintSpecialCharacters() {
        runOnFxThread(() -> consoleOutput.printMessage("Spécial: éàü €$£"));
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().contains("Spécial: éàü €$£"),
                    "Les caractères spéciaux doivent être affichés");
        });
    }

    @Test
    void testConsoleHasChildren() {
        runOnFxThread(() -> {
            assertFalse(consoleOutput.getChildren().isEmpty(),
                    "La console doit avoir des enfants (TextArea et Button)");
            assertEquals(2, consoleOutput.getChildren().size(), "La console doit avoir 2 enfants");
        });
    }

    @Test
    void testPrintLongMessage() {
        String longMessage = "A".repeat(1000);
        runOnFxThread(() -> consoleOutput.printMessage(longMessage));
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            assertTrue(textArea.getText().contains(longMessage), "Les longs messages doivent être affichés");
        });
    }

    @Test
    void testMessagesPreserveOrder() {
        runOnFxThread(() -> {
            consoleOutput.printMessage("1");
            consoleOutput.printMessage("2");
            consoleOutput.printMessage("3");
        });
        WaitForAsyncUtils.waitForFxEvents();

        runOnFxThread(() -> {
            TextArea textArea = getTextArea();
            String content = textArea.getText();
            int pos1 = content.indexOf("1");
            int pos2 = content.indexOf("2");
            int pos3 = content.indexOf("3");
            assertTrue(pos1 < pos2 && pos2 < pos3, "L'ordre des messages doit être préservé");
        });
    }

    // Méthodes utilitaires pour accéder aux composants internes
    private TextArea getTextArea() {
        return (TextArea) consoleOutput.getChildren().stream()
                .filter(node -> node instanceof TextArea)
                .findFirst()
                .orElse(null);
    }

    private Button getClearButton() {
        return (Button) consoleOutput.getChildren().stream()
                .filter(node -> node instanceof Button)
                .findFirst()
                .orElse(null);
    }
}

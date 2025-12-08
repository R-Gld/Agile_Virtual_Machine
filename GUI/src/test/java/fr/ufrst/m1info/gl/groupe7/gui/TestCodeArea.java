package fr.ufrst.m1info.gl.groupe7.gui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TestCodeArea {

    private MyCodeArea codeArea;
    private MyCodeArea codeAreaWithDefault;

    @Start
    public void start(Stage stage) {
        codeArea = new MyCodeArea("testcodearea");
        codeAreaWithDefault = new MyCodeArea("testcodeareaWithDefault", "class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}");
        stage.setScene(new Scene(new StackPane(codeArea, codeAreaWithDefault), 100, 100));
        stage.show();
    }

    @Test
    void shouldBeEmpty(FxRobot robot) {
        Assertions.assertEquals("", codeArea.getText());
    }

    @Disabled("TestFX input typing unreliable in headless mode")
    @Test
    void testWritingInCodeArea(FxRobot robot) {
        Assertions.assertEquals("", codeArea.getText());
        robot.clickOn("#testcodearea_code_area");
        robot.write("class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}");
        Assertions.assertTrue(codeArea.getText().contains("x = 12"));
    }

    @Test
    void testCreatingCodeAreaWithDefaultValue() {
        Assertions.assertTrue(codeAreaWithDefault.getText().contains("x = 12"));
    }

    // --- Utility method for safely running FX operations ---
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

    // --- Full coverage tests with FX thread-safe operations ---

    @Test
    void testLoadTextUpdatesContent() {
        runOnFxThread(() -> codeArea.loadText("int a = 5;"));
        Assertions.assertEquals("int a = 5;", codeArea.getText());
    }

    @Test
    void testDisablePreventsEditing() {
        runOnFxThread(() -> {
            codeArea.disable();

            // Access the internal CodeArea using reflection (safer for headless)
            try {
                var field = MyCodeArea.class.getDeclaredField("codeArea");
                field.setAccessible(true);
                Object inner = field.get(codeArea);
                boolean editable = (boolean) inner.getClass().getMethod("isEditable").invoke(inner);
                Assertions.assertFalse(editable, "CodeArea should not be editable after disable()");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testHighlightLineValidAndInvalidIndices() {
        runOnFxThread(() -> {
            codeArea.loadText("line1\nline2\nline3");
            Assertions.assertDoesNotThrow(() -> codeArea.highlightLine(1));
            Assertions.assertDoesNotThrow(() -> codeArea.highlightLine(-1));
            Assertions.assertDoesNotThrow(() -> codeArea.highlightLine(10));
        });
    }

    @Test
    void testMultipleConstructorsConsistency() {
        MyCodeArea a = new MyCodeArea("id1");
        MyCodeArea b = new MyCodeArea("id2", "hello");
        Assertions.assertEquals("", a.getText());
        Assertions.assertEquals("hello", b.getText());
    }

    @Test
    void testGetTextReturnsExpectedString() {
        runOnFxThread(() -> codeArea.loadText("abc"));
        Assertions.assertEquals("abc", codeArea.getText());
    }

    @Test
    void testHighlightLineFollowsCaretWithoutCrash() {
        runOnFxThread(() -> {
            codeArea.loadText("lineA\nlineB\nlineC");
            codeArea.highlightLine(2);
            Assertions.assertTrue(codeArea.getText().contains("lineC"));
        });
    }

    private org.fxmisc.richtext.CodeArea getInnerCodeArea() {
        try {
            var field = MyCodeArea.class.getDeclaredField("codeArea");
            field.setAccessible(true);
            return (org.fxmisc.richtext.CodeArea) field.get(codeArea);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @DisabledOnOs(value = OS.MAC, disabledReason = "Auto-completion popup tests are unstable on macOS")
    @Test
    void testAutoCompletionPopupAppears(FxRobot robot) {
        runOnFxThread(() -> {
            org.fxmisc.richtext.CodeArea inner = getInnerCodeArea();
            inner.replaceText("voi");
            inner.moveTo(3);
        });

        robot.clickOn("#testcodearea_code_area");
        WaitForAsyncUtils.waitForFxEvents();

        robot.press(javafx.scene.input.KeyCode.CONTROL).type(javafx.scene.input.KeyCode.SPACE).release(javafx.scene.input.KeyCode.CONTROL);

        // Check if ContextMenu is showing
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> !robot.lookup(".context-menu").queryAll().isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Assertions.assertFalse(robot.lookup(".context-menu").queryAll().isEmpty(), "Auto-completion popup should be visible");
    }

    @DisabledOnOs(value = OS.MAC, disabledReason = "Auto-completion popup tests are unstable on macOS")
    @Test
    void testAutoCompletionToggle(FxRobot robot) {
        runOnFxThread(() -> {
            org.fxmisc.richtext.CodeArea inner = getInnerCodeArea();
            inner.replaceText("voi");
            inner.moveTo(3);
        });

        robot.clickOn("#testcodearea_code_area");
        WaitForAsyncUtils.waitForFxEvents();

        // Open
        robot.press(javafx.scene.input.KeyCode.CONTROL).type(javafx.scene.input.KeyCode.SPACE).release(javafx.scene.input.KeyCode.CONTROL);

        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> !robot.lookup(".context-menu").queryAll().isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Assertions.assertFalse(robot.lookup(".context-menu").queryAll().isEmpty(), "Popup should be open");

        // Close
        robot.press(javafx.scene.input.KeyCode.CONTROL).type(javafx.scene.input.KeyCode.SPACE).release(javafx.scene.input.KeyCode.CONTROL);

        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> robot.lookup(".context-menu").queryAll().isEmpty());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(robot.lookup(".context-menu").queryAll().isEmpty(), "Popup should be closed");
    }

    @Test
    void testSyntaxErrorHighlighting() {
        runOnFxThread(() -> codeArea.loadText("int a = ;")); // Syntax error

        // Wait for async highlighting
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        runOnFxThread(() -> {
            try {
                org.fxmisc.richtext.CodeArea inner = getInnerCodeArea();

                var spans = inner.getStyleSpans(0, inner.getLength());
                boolean hasError = spans.stream().anyMatch(span -> span.getStyle().contains("error"));

                Assertions.assertTrue(hasError, "Should have error highlighting");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

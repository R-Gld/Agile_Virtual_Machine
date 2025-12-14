package fr.ufrst.m1info.gl.groupe7.gui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import javafx.scene.control.ContextMenu;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;

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
        stage.toFront();
        stage.requestFocus();
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

    @Disabled
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

    @Disabled
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

    @Test
    void testDeclarationAfterInstructionHighlightingAndTooltip() throws Exception {
        runOnFxThread(() -> codeArea.loadText("class C {\n  int x = 0;\n  main {\n    x = 12;\n    int my;\n  }\n}\n"));

        // give some time for parsing/highlighting
        Thread.sleep(200);

        runOnFxThread(() -> {
            try {
                Field f = MyCodeArea.class.getDeclaredField("errorDeclaredAfterInstructionRanges");
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<javafx.scene.control.IndexRange> myRanges = (List<javafx.scene.control.IndexRange>) f.get(codeArea);
                Assertions.assertFalse(myRanges.isEmpty(), "Expected declaration-after-instruction ranges to be detected");
                String txt = codeArea.getText();
                boolean hasInt = myRanges.stream().anyMatch(r -> txt.substring(r.getStart(), Math.min(r.getEnd(), txt.length())).equals("int"));
                Assertions.assertTrue(hasInt, "The 'int' token should appear in the reported ranges");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testHandleAutoClosePairsAndSkip() {
        runOnFxThread(() -> {
            EditorPreferences.getInstance().setAutoClosePairs(true);
            codeArea.loadText("");
            var inner = getInnerCodeArea();
            inner.replaceText("");
            inner.moveTo(0);
            try {
                Method m = MyCodeArea.class.getDeclaredMethod("handleAutoClose", KeyEvent.class);
                m.setAccessible(true);
                KeyEvent open = new KeyEvent(KeyEvent.KEY_TYPED, "(", "(", KeyCode.UNDEFINED, false, false, false, false);
                m.invoke(codeArea, open);
                Assertions.assertEquals("()", codeArea.getText());

                // Now ensure typing a closing bracket when one exists just moves caret
                inner.moveTo(1); // caret between ( and )
                KeyEvent close = new KeyEvent(KeyEvent.KEY_TYPED, ")", ")", KeyCode.UNDEFINED, false, false, false, false);
                m.invoke(codeArea, close);
                // After moving caret, text remains the same and caret goes past closing char
                Assertions.assertEquals("()", codeArea.getText());
                Assertions.assertEquals(2, inner.getCaretPosition());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testHandleSmartIndentationBetweenBraces() {
        runOnFxThread(() -> {
            EditorPreferences.getInstance().setSmartIndentation(true);
            EditorPreferences.getInstance().setTabSize(4);
            codeArea.loadText("{}");
            var inner = getInnerCodeArea();
            inner.moveTo(1); // between { and }
            try {
                Method m = MyCodeArea.class.getDeclaredMethod("handleSmartIndentation", javafx.scene.input.KeyEvent.class);
                m.setAccessible(true);
                KeyEvent enter = new KeyEvent(KeyEvent.KEY_PRESSED, "\r", "\r", KeyCode.ENTER, false, false, false, false);
                m.invoke(codeArea, enter);
                // After smart indent, text should contain a line between braces with spaces
                Assertions.assertTrue(codeArea.getText().contains("{\n    \n}"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testShowManualCompletionAndDisplaySuggestionsViaReflection() {
        runOnFxThread(() -> {
            // Put a prefix 'voi' which should match 'void'
            var inner = getInnerCodeArea();
            inner.replaceText("voi");
            inner.moveTo(3);
            try {
                Method showManual = MyCodeArea.class.getDeclaredMethod("showManualCompletion");
                showManual.setAccessible(true);
                showManual.invoke(codeArea);

                Field popupField = MyCodeArea.class.getDeclaredField("autoCompletionPopup");
                popupField.setAccessible(true);
                ContextMenu cm = (ContextMenu) popupField.get(codeArea);
                Assertions.assertNotNull(cm);
                // If suggestions available, menu items should not be empty
                Assertions.assertFalse(cm.getItems().isEmpty());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testToggleBreakpointReflectively() {
        runOnFxThread(() -> {
            try {
                Field bpField = MyCodeArea.class.getDeclaredField("breakpoints");
                bpField.setAccessible(true);
                @SuppressWarnings("unchecked")
                var set = (java.util.Set<Integer>) bpField.get(codeArea);
                Assertions.assertTrue(set.isEmpty());

                Method m = MyCodeArea.class.getDeclaredMethod("toggleBreakpoint", int.class, javafx.scene.shape.Circle.class);
                m.setAccessible(true);
                Circle c = new Circle(5);
                m.invoke(codeArea, 3, c);
                Assertions.assertTrue(set.contains(3));
                Assertions.assertTrue(c.isVisible());
                m.invoke(codeArea, 3, c);
                Assertions.assertFalse(set.contains(3));
                Assertions.assertFalse(c.isVisible());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testAutoCloseOtherPairs() {
        runOnFxThread(() -> {
            EditorPreferences.getInstance().setAutoClosePairs(true);
            codeArea.loadText("");
            var inner = getInnerCodeArea();
            inner.replaceText("");
            inner.moveTo(0);
            try {
                Method m = MyCodeArea.class.getDeclaredMethod("handleAutoClose", KeyEvent.class);
                m.setAccessible(true);
                // Brace
                KeyEvent brace = new KeyEvent(KeyEvent.KEY_TYPED, "{", "{", KeyCode.UNDEFINED, false, false, false, false);
                m.invoke(codeArea, brace);
                Assertions.assertEquals("{}", codeArea.getText());
                // Bracket
                inner.replaceText("");
                inner.moveTo(0);
                KeyEvent bracket = new KeyEvent(KeyEvent.KEY_TYPED, "[", "[", KeyCode.UNDEFINED, false, false, false, false);
                m.invoke(codeArea, bracket);
                Assertions.assertEquals("[]", codeArea.getText());
                // Quote
                inner.replaceText("");
                inner.moveTo(0);
                KeyEvent quote = new KeyEvent(KeyEvent.KEY_TYPED, "\"", "\"", KeyCode.UNDEFINED, false, false, false, false);
                m.invoke(codeArea, quote);
                Assertions.assertEquals("\"\"", codeArea.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testShowAutoCompletionDisplaysPopup() {
        runOnFxThread(() -> {
            // Setup a prefix and ensure caret bounds exist
            var inner = getInnerCodeArea();
            inner.replaceText("voi");
            inner.moveTo(3);
            inner.requestFocus();
            try {
                Method disp = MyCodeArea.class.getDeclaredMethod("displaySuggestions", java.util.List.class, int.class, int.class);
                disp.setAccessible(true);
                List<String> suggestions = java.util.List.of("void", "main");
                disp.invoke(codeArea, suggestions, 0, 3);
                Field popupField = MyCodeArea.class.getDeclaredField("autoCompletionPopup");
                popupField.setAccessible(true);
                ContextMenu cm = (ContextMenu) popupField.get(codeArea);
                // If bounds were available, show() would set showing to true; otherwise the items are set
                Assertions.assertNotNull(cm);
                Assertions.assertFalse(cm.getItems().isEmpty());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testSmartIndentationWhenLineEndsWithBrace() {
        runOnFxThread(() -> {
            EditorPreferences.getInstance().setSmartIndentation(true);
            EditorPreferences.getInstance().setTabSize(2);
            codeArea.loadText("line {");
            var inner = getInnerCodeArea();
            // Put caret at end of line (after '{') so trimmedLine.endsWith("{") is true
            inner.moveTo(inner.getLength());
            try {
                Method m = MyCodeArea.class.getDeclaredMethod("handleSmartIndentation", javafx.scene.input.KeyEvent.class);
                m.setAccessible(true);
                KeyEvent enter = new KeyEvent(KeyEvent.KEY_PRESSED, "\r", "\r", KeyCode.ENTER, false, false, false, false);
                m.invoke(codeArea, enter);
                // After smart indent insertion, we expect a newline with additional indentation
                Assertions.assertTrue(codeArea.getText().contains("{\n  "));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

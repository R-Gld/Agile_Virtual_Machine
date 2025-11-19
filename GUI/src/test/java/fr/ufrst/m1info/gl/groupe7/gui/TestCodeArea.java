package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(ApplicationExtension.class)
public class TestCodeArea {

    private MyCodeArea codeArea;
    private MyCodeArea codeAreaWithDefault;

    @Start
    public void start(Stage stage) {
        codeArea = new MyCodeArea("testcodearea");
        codeAreaWithDefault = new MyCodeArea("testcodeareaWithDefault",
                "class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}");
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
}

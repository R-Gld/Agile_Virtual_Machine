package fr.ufrst.m1info.gl.groupe7.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TestCodeAreaSuggestions {

    private MyCodeArea area;

    @Start
    public void start(Stage stage) {
        area = new MyCodeArea("test", "class C {\n  int myVar = 0;\n  void foo() { }\n  main { myVar = 1; foo(); }\n}\n");
        stage.setScene(new Scene(new StackPane(area), 100, 100));
        stage.show();
    }

    // Utility to run things on FX thread synchronously
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
    void declaredMethodsAndVariablesAppearInSuggestions() throws Exception {
        // give some time for initial highlighting and semantic analysis
        Thread.sleep(200);

        // Use reflection to invoke private getSuggestions
        Method m = MyCodeArea.class.getDeclaredMethod("getSuggestions", String.class);
        m.setAccessible(true);

        runOnFxThread(() -> {
            try {
                @SuppressWarnings("unchecked")
                List<String> suggestionsF = (List<String>) m.invoke(area, "f");
                assertTrue(suggestionsF.contains("foo"), "Suggestions should contain declared method 'foo'");

                @SuppressWarnings("unchecked")
                List<String> suggestionsMy = (List<String>) m.invoke(area, "my");
                assertTrue(suggestionsMy.contains("myVar"), "Suggestions should contain declared variable 'myVar' when using prefix length >= 2");

                @SuppressWarnings("unchecked")
                List<String> suggestionsM = (List<String>) m.invoke(area, "m");
                assertTrue(!suggestionsM.contains("myVar"), "Single-letter prefix should not suggest declared variable 'myVar'");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

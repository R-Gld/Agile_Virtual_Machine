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
                assertTrue(suggestionsF.contains("foo()"), "Suggestions should contain declared method 'foo()'");

                @SuppressWarnings("unchecked")
                List<String> suggestionsMy = (List<String>) m.invoke(area, "my");
                assertTrue(suggestionsMy.contains("myVar"), "Suggestions should contain declared variable 'myVar'");

                // Check ordering: methods should appear before generic keywords for matching prefixes
                @SuppressWarnings("unchecked")
                List<String> suggestionsF2 = (List<String>) m.invoke(area, "f");
                int idxFoo = suggestionsF2.indexOf("foo()");
                int idxFinal = suggestionsF2.indexOf("final");
                assertTrue(idxFoo >= 0 && idxFinal >= 0 && idxFoo < idxFinal, "Method 'foo()' should be suggested before keyword 'final'");

                // For variables: variables from closest scope should be before generic keywords like 'main'
                @SuppressWarnings("unchecked")
                List<String> suggestionsM2 = (List<String>) m.invoke(area, "m");
                int idxMyVar = suggestionsM2.indexOf("myVar");
                int idxMain = suggestionsM2.indexOf("main");
                assertTrue(idxMyVar >= 0 && idxMain >= 0 && idxMyVar < idxMain, "Variable 'myVar' should be suggested before keyword 'main'");

                @SuppressWarnings("unchecked")
                List<String> suggestionsM3 = (List<String>) m.invoke(area, "m");
                assertTrue(suggestionsM3.contains("myVar"), "Single-letter prefix should suggest declared variable 'myVar' when not declaring");

                // Now simulate typing a new variable after a type keyword; suggestions should NOT propose existing variable names
                runOnFxThread(() -> {
                    try {
                        var field = MyCodeArea.class.getDeclaredField("codeArea");
                        field.setAccessible(true);
                        org.fxmisc.richtext.CodeArea inner = (org.fxmisc.richtext.CodeArea) field.get(area);
                        inner.replaceText("class C {\n  int myVar = 0;\n  main {\n    int my\n  }\n}\n");
                        // move caret to after 'my' in the declaration
                        inner.moveTo(inner.getText().indexOf("int my") + "int my".length());

                        @SuppressWarnings("unchecked")
                        List<String> declSuggestions = (List<String>) m.invoke(area, "my");
                        assertTrue(!declSuggestions.contains("myVar"), "While declaring a variable (after 'int'), existing variable 'myVar' should not be suggested");

                        runOnFxThread(() -> {
                            try {
                                var field2 = MyCodeArea.class.getDeclaredField("codeArea");
                                field2.setAccessible(true);
                                org.fxmisc.richtext.CodeArea inner2 = (org.fxmisc.richtext.CodeArea) field2.get(area);
                                inner2.replaceText("class C {\n  int myVar = 0;\n  void foo() { }\n  void ba\n  main { myVar = 1; foo(); }\n}\n");
                                inner2.moveTo(inner2.getText().indexOf("void ba") + "void ba".length());

                                @SuppressWarnings("unchecked")
                                List<String> methodDeclSuggestions = (List<String>) m.invoke(area, "ba");
                                assertTrue(methodDeclSuggestions.isEmpty(), "While declaring a method (after 'void'), there should be no suggestions");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

package fr.ufrst.m1info.gl.groupe7.IHM;

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
        Assertions.assertEquals(codeArea.getText(), "");

    }

    @Disabled("Problème avec testfx qui marche pas en local, mais marche parfois avec maven")
    @Test
    void testWritingInCodeArea(FxRobot robot) {
        Assertions.assertEquals("", codeArea.getText());
        robot.clickOn("#testcodearea_code_area");
        robot.write("class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}");
        Assertions.assertEquals("class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}", codeArea.getText());
    }

    @Test
    void testCreatingCodeAreaWithDefaultValue(FxRobot robot) {
        Assertions.assertEquals("class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}", codeAreaWithDefault.getText());
    }
}

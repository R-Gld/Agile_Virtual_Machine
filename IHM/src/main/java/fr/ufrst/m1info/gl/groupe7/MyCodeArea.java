package fr.ufrst.m1info.gl.groupe7;

import javafx.scene.layout.AnchorPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class MyCodeArea extends AnchorPane {
    private final CodeArea codeArea;
    public MyCodeArea() {
        String codeSample = "class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}";
        codeArea = new CodeArea(codeSample);
        codeArea.setWrapText(true);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        AnchorPane.setTopAnchor(codeArea, 0d);
        AnchorPane.setBottomAnchor(codeArea,0d);
        AnchorPane.setLeftAnchor(codeArea,0d);
        AnchorPane.setRightAnchor(codeArea,0d);
        this.getChildren().add(codeArea);
    }
}

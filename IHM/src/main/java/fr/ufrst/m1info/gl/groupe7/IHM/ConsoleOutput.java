package fr.ufrst.m1info.gl.groupe7.IHM;

import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class ConsoleOutput extends AnchorPane {
    private TextArea console;

    public ConsoleOutput(String id) {
        this.setId(id);
        console = new TextArea();
        console.setWrapText(true);

        AnchorPane.setTopAnchor(console, 10.0);
        AnchorPane.setLeftAnchor(console, 10.0);
        AnchorPane.setBottomAnchor(console, 10.0);
        AnchorPane.setRightAnchor(console, 10.0);

        console.setEditable(false);

        this.getChildren().add(console);
    }
}

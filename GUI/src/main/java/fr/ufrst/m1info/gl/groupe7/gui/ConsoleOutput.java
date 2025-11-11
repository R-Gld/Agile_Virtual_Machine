package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class ConsoleOutput extends AnchorPane {
    private final TextArea console;

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

    // ==== START ADDED ====
    /**
     * Ajoute un message dans la console.
     * Chaque message est affiché sur une nouvelle ligne.
     */
    public void printMessage(String msg) {
        if (msg == null) return;
        console.appendText(msg + "\n");
    }

    /**
     * Efface le contenu de la console.
     */
    public void clear() {
        console.clear();
    }
    // ==== END ADDED ====
}

package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

/**
 * ConsoleOutput component displaying application logs.
 * Uses external CSS (light/dark) for colors.
 */

public class ConsoleOutput extends StackPane {
    private final TextArea console;
    private final Button clearButton;

    /**
     * Creates a console with an internal Clear button.
     *
     * @param id JavaFX node id used for CSS.
     */
    public ConsoleOutput(String id) {
        // Root node id (e.g. "console")
        this.setId(id);

        console = new TextArea();
        console.setWrapText(true);
        console.setEditable(false);
        console.setFont(Font.font("Consolas", 12));
        console.setFocusTraversable(false);

        // Clear button inside the console
        clearButton = new Button("Clear");
        clearButton.getStyleClass().add("console-clear-button");
        clearButton.setOnAction(e -> clear());
        clearButton.setFocusTraversable(false);

        StackPane.setAlignment(clearButton, Pos.TOP_RIGHT);
        StackPane.setMargin(clearButton, new Insets(5, 20, 0, 0));

        // StackPane children
        this.getChildren().addAll(console, clearButton);
    }

    /**
     * Ajoute un message dans la console.
     * Chaque message est affiché sur une nouvelle ligne.
     *
     * @param msg le texte à afficher dans la console
     */
    public void printMessage(String msg) {
        if (msg == null) {
            return;
        }
        console.appendText(msg + "\n");
        console.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * Efface le contenu de la console.
     */
    public void clear() {
        console.clear();
    }
}

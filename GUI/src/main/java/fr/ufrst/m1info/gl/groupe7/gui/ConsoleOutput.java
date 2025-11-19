package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

/**
 * ConsoleOutput
 * Composant graphique affichant la sortie texte de l'application (logs, messages, erreurs)
 * avec une apparence type terminal (fond noir / texte vert) et un bouton Clear intégré.
 */
public class ConsoleOutput extends StackPane {
    private final TextArea console;
    private final Button clearButton;

    /**
     * Crée une console stylisée avec fond noir et texte vert,
     * et un bouton 🧹 Clear positionné en haut à droite à l'intérieur.
     *
     * @param id identifiant du composant JavaFX
     */
    public ConsoleOutput(String id) {
        this.setId(id);

        // Configuration du TextArea (console)
        console = new TextArea();
        console.setWrapText(true);
        console.setEditable(false);
        console.setFont(Font.font("Consolas", 14));
        console.setStyle("-fx-control-inner-background: black; -fx-text-fill: #F8F8F2;");
        console.setFocusTraversable(false);

        //   Bouton Clear
        clearButton = new Button("clear");
        clearButton.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10;" +
                        "-fx-background-radius: 6;"
        );
        clearButton.setOnAction(e -> clear());
        clearButton.setFocusTraversable(false);
        StackPane.setAlignment(clearButton, Pos.TOP_RIGHT);
        StackPane.setMargin(clearButton, new Insets(5, 5, 0, 0));

        // StackPane
        this.getChildren().addAll(console, clearButton);

    }


    /**
     * Ajoute un message dans la console.
     * Chaque message est affiché sur une nouvelle ligne.
     *
     * @param msg le texte à afficher dans la console
     */
    public void printMessage(String msg) {
        if (msg == null) return;
        console.appendText(msg + "\n");
        console.setScrollTop(Double.MAX_VALUE); // auto-scroll vers le bas
    }

    /**
     * Efface le contenu de la console.
     */
    public void clear() {
        console.clear();
    }
    // ==== END  ====
}

package fr.ufrst.m1info.gl.groupe7.gui;

import ch.qos.logback.classic.Level;
import fr.ufrst.m1info.gl.groupe7.memoire.logging.LoggerConfigManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * LogControlPanel
 * Composant graphique permettant de contrôler les niveaux de log en temps réel.
 *
 * Fonctionnalités :
 * - Sélection du niveau global (TRACE, DEBUG, INFO, WARN, ERROR, OFF)
 * - Changements appliqués immédiatement sans redémarrage
 */
public class LogControlPanel extends HBox {

    private final ComboBox<String> levelSelector;

    /**
     * Crée un panneau de contrôle des logs avec sélecteur de niveau.
     */
    public LogControlPanel() {
        this.setSpacing(10);
        this.setPadding(new Insets(5));
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle("-fx-background-color: rgba(50, 50, 50, 0.3); -fx-background-radius: 5;");

        // Label
        Label label = new Label("Niveau de log:");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 12;");

        // ComboBox pour sélection du niveau
        levelSelector = new ComboBox<>();
        levelSelector.getItems().addAll("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF");
        levelSelector.setValue("INFO"); // Valeur par défaut
        levelSelector.setStyle("-fx-font-size: 11;");
        levelSelector.setPrefWidth(100);

        // Action : appliquer le niveau sélectionné
        levelSelector.setOnAction(e -> {
            String selectedLevel = levelSelector.getValue();
            if (selectedLevel != null) {
                Level level = Level.valueOf(selectedLevel);
                LoggerConfigManager.setRootLevel(level);
            }
        });

        // Assemblage du panneau
        this.getChildren().addAll(label, levelSelector);
    }
}

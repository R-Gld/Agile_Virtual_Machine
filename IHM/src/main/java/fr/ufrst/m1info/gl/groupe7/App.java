package fr.ufrst.m1info.gl.groupe7;

import javafx.application.Application;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


/**
 * JavaFX App
 */
public class App extends Application {

    private Stage appStage;
    private MyCodeArea mjjCodeArea;

    @Override
    public void start(Stage stage) {
        appStage = stage;
        // Structure de l'interface :
        BorderPane root = new BorderPane();
        root.setTop(buildMenu());
        var scene = new Scene(root, 640, 480);

        String codeSample = "class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}";
        mjjCodeArea = new MyCodeArea("mjj-code", codeSample);

        root.setCenter(mjjCodeArea);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Fonction pour construire le menu de l'ihm
     * @return MenuBar le menu de l'application
     */
    private MenuBar buildMenu() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);

        MenuItem saveItem = new MenuItem("Save");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> {
            loadFile();
        });

        saveItem.setOnAction(e -> {
            saveFile();
        });

        fileMenu.getItems().addAll(saveItem, openItem);

        return menuBar;
    }

    /**
     * Fonction pour gerer l'ouverture d'un fichier
     */
    private void loadFile() {

        /* Selection du ficher à ouvrir */
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MiniJaja", "*.mjj"),
                new FileChooser.ExtensionFilter("JajaCode", "*.jjc")
        );
        File file = fileChooser.showOpenDialog(appStage);
        if(file == null) return;

        /* Lecture du fichier selectionner depuis l'explorateur de fichier */
        StringBuilder fileContent = new StringBuilder();
        try(Scanner scanner = new Scanner(file)) {
            while(scanner.hasNextLine()) {
                fileContent.append(scanner.nextLine());
                fileContent.append("\n");
            }
        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur lors de l'ouverture du fichier : ");
            alert.setContentText(e.toString());
            alert.showAndWait();
            return;
        } catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Impossible de lire le fichier");
            alert.setContentText(e.toString());
            alert.showAndWait();
            return;
        }

        /* Chargement du texte dans l'interface */
        mjjCodeArea.loadText(fileContent.toString());
    }

    /**
     * Sauvegarde du contenu de la code area dans un fichier
     */
    private void saveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MiniJaja", "*.mjj"),
                new FileChooser.ExtensionFilter("JajaCode", "*.jjc")
        );
        File file = fileChooser.showSaveDialog(appStage);
        if(file == null) return;

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(mjjCodeArea.getText());
            fileWriter.close();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur lors de la sauvegarde du fichier");
            alert.setContentText(e.toString());
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Impossible de sauvegarder le fichier");
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
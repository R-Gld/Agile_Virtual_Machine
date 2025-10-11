package fr.ufrst.m1info.gl.groupe7;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Structure de l'interface :
        BorderPane root = new BorderPane();
        root.setTop(buildMenu());
        var scene = new Scene(root, 640, 480);

        MyCodeArea codeArea = new MyCodeArea();

        root.setCenter(codeArea);
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar buildMenu() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);

        return menuBar;
    }

    public static void main(String[] args) {
        launch();
    }

}
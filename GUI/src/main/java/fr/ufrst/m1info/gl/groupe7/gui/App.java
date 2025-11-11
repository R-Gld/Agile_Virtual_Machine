package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreter;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter;
import fr.ufrst.m1info.gl.groupe7.compiler.Compiler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Scanner;


/**
 * JavaFX App
 */
public class App extends Application {

    private Stage appStage;
    private MyCodeArea mjjCodeArea;
    private MyCodeArea jjcCodeArea;
    private ChoiceBox<String> fileToRun;

    // ==== START ADDED ====
    /**
     * Console de sortie utilisée pour afficher des messages (debug, info, erreurs)
     */
    private ConsoleOutput console;

    /**
     * État simple de debug : mode actif / ligne courante
     */
    private boolean debugMode = false;
    private int debugCurrentLine = -1;
    // ==== END ADDED ====

    @Override
    public void start(Stage stage) {
        appStage = stage;
        // Structure de l'interface :
        BorderPane root = new BorderPane();
        root.setTop(buildMenu());
        var scene = new Scene(root, 800, 600);

        String codeSample = "class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}";
        mjjCodeArea = new MyCodeArea("mjj-code", codeSample);
        jjcCodeArea = new MyCodeArea("jjc-code");
        jjcCodeArea.disable();

        SplitPane splitPane = new SplitPane(mjjCodeArea, jjcCodeArea);

        root.setCenter(splitPane);

        ConsoleOutput console = new ConsoleOutput("console");
        root.setBottom(console);

        // ==== START ADDED ====
        // On garde aussi une référence dans un attribut pour pouvoir écrire dedans
        this.console = console;
        // ==== END ADDED ====

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Fonction pour construire le menu de l'ihm
     * @return MenuBar le menu de l'application
     */
    private HBox buildMenu() {
        HBox hbox = new HBox();
        hbox.setSpacing(10);
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

        hbox.getChildren().add(menuBar);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.getChildren().add(spacer);

        /* Build button */
        Button buildButton = new Button("");
        buildButton.setGraphic(new ImageView(getClass().getResource("/icons/build.png").toExternalForm()));
        buildButton.setOnAction(e -> {
            compile();
        });
        buildButton.setTooltip(new Tooltip("Compile file"));

        hbox.getChildren().add(buildButton);

        /* Select file to be interpreted */
        fileToRun = new ChoiceBox<>();
        fileToRun.getItems().addAll("MiniJaja", "Jajacode");
        fileToRun.setValue("MiniJaja");

        hbox.getChildren().add(fileToRun);

        /* Execute button */
        Button runButton = new Button("");
        runButton.setGraphic(new ImageView(getClass().getResource("/icons/threadRunning.png").toExternalForm()));
        runButton.setTooltip(new Tooltip("Run"));
        runButton.setOnAction(e -> {
            run();
        });

        hbox.getChildren().add(runButton);

        // ==== START ADDED ====
        /**
         * Boutons pour un debug très simple : Debug / Step / Stop
         * (pas de modification de la logique existante, seulement ajout)
         */

        Button debugButton = new Button("Debug");
        debugButton.setTooltip(new Tooltip("Start simple debug (step through MiniJaja lines)"));
        debugButton.setOnAction(e -> {
            startDebug();
        });
        hbox.getChildren().add(debugButton);

        Button stepButton = new Button("Step");
        stepButton.setTooltip(new Tooltip("Step to next MiniJaja line"));
        stepButton.setOnAction(e -> {
            stepDebug();
        });
        hbox.getChildren().add(stepButton);

        Button stopButton = new Button("Stop");
        stopButton.setTooltip(new Tooltip("Stop debug mode"));
        stopButton.setOnAction(e -> {
            stopDebug();
        });
        hbox.getChildren().add(stopButton);
        // ==== END ADDED ====

        return hbox;
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

        // ==== START ADDED ====
        if (console != null) {
            console.printMessage("File loaded: " + file.getName());
        }
        // ==== END ADDED ====
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

        // ==== START ADDED ====
        if (console != null && file != null) {
            console.printMessage("File saved: " + file.getAbsolutePath());
        }
        // ==== END ADDED ====
    }

    /**
     * Fonction utiliser pour appeler les methodes necessaires à la compilation du minijaja
     * Ecris le resultat de la compilation dans la zone prévu pour le jajacode
     */
    private void compile() {
        String code = mjjCodeArea.getText();

        Compiler compiler = new Compiler(code, Compiler.Destination.STRING, null);

        String compileResult = compiler.compileToString();
        jjcCodeArea.loadText(compileResult);

        // ==== START ADDED ====
        if (console != null) {
            console.printMessage("Compilation finished.");
        }
        // ==== END ADDED ====
    }

    /**
     * Fonction utiliser pour interpreter le minijaja ou le jajacode présent
     */
    private void run() {
        if (fileToRun.getValue().equals("MiniJaja")) {
            String mjj = mjjCodeArea.getText();
            // Call minijaja interpretor
            MiniJajaInterpreter interpreter = new MiniJajaInterpreter();
            interpreter.run(mjj);

            // ==== START ADDED ====
            if (console != null) {
                console.printMessage("MiniJaja executed.");
            }
            // ==== END ADDED ====
        } else {
            String jjc = jjcCodeArea.getText();
            // call jajacode interpretor
            JajaCodeInterpreter jjcInterpretor = new JajaCodeInterpreter();
            String[] lines = jjc.split("\\n");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                result.append(i + 1)
                        .append(" ")
                        .append(lines[i])
                        .append("\n");
            }
            jjcInterpretor.run(result.toString());

            // ==== START ADDED ====
            if (console != null) {
                console.printMessage("JajaCode executed.");
            }
            // ==== END ADDED ====

        }

    }

    // ==== START ADDED ====
    /**
     * Lance le mode debug simple : on se contente de "pointer" ligne par ligne
     * dans la zone MiniJaja et d'afficher la ligne courante dans la console.
     * (Sans modifier l'interpréteur existant).
     */
    private void startDebug() {
        if (debugMode) {
            return;
        }
        debugMode = true;
        debugCurrentLine = -1;
        if (console != null) {
            console.printMessage("[DEBUG] Debug mode started.");
        }
        stepDebug();
    }

    /**
     * Passe à la ligne suivante dans le code MiniJaja et "highlight" logique
     * (déplacement du caret) sur cette ligne.
     */
    private void stepDebug() {
        if (!debugMode) {
            return;
        }
        String text = mjjCodeArea.getText();
        String[] lines = text.split("\\R", -1);
        if (lines.length == 0) {
            if (console != null) {
                console.printMessage("[DEBUG] No lines to debug.");
            }
            stopDebug();
            return;
        }

        debugCurrentLine++;
        if (debugCurrentLine >= lines.length) {
            if (console != null) {
                console.printMessage("[DEBUG] End of file reached.");
            }
            stopDebug();
            return;
        }

        if (console != null) {
            console.printMessage("[DEBUG] Line " + (debugCurrentLine + 1) + ": " + lines[debugCurrentLine]);
        }

        mjjCodeArea.highlightLine(debugCurrentLine);
    }

    /**
     * Arrête le mode debug simple.
     */
    private void stopDebug() {
        if (!debugMode) {
            return;
        }
        debugMode = false;
        debugCurrentLine = -1;
        if (console != null) {
            console.printMessage("[DEBUG] Debug mode stopped.");
        }
    }
    // ==== END ADDED ====

    public static void main(String[] args) {
        launch();
    }

}

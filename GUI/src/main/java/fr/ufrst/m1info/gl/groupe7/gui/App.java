package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreter;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.compiler.Compiler;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * JavaFX App
 */
public class App extends Application {

    private Stage appStage;
    private MyCodeArea mjjCodeArea;
    private MyCodeArea jjcCodeArea;
    private ChoiceBox<String> fileToRun;

    // === Added section: console output and debug state tracking ===
    /**
     * Console used to display messages (debug, info, errors)
     */
    private ConsoleOutput console;

    /**
     * Simple debug state (active flag and current line)
     */
    private boolean debugMode = false;
    private int debugCurrentLine = -1;
    // === End of added section ===


    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("app-worker");
        return t;
    });

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

        // === Console initialization (clean version) ===
        this.console = new ConsoleOutput("console");
        root.setBottom(this.console);
        // === End ===

        stage.setScene(scene);
        stage.show();
    }


    /**
     * Stop the application.
     * @throws Exception if an error occurs
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        executor.shutdownNow();
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

        // === Added section: debug controls (icons only, no text) ===
        /**
         * Simple debug control bar with icons only: Start Debug / Step / Stop
         */

        Button debugButton = new Button();
        Button stepButton = new Button();
        Button stopButton = new Button();

        debugButton.setTooltip(new Tooltip("Start simple debug (step through MiniJaja lines)"));
        ImageView debugIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/bug.png"), 20, 20, true, true));
        debugButton.setGraphic(debugIcon);
        hbox.getChildren().add(debugButton);
        debugButton.setOnAction(e -> startDebug(stepButton, stopButton));

        stepButton.setTooltip(new Tooltip("Step to next MiniJaja line"));
        stepButton.setDisable(true);
        ImageView stepIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/next.png"), 20, 20, true, true));
        stepButton.setGraphic(stepIcon);
        hbox.getChildren().add(stepButton);
        stepButton.setOnAction(e -> stepDebug(stepButton, stopButton));

        stopButton.setTooltip(new Tooltip("Stop debug mode"));
        stopButton.setDisable(true);
        ImageView stopIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/stop.png"), 20, 20, true, true));
        stopButton.setGraphic(stopIcon);
        hbox.getChildren().add(stopButton);
        stopButton.setOnAction(e -> stopDebug(stepButton, stopButton));
        // === End of added section ===

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

        // === Added section: log message after file loading ===
        if (console != null) {
            console.printMessage("File loaded: " + file.getName());
        }
        // === End of added section ===
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

        // === Added section: log message after saving ===
        if (console != null) {
            console.printMessage("File saved: " + file.getAbsolutePath());
        }
        // === End of added section ===
    }

    /**
     * Fonction utiliser pour appeler les methodes necessaires à la compilation du minijaja
     * Ecris le resultat de la compilation dans la zone prévu pour le jajacode
     */
    private void compile() {
        // On lit le texte sur le thread FX
        String code = mjjCodeArea.getText();

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                Compiler compiler = new Compiler(code, Compiler.Destination.STRING, null);
                return compiler.compileToString();
            }
        };

        task.setOnSucceeded(ev -> {
            String compileResult = task.getValue();
            jjcCodeArea.loadText(compileResult);

            if (console != null) {
                console.printMessage("Compilation finished.");
            }
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            if (console != null) {
                console.printMessage("Compilation failed: " + ex.getMessage());
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de compilation");
            alert.setHeaderText("Une erreur est survenue pendant la compilation");
            alert.setContentText(ex.toString());
            alert.showAndWait();
        });

        executor.submit(task);
    }

    /**
     * Fonction utiliser pour interpreter le minijaja ou le jajacode présent
     */
    private void run() {
        // On lit ce qu’il faut sur le thread FX
        String choice = fileToRun.getValue();
        String mjjText = mjjCodeArea.getText();
        String jjcText = jjcCodeArea.getText();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    if ("MiniJaja".equals(choice)) {
                        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(mjjText, new DiagnosticCollector());
                        interpreter.run();
                    } else {
                        String[] lines = jjcText.split("\\n");
                        StringBuilder result = new StringBuilder();
                        for (int i = 0; i < lines.length; i++) {
                            result.append(i + 1)
                                    .append(" ")
                                    .append(lines[i])
                                    .append("\n");
                        }
                        JajaCodeInterpreter jjcInterpreter = new JajaCodeInterpreter(result.toString(), new DiagnosticCollector());
                        jjcInterpreter.run();
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
                return null;
            }
        };

        task.setOnSucceeded(ev -> {
            if (console != null) {
                if ("MiniJaja".equals(choice)) {
                    console.printMessage("MiniJaja executed.");
                } else {
                    console.printMessage("JajaCode executed.");
                }
            }
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            if (console != null) {
                console.printMessage("Execution failed: " + ex.getMessage());
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'exécution");
            alert.setHeaderText("Une erreur est survenue pendant l'exécution");
            alert.setContentText(ex.toString());
            alert.showAndWait();
        });

        executor.submit(task);
    }

    // === Added section: Simple Debug methods (Start / Step / Stop) ===
    /**
     * Starts simple debug mode and prints current line information in console.
     */
    private void startDebug(Button stepButton, Button stopButton) {
        if (debugMode) {
            return;
        }
        debugMode = true;
        stepButton.setDisable(false);
        stopButton.setDisable(false);
        debugCurrentLine = -1;
        if (console != null) {
            console.printMessage("[DEBUG] Debug mode started.");
        }
        stepDebug(stepButton, stopButton);
    }

    /**
     * Moves to the next line in MiniJaja code and highlights it.
     */
    private void stepDebug(Button stepButton, Button stopButton) {
        if (!debugMode) {
            return;
        }
        String text = mjjCodeArea.getText();
        String[] lines = text.split("\\R", -1);
        if (lines.length == 0) {
            if (console != null) {
                console.printMessage("[DEBUG] No lines to debug.");
            }
            stopDebug(stepButton, stopButton);
            return;
        }

        debugCurrentLine++;
        if (debugCurrentLine >= lines.length) {
            if (console != null) {
                console.printMessage("[DEBUG] End of file reached.");
            }
            stopDebug(stepButton, stopButton);
            return;
        }

        if (console != null) {
            console.printMessage("[DEBUG] Line " + (debugCurrentLine + 1) + ": " + lines[debugCurrentLine]);
        }

        mjjCodeArea.highlightLine(debugCurrentLine);
    }

    /**
     * Stops simple debug mode.
     */
    private void stopDebug(Button stepButton, Button stopButton) {
        if (!debugMode) {
            return;
        }
        debugMode = false;
        debugCurrentLine = -1;
        if (console != null) {
            console.printMessage("[DEBUG] Debug mode stopped.");
        }
        stepButton.setDisable(true);
        stopButton.setDisable(true);
    }
    // === End of added section ===

    public static void main(String[] args) {
        launch();
    }

}

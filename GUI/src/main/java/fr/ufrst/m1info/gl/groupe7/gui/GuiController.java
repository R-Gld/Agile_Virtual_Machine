package fr.ufrst.m1info.gl.groupe7.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.ufrst.m1info.gl.groupe7.compiler.Compiler;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeDebug;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreter;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for handling GUI logic in the MiniJaja IDE.
 */
public class GuiController {

    private static final Logger logger = LoggerFactory.getLogger(GuiController.class);

    private final Stage appStage;
    private final MyCodeArea mjjCodeArea;
    private final MyCodeArea jjcCodeArea;
    private final ChoiceBox<String> fileToRun;
    private final ConsoleOutput console;
    private final TableView<JajaCodeDebug.VariableInfo> stackTable;
    private final TableView<JajaCodeDebug.HeapInfo> heapTable;
    private final Button stepButton;
    private final Button stopButton;
    private final Button continueButton;

    /**
     * Simple debug state (active flag and current line)
     */
    private boolean debugMode = false;
    private int debugCurrentLine = -1;

    /**
     * Breakpoints snapshot used during debug session
     */
    private final Set<Integer> debugBreakpoints = new HashSet<>();

    /**
     * From which editor are we debugging?
     * MINIJAJA → mjjCodeArea
     * JAJACODE → jjcCodeArea
     */
    private enum DebugSource {
        MINIJAJA, JAJACODE
    }

    private DebugSource debugSource = DebugSource.MINIJAJA;

    private MiniJajaDebugHandler mjjDebugHandler;
    private JajaCodeDebugHandler debugHandler;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("app-worker");
        return t;
    });

    public GuiController(Stage appStage, MyCodeArea mjjCodeArea, MyCodeArea jjcCodeArea,
                         ChoiceBox<String> fileToRun, ConsoleOutput console,
                         TableView<JajaCodeDebug.VariableInfo> stackTable,
                         TableView<JajaCodeDebug.HeapInfo> heapTable,
                         Button stepButton, Button stopButton, Button continueButton) {
        this.appStage = appStage;
        this.mjjCodeArea = mjjCodeArea;
        this.jjcCodeArea = jjcCodeArea;
        this.fileToRun = fileToRun;
        this.console = console;
        this.stackTable = stackTable;
        this.heapTable = heapTable;
        this.stepButton = stepButton;
        this.stopButton = stopButton;
        this.continueButton = continueButton;
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public void loadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter(App.MINI_JAJA_NAME, "*.mjj"),
            new FileChooser.ExtensionFilter(App.JAJA_CODE_NAME, "*.jjc")
        );
        File file = fileChooser.showOpenDialog(appStage);
        loadFileContent(file);
    }

    public void loadFileContent(File file) {
        if (file == null) {
            return;
        }

        /* Lecture du fichier selectionner */
        StringBuilder fileContent = new StringBuilder();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContent.append(scanner.nextLine());
                fileContent.append("\n");
            }
        } catch (FileNotFoundException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur lors de l'ouverture du fichier : ");
                alert.setContentText(e.toString());
                alert.showAndWait();
            });
            return;
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Impossible de lire le fichier");
                alert.setContentText(e.toString());
                alert.showAndWait();
            });
            return;
        }

        /* Chargement du texte dans l'interface */
        String name = file.getName().toLowerCase();
        String content = fileContent.toString();

        if (name.endsWith(".mjj")) {
            mjjCodeArea.loadText(content);
            fileToRun.setValue(App.MINI_JAJA_NAME);
        } else if (name.endsWith(".jjc")) {
            jjcCodeArea.loadText(content);
            fileToRun.setValue("Jajacode");
        }

        if (console != null) {
            console.printMessage("File loaded: " + file.getName());
        }
    }

    public void saveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter(App.MINI_JAJA_NAME, "*.mjj"),
            new FileChooser.ExtensionFilter(App.JAJA_CODE_NAME, "*.jjc")
        );
        File file = fileChooser.showSaveDialog(appStage);
        if (file == null) return;

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(mjjCodeArea.getText());
            fileWriter.close();
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur lors de la sauvegarde du fichier");
                alert.setContentText(e.toString());
                alert.showAndWait();
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Impossible de sauvegarder le fichier");
                alert.setContentText(e.toString());
                alert.showAndWait();
            });
        }

        if (console != null) {
            console.printMessage("File saved: " + file.getAbsolutePath());
        }
    }

    public void compile() {
        // On lit le texte sur le thread FX
        String code = mjjCodeArea.getText();

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                Compiler compiler = new Compiler(code, Compiler.Destination.STRING, null);
                String compilerOutput = compiler.compileToString();
                logger.debug("Starting compilation task...");
                logger.debug("MiniJaja code length: {} characters", code.length());
                logger.debug("Instruction jajacode générée :");
                logger.debug("{}", compilerOutput);
                return compilerOutput;
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
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur de compilation");
                alert.setHeaderText("Une erreur est survenue pendant la compilation");
                alert.setContentText(ex.toString());
                alert.showAndWait();
            });
        });

        executor.submit(task);
    }

    public void run() {
        // On lit ce qu’il faut sur le thread FX
        String choice = fileToRun.getValue();
        String mjjText = mjjCodeArea.getText();
        String jjcText = jjcCodeArea.getText();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    switch (choice) {
                        case App.MINI_JAJA_NAME: {
                            MiniJajaInterpreter interpreter = new MiniJajaInterpreter(mjjText, new DiagnosticCollector());
                            interpreter.run();
                            break;
                        }
                        case App.JAJA_CODE_NAME: {
                            String[] lines = jjcText.split("\\n");
                            StringBuilder result = new StringBuilder();
                            for (int i = 0; i < lines.length; i++) {
                                result.append(i + 1).append(" ").append(lines[i]).append("\n");
                            }
                            JajaCodeInterpreter jjcInterpreter = new JajaCodeInterpreter(result.toString(), new DiagnosticCollector());
                            jjcInterpreter.run();
                            break;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };

        task.setOnSucceeded(ev -> {
            if (console != null) {
                if (App.MINI_JAJA_NAME.equals(choice)) {
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
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur d'exécution");
                alert.setHeaderText("Une erreur est survenue pendant l'exécution");
                alert.setContentText(ex.toString());
                alert.showAndWait();
            });
        });

        executor.submit(task);
    }

    public void startDebug() {
        if (debugMode) {
            return;
        }

        String choice = fileToRun.getValue();

        if (App.MINI_JAJA_NAME.equals(choice)) {
            // Debug MiniJaja directly
            startMiniJajaDebug();
        } else {
            // Debug JajaCode
            startJajaCodeDebug();
        }
    }

    private void startMiniJajaDebug() {
        String code = mjjCodeArea.getText();
        Set<Integer> breakpoints = mjjCodeArea.getBreakpoints();

        if (breakpoints.isEmpty()) {
            console.printMessage("[DEBUG MJJ] Warning: No breakpoints set. Add breakpoints by clicking on line numbers.");
        }

        mjjDebugHandler = new MiniJajaDebugHandler(console, mjjCodeArea, stackTable, heapTable);
        mjjDebugHandler.start(code, breakpoints);

        if (!mjjDebugHandler.isRunning()) {
            return;
        }

        debugMode = true;
        debugSource = DebugSource.MINIJAJA;
        stepButton.setDisable(false);
        stopButton.setDisable(false);
        continueButton.setDisable(false);
    }

    private void startJajaCodeDebug() {
        String codeToDebug = jjcCodeArea.getText();

        // Prepare JajaCode with line numbers (interpreter expects "1 init" format)
        String[] lines = codeToDebug.split("\\n");
        StringBuilder numberedCode = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            numberedCode.append(i + 1).append(" ").append(lines[i]).append("\n");
        }

        // Initialize handler with tables
        debugHandler = new JajaCodeDebugHandler(console, jjcCodeArea, stackTable, heapTable);
        debugHandler.start(numberedCode.toString());

        if (!debugHandler.isRunning()) {
            return; // Failed to start
        }

        debugMode = true;
        debugSource = DebugSource.JAJACODE;
        stepButton.setDisable(false);
        stopButton.setDisable(false);
        continueButton.setDisable(false);
    }

    public void stepDebug() {
        if (!debugMode) {
            return;
        }

        if (debugSource == DebugSource.MINIJAJA) {
            if (mjjDebugHandler != null) {
                mjjDebugHandler.step();
                if (!mjjDebugHandler.isRunning()) {
                    stopDebugWithReset();
                }
            }
        } else {
            if (debugHandler != null) {
                debugHandler.step();
                if (!debugHandler.isRunning()) {
                    stopDebugWithReset();
                }
            }
        }
    }

    public void continueDebug() {
        if (!debugMode) return;

        if (debugSource == DebugSource.MINIJAJA) {
            if (mjjDebugHandler != null) {
                Set<Integer> breakpoints = mjjCodeArea.getBreakpoints();
                mjjDebugHandler.continueDebug(breakpoints);
                if (!mjjDebugHandler.isRunning()) {
                    stopDebugWithReset();
                }
            }
        } else {
            if (debugHandler != null) {
                debugBreakpoints.clear();
                debugBreakpoints.addAll(jjcCodeArea.getBreakpoints());
                debugHandler.continueDebug(debugBreakpoints);
                if (!debugHandler.isRunning()) {
                    stopDebugWithReset();
                }
            }
        }
    }

    public void stopDebug() {
        if (!debugMode) {
            return;
        }

        if (debugSource == DebugSource.MINIJAJA) {
            if (mjjDebugHandler != null) {
                mjjDebugHandler.stop();
                mjjDebugHandler = null;
            }
        } else {
            if (debugHandler != null) {
                debugHandler.stop();
                debugHandler = null;
            }
        }

        debugMode = false;
        debugCurrentLine = -1;

        if (console != null) {
            console.printMessage("[DEBUG] Debug mode stopped.");
        }
        stepButton.setDisable(true);
        stopButton.setDisable(true);
        continueButton.setDisable(true);
    }

    public void stopDebugWithReset() {
        DebugSource currentSource = debugSource;
        stopDebug();
        if (currentSource == DebugSource.MINIJAJA) {
            mjjCodeArea.highlightLine(-1);
        } else {
            jjcCodeArea.highlightLine(-1);
        }
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}
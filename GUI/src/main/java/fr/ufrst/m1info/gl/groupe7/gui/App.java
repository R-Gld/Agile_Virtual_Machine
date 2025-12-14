package fr.ufrst.m1info.gl.groupe7.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.ufrst.m1info.gl.groupe7.compiler.Compiler;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreter;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaDebugger;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.memoire.logging.GuiAppender;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Breakpoint support
import java.util.HashSet;
import java.util.Set;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private Stage appStage;
    private MyCodeArea mjjCodeArea;
    private MyCodeArea jjcCodeArea;
    private ChoiceBox<String> fileToRun;
    private ToggleButton darkMode;

    // Debug Buttons
    private Button debugButton;
    private Button stepButton;
    private Button stopButton;
    private Button continueButton;
    // Debug minijaja
    DebugPauseHandler mjjPauseHandler;
    Debug mjjDebugWalker;
    MiniJajaDebugger mjjDebugger;

    /**
     * Console used to display messages (debug, info, errors)
     */
    private ConsoleOutput console;

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
        MINIJAJA,
        JAJACODE
    }

    private DebugSource debugSource = DebugSource.MINIJAJA;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("app-worker");
        return t;
    });

    @Override
    public void start(Stage stage) {
        appStage = stage;
        mjjDebugWalker = new Debug();
        mjjPauseHandler = new DebugPauseHandler(mjjDebugWalker);

        // Structure de l'interface :
        BorderPane root = new BorderPane();
        HBox titleBar = buildTitleBar();
        HBox topMenu = buildMenu();
        HBox toolbar = buildToolbar();
        VBox topContainer = new VBox(titleBar, topMenu, toolbar);
        root.setTop(topContainer);

        // Editors
        String codeSample = "class C {\n\tint x = 0;\n\n\tmain {\n\t\tx = 12;\n\t}\n}";
        mjjCodeArea = new MyCodeArea("mjj-code", codeSample, MyCodeArea.Language.MINIJAJA);
        jjcCodeArea = new MyCodeArea("jjc-code", MyCodeArea.Language.JAJACODE);
        jjcCodeArea.disable();

        // Wrapper for MiniJaja area with title bar
        VBox miniWrapper = new VBox();
        miniWrapper.setSpacing(0);

        HBox miniTitleBar = new HBox();
        miniTitleBar.getStyleClass().add("panel-titlebar");
        // Fixed height for MiniJaja bar
        miniTitleBar.setMinHeight(26);
        miniTitleBar.setPrefHeight(26);
        miniTitleBar.setMaxHeight(26);

        Label mjjLabel = new Label("MiniJaja");
        mjjLabel.getStyleClass().add("panel-title-label");

        Region mjjSpacer = new Region();
        HBox.setHgrow(mjjSpacer, Priority.ALWAYS);

        miniTitleBar.getChildren().addAll(mjjLabel, mjjSpacer);
        miniWrapper.getChildren().addAll(miniTitleBar, mjjCodeArea);
        VBox.setVgrow(mjjCodeArea, Priority.ALWAYS);

        // Wrapper for JajaCode area with title bar and Clear button
        VBox jajaWrapper = new VBox();
        jajaWrapper.setSpacing(0);

        HBox jajaTitleBar = new HBox();
        jajaTitleBar.getStyleClass().add("panel-titlebar");
        jajaTitleBar.setMinHeight(26);
        jajaTitleBar.setPrefHeight(26);
        jajaTitleBar.setMaxHeight(26);

        Label jjcLabel = new Label("JajaCode");
        jjcLabel.getStyleClass().add("panel-title-label");

        Region jajaSpacer = new Region();
        HBox.setHgrow(jajaSpacer, Priority.ALWAYS);

        Button clearJajaButton = new Button("Clear");
        clearJajaButton.getStyleClass().add("panel-title-button");
        clearJajaButton.setTooltip(new Tooltip("Clear JajaCode output"));
        clearJajaButton.setOnAction(e -> jjcCodeArea.loadText(""));

        jajaTitleBar.getChildren().addAll(jjcLabel, jajaSpacer, clearJajaButton);
        jajaWrapper.getChildren().addAll(jajaTitleBar, jjcCodeArea);
        VBox.setVgrow(jjcCodeArea, Priority.ALWAYS);

        SplitPane editorSplitPane = new SplitPane(miniWrapper, jajaWrapper);

        /*
         * Bottom view
         */
        HBox hBox = new HBox();
        // Console
        this.console = new ConsoleOutput("console");
        hBox.getChildren().add(console);

        // Setup memory table view
        ScrollPane  scrollPane = new ScrollPane();
        TableView<StackModel> tableView = new TableView<StackModel>();
        ObservableList<StackModel> list = FXCollections.observableArrayList();

        tableView.setItems(list);

        // Setup memory table column
        TableColumn<StackModel, Integer> address = new TableColumn<>("address");
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        TableColumn<StackModel, String> ident = new TableColumn<>("ident");
        ident.setCellValueFactory(new PropertyValueFactory<>("ident"));
        TableColumn<StackModel, Object> value = new TableColumn<>("value");
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        TableColumn<StackModel, String> object = new TableColumn<>("object");
        object.setCellValueFactory(new PropertyValueFactory<>("object"));
        TableColumn<StackModel, String> type = new TableColumn<>("type");
        type.setCellValueFactory(new PropertyValueFactory<>("type"));

        tableView.getColumns().setAll(address, ident, value, object, type);

        tableView.setMinWidth(400);
        hBox.getChildren().add(tableView);


        HBox.setHgrow(console, Priority.ALWAYS);

        root.setBottom(hBox);

        // Register the console with the logging system
        GuiAppender.setGuiConsole(this.console);
        logger.info("GUI application started successfully");
        // === End ===
        this.console.setStyle("-fx-border-color: #c0c0c0; -fx-border-width: 1 0 0 0;");

        GuiAppender.setGuiConsole(this.console);
        logger.info("GUI application started successfully");
        SplitPane mainSplitPane = new SplitPane(editorSplitPane, hBox);
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.setDividerPositions(0.7);

        root.setCenter(mainSplitPane);

        // Scene + styles
        Scene scene = new Scene(root, 1150, 720);

        String lightCss = getClass().getResource("/light.css").toExternalForm();
        scene.getStylesheets().add(lightCss);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Stop the application.
     * 
     * @throws Exception if an error occurs
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        executor.shutdownNow();
    }

    private HBox buildTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setSpacing(8);
        titleBar.getStyleClass().add("app-titlebar");

        Label titleLabel = new Label("MiniJaja IDE");
        titleLabel.getStyleClass().add("app-title-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minButton = new Button("-");
        minButton.getStyleClass().addAll("window-button", "window-button-min");

        Button maxButton = new Button("□");
        maxButton.getStyleClass().addAll("window-button", "window-button-max");

        Button closeButton = new Button("X");
        closeButton.getStyleClass().addAll("window-button", "window-button-close");

        minButton.setOnAction(e -> appStage.setIconified(true));
        maxButton.setOnAction(e -> appStage.setMaximized(!appStage.isMaximized()));
        closeButton.setOnAction(e -> appStage.close());

        titleBar.getChildren().addAll(titleLabel, spacer, minButton, maxButton, closeButton);

        final double[] dragDelta = new double[2];
        titleBar.setOnMousePressed(e -> {
            dragDelta[0] = appStage.getX() - e.getScreenX();
            dragDelta[1] = appStage.getY() - e.getScreenY();
        });
        titleBar.setOnMouseDragged(e -> {
            appStage.setX(e.getScreenX() + dragDelta[0]);
            appStage.setY(e.getScreenY() + dragDelta[1]);
        });

        return titleBar;
    }

    /**
     * Fonction pour construire le menu de l'ihm
     *
     * @return MenuBar le menu de l'application
     */
    private HBox buildMenu() {
        HBox hbox = new HBox();
        hbox.getStyleClass().add("top-strip");

        hbox.setSpacing(10);
        hbox.setStyle("-fx-padding: 2 12 2 12;");
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);

        MenuItem saveItem = new MenuItem("Save");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> loadFile());

        saveItem.setOnAction(e -> saveFile());

        fileMenu.getItems().addAll(saveItem, openItem);

        hbox.getChildren().add(menuBar);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.getChildren().add(spacer);
        /* Log Control Panel */
        LogControlPanel logControlPanel = new LogControlPanel();
        hbox.getChildren().add(logControlPanel);
        // load moon icon
        java.net.URL moonUrl = getClass().getResource("/icons/moon.png");
        if (moonUrl == null) {
            throw new IllegalStateException("Resource /icons/moon.png not found on classpath");
        }
        ImageView moonIcon = new ImageView(new Image(moonUrl.toExternalForm()));
        moonIcon.setFitWidth(16);
        moonIcon.setFitHeight(16);
        moonIcon.setPreserveRatio(true);

        // load sun icon
        java.net.URL sunUrl = getClass().getResource("/icons/sun.png");
        if (sunUrl == null) {
            throw new IllegalStateException("Resource /icons/sun.png not found on classpath");
        }
        ImageView sunIcon = new ImageView(new Image(sunUrl.toExternalForm()));
        sunIcon.setFitWidth(16);
        sunIcon.setFitHeight(16);
        sunIcon.setPreserveRatio(true);

        darkMode = new ToggleButton();
        darkMode.setText(null);
        darkMode.setGraphic(moonIcon);

        darkMode.setOnAction(e -> {
            if (appStage != null && appStage.getScene() != null) {
                Scene scene = appStage.getScene();

                String darkCss = getClass().getResource("/dark.css").toExternalForm();
                String lightCss = getClass().getResource("/light.css").toExternalForm();

                if (darkMode.isSelected()) {
                    scene.getStylesheets().remove(lightCss);
                    if (!scene.getStylesheets().contains(darkCss)) {
                        scene.getStylesheets().add(darkCss);
                    }
                    darkMode.setGraphic(sunIcon);
                } else {
                    scene.getStylesheets().remove(darkCss);
                    if (!scene.getStylesheets().contains(lightCss)) {
                        scene.getStylesheets().add(lightCss);
                    }
                    darkMode.setGraphic(moonIcon);
                }
            }
        });

        hbox.getChildren().add(darkMode);

        return hbox;
    }

    private HBox buildToolbar() {
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setStyle("-fx-padding: 4 12 4 12;");
        hbox.setId("debug-toolbar");

        Button buildButton = new Button("");
        buildButton.setGraphic(
                new ImageView(Objects.requireNonNull(getClass().getResource("/icons/build.png")).toExternalForm()));
        buildButton.setOnAction(e -> compile());
        buildButton.setTooltip(new Tooltip("Compile file"));
        hbox.getChildren().add(buildButton);

        /* Select file to be interpreted */
        fileToRun = new ChoiceBox<>();
        fileToRun.getItems().addAll("MiniJaja", "Jajacode");
        fileToRun.setValue("MiniJaja");
        hbox.getChildren().add(fileToRun);

        /* Execute button */
        Button runButton = new Button("");
        runButton.setGraphic(new ImageView(
                Objects.requireNonNull(getClass().getResource("/icons/threadRunning.png")).toExternalForm()));
        runButton.setTooltip(new Tooltip("Run"));
        runButton.setOnAction(e -> run());

        hbox.getChildren().add(runButton);

        // Added section: debug controls (icons only, no text)
        /**
         * Simple debug control bar with icons only: Start Debug / Step / Stop
         */

        debugButton = new Button();
        stepButton = new Button();
        stopButton = new Button();
        continueButton = new Button();

        debugButton.setTooltip(new Tooltip("Start debug on current file"));
        ImageView debugIcon = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/bug.png")), 20, 20, true, true));
        debugButton.setGraphic(debugIcon);
        hbox.getChildren().add(debugButton);
        debugButton.setOnAction(e -> startDebugEnhanced());

        stepButton.setTooltip(new Tooltip("Step to next line"));
        stepButton.setDisable(true);
        ImageView stepIcon = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/next.png")), 20, 20, true, true));
        stepButton.setGraphic(stepIcon);
        hbox.getChildren().add(stepButton);
        stepButton.setOnAction(e -> stepDebugEnhanced());

        continueButton.setTooltip(new Tooltip("Continue to next breakpoint"));
        continueButton.setDisable(true);
        ImageView continueIcon = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/fast-forward.png")), 20, 20, true,
                true));
        continueButton.setGraphic(continueIcon);
        hbox.getChildren().add(continueButton);
        continueButton.setOnAction(e -> continueDebug());

        stopButton.setTooltip(new Tooltip("Stop debug mode"));
        stopButton.setDisable(true);
        ImageView stopIcon = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/stop.png")), 20, 20, true, true));
        stopButton.setGraphic(stopIcon);
        hbox.getChildren().add(stopButton);
        stopButton.setOnAction(e -> stopDebugWithReset());

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
                new FileChooser.ExtensionFilter("JajaCode", "*.jjc"));
        File file = fileChooser.showOpenDialog(appStage);
        if (file == null)
            return;

        /* Lecture du fichier selectionner depuis l'explorateur de fichier */
        StringBuilder fileContent = new StringBuilder();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContent.append(scanner.nextLine());
                fileContent.append("\n");
            }
        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur lors de l'ouverture du fichier : ");
            alert.setContentText(e.toString());
            alert.showAndWait();
            return;
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Impossible de lire le fichier");
            alert.setContentText(e.toString());
            alert.showAndWait();
            return;
        }

        /* Chargement du texte dans l'interface */
        String name = file.getName().toLowerCase();
        String content = fileContent.toString();

        if (name.endsWith(".mjj")) {
            mjjCodeArea.loadText(content);
            fileToRun.setValue("MiniJaja");
        } else if (name.endsWith(".jjc")) {
            jjcCodeArea.loadText(content);
            fileToRun.setValue("Jajacode");
        }

        if (console != null) {
            console.printMessage("File loaded: " + file.getName());
        }
    }

    /**
     * Sauvegarde du contenu de la code area dans un fichier
     */
    private void saveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MiniJaja", "*.mjj"),
                new FileChooser.ExtensionFilter("JajaCode", "*.jjc"));
        File file = fileChooser.showSaveDialog(appStage);
        if (file == null)
            return;

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

        if (console != null) {
            console.printMessage("File saved: " + file.getAbsolutePath());
        }
    }

    /**
     * Fonction utiliser pour appeler les methodes necessaires à la compilation du
     * minijaja
     * Ecris le resultat de la compilation dans la zone prévu pour le jajacode
     */
    private void compile() {
        // On lit le texte sur le thread FX
        String code = mjjCodeArea.getText();

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                Compiler compiler = new Compiler(code, Compiler.Destination.STRING, null);
                logger.debug("Starting compilation task...");
                logger.debug("MiniJaja code length: {} characters", code.length());
                logger.debug("Instruction jajacode générée :");
                logger.debug("{}", compiler.compileToString());
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
                        JajaCodeInterpreter jjcInterpreter = new JajaCodeInterpreter(result.toString(),
                                new DiagnosticCollector());
                        jjcInterpreter.run();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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

    // DEBUG (Start / Step / Stop) pour MiniJaja et JajaCode

    /**
     * Continue button jumps from breakpoint to breakpoint.
     * If no more breakpoints exist after current line, stops execution.
     */
    private void continueDebug() {
        if (!debugMode)
            return;

        MyCodeArea currentArea = (debugSource == DebugSource.MINIJAJA) ? mjjCodeArea : jjcCodeArea;

        // Find next breakpoint after current line
        int nextBreakpoint = findNextBreakpointAfter(debugCurrentLine);

        if (nextBreakpoint >= 0) {
            // Jump to next breakpoint
            if (debugSource == DebugSource.MINIJAJA) {
                mjjDebugWalker.addBreakPoint(nextBreakpoint);
            }
            debugCurrentLine = nextBreakpoint;
            if (console != null) {
                String text = currentArea.getText();
                String[] lines = text.split("\\R", -1);
                console.printMessage("[DEBUG] Hit breakpoint at line " + (debugCurrentLine + 1) +
                        ": " + lines[debugCurrentLine]);
            }
            currentArea.highlightLine(debugCurrentLine);
        } else {
            // No more breakpoints, stop execution
            if (console != null) {
                console.printMessage("[DEBUG] No more breakpoints. Execution stopped.");
            }
            stopDebugWithReset();
        }

        // Update debug status even if there is no more breakpoint so the debug can finish
        if (debugSource == DebugSource.MINIJAJA) {
            mjjPauseHandler.updateStatus(Status.NEXT_BREAKPOINT);
        }
    }

    /**
     * Stops simple debug mode.
     */
    private void stopDebug() {
        if (!debugMode) {
            return;
        }
        if (debugSource == DebugSource.MINIJAJA) {
            mjjPauseHandler.updateStatus(Status.STOP);
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

    /**
     * Starts debug mode with improved step behavior using breakpoints.
     */
    private void startDebugEnhanced() {
        if (debugMode) {
            return;
        }

        String choice = fileToRun.getValue();
        if ("Jajacode".equals(choice)) {
            debugSource = DebugSource.JAJACODE;
        } else {
            debugSource = DebugSource.MINIJAJA;
        }

        debugMode = true;
        debugCurrentLine = -1;
        stepButton.setDisable(false);
        stopButton.setDisable(false);
        continueButton.setDisable(false);

        MyCodeArea currentArea = (debugSource == DebugSource.MINIJAJA) ? mjjCodeArea : jjcCodeArea;
        if (debugSource == DebugSource.MINIJAJA) {
            mjjDebugger = new MiniJajaDebugger(currentArea.getText(), new DiagnosticCollector(), mjjPauseHandler::handlePause);
        }
        debugBreakpoints.clear();
        debugBreakpoints.addAll(currentArea.getBreakpoints());

        if (console != null) {
            console.printMessage("[DEBUG] Debug mode started on " +
                    (debugSource == DebugSource.MINIJAJA ? "MiniJaja" : "JajaCode") + ".");
        }

        // Conditional start behavior based on breakpoints
        String text = currentArea.getText();
        String[] lines = text.split("\\R", -1);

        if (debugBreakpoints.isEmpty()) {
            // No breakpoints: start at the first executable line
            int firstExecutable = findNextExecutableLine(lines, -1);
            if (firstExecutable < 0) {
                if (console != null) {
                    console.printMessage("[DEBUG] No executable lines.");
                }
                stopDebugWithReset();
                return;
            }
            debugCurrentLine = firstExecutable;
            if (console != null) {
                console.printMessage("[DEBUG] " +
                        (debugSource == DebugSource.MINIJAJA ? "MiniJaja" : "JajaCode") +
                        " line " + (debugCurrentLine + 1) + ": " + lines[debugCurrentLine]);
            }
            currentArea.highlightLine(debugCurrentLine);
        } else {
            // Breakpoints exist: start at the first breakpoint
            int firstBreakpoint = findNextBreakpointAfter(-1);
            if (firstBreakpoint < 0) {
                if (console != null) {
                    console.printMessage("[DEBUG] No valid breakpoints found.");
                }
                stopDebugWithReset();
                return;
            }
            debugCurrentLine = firstBreakpoint;
            if (console != null) {
                console.printMessage("[DEBUG] Hit breakpoint at line " + (debugCurrentLine + 1));
            }
            currentArea.highlightLine(debugCurrentLine);
        }
    }

    /**
     * Steps in debug mode line by line.
     * After the initial jump to first breakpoint (if any), always go line by line.
     */
    private void stepDebugEnhanced() {
        if (!debugMode) {
            return;
        }

        MyCodeArea currentArea = (debugSource == DebugSource.MINIJAJA) ? mjjCodeArea : jjcCodeArea;
        String text = currentArea.getText();
        String[] lines = text.split("\\R", -1);

        if (lines.length == 0) {
            if (console != null) {
                console.printMessage("[DEBUG] No lines to debug.");
            }
            stopDebugWithReset();
            return;
        }

        // Always go line by line, ignoring breakpoints
        int nextLine = findNextExecutableLine(lines, debugCurrentLine);
        if (nextLine < 0) {
            if (console != null) {
                console.printMessage("[DEBUG] End of file reached.");
            }
            stopDebugWithReset();
            return;
        }
        debugCurrentLine = nextLine;

        if (debugCurrentLine < 0 || debugCurrentLine >= lines.length) {
            if (console != null) {
                console.printMessage("[DEBUG] End of file reached.");
            }
            stopDebugWithReset();
            return;
        }

        if (console != null) {
            console.printMessage("[DEBUG] " +
                    (debugSource == DebugSource.MINIJAJA ? "MiniJaja" : "JajaCode") +
                    " line " + (debugCurrentLine + 1) + ": " + lines[debugCurrentLine]);
        }
        if (debugSource ==  DebugSource.MINIJAJA) {
            mjjPauseHandler.updateStatus(Status.NEXT_STEP);
        }

        currentArea.highlightLine(debugCurrentLine);
    }

    /**
     * Finds the next non-empty line after a given index.
     */
    private int findNextExecutableLine(String[] lines, int fromIndex) {
        int index = fromIndex + 1;
        while (index < lines.length) {
            if (!lines[index].trim().isEmpty()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Finds the next breakpoint strictly after the given line index.
     */
    private int findNextBreakpointAfter(int lineIndex) {
        if (debugBreakpoints.isEmpty()) {
            return -1;
        }
        int candidate = Integer.MAX_VALUE;
        for (int bp : debugBreakpoints) {
            if (bp > lineIndex && bp < candidate) {
                candidate = bp;
            }
        }
        return (candidate == Integer.MAX_VALUE) ? -1 : candidate;
    }

    /**
     * Stops debug mode and resets the current line highlight.
     */
    private void stopDebugWithReset() {
        stopDebug();
        MyCodeArea currentArea = (debugSource == DebugSource.MINIJAJA) ? mjjCodeArea : jjcCodeArea;
        currentArea.highlightLine(0);
    }

    public static void main(String[] args) {
        launch();
    }

}
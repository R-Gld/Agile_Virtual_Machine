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
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeDebug;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreter;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaDebugger;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.logging.GuiAppender;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

// Breakpoint support
import java.util.HashSet;
import java.util.Set;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static final String MINI_JAJA_NAME = "MiniJaja";
    public static final String JAJA_CODE_NAME = "JajaCode";

    private Stage appStage;
    private MyCodeArea mjjCodeArea;
    private MyCodeArea jjcCodeArea;
    private ChoiceBox<String> fileToRun;
    private ToggleButton darkMode;

    private Button stepButton;
    private Button stopButton;
    private Button continueButton;

    private GuiController controller;

    private MenuItem openItem;
    private MenuItem saveItem;
    private Button buildButton;
    private Button runButton;
    private Button debugButton;

    /**
     * Console used to display messages (debug, info, errors)
     */
    private ConsoleOutput console;
    private TableView<JajaCodeDebug.VariableInfo> stackTable;
    private TableView<JajaCodeDebug.HeapInfo> heapTable;

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

        Label mjjLabel = new Label(MINI_JAJA_NAME);
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

        Label jjcLabel = new Label(JAJA_CODE_NAME);
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

        // Memory View
        SplitPane memoryView = buildMemoryView();

        // Horizontal split: Editors | Memory View
        SplitPane contentSplitPane = new SplitPane(editorSplitPane, memoryView);
        contentSplitPane.setDividerPositions(0.7);

        // Console
        this.console = new ConsoleOutput("console");
        this.console.setStyle("-fx-border-color: #c0c0c0; -fx-border-width: 1 0 0 0;");

        GuiAppender.setGuiConsole(this.console);
        logger.info("GUI application started successfully");

        // Vertical split: Content | Console
        SplitPane mainSplitPane = new SplitPane(contentSplitPane, this.console);
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.setDividerPositions(0.75);

        root.setCenter(mainSplitPane);

        // --- Drag and Drop ---
        root.setOnDragOver(event -> {
            if (event.getGestureSource() != root && event.getDragboard().hasFiles()) {
                // Accept the drop only if one of the files is a .mjj or .jjc file
                boolean canAccept = event.getDragboard().getFiles().stream().anyMatch(file -> {
                    String name = file.getName().toLowerCase();
                    return name.endsWith(".mjj") || name.endsWith(".jjc");
                });
                if (canAccept) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
            }
            event.consume();
        });

        root.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                // Find the first valid file and load it.
                db.getFiles().stream().filter(file -> file.getName().toLowerCase().endsWith(".mjj") || file.getName().toLowerCase().endsWith(".jjc")).findFirst().ifPresent(file -> controller.loadFileContent(file));
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Scene + styles
        Scene scene = new Scene(root, 1150, 720);

        String themeCss = Objects.requireNonNull(getClass().getResource("/theme.css")).toExternalForm();
        scene.getStylesheets().add(themeCss);
        root.getStyleClass().add("theme-light");

        stage.setScene(scene);

        // Initialize controller and set up button actions
        this.controller = new GuiController(appStage, mjjCodeArea, jjcCodeArea, fileToRun, console, stackTable, heapTable, stepButton, stopButton, continueButton);
        
        // Set up button actions
        buildButton.setOnAction(e -> controller.compile());
        runButton.setOnAction(e -> controller.run());
        debugButton.setOnAction(e -> controller.startDebug());
        stepButton.setOnAction(e -> controller.stepDebug());
        continueButton.setOnAction(e -> controller.continueDebug());
        stopButton.setOnAction(e -> controller.stopDebug());
        
        // Set up menu actions
        openItem.setOnAction(e -> controller.loadFile());
        saveItem.setOnAction(e -> controller.saveFile());

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

        titleBar.getChildren().addAll(titleLabel);

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

        this.openItem = new MenuItem("Open");
        this.openItem.setId("openItem");
        this.saveItem = new MenuItem("Save");
        this.saveItem.setId("saveItem");

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
        darkMode.setId("darkMode");
        darkMode.setText(null);
        darkMode.setGraphic(moonIcon);

        darkMode.setOnAction(e -> {
            if (appStage != null && appStage.getScene() != null) {
                // The root pane is a BorderPane, as defined in the start() method.
                Pane rootPane = (Pane) appStage.getScene().getRoot();
                if (darkMode.isSelected()) {
                    rootPane.getStyleClass().remove("theme-light");
                    rootPane.getStyleClass().add("theme-dark");
                    darkMode.setGraphic(sunIcon);
                } else {
                    rootPane.getStyleClass().remove("theme-dark");
                    rootPane.getStyleClass().add("theme-light");
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

        this.buildButton = new Button("");
        this.buildButton.setId("buildButton");
        buildButton.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/icons/build.png")).toExternalForm()));
        buildButton.setTooltip(new Tooltip("Compile file"));
        hbox.getChildren().add(buildButton);

        /* Select file to be interpreted */
        fileToRun = new ChoiceBox<>();
        fileToRun.setId("fileToRun");
        fileToRun.getItems().addAll(MINI_JAJA_NAME, "Jajacode");
        fileToRun.setValue(MINI_JAJA_NAME);
        hbox.getChildren().add(fileToRun);

        /* Execute button */
        this.runButton = new Button("");
        this.runButton.setId("runButton");
        runButton.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/icons/threadRunning.png")).toExternalForm()));
        runButton.setTooltip(new Tooltip("Run"));

        hbox.getChildren().add(runButton);

        // Added section: debug controls (icons only, no text)
        /*
         * Simple debug control bar with icons only: Start Debug / Step / Stop
         */

        // Debug Buttons
        this.debugButton = new Button();
        stepButton = new Button();
        stopButton = new Button();
        continueButton = new Button();

        debugButton.setTooltip(new Tooltip("Start debug on current file"));
        debugButton.setId("debugButton");
        ImageView debugIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/bug.png")), 20, 20, true, true));
        debugButton.setGraphic(debugIcon);
        hbox.getChildren().add(debugButton);

        stepButton.setTooltip(new Tooltip("Step to next line"));
        stepButton.setId("stepButton");
        stepButton.setDisable(true);
        ImageView stepIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/next.png")), 20, 20, true, true));
        stepButton.setGraphic(stepIcon);
        hbox.getChildren().add(stepButton);

        continueButton.setTooltip(new Tooltip("Continue to next breakpoint"));
        continueButton.setId("continueButton");
        continueButton.setDisable(true);
        ImageView continueIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/fast-forward.png")), 20, 20, true, true));
        continueButton.setGraphic(continueIcon);
        hbox.getChildren().add(continueButton);

        stopButton.setTooltip(new Tooltip("Stop debug mode"));
        stopButton.setId("stopButton");
        stopButton.setDisable(true);
        ImageView stopIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/stop.png")), 20, 20, true, true));
        stopButton.setGraphic(stopIcon);
        hbox.getChildren().add(stopButton);

        return hbox;
    }













    /**
     * Builds the memory view panel with Stack and Heap tables.
     */
    private SplitPane buildMemoryView() {
        // Stack Table
        stackTable = new TableView<>();

        TableColumn<JajaCodeDebug.VariableInfo, String> idCol = new TableColumn<>("Identifier");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().identifier()));
        idCol.setPrefWidth(120);

        TableColumn<JajaCodeDebug.VariableInfo, Object> valCol = new TableColumn<>("Value");
        valCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().value()));
        valCol.setPrefWidth(80);

        TableColumn<JajaCodeDebug.VariableInfo, String> kindCol = new TableColumn<>("Kind");
        kindCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().kind()));
        kindCol.setPrefWidth(60);

        TableColumn<JajaCodeDebug.VariableInfo, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().type())));
        typeCol.setPrefWidth(70);

        stackTable.getColumns().addAll(idCol, valCol, kindCol, typeCol);
        stackTable.setPlaceholder(new Label("Stack is empty - Start debugging to see memory"));

        Label stackLabel = new Label("Stack");
        stackLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
        VBox stackBox = new VBox(stackLabel, stackTable);
        VBox.setVgrow(stackTable, Priority.ALWAYS);

        // Heap Table
        heapTable = new TableView<>();

        TableColumn<JajaCodeDebug.HeapInfo, String> heapIdCol = new TableColumn<>("Array");
        heapIdCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().identifier()));
        heapIdCol.setPrefWidth(100);

        TableColumn<JajaCodeDebug.HeapInfo, Integer> addrCol = new TableColumn<>("Addr");
        addrCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().baseAddress()));
        addrCol.setPrefWidth(50);

        TableColumn<JajaCodeDebug.HeapInfo, Integer> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().size()));
        sizeCol.setPrefWidth(50);

        TableColumn<JajaCodeDebug.HeapInfo, String> elementsCol = new TableColumn<>("Elements");
        elementsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().elements().toString()));
        elementsCol.setPrefWidth(150);

        heapTable.getColumns().addAll(heapIdCol, addrCol, sizeCol, elementsCol);
        heapTable.setPlaceholder(new Label("Heap is empty"));

        Label heapLabel = new Label("Heap (Arrays)");
        heapLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
        VBox heapBox = new VBox(heapLabel, heapTable);
        VBox.setVgrow(heapTable, Priority.ALWAYS);

        SplitPane memoryPane = new SplitPane(stackBox, heapBox);
        memoryPane.setOrientation(Orientation.VERTICAL);
        memoryPane.setDividerPositions(0.7);

        return memoryPane;
    }

    /**
     * Load file content - delegate to controller.
     * This method is kept for backward compatibility with tests.
     */
    public void loadFileContent(File file) {
        if (controller != null) {
            controller.loadFileContent(file);
        }
    }

    public GuiController getController() {
        return controller;
    }

    public static void main(String[] args) {
        launch();
    }

}

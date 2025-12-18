package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeDebug;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JajaCodeDebugHandler {
    private JajaCodeInterpreter interpreter;
    private final ConsoleOutput console;
    private final MyCodeArea codeArea;
    private final TableView<JajaCodeDebug.VariableInfo> stackTable;
    private final TableView<JajaCodeDebug.HeapInfo> heapTable;
    private volatile boolean isRunning = false;
    private ExecutorService executor;

    public JajaCodeDebugHandler(ConsoleOutput console, MyCodeArea codeArea,
                                TableView<JajaCodeDebug.VariableInfo> stackTable,
                                TableView<JajaCodeDebug.HeapInfo> heapTable) {
        this.console = console;
        this.codeArea = codeArea;
        this.stackTable = stackTable;
        this.heapTable = heapTable;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void start(String code) {
        try {
            interpreter = new JajaCodeInterpreter(code, new DiagnosticCollector());
            isRunning = true;
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "jajacode-debugger");
                t.setDaemon(true);
                return t;
            });
            Platform.runLater(this::updateView);
            console.printMessage("[DEBUG] Debug session started.");
        } catch (Exception e) {
            showError("Failed to start debugger", e);
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        interpreter = null;
        Platform.runLater(() -> {
            codeArea.highlightLine(-1); // Clear highlight
            if (stackTable != null) stackTable.getItems().clear();
            if (heapTable != null) heapTable.getItems().clear();
            console.printMessage("[DEBUG] Debug session stopped.");
        });
    }

    public void step() {
        if (!isRunning || interpreter == null || executor == null) return;
        executor.submit(() -> {
            try {
                boolean hasMore = interpreter.step();
                Platform.runLater(this::updateView);
                if (!hasMore || interpreter.isFinished()) {
                    Platform.runLater(() -> console.printMessage("[DEBUG] Execution finished."));
                    stop();
                }
            } catch (Exception e) {
                showError("Runtime Error", e);
                stop();
            }
        });
    }

    public void continueDebug(Set<Integer> breakpoints) {
        if (!isRunning || interpreter == null || executor == null) return;
        executor.submit(() -> {
            try {
                boolean hasMore = true;
                boolean hitBreakpoint = false;

                // Step at least once to move past current instruction/breakpoint
                hasMore = interpreter.step();

                while (hasMore && !interpreter.isFinished() && !Thread.currentThread().isInterrupted()) {
                    int pc = interpreter.getCurrentInstructionIndex();
                    // pc is 1-based address. Breakpoints are 0-based line numbers.
                    // Assuming 1-to-1 mapping: address 1 is line 0.
                    if (breakpoints.contains(pc - 1)) {
                        hitBreakpoint = true;
                        break;
                    }
                    hasMore = interpreter.step();
                }
                Platform.runLater(this::updateView);

                if (hitBreakpoint) {
                    int pc = interpreter.getCurrentInstructionIndex();
                    Platform.runLater(() -> console.printMessage("[DEBUG] Breakpoint hit at line " + pc));
                } else if (!hasMore || interpreter.isFinished()) {
                    Platform.runLater(() -> console.printMessage("[DEBUG] Execution finished."));
                    stop();
                }
            } catch (Exception e) {
                showError("Runtime Error", e);
                stop();
            }
        });
    }

    private void updateView() {
        if (interpreter == null) return;
        int pc = interpreter.getCurrentInstructionIndex();
        // pc is 1-based address. Highlight expects 0-based line index.
        if (pc > 0) {
            codeArea.highlightLine(pc - 1);
        }

        JajaCodeDebug.MemorySnapshot snapshot = interpreter.captureMemoryState();
        if (snapshot != null) {
            console.printMessage("[DEBUG] Memory Snapshot at PC=" + pc + ": Stack size=" + snapshot.stackState().size() + ", Heap size=" + snapshot.heapState().size());
            if (stackTable != null) {
                stackTable.setItems(FXCollections.observableArrayList(snapshot.stackState()));
                stackTable.refresh();
            }
            if (heapTable != null) {
                heapTable.setItems(FXCollections.observableArrayList(snapshot.heapState()));
                heapTable.refresh();
            }
        } else {
            console.printMessage("[DEBUG] Memory Snapshot is null at PC=" + pc);
        }
    }

    private void showError(String title, Exception e) {
        Platform.runLater(() -> {
            console.printMessage("[ERROR] " + title + ": " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        });
    }
}

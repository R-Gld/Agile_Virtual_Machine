package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeDebug;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaDebugger;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.HandlePauseCallback;
import fr.ufrst.m1info.gl.groupe7.memoire.ArrayInfo;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handler for MiniJaja debugging.
 * Manages step-by-step execution, breakpoints, and memory display.
 */
public class MiniJajaDebugHandler {

    private final ConsoleOutput console;
    private final MyCodeArea codeArea;
    private final TableView<JajaCodeDebug.VariableInfo> stackTable;
    private final TableView<JajaCodeDebug.HeapInfo> heapTable;

    private Debug debugController;
    private MiniJajaDebugger debugger;
    private Thread debugThread;

    private volatile boolean isRunning = false;
    private volatile boolean waitingForUserAction = false;
    private volatile boolean shouldStep = false;
    private volatile boolean shouldContinue = false;
    private volatile boolean shouldStop = false;

    @SuppressWarnings("unused")
    private int currentLine = -1;
    @SuppressWarnings("unused")
    private Stacks currentStacks;

    public MiniJajaDebugHandler(ConsoleOutput console, MyCodeArea codeArea,
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

    /**
     * Start a new MiniJaja debug session.
     *
     * @param code        The MiniJaja source code to debug.
     * @param breakpoints The set of breakpoint line numbers (0-based).
     */
    public void start(String code, Set<Integer> breakpoints) {
        if (isRunning) {
            console.printMessage("[DEBUG MJJ] Already running.");
            return;
        }

        try {
            debugController = new Debug(Debug.Mode.STEP_BY_STEP);

            // Add breakpoints (convert from 0-based UI to 1-based line numbers)
            for (int bp : breakpoints) {
                debugController.addBreakPoint(bp + 1);
            }

            // Create the callback that will be invoked when a breakpoint is hit
            HandlePauseCallback pauseCallback = this::handlePause;

            debugger = new MiniJajaDebugger(code, new DiagnosticCollector(), pauseCallback, debugController);

            isRunning = true;
            shouldStop = false;

            // Run debugger in a separate thread
            debugThread = new Thread(() -> {
                try {
                    debugger.run();
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        console.printMessage("[DEBUG MJJ] Error: " + e.getMessage());
                        showError("Runtime Error", e);
                    });
                } finally {
                    Platform.runLater(() -> {
                        console.printMessage("[DEBUG MJJ] Execution finished.");
                        stop();
                    });
                }
            }, "minijaja-debugger");
            debugThread.setDaemon(true);
            debugThread.start();

            console.printMessage("[DEBUG MJJ] Debug session started (Step-by-step mode).");

        } catch (Exception e) {
            showError("Failed to start MiniJaja debugger", e);
            stop();
        }
    }

    /**
     * Callback invoked by the Debug controller when a breakpoint is hit or step is needed.
     */
    private boolean handlePause(int line, AstNode node, Stacks stacks) {
        currentLine = line;
        currentStacks = stacks;

        // Update UI on the FX thread
        Platform.runLater(() -> {
            // Highlight line (convert 1-based to 0-based)
            if (line > 0) {
                codeArea.highlightLine(line - 1);
            }
            updateMemoryView(stacks);
            console.printMessage("[DEBUG MJJ] Paused at line " + line + " (" + node.getClass().getSimpleName() + ")");
        });

        // Wait for user action
        waitingForUserAction = true;
        shouldStep = false;
        shouldContinue = false;

        while (waitingForUserAction && !shouldStop) {
            if (shouldStep) {
                waitingForUserAction = false;
                // Don't call debugController.step() here - just return true to execute this node
                // The Debug controller in STEP_BY_STEP mode will pause again at the next node
                return true;
            }
            if (shouldContinue) {
                waitingForUserAction = false;
                // Switch to breakpoints mode and continue
                debugController.continueToNextBreakpoint();
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return !shouldStop;
    }

    /**
     * Step to the next instruction.
     */
    public void step() {
        if (!isRunning || !waitingForUserAction) return;
        shouldStep = true;
    }

    /**
     * Continue execution until the next breakpoint.
     */
    public void continueDebug(Set<Integer> breakpoints) {
        if (!isRunning) return;

        // Update breakpoints
        if (debugController != null) {
            debugController.clearBreakPoints();
            for (int bp : breakpoints) {
                debugController.addBreakPoint(bp + 1); // Convert 0-based to 1-based
            }
            // Switch to breakpoints mode
            debugController.setMode(Debug.Mode.BREAKPOINTS);
        }

        if (waitingForUserAction) {
            shouldContinue = true;
        }
    }

    /**
     * Stop the debug session.
     */
    public void stop() {
        shouldStop = true;
        waitingForUserAction = false;
        isRunning = false;

        if (debugController != null) {
            debugController.stop();
        }

        if (debugThread != null && debugThread.isAlive()) {
            debugThread.interrupt();
        }

        Platform.runLater(() -> {
            codeArea.highlightLine(-1);
            if (stackTable != null) stackTable.getItems().clear();
            if (heapTable != null) heapTable.getItems().clear();
            console.printMessage("[DEBUG MJJ] Debug session stopped.");
        });
    }

    /**
     * Update the memory view tables with the current stack and heap state.
     */
    private void updateMemoryView(Stacks stacks) {
        if (stacks == null) return;

        // Build stack info list
        List<JajaCodeDebug.VariableInfo> stackInfo = new ArrayList<>();
        List<Stacks.Quad> stackContent = stacks.getStackFromTopToBottom();
        int position = stackContent.size();
        for (Stacks.Quad quad : stackContent) {
            boolean isTemp = "_".equals(quad.ident);
            stackInfo.add(new JajaCodeDebug.VariableInfo(
                    quad.ident,
                    quad.value,
                    quad.object,
                    quad.type,
                    position--,
                    isTemp
            ));
        }

        // Build heap info list
        List<JajaCodeDebug.HeapInfo> heapInfo = new ArrayList<>();
        for (Stacks.Quad quad : stackContent) {
            if ("tab".equals(quad.object) && quad.value instanceof ArrayInfo info) {
                int size = info.getSize();
                int baseAddr = info.getBaseAddress();
                List<Object> elements = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    try {
                        Object val = stacks.getArrayValue(quad.ident, i);
                        elements.add(val != null ? val : "null");
                    } catch (Exception e) {
                        elements.add("?");
                    }
                }
                heapInfo.add(new JajaCodeDebug.HeapInfo(
                        quad.ident,
                        baseAddr,
                        size,
                        size, // allocatedSize = size for simplicity
                        quad.type,
                        elements
                ));
            }
        }

        // Update tables on FX thread
        if (stackTable != null) {
            stackTable.setItems(FXCollections.observableArrayList(stackInfo));
            stackTable.refresh();
        }
        if (heapTable != null) {
            heapTable.setItems(FXCollections.observableArrayList(heapInfo));
            heapTable.refresh();
        }
    }

    private void showError(String title, Exception e) {
        console.printMessage("[ERROR] " + title + ": " + e.getMessage());
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        });
    }
}


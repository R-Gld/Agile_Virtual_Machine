package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Debug controller for the AST Walker.
 * Supports breakpoints, step-by-step execution, and continue to next breakpoint.
 */
public class Debug {
    
    public enum Mode {
        DISABLED,       // No debugging, run normally
        STEP_BY_STEP,   // Stop at every node
        BREAKPOINTS     // Stop only at breakpoints
    }
    
    private Mode mode = Mode.DISABLED;
    private final Set<Integer> breakPoints = new HashSet<>();
    private int currentLine = 0;
    private boolean paused = false;
    private boolean stepNext = false;
    private final Scanner scanner;
    private static final Logger logger = LoggerFactory.getLogger(Debug.class);
    
    // Listener for debug events (optional, for GUI integration)
    private DebugListener listener;
    
    public interface DebugListener {
        void onBreakpoint(int line, AstNode node, Stacks stacks);
        void onStep(int line, AstNode node, Stacks stacks);
        void onResume();
    }

    public Debug() {
        this.scanner = new Scanner(System.in);
    }
    
    public Debug(Mode mode) {
        this();
        this.mode = mode;
    }

    // ==================== BREAKPOINT MANAGEMENT ====================
    
    public void addBreakPoint(int line) {
        breakPoints.add(line);
    }
    
    public void removeBreakPoint(int line) {
        breakPoints.remove(line);
    }
    
    public void clearBreakPoints() {
        breakPoints.clear();
    }
    
    public Set<Integer> getBreakPoints() {
        return new HashSet<>(breakPoints);
    }
    
    public boolean hasBreakPoint(int line) {
        return breakPoints.contains(line);
    }

    // ==================== MODE CONTROL ====================
    
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public boolean isEnabled() {
        return mode != Mode.DISABLED;
    }
    
    public void enable() {
        if (mode == Mode.DISABLED) {
            mode = Mode.BREAKPOINTS;
        }
    }
    
    public void disable() {
        mode = Mode.DISABLED;
    }

    // ==================== EXECUTION CONTROL ====================
    
    /**
     * Called before each node is interpreted.
     * Returns true if execution should proceed, false if paused.
     */
    public boolean beforeNode(int line, AstNode node, Stacks stacks, HandlePauseCallback callback) {
        if (callback == null) {
            callback = this::handlePause;
        }

        if (mode == Mode.DISABLED) {
            return true;
        }
        
        currentLine = line;
        
        boolean shouldStop = false;
        
        if (mode == Mode.STEP_BY_STEP) {
            shouldStop = true;
        } else if (mode == Mode.BREAKPOINTS && hasBreakPoint(line)) {
            shouldStop = true;
            logger.info("\n BREAKPOINT HIT at line " + line);
        }
        
        if (stepNext) {
            shouldStop = true;
            stepNext = false;
        }
        
        if (shouldStop) {
            paused = true;
            
            if (listener != null) {
                listener.onBreakpoint(line, node, stacks);
            }
            
            return callback.run(line, node, stacks);
        }
        
        return true;
    }
    
    /**
     * Handles the debug pause - shows menu and waits for user input.
     */
    private boolean handlePause(int line, AstNode node, Stacks stacks) {
        printDebugState(line, node, stacks);
        
        while (paused) {
            logger.debug("\n (s)tep | (c)ontinue | (n)ext breakpoint | (p)rint stack | (v)ars | (q)uit > ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            switch (input) {    
                case "s", "step" -> {
                    step();
                    return true;
                }
                case "c", "continue" -> {
                    continueStepByStep();
                    return true;
                }
                case "n", "next" -> {
                    continueToNextBreakpoint();
                    return true;
                }
                case "p", "print", "stack" -> {
                    // Print stack
                    logger.debug("\n STACK STATE:");
                    stacks.printStack();
                }
                case "v", "vars", "symbols" -> {
                    // Print symbol table
                    logger.debug("\n SYMBOL TABLE:");
                    stacks.printSymbolTable();
                }
                case "b", "breakpoints" -> {
                    // List breakpoints
                    logger.debug("\n BREAKPOINTS: " + breakPoints);
                }
                case "q", "quit", "exit" -> {
                    // Quit debugging
                    stop();
                    scanner.close();
                    return false; // Signal to stop execution
                }
                
                default -> {
                    // Try to parse as breakpoint command: "b 10" or "+10" or "-10"
                    if (input.startsWith("b ") || input.startsWith("+ ")) {
                        try {
                            int bp = Integer.parseInt(input.substring(2).trim());
                            addBreakPoint(bp);
                            logger.debug("Breakpoint added at line " + bp);
                        } catch (NumberFormatException e) {
                            logger.debug(" Invalid line number");
                        }
                    } else if (input.startsWith("-")) {
                        try {
                            int bp = Integer.parseInt(input.substring(1).trim());
                            removeBreakPoint(bp);
                            logger.debug(" Breakpoint removed from line " + bp);
                        } catch (NumberFormatException e) {
                            logger.debug(" Invalid line number");
                        }
                    } else {
                        logger.debug(" Unknown command. Type 'h' for help.");
                    }
                }
            }
        }
        
        return true;
    }
    
    private void printDebugState(int line, AstNode node, Stacks stacks) {
        logger.debug("\n" + "═".repeat(60));
        logger.debug(" DEBUG PAUSE at line " + line);
        logger.debug("═".repeat(60));
        logger.debug(" Node: " + node.getClass().getSimpleName());
        logger.debug(" AST:  " + truncate(node.toStringTree(), 80));
        logger.debug(" Context: " + (stacks.isInMethodContext() ? stacks.getCurrentContext() : "main"));
        logger.debug("═".repeat(60));
    }
    
    private String truncate(String str, int maxLen) {
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    public void stop() {
        logger.debug(" Debug session ended.");
        mode = Mode.DISABLED;
        paused = false;
    }

    // ==================== LISTENER ====================
    
    public void setListener(DebugListener listener) {
        this.listener = listener;
    }
    
    public int getCurrentLine() {
        return currentLine;
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    /**
     * Resume execution programmatically (for GUI).
     */
    public void resume() {
        paused = false;
        if (listener != null) listener.onResume();
    }
    
    /**
     * Step to next node programmatically (for GUI).
     */
    public void step() {
        stepNext = true;
        paused = false;
        if (listener != null) listener.onResume();
    }
    
    /**
     * Continue in step-by-step mode.
     */
    public void continueStepByStep() {
        mode = Mode.STEP_BY_STEP;
        paused = false;
        if (listener != null) listener.onResume();
    }
    
    /**
     * Continue execution until next breakpoint.
     */
    public void continueToNextBreakpoint() {
        mode = Mode.BREAKPOINTS;
        paused = false;
        if (listener != null) listener.onResume();
    }
}

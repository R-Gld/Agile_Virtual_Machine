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
 * <p>
 * This class provides debugging capabilities for the MiniJaja interpreter, allowing for
 * step-by-step execution, management of breakpoints, and inspection of the runtime state
 * (stack and symbol table). It can be controlled via a command-line interface or integrated
 * into a GUI through the {@link DebugListener}.
 * </p>
 */
public class Debug {
    
    /**
     * Defines the execution mode for the debugger.
     */
    public enum Mode {
        /** No debugging, the program runs without interruption. */
        DISABLED,
        /** The debugger pauses before executing each node. */
        STEP_BY_STEP,
        /** The debugger pauses only at user-defined breakpoints. */
        BREAKPOINTS
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

    /**
     * A listener for debugger events, useful for GUI integration.
     * Allows external components to react to debugger state changes.
     */
    public interface DebugListener {
        /**
         * Called when a breakpoint is hit or when the debugger pauses in step-by-step mode.
         *
         * @param line   The current line number (node count) where the pause occurred.
         * @param node   The {@link AstNode} that is about to be executed.
         * @param stacks The current state of the {@link Stacks} (memory).
         */
        void onBreakpoint(int line, AstNode node, Stacks stacks);
        
        /**
         * Called on every step when in step-by-step mode.
         * Note: This is not currently called by the Debugger but is here for potential future use.
         * @param line The current line number.
         * @param node The current AST node.
         * @param stacks The current state of the stacks.
         */
        void onStep(int line, AstNode node, Stacks stacks);

        /**
         * Called when the debugger's execution is resumed (e.g., after a 'continue' or 'step' command).
         */
        void onResume();
    }

    /**
     * Constructs a new Debug controller, initialized in DISABLED mode.
     */
    public Debug() {
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Constructs a new Debug controller with a specified initil mode.
     *
     * @param mode The initial {@link Mode} for the debugger.
     */
    public Debug(Mode mode) {
        this();
        this.mode = mode;
    }

    // ==================== BREAKPOINT MANAGEMENT ====================
    
    /**
     * Adds a breakpoint at a specific line number.
     * The walker will pause before executing the node at this line.
     *
     * @param line The line number to set the breakpoint on.
     */
    public void addBreakPoint(int line) {
        breakPoints.add(line);
    }
    
    /**
     * Removes a breakpoint from a specific line number.
     *
     * @param line The line number to remove the breakpoint from.
     */
    public void removeBreakPoint(int line) {
        breakPoints.remove(line);
    }
    
    /**
     * Removes all currently set breakpoints.
     */
    public void clearBreakPoints() {
        breakPoints.clear();
    }
    
    /**
     * Retrieves a copy of the set of all currently set breakpoints.
     *
     * @return A new {@link Set} containing all breakpoint line numbers.
     */
    public Set<Integer> getBreakPoints() {
        return new HashSet<>(breakPoints);
    }
    
    /**
     * Checks if a breakpoint is set at a specific line number.
     *
     * @param line The line number to check.
     * @return {@code true} if a breakpoint exists at the given line, {@code false} otherwise.
     */
    public boolean hasBreakPoint(int line) {
        return breakPoints.contains(line);
    }

    // ==================== MODE CONTROL ====================
    
    /**
     * Sets the debugger's execution mode.
     *
     * @param mode The {@link Mode} to set.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    /**
     * Gets the current debugger execution mode.
     *
     * @return The current {@link Mode}.
     */
    public Mode getMode() {
        return mode;
    }
    
    /**
     * Checks if the debugger is currently active (i.e., not in {@link Mode#DISABLED}).
     *
     * @return {@code true} if the mode is {@link Mode#STEP_BY_STEP} or {@link Mode#BREAKPOINTS}, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return mode != Mode.DISABLED;
    }
    
    /**
     * Enables the debugger. If it was disabled, it defaults to {@link Mode#BREAKPOINTS}.
     */
    public void enable() {
        if (mode == Mode.DISABLED) {
            mode = Mode.BREAKPOINTS;
        }
    }
    
    /**
     * Disables the debugger, allowing the program to run without interruption.
     */
    public void disable() {
        mode = Mode.DISABLED;
    }

    // ==================== EXECUTION CONTROL ====================
    
    /**
     * This method is called by the {@link Walker} before each node is interpreted.
     * It determines whether the execution should pause based on the current debug mode and breakpoints.
     *
     * @param line   The current line number (node count).
     * @param node   The {@link AstNode} about to be executed.
     * @param stacks The current state of the {@link Stacks}.
     * @return {@code true} if execution should proceed, {@code false} if the walker should stop.
     */
    public boolean beforeNode(int line, AstNode node, Stacks stacks) {
        if (mode == Mode.DISABLED) {
            return true;
        }
        
        currentLine = line;
        
        boolean shouldStop = false;
        
        // Determine if we should pause at the current node
        if (mode == Mode.STEP_BY_STEP) {
            shouldStop = true;
        } else if (mode == Mode.BREAKPOINTS && hasBreakPoint(line)) {
            shouldStop = true;
            logger.info("\n BREAKPOINT HIT at line {}", line);
        }
        
        // If a single step was requested, stop now and reset the flag
        if (stepNext) {
            shouldStop = true;
            stepNext = false;
        }
        
        if (shouldStop) {
            paused = true;
            
            // Notify the listener that a breakpoint/step has occurred
            if (listener != null) {
                listener.onBreakpoint(line, node, stacks);
            }
            
            // Enter the interactive pause loop
            return handlePause(line, node, stacks);
        }
        
        return true;
    }
    
    /**
     * Handles the interactive debug pause. It displays a menu of debug commands and waits for user
     * input from the console to determine the next action (step, continue, quit, etc.).
     *
     * @param line   The line number where the pause occurred.
     * @param node   The current {@link AstNode}.
     * @param stacks The current {@link Stacks} state.
     * @return {@code false} if the user chooses to quit, {@code true} otherwise.
     */
    private boolean handlePause(int line, AstNode node, Stacks stacks) {
        printDebugState(line, node, stacks);
        
        while (paused) {
            logger.debug("\n (s)tep | (c)ontinue | (n)ext breakpoint | (p)rint stack | (v)ars | (q)uit > ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            switch (input) {    
                case "s", "step" -> {
                    step();
                    return true; // Exit loop and execute one step
                }
                case "c", "continue" -> {
                    continueStepByStep();
                    return true; // Exit loop and continue
                }
                case "n", "next" -> {
                    continueToNextBreakpoint();
                    return true; // Exit loop and continue
                }
                case "p", "print", "stack" -> {
                    // Print current stack state
                    logger.debug("\n STACK STATE:");
                    stacks.printStack();
                }
                case "v", "vars", "symbols" -> {
                    // Print current symbol table
                    logger.debug("\n SYMBOL TABLE:");
                    stacks.printSymbolTable();  
                }
                case "b", "breakpoints" -> // List all breakpoints
                        logger.debug("\n BREAKPOINTS: " + breakPoints);
                case "q", "quit", "exit" -> {
                    // Quit debugging session
                    logger.debug(" Debug session ended.");
                    mode = Mode.DISABLED;
                    paused = false;
                    scanner.close();
                    return false; // Signal to stop execution
                }case "h", "help" -> // Print help menu
                        printHelp();
                
                default -> {
                    // Try to parse as a breakpoint command, e.g., "b 10", "+10", or "-10"
                    if (input.startsWith("b ") || input.startsWith("+")) {
                        try {
                            String num = input.startsWith("+") ? input.substring(1) : input.substring(2);
                            int bp = Integer.parseInt(num.trim());
                            addBreakPoint(bp);
                            logger.debug("Breakpoint added at line {}", bp);
                        } catch (NumberFormatException e) {
                            logger.debug(" Invalid line number");
                        }
                    } else if (input.startsWith("-")) {
                        try {
                            int bp = Integer.parseInt(input.substring(1).trim());
                            removeBreakPoint(bp);
                            logger.debug(" Breakpoint removed from line {}", bp);
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
    
    /**
     * Prints the current state of the debugger to the console, including the current line,
     * the node being executed, and the current memory context.
     *
     * @param line   The current line number.
     * @param node   The current {@link AstNode}.
     * @param stacks The current {@link Stacks} state.
     */
    private void printDebugState(int line, AstNode node, Stacks stacks) {
        logger.debug("\n{}", "═".repeat(60));
        logger.debug(" DEBUG PAUSE at line {}", line);
        logger.debug("═".repeat(60));
        logger.debug(" Node: {}", node.getClass().getSimpleName());
        logger.debug(" AST:  {}", truncate(node.toStringTree(), 80));
        logger.debug(" Context: {}", stacks.isInMethodContext() ? stacks.getCurrentContext() : "main");
        logger.debug("═".repeat(60));
    }

    private void printHelp() {
        System.err.println("""
            
            ╔══════════════════════════════════════════════════════════╗
            ║                    DEBUG COMMANDS                        ║
            ╠══════════════════════════════════════════════════════════╣
            ║  s, step      - Execute next node (step into)            ║
            ║  c, continue  - Continue step-by-step                    ║
            ║  n, next      - Run until next breakpoint                ║
            ║  p, print     - Print current stack                      ║
            ║  v, vars      - Print symbol table                       ║
            ║  b, breakpoints - List all breakpoints                   ║
            ║  b <line>     - Add breakpoint at line                   ║
            ║  -<line>      - Remove breakpoint at line                ║
            ║  q, quit      - Stop execution                           ║
            ║  h, help      - Show this help                           ║
            ╚══════════════════════════════════════════════════════════╝
            """);
    }
    
   
    
    /**
     * Truncates a string to a maximum length, appending "..." if it exceeds the limit.
     *
     * @param str    The string to truncate.
     * @param maxLen The maximum allowed length.
     * @return The truncated string.
     */
    private String truncate(String str, int maxLen) {
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    // ==================== LISTENER ====================
    
    /**
     * Sets the listener for debug events.
     *
     * @param listener The {@link DebugListener} to notify of debug events.
     */
    public void setListener(DebugListener listener) {
        this.listener = listener;
    }
    
    /**
     * Gets the current line number (node count) where the debugger is paused.
     *
     * @return The current line number.
     */
    public int getCurrentLine() {
        return currentLine;
    }
    
    /**
     * Checks if the debugger is currently paused.
     *
     * @return {@code true} if paused, {@code false} otherwise.
     */
    public boolean isPaused() {
        return paused;
    }
    
    /**
     * Programmatically resumes execution.
     * This is intended for use by a GUI or other external controller.
     */
    public void resume() {
        paused = false;
        if (listener != null) listener.onResume();
    }
    
    /**
     * Programmatically executes a single step.
     * The debugger will pause again at the very next node.
     * This is intended for use by a GUI or other external controller.
     */
    public void step() {
        stepNext = true;
        paused = false;
        if (listener != null) listener.onResume();
    }
    
    /**
     * Programmatically resumes execution and sets the mode to {@link Mode#STEP_BY_STEP}.
     * The debugger will pause at every subsequent node.
     */
    public void continueStepByStep() {
        mode = Mode.STEP_BY_STEP;
        paused = false;
        if (listener != null) listener.onResume();
    }
    
    /**
     * Programmatically resumes execution and sets the mode to {@link Mode#BREAKPOINTS}.
     * The debugger will continue until it hits the next breakpoint or the program ends.
     */
    public void continueToNextBreakpoint() {
        mode = Mode.BREAKPOINTS;
        paused = false;
        if (listener != null) listener.onResume();
    }
}
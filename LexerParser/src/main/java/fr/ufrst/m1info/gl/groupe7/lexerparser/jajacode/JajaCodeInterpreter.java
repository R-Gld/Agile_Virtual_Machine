package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions.SyntaxException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class JajaCodeInterpreter implements Runnable {
    private final String jajaCode;
    private final DiagnosticCollector collector;

    // === Added fields for step-by-step debugging ===
    private Stacks stacks;
    private JajaCodeParser.ClasseContext arbreJajaCode;
    private JajaCodeInterpreterVisitor interpreteur;
    private boolean initialized = false;
    // === End added fields ===

    public JajaCodeInterpreter(String jajaCode, DiagnosticCollector collector) {
        this.jajaCode = jajaCode;
        this.collector = collector;
    }

    public static void main(String[] args) {
        String jajaCode = """
                    1 init
                    2 push(0)
                    3 new(x@global, INT, VARIABLE, 0)
                    4 push(5)
                    5 store(x@global)
                    6 push(0)
                    7 swap
                    8 pop
                    9 pop
                    10 jcstop
                """;

        new JajaCodeInterpreter(jajaCode, new DiagnosticCollector()).run();
    }

    /**
     * Fonction utiliser par l'interface pour interpreter du jajacode
     */
    @Override
    public void run() {
        // Use the same initialization logic as for step-by-step,
        // but execute the whole program at once.
        ensureInitialized();
        interpreteur.run();
    }

    // === Added methods for level 2 (step-by-step execution) ===

    /**
     * Ensures that the JajaCode program is parsed, loaded and ready to run/step.
     * This method is idempotent and can be safely called multiple times.
     */
    private void ensureInitialized() {
        if (initialized) {
            return;
        }

        stacks = new Stacks();
        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);

        SyntaxErrorListener sel = new SyntaxErrorListener(collector, null);
        sel.register(lexer);
        sel.register(parser);

        arbreJajaCode = parser.classe();

        if (collector.hasErrors()) {
            throw new SyntaxException(collector);
        }

        interpreteur = new JajaCodeInterpreterVisitor(stacks);

        // Loads the JajaCode program into the interpreter internal structures
        interpreteur.load(arbreJajaCode);

        initialized = true;
    }

    /**
     * Resets the interpreter state so that execution can start again from the beginning.
     * This does not change the original JajaCode string.
     */
    public void reset() {
        initialized = false;
        stacks = null;
        arbreJajaCode = null;
        interpreteur = null;
    }

    /**
     * Execute exactly one JajaCode instruction.
     *
     * @return true if there are still instructions to execute after this step,
     *         false if the program has finished (reached halt/jcstop).
     * <p>
     * Note: This method expects that JajaCodeInterpreterVisitor implements
     * a step() method that executes a single instruction and returns whether
     * the program has finished or not.
     */
    public boolean step() {
        ensureInitialized();
        return interpreteur.step();
    }

    /**
     * Indicates whether the current debug session has finished.
     * This is a convenience wrapper around the visitor state.
     */
    public boolean isFinished() {
        if (!initialized) {
            return false;
        }
        // You must implement isFinished() inside JajaCodeInterpreterVisitor
        return interpreteur.isFinished();
    }

    /**
     * Returns the index of the current instruction (0-based) in the
     * internal JajaCode program representation.
     * <p>
     * This can be used by the GUI to highlight the current JajaCode line.
     * <p>
     * Note: You must implement getCurrentInstructionIndex() in
     * JajaCodeInterpreterVisitor.
     */
    public int getCurrentInstructionIndex() {
        if (!initialized) {
            return -1;
        }
        return interpreteur.getCurrentInstructionIndex();
    }

    // === Debug methods for GUI ===

    /**
     * Capture the full memory state (stack and heap) for the UI.
     * To be used during step-by-step execution.
     *
     * @return A complete snapshot of the memory state, or null if not initialized
     */
    public JajaCodeDebug.MemorySnapshot captureMemoryState() {
        ensureInitialized();
        if (stacks == null) {
            return null;
        }
        int pc = interpreteur.getCurrentInstructionIndex();
        String instruction = interpreteur.getCurrentInstructionText();
        return JajaCodeDebug.captureMemoryState(stacks, pc, instruction);
    }

    /**
     * Returns information about a specific variable.
     *
     * @param identifier The variable identifier (e.g., "x@global")
     * @return VariableInfo or null if not found
     */
    public JajaCodeDebug.VariableInfo getVariableInfo(String identifier) {
        if (!initialized || stacks == null) {
            return null;
        }
        return JajaCodeDebug.getVariableInfo(stacks, identifier);
    }

    /**
         * Retrieves the value of a variable.
         *
         * @param identifier The variable identifier (e.g., "x@global")
         * @return The value or null if not found
         */
    public Object getVariableValue(String identifier) {
        if (!initialized || stacks == null) {
            return null;
        }
        return JajaCodeDebug.getVariableValue(stacks, identifier);
    }

   /**
    * Retrieves information about an array, including its elements.
    *
    * @param arrayIdentifier The identifier of the array
    * @return HeapInfo or null if not found
    */
    public JajaCodeDebug.HeapInfo getArrayInfo(String arrayIdentifier) {
        if (!initialized || stacks == null) {
            return null;
        }
        return JajaCodeDebug.getArrayInfo(stacks, arrayIdentifier);
    }

    /**
     * Returns the Stacks instance for direct access if needed.
     *
     * @return The Stacks instance or null if not initialized
     */
    public Stacks getStacks() {
        return stacks;
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParserBaseVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.JajaCodeRuntimeException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node.*;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * JajaCodeInterpreterVisitor is a visitor class responsible for interpreting and executing
 * JajaCode instructions using the visitor pattern. It encapsulates the state of the virtual
 * machine, manages the instruction set (axioms), and executes instructions step by step.
 * <p>
 * This class implements the {@link Runnable} interface, allowing the interpreter to be run
 * in a separate thread. It also provides step-by-step execution for debugging and testing.
 * <p>
 * Main responsibilities:
 * - Initialize the machine context and register all supported instructions (axioms).
 * - Load a JajaCode program into memory, mapping addresses to instructions.
 * - Interpret and execute instructions by dispatching to the appropriate axiom.
 * - Handle errors and provide debugging information via logging.
 * - Support step-by-step execution for testing and debugging purposes.
 * <p>
 * The interpreter supports a wide range of instructions, including memory operations,
 * arithmetic and logic operations, control flow, input/output, and array manipulations.
 * <p>
 * Example usage:
 *     Stacks stacks = new Stacks();
 *     JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
 *     interpreter.load(parsedClasseContext);
 *     interpreter.run();
 *
 * @author groupe-7
 */
public class JajaCodeInterpreterVisitor extends JajaCodeParserBaseVisitor<Object> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JajaCodeInterpreterVisitor.class);

    // The machine state is now encapsulated here
    private final MachineContext context;

    // The command registry (Command Pattern)
    private final Map<JajaCodeInstr, JajaAxiome> axiomes = new HashMap<>();

    // The program (Map address -> instruction)
    private final Map<Integer, JajaCodeParser.InstrContext> programme = new HashMap<>();

    public JajaCodeInterpreterVisitor(Stacks stacks) {
        // 1. Initialize the context
        this.context = new MachineContext(stacks);

        // 2. Register axioms
        initAxiomes();
    }

    private void initAxiomes() {
        // --- Basic instructions ---
        axiomes.put(JajaCodeInstr.INIT, new InitAxiome());
        axiomes.put(JajaCodeInstr.JCSTOP, new JCStopAxiome());

        // --- Memory and Stack ---
        axiomes.put(JajaCodeInstr.PUSH, new PushAxiome());
        axiomes.put(JajaCodeInstr.POP, new PopAxiome());
        axiomes.put(JajaCodeInstr.SWAP, new SwapAxiome());
        axiomes.put(JajaCodeInstr.NEW, new NewAxiome());
        axiomes.put(JajaCodeInstr.STORE, new StoreAxiome());
        axiomes.put(JajaCodeInstr.LOAD, new LoadAxiome());
        axiomes.put(JajaCodeInstr.INVOKE, new InvokeAxiome());
        axiomes.put(JajaCodeInstr.RETURN, new ReturnAxiome());

        // --- Control flow ---
        axiomes.put(JajaCodeInstr.IF, new IfAxiome());
        axiomes.put(JajaCodeInstr.GOTO, new GotoAxiome());

        // --- Arithmetic and Logic ---
        axiomes.put(JajaCodeInstr.ADD, new AddAxiome());
        axiomes.put(JajaCodeInstr.SUB, new SubAxiome());
        axiomes.put(JajaCodeInstr.MUL, new MulAxiome());
        axiomes.put(JajaCodeInstr.DIV, new DivAxiome());
        axiomes.put(JajaCodeInstr.INC, new IncAxiome());
        axiomes.put(JajaCodeInstr.SUP, new SupAxiome());
        axiomes.put(JajaCodeInstr.CMP, new CmpAxiome());
        axiomes.put(JajaCodeInstr.NEG, new NegAxiome());
        axiomes.put(JajaCodeInstr.NOT, new NotAxiome());
        axiomes.put(JajaCodeInstr.OR, new OrAxiome());
        axiomes.put(JajaCodeInstr.AND, new AndAxiome());

        // --- Input / Output ---
        axiomes.put(JajaCodeInstr.WRITE, new WriteAxiome());
        axiomes.put(JajaCodeInstr.WRITELN, new WriteLnAxiome());

        // --- Array operations ---
        axiomes.put(JajaCodeInstr.NEWARRAY, new NewarrayAxiome());
        axiomes.put(JajaCodeInstr.ALOAD, new AloadAxiome());
        axiomes.put(JajaCodeInstr.ASTORE, new AstoreAxiome());
        axiomes.put(JajaCodeInstr.AINC, new AincAxiome());
        axiomes.put(JajaCodeInstr.LENGTH, new LengthAxiome());
    }

    public void load(JajaCodeParser.ClasseContext arbre) {
        new JajaCodeParserBaseVisitor<Void>() {
            @Override
            public Void visitClasse(JajaCodeParser.ClasseContext ctx) {
                if (ctx.instr() != null) {
                    int adresse = Integer.parseInt(ctx.adresse().getText());
                    programme.put(adresse, ctx.instr());
                    visit(ctx.classe());
                }
                return null;
            }
        }.visit(arbre);
    }

    @Override
    public void run() {
        // Reset the context
        context.setInstructionCounter(1);

        logger.debug("Interpreter: Execution started at address 1.");

        while (context.isRunning()) {
            int pc = context.getInstructionCounter();
            JajaCodeParser.InstrContext instruction = programme.get(pc);

            if (instruction == null) {
                logger.error("Error: instruction at @{} not found!", pc);
                context.stop();
                break;
            }
            logger.debug("PC: {} -> {}", pc, instruction.getText());

            // The visitor will dispatch to the correct axiom
            visit(instruction);
        }
        logger.info("--- Execution Finished ---");
        context.getStacks().printStack();
        context.getStacks().printSymbolTable();
    }

    @Override
    public Object visitInstr(JajaCodeParser.InstrContext ctx) {
        // 1. Instructions without arguments
        if (ctx.INIT() != null) dispatch(JajaCodeInstr.INIT);
        else if (ctx.SWAP() != null) dispatch(JajaCodeInstr.SWAP);
        else if (ctx.POP() != null) dispatch(JajaCodeInstr.POP);
        else if (ctx.JCSTOP() != null) dispatch(JajaCodeInstr.JCSTOP);
        else if (ctx.WRITE() != null) dispatch(JajaCodeInstr.WRITE);
        else if (ctx.WRITELN() != null) dispatch(JajaCodeInstr.WRITELN);
        else if (ctx.RETURN() != null) dispatch(JajaCodeInstr.RETURN);

        // 2. Instructions with VALUE
        else if (ctx.PUSH() != null) {
            // Extract raw text ("5", "true", "x")
            String valText = ctx.valeur().getText();
            // Special case for quoted strings if needed
            if (ctx.valeur().STRING() != null) {
                valText = ctx.valeur().STRING().getText().replace("\"", "");
            }
            dispatch(JajaCodeInstr.PUSH, valText);
        }

        // 3. Instructions with IDENTIFIER
        else if (ctx.ident() != null) {
            String ident = ctx.ident().getText();
            if (ctx.STORE() != null) dispatch(JajaCodeInstr.STORE, ident);
            else if (ctx.LOAD() != null) dispatch(JajaCodeInstr.LOAD, ident);
            else if (ctx.INC() != null) dispatch(JajaCodeInstr.INC, ident);
            else if (ctx.INVOKE() != null) dispatch(JajaCodeInstr.INVOKE, ident);
                // NEW is special (4 args)
            else if (ctx.NEW() != null) handleNew(ctx);
                // Array operations
            else if (ctx.ALOAD() != null) dispatch(JajaCodeInstr.ALOAD, ident);
            else if (ctx.ASTORE() != null) dispatch(JajaCodeInstr.ASTORE, ident);
            else if (ctx.AINC() != null) dispatch(JajaCodeInstr.AINC, ident);
            else if (ctx.LENGTH() != null) dispatch(JajaCodeInstr.LENGTH, ident);
                // NEWARRAY is special (2 args: ident, type)
            else if (ctx.NEWARRAY() != null) handleNewarray(ctx);
        }

        // 4. Instructions with ADDRESS
        else if (ctx.adresse() != null) {
            String addr = ctx.adresse().getText();
            if (ctx.IF() != null) dispatch(JajaCodeInstr.IF, addr);
            else if (ctx.GOTO() != null) dispatch(JajaCodeInstr.GOTO, addr);
        }

        // 5. Operations (delegated to sub-visitors)
        else if (ctx.oper() != null) {
            visitChildren(ctx);
        }

        return null;
    }

    private void dispatch(JajaCodeInstr command) {
        dispatch(command, null);
    }

    // Helper to execute an axiom
    private void dispatch(JajaCodeInstr command, String arg) {
        JajaAxiome axiome = axiomes.get(command);
        if (axiome != null) {
            try {
                axiome.execute(context, arg);
            } catch (JajaCodeRuntimeException e) {
                logger.error("\nJAJACODE EXECUTION ERROR:");
                logger.error(e.getMessage());
                logger.error("");

                context.stop();

                throw e;
            }
        } else {
            logger.error("Axiom not implemented: {}", command);
            context.incrementPC(); // To avoid infinite loop
        }
    }

    // Specific handling for NEW (argument concatenation)
    private void handleNew(JajaCodeParser.InstrContext ctx) {
        String ident = ctx.ident().getText();
        String type = (ctx.TYPE() != null) ? ctx.TYPE().getText() : "void";
        String sorte = (ctx.SORTE() != null) ? ctx.SORTE().getText() : "var";
        String depth = (ctx.adresse() != null) ? ctx.adresse().getText() : "0";

        logger.debug("\t\t[DEBUG handleNew] ident={}, type={}, sorte={}, depth={}", ident, type, sorte, depth);

        // Pack arguments for generic interface (ident,type,sorte,depth)
        String packedArgs = ident + "," + type + "," + sorte + "," + depth;
        dispatch(JajaCodeInstr.NEW, packedArgs);
    }

    // Specific handling for NEWARRAY (argument concatenation: ident,type)
    private void handleNewarray(JajaCodeParser.InstrContext ctx) {
        String ident = ctx.ident().getText();
        String type = (ctx.TYPE() != null) ? ctx.TYPE().getText() : "int";

        logger.debug("\t\t[DEBUG handleNewarray] ident={}, type={}", ident, type);

        // Pack arguments for the generic interface (ident,type)
        String packedArgs = ident + "," + type;
        dispatch(JajaCodeInstr.NEWARRAY, packedArgs);
    }

    @Override
    public Object visitOper2(JajaCodeParser.Oper2Context ctx) {
        if (ctx.ADD() != null) dispatch(JajaCodeInstr.ADD);
        else if (ctx.SUB() != null) dispatch(JajaCodeInstr.SUB);
        else if (ctx.MUL() != null) dispatch(JajaCodeInstr.MUL);
        else if (ctx.DIV() != null) dispatch(JajaCodeInstr.DIV);
        else if (ctx.AND() != null) dispatch(JajaCodeInstr.AND);
        else if (ctx.OR() != null) dispatch(JajaCodeInstr.OR);
        else if (ctx.SUP() != null) dispatch(JajaCodeInstr.SUP);
        else if (ctx.CMP() != null) dispatch(JajaCodeInstr.CMP);
        return null;
    }

    @Override
    public Object visitOper1(JajaCodeParser.Oper1Context ctx) {
        if (ctx.NEG() != null) dispatch(JajaCodeInstr.NEG);
        else if (ctx.NOT() != null) dispatch(JajaCodeInstr.NOT);
        return null;
    }

    // for test and debug

    private boolean stepInitialized = false;

    public boolean step() {
        // Initialize PC to 1 on first step (same as run())
        if (!stepInitialized) {
            context.setInstructionCounter(1);
            stepInitialized = true;
            logger.debug("Step mode: Execution started at address 1.");
        }

        int pc = context.getInstructionCounter();
        JajaCodeParser.InstrContext instruction = programme.get(pc);

        if (instruction == null) {
            logger.error("Error: @{} not found!", pc);
            context.stop();
            return false; // Program finished (error case)
        }

        visit(instruction);

        // Return true if there are more instructions, false if finished
        return context.isRunning();
    }

    /**
     * Resets the step execution state for a new debug session.
     */
    public void resetStep() {
        stepInitialized = false;
        context.setInstructionCounter(1);
    }

    public boolean isFinished() {
        return !context.isRunning();
    }

    public int getCurrentInstructionIndex() {
        return context.getInstructionCounter();
    }

    /**
     * Returns the text of the current instruction for debugging.
     *
     * @return The instruction text or "N/A" if not found
     */
    public String getCurrentInstructionText() {
        int pc = context.getInstructionCounter();
        JajaCodeParser.InstrContext instruction = programme.get(pc);
        if (instruction != null) {
            return instruction.getText();
        }
        return "N/A";
    }

}
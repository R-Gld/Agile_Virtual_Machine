package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome representing the JajaCode instruction {@code and}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ and –» &lt;&lt;w, v1 ∧ v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>This instruction pops two boolean values {@code v1} and {@code v2},
 * performs the logical AND {@code v1 && v2}, then pushes the result.</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack contains fewer than 2 elements</li>
 *   <li>{@link TypeMismatchException} if the operands are not booleans</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class AndAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(AndAxiome.class);

    /**
     * Executes the {@code and} instruction.
     *
     * @param ctx the virtual machine context holding the execution state
     * @param arg unused parameter for this instruction
     * @throws StackUnderflowException if the stack contains fewer than 2 elements
     * @throws TypeMismatchException if the operands are not booleans
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Pop operands
        // op2 is at the top, op1 is below it
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Check stack underflow
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", JajaCodeInstr.AND.toString(), ctx.getInstructionCounter());
        }

        // 3. Type check (booleans only)
        if (!(op1.value instanceof Boolean) || !(op2.value instanceof Boolean)) {
            throw new TypeMismatchException(
                "Tentative d'opération sur des types non booléens (" + op1.value + " && " + op2.value + ")",
                JajaCodeInstr.AND.toString(),
                ctx.getInstructionCounter()
            );
        }

        // 4. Compute (AND logical operation)
        boolean result = (Boolean) op1.value && (Boolean) op2.value;

        // 5. Push the result
        ctx.getStacks().push(new Stacks.Quad(result, Type.BOOLEEN));

        // 6. Log and advance to next instruction
        logger.debug("\t\tAxiome AND exécuté: {} && {} = {}", op1.value, op2.value, result);
        ctx.incrementPC();
    }
}

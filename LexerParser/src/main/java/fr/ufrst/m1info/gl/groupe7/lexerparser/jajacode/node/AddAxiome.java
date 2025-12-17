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
 * Axiome representing the JajaCode instruction {@code add}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ add –» &lt;&lt;w, v1 + v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>This instruction pops two integer values {@code v1} and {@code v2},
 * performs the addition {@code v1 + v2}, then pushes the result.</p>
 *
 * <p><b>Operand order:</b> The penultimate element of the stack ({@code v1}) is added
 * to the top element ({@code v2}).</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack contains fewer than 2 elements</li>
 *   <li>{@link TypeMismatchException} if the operands are not integers</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class AddAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(AddAxiome.class);

    /**
     * Executes the {@code add} instruction.
     *
     * @param ctx the virtual machine context holding the execution state
     * @param arg unused parameter for this instruction
     * @throws StackUnderflowException if the stack contains fewer than 2 elements
     * @throws TypeMismatchException if the operands are not integers
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Pop operands
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Check stack underflow
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", JajaCodeInstr.ADD.toString(), ctx.getInstructionCounter());
        }

        // 3. Type check (integers only)
        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Opérandes non entiers (" + op1.value + " + " + op2.value + ")",
                    JajaCodeInstr.ADD.toString(),
                ctx.getInstructionCounter()
            );
        }

        // 4. Compute
        int result = (Integer) op1.value + (Integer) op2.value;

        // 5. Push the result
        ctx.getStacks().push(new Stacks.Quad(result, Type.ENTIER));

        // 6. Log and continue
        logger.debug("\t\tAxiome ADD exécuté: {} + {} = {}", op1.value, op2.value, result);
        ctx.incrementPC();
    }
}

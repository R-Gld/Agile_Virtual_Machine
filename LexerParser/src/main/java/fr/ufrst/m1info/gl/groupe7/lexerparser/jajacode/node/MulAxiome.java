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
 * Axiome representing the JajaCode instruction {@code mul}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ mul –» &lt;&lt;w, v1 * v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>This instruction pops two integer values {@code v1} and {@code v2},
 * performs the multiplication {@code v1 * v2}, and then pushes the result.</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack contains fewer than 2 elements</li>
 *   <li>{@link TypeMismatchException} if the operands are not integers</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class MulAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(MulAxiome.class);

    /**
     * Exécute l'instruction {@code mul}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile contient moins de 2 éléments
     * @throws TypeMismatchException si les opérandes ne sont pas de type entier
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Pop
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Check for stack underflow
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Requires 2 operands", "MUL", ctx.getInstructionCounter());
        }

        // 3. Type check (integers only)
        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Non-integer operands (" + op1.value + " * " + op2.value + ")",
                JajaCodeInstr.MUL.toString(),
                ctx.getInstructionCounter()
            );
        }

        // 5. Compute
        int result = (Integer) op1.value * (Integer) op2.value;

        // 6. Push the result
        ctx.getStacks().push(new Stacks.Quad(result, Type.ENTIER));

        // 7. Log and continue
        logger.debug("\t\tAxiome MUL executed: {} * {} = {}", op1.value, op2.value, result);
        ctx.incrementPC();
    }
}

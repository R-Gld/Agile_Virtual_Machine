package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.DivisionByZeroException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome representing the JajaCode instruction {@code div}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ div –» &lt;&lt;w, v1 / v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>This instruction pops two integer values {@code v1} and {@code v2},
 * performs the integer division {@code v1 / v2}, and then pushes the result.</p>
 *
 * <p><b>Operand order:</b> The penultimate element on the stack ({@code v1}) is the dividend,
 * the last element ({@code v2}) is the divisor.</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack contains fewer than 2 elements</li>
 *   <li>{@link TypeMismatchException} if the operands are not integers</li>
 *   <li>{@link DivisionByZeroException} if the divisor equals zero</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class DivAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(DivAxiome.class);

    /**
     * Exécute l'instruction {@code div}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile contient moins de 2 éléments
     * @throws TypeMismatchException si les opérandes ne sont pas de type entier
     * @throws DivisionByZeroException si le diviseur est égal à zéro
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Pop (op2 is the divisor, op1 is the dividend)
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Check for stack underflow
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", "DIV", ctx.getInstructionCounter());
        }

        // 3. Type check (integers only)
        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Opérandes non entiers (" + op1.value + " / " + op2.value + ")",
                JajaCodeInstr.DIV.toString(),
                ctx.getInstructionCounter()
            );
        }

        // 4. Division by zero check (specific to DIV)
        if ((Integer) op2.value == 0) {
            throw new DivisionByZeroException("DIV", ctx.getInstructionCounter());
        }

        // 5. Compute
        int result = (Integer) op1.value / (Integer) op2.value;

        // 6. Push the result
        ctx.getStacks().push(new Stacks.Quad(result, Type.ENTIER));

        // 7. Log and continue
        logger.debug("\t\tAxiome DIV executed: {} / {} = {}", op1.value, op2.value, result);
        ctx.incrementPC();
    }
}

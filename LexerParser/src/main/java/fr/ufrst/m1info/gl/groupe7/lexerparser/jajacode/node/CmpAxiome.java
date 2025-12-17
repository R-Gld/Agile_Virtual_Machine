package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import java.util.Objects;

/**
 * Axiome representing the JajaCode instruction {@code cmp}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ cmp –» &lt;&lt;w, v1 == v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>This instruction pops two values {@code v1} and {@code v2},
 * compares their equality {@code v1 == v2}, and then pushes the boolean result.</p>
 *
 * <p><b>Note:</b> This implementation uses {@link Objects#equals}
 * to support comparison across different value types (integers, booleans, etc.).</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack contains fewer than 2 elements</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class CmpAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(CmpAxiome.class);

    /**
     * Exécute l'instruction {@code cmp}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile contient moins de 2 éléments
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Pop (Note: op2 is the top, op1 is below)
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Check for empty stack
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", JajaCodeInstr.CMP.toString(), ctx.getInstructionCounter());
        }

        // 3. Compute equality
        boolean result = Objects.equals(op1.value, op2.value);

        // 4. Push the result
        ctx.getStacks().push(new Stacks.Quad(result, Type.BOOLEEN));

        // 5. Log and increment PC
        logger.debug("\t\tAxiome {} exécuté: {} == {} = {}", JajaCodeInstr.CMP, op1.value, op2.value, result);
        ctx.incrementPC();
    }
}

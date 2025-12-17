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
 * Axiome representing the JajaCode instruction {@code not}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [op1] : &lt;&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ not –» &lt;&lt;w, ¬v1, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>This instruction pops a boolean value {@code v1},
 * performs logical negation {@code !v1}, and then pushes the result.</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack is empty</li>
 *   <li>{@link TypeMismatchException} if the operand is not boolean</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class NotAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(NotAxiome.class);

    /**
     * Exécute l'instruction {@code not}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile est vide
     * @throws TypeMismatchException si l'opérande n'est pas de type booléen
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Pop the single operand
        Stacks.Quad op = ctx.getStacks().pop();

        // 2. Check for empty stack
        if (op == null) {
            throw new StackUnderflowException(JajaCodeInstr.NOT.toString(), ctx.getInstructionCounter());
        }

        // 3. Type check (boolean only)
        if (!(op.value instanceof Boolean)) {
            throw new TypeMismatchException(
                "Operand must be boolean (found: " + op.value + ")",
                JajaCodeInstr.NOT.toString(),
                ctx.getInstructionCounter()
            );
        }

        // 4. Compute (Negation)
        boolean result = !(Boolean) op.value;

        // 5. Push the result
        ctx.getStacks().push(new Stacks.Quad(result, Type.BOOLEEN));

        // 6. Log and continue
        logger.debug("\t\tAxiome NOT executed: !{} = {}", op.value, result);
        ctx.incrementPC();
    }
}

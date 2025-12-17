package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Axiome representing the JajaCode instruction {@code write}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [write] : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ write –» &lt;Afficher(v,m),a+1&gt;
 * </pre>
 *
 * <p>This instruction pops a value {@code v} and prints it to standard output
 * without a newline.</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack is empty</li>
 * </ul>
 *
 * @see JajaAxiome
 * @see WriteLnAxiome
 */
public class WriteAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(WriteAxiome.class);

    /**
     * Exécute l'instruction {@code write}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile est vide
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        Stacks.Quad valeur = ctx.getStacks().pop();

        if (valeur == null) {
            throw new StackUnderflowException("Missing value to write", "WRITE", ctx.getInstructionCounter());
        }

        logger.info("{}", valeur.value);
        logger.debug("\t\tAxiome WRITE executed: {} printed.", valeur.value);
        ctx.incrementPC();
    }
}

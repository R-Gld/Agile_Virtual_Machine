package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Axiome representing the JajaCode instruction {@code writeln}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [writeln] : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ writeln –» &lt;AfficherLn(v,m),a+1&gt;
 * </pre>
 *
 * <p>This instruction pops a value {@code v} and prints it to standard output
 * followed by a newline.</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack is empty</li>
 * </ul>
 *
 * @see JajaAxiome
 * @see WriteAxiome
 */
public class WriteLnAxiome implements JajaAxiome{

    private static final Logger logger = LoggerFactory.getLogger(WriteLnAxiome.class);

    /**
     * Exécute l'instruction {@code writeln}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile est vide
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        Stacks.Quad valeur = ctx.getStacks().pop();
        if (valeur == null) {
            throw new StackUnderflowException("Missing value to write", "WRITELN", ctx.getInstructionCounter());
        }
        logger.info("{}\n", valeur.value);
        logger.debug("\t\tAxiome WRITELN executed: {} printed with newline.", valeur.value);
        ctx.incrementPC();
    }
}

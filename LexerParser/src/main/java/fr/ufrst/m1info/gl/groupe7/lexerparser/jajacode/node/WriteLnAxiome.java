package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Axiome représentant l'instruction JajaCode {@code writeln}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [writeln] : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ writeln –» &lt;AfficherLn(v,m),a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile une valeur {@code v} et l'affiche sur la sortie standard
 * avec un retour à la ligne.</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide</li>
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
            throw new StackUnderflowException("Manque la valeur à écrire", "WRITELN", ctx.getInstructionCounter());
        }
        logger.info("{}", valeur.value);
        logger.debug("\t\tAxiome WRITELN exécuté: {} affiché avec retour à la ligne.", valeur.value);
        ctx.incrementPC();
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import java.util.Objects;

/**
 * Axiome représentant l'instruction JajaCode {@code cmp}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ cmp –» &lt;&lt;w, v1 == v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile deux valeurs {@code v1} et {@code v2},
 * compare leur égalité {@code v1 == v2}, puis empile le résultat booléen.</p>
 *
 * <p><b>Note :</b> Cette implémentation utilise {@link Objects#equals} pour
 * supporter la comparaison de différents types de valeurs (entiers, booléens, etc.).</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile contient moins de 2 éléments</li>
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
        // 1. Dépilement (Attention : op2 est le sommet, op1 est en dessous)
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Vérification pile vide
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", "CMP", ctx.getInstructionCounter());
        }

        // 3. Calcul de l'égalité
        boolean result = Objects.equals(op1.value, op2.value);

        // 4. Empilement du résultat
        ctx.getStacks().push(new Stacks.Quad(
                ctx.getTEMP_VALUE(), // Identifiant temporaire
                result,              // Valeur calculée (true/false)
                ctx.getTEMP_VALUE(), // Sorte temporaire
                Type.BOOLEEN         // Type explicite
        ));

        // 5. Log et incrément PC
        logger.debug("\t\tAxiome CMP exécuté: {} == {} = {}", op1.value, op2.value, result);
        ctx.incrementPC();
    }
}

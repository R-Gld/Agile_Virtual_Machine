package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code sub}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ sub –» &lt;&lt;w, v1 - v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile deux valeurs entières {@code v1} et {@code v2},
 * effectue la soustraction {@code v1 - v2}, puis empile le résultat.</p>
 *
 * <p><b>Ordre des opérandes :</b> L'avant-dernier élément de la pile ({@code v1}) est le minuende,
 * le dernier élément ({@code v2}) est le soustracteur.</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile contient moins de 2 éléments</li>
 *   <li>{@link TypeMismatchException} si les opérandes ne sont pas de type entier</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class SubAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(SubAxiome.class);

    /**
     * Exécute l'instruction {@code sub}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile contient moins de 2 éléments
     * @throws TypeMismatchException si les opérandes ne sont pas de type entier
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Dépilement
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Vérification pile vide
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", "SUB", ctx.getInstructionCounter());
        }

        // 3. Vérification de type (Entiers uniquement)
        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Opérandes non entiers (" + op1.value + " - " + op2.value + ")",
                "SUB",
                ctx.getInstructionCounter()
            );
        }

        // 5. Calcul
        int result = (Integer) op1.value - (Integer) op2.value;

        // 6. Empilement du résultat
        ctx.getStacks().push(new Stacks.Quad(result, Type.ENTIER));

        // 7. Log et suite
        logger.debug("\t\tAxiome SUB exécuté: {} - {} = {}", op1.value, op2.value, result);
        ctx.incrementPC();
    }
}

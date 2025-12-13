package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code sup}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ sup –» &lt;&lt;w, v1 &gt; v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile deux valeurs entières {@code v1} et {@code v2},
 * compare si {@code v1 > v2}, puis empile le résultat booléen.</p>
 *
 * <p><b>Ordre des opérandes :</b> L'avant-dernier élément de la pile ({@code v1})
 * est comparé avec le dernier élément ({@code v2}).</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile contient moins de 2 éléments</li>
 *   <li>{@link TypeMismatchException} si les opérandes ne sont pas de type entier</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class SupAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(SupAxiome.class);

    /**
     * Exécute l'instruction {@code sup}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile contient moins de 2 éléments
     * @throws TypeMismatchException si les opérandes ne sont pas de type entier
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Dépiler les deux opérandes
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Vérification de la pile vide
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", "SUP", ctx.getInstructionCounter());
        }

        // 3. Vérification de type (Comparaison d'entiers uniquement)
        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Tentative de comparer des valeurs non entières (" + op1.value + " > " + op2.value + ")",
                "SUP",
                ctx.getInstructionCounter()
            );
        }

        // 4. Calcul de la comparaison
        boolean result = (Integer) op1.value > (Integer) op2.value;

        // 5. Empiler le résultat (Type BOOLEEN)
        ctx.getStacks().push(new Stacks.Quad(result, Type.BOOLEEN));

        // 6. Log et incrément du PC
        logger.debug("\t\tAxiome SUP exécuté: {} > {} = {}", op1.value, op2.value, result);
        ctx.incrementPC();
    }
}

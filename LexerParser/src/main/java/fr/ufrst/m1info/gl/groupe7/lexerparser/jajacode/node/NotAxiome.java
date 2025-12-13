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
 * Axiome représentant l'instruction JajaCode {@code not}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [op1] : &lt;&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ not –» &lt;&lt;w, ¬v1, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile une valeur booléenne {@code v1},
 * effectue la négation logique {@code !v1}, puis empile le résultat.</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide</li>
 *   <li>{@link TypeMismatchException} si l'opérande n'est pas de type booléen</li>
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
        // 1. Dépilement de l'unique opérande
        Stacks.Quad op = ctx.getStacks().pop();

        // 2. Vérification pile vide
        if (op == null) {
            throw new StackUnderflowException(JajaCodeInstr.NOT.toString(), ctx.getInstructionCounter());
        }

        // 3. Vérification de type (Booléen uniquement)
        if (!(op.value instanceof Boolean)) {
            throw new TypeMismatchException(
                "L'opérande doit être booléen (trouvé: " + op.value + ")",
                JajaCodeInstr.NOT.toString(),
                ctx.getInstructionCounter()
            );
        }

        // 4. Calcul (Négation)
        boolean result = !(Boolean) op.value;

        // 5. Empilement du résultat
        ctx.getStacks().push(new Stacks.Quad(result, Type.BOOLEEN));

        // 6. Log et suite
        logger.debug("\t\tAxiome NOT exécuté: !{} = {}", op.value, result);
        ctx.incrementPC();
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Axiome représentant l'instruction JajaCode {@code ainc(i)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [ainc] : &lt;&lt;w, v, cst,*&gt;.&lt;w, ind, cst,*&gt;.m,a&gt; ⊢ ainc(i) –»
 *          &lt;AffecterValT(i,ind,ValT(i,ind,m)+v,m), a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile contient moins de 2 éléments</li>
 *   <li>{@link UndefinedSymbolException} si le tableau n'existe pas</li>
 *   <li>{@link TypeMismatchException} si les valeurs ne sont pas des entiers</li>
 *   <li>{@link RuntimeException} si l'indice est hors bornes</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class AincAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(AincAxiome.class);

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Dépiler la VALEUR d'incrémentation (sommet de pile)
        Stacks.Quad incrementQuad = ctx.getStacks().pop();
        if (incrementQuad == null) {
            throw new StackUnderflowException("Manque la valeur d'incrémentation pour ainc",
                                               "AINC", ctx.getInstructionCounter());
        }

        // 2. Dépiler l'INDICE (second élément)
        Stacks.Quad indexQuad = ctx.getStacks().pop();
        if (indexQuad == null) {
            throw new StackUnderflowException("Manque l'indice pour ainc",
                                               "AINC", ctx.getInstructionCounter());
        }

        // 3. Vérifier que l'indice est un entier
        if (!(indexQuad.value instanceof Integer index)) {
            throw new TypeMismatchException("Indice de tableau invalide (attendu entier, reçu " +
                                             indexQuad.value.getClass().getSimpleName() + ")",
                                             "AINC", ctx.getInstructionCounter());
        }

        // 4. Vérifier que l'incrément est un entier
        if (!(incrementQuad.value instanceof Integer increment)) {
            throw new TypeMismatchException("Valeur d'incrémentation invalide (attendu entier, reçu " +
                                             incrementQuad.value.getClass().getSimpleName() + ")",
                                             "AINC", ctx.getInstructionCounter());
        }

        logger.debug("\t\t[DEBUG] Incrément dépilé: {}", increment);
        logger.debug("\t\t[DEBUG] Indice dépilé: {}", index);

        // 5. Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        // 6. Vérifier que le tableau existe
        if (ctx.getStacks().findQuad(scopedIdent) == null) {
            throw new UndefinedSymbolException(scopedIdent, JajaCodeInstr.AINC.toString(), ctx.getInstructionCounter());
        }

        // 7. Charger la valeur actuelle du tableau
        Object currentValue = ctx.getStacks().getArrayValue(scopedIdent, index);

        // 8. Vérifier que la valeur actuelle est un entier
        if (!(currentValue instanceof Integer currentInt)) {
            throw new TypeMismatchException("Tentative d'incrémenter une valeur non entière dans " +
                                             scopedIdent + "[" + index + "]",
                                             "AINC", ctx.getInstructionCounter());
        }

        // 9. Calculer la nouvelle valeur
        int newValue = currentInt + increment;

        // 10. Stocker la nouvelle valeur
        ctx.getStacks().setArrayValue(scopedIdent, index, newValue);

        logger.debug("\t\tAxiome AINC exécuté: {}[{}] += {} -> {}", scopedIdent, index, increment, newValue);

        // 11. Incrémenter PC
        ctx.incrementPC();
    }

    /**
     * Résout le nom scopé pour la récursivité.
     */
    private String resolveScopedName(MachineContext ctx, String ident) {
        if (ident.endsWith("@global") || !ident.contains("@")) {
            return ident;
        }

        String currentContext = ctx.getStacks().getCurrentContext();
        if (currentContext != null) {
            String methodName = ctx.getStacks().getCurrentMethodName();
            int recursionLevel = ctx.getStacks().getRecursionDepth(methodName);

            if (recursionLevel > 1) {
                String scopedIdent = ident + "$" + (recursionLevel - 1);
                if (ctx.getStacks().findQuad(scopedIdent) != null) {
                    return scopedIdent;
                }
            }
        }

        return ident;
    }
}

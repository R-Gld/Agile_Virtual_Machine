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
 * Axiome représentant l'instruction JajaCode {@code astore(i)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [astore] : &lt;&lt;w, v, cst,*&gt;.&lt;w, ind, cst,*&gt;.m,a&gt; ⊢ astore(i) –» &lt;AffecterValT(i, ind, v,m), a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile contient moins de 2 éléments</li>
 *   <li>{@link UndefinedSymbolException} si le tableau n'existe pas</li>
 *   <li>{@link TypeMismatchException} si l'indice n'est pas un entier</li>
 *   <li>{@link RuntimeException} si l'indice est hors bornes (via Stacks.setArrayValue)</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class AstoreAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(AstoreAxiome.class);

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Dépiler la VALEUR (sommet de pile)
        Stacks.Quad valueQuad = ctx.getStacks().pop();
        if (valueQuad == null) {
            throw new StackUnderflowException("Manque la valeur pour astore",
                    JajaCodeInstr.ASTORE.toString(), ctx.getInstructionCounter());
        }

        // 2. Dépiler l'INDICE (second élément)
        Stacks.Quad indexQuad = ctx.getStacks().pop();
        if (indexQuad == null) {
            throw new StackUnderflowException("Manque l'indice pour astore",
                    JajaCodeInstr.ASTORE.toString(), ctx.getInstructionCounter());
        }

        // 3. Vérifier que l'indice est un entier
        if (!(indexQuad.value instanceof Integer index)) {
            throw new TypeMismatchException("Indice de tableau invalide (attendu entier, reçu " +
                                             indexQuad.value.getClass().getSimpleName() + ")",
                    JajaCodeInstr.ASTORE.toString(), ctx.getInstructionCounter());
        }

        Object valeur = valueQuad.value;

        logger.debug("\t\t[DEBUG] Valeur dépilée: {}", valeur);
        logger.debug("\t\t[DEBUG] Indice dépilé: {}", index);

        // 4. Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        // 5. Vérifier que le tableau existe
        if (ctx.getStacks().findQuad(scopedIdent) == null) {
            throw new UndefinedSymbolException(scopedIdent, JajaCodeInstr.ASTORE.toString(), ctx.getInstructionCounter());
        }

        // 6. Affecter la valeur au tableau (gère bornes et types en interne)
        ctx.getStacks().setArrayValue(scopedIdent, index, valeur);

        logger.debug("\t\tAxiome ASTORE exécuté: {}[{}] = {}", scopedIdent, index, valeur);

        // 7. Incrémenter PC
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

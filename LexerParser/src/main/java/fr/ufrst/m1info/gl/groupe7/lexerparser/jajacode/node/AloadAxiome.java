package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code aload(i)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [aload] : &lt;&lt;w, ind, cst,*&gt;.m,a&gt; ⊢ aload(i) –» &lt;&lt;w, ValT(i, ind, m), cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide</li>
 *   <li>{@link UndefinedSymbolException} si le tableau n'existe pas</li>
 *   <li>{@link TypeMismatchException} si l'indice n'est pas un entier</li>
 *   <li>{@link RuntimeException} si l'indice est hors bornes (via Stacks.getArrayValue)</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class AloadAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(AloadAxiome.class);

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Dépiler l'indice depuis la pile
        Stacks.Quad indexQuad = ctx.getStacks().pop();
        if (indexQuad == null) {
            throw new StackUnderflowException("Manque l'indice pour aload",
                                               "ALOAD", ctx.getInstructionCounter());
        }

        // 2. Vérifier que l'indice est un entier
        if (!(indexQuad.value instanceof Integer index)) {
            throw new TypeMismatchException("Indice de tableau invalide (attendu entier, reçu " +
                                             indexQuad.value.getClass().getSimpleName() + ")",
                                             "ALOAD", ctx.getInstructionCounter());
        }

        logger.debug("\t\t[DEBUG] Indice dépilé: {}", index);

        // 3. Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        // 4. Vérifier que le tableau existe
        if (!ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
            throw new UndefinedSymbolException(scopedIdent, "ALOAD", ctx.getInstructionCounter());
        }

        // 5. Charger la valeur depuis le tableau (gère les bornes en interne)
        Object valeur = ctx.getStacks().getArrayValue(scopedIdent, index);

        // 6. Récupérer le type de l'élément (même type que le tableau)
        Type type = ctx.getStacks().getDataType(scopedIdent);

        // 7. Empiler la valeur chargée
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), valeur,
                                              ctx.getTEMP_VALUE(), type));

        logger.debug("\t\tAxiome ALOAD exécuté: {}[{}] = {} chargé sur la pile.", scopedIdent, index, valeur);

        // 8. Incrémenter PC
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
                if (ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
                    return scopedIdent;
                }
            }
        }

        return ident;
    }
}

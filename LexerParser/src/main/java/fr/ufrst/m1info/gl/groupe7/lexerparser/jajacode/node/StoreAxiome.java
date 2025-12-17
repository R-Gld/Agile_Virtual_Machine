package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.AssignmentException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Axiome représentant l'instruction JajaCode {@code store(i)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [store] : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ store(i) –» &lt;AffecterVal(i,v,m), a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile une valeur {@code v} du sommet de la pile et l'affecte
 * à la variable identifiée par {@code i} dans la mémoire.</p>
 *
 * <p><b>Gestion du scopage :</b> Cette implémentation supporte la récursivité
 * en résolvant les noms de variables scopées (suffixe {@code $N} pour les appels récursifs).</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide</li>
 *   <li>{@link AssignmentException} si l'affectation échoue</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class StoreAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(StoreAxiome.class);

    /**
     * Exécute l'instruction {@code store(i)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param ident l'identifiant de la variable à modifier
     * @throws StackUnderflowException si la pile est vide
     * @throws AssignmentException si l'affectation échoue
     */
    @Override
    public void execute(MachineContext ctx, String ident) {
        Stacks.Quad valeur = ctx.getStacks().pop();

        if (valeur == null) {
            throw new StackUnderflowException(JajaCodeInstr.STORE.toString(), ctx.getInstructionCounter());
        }

        // Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        boolean success = ctx.getStacks().affecterValJJC(scopedIdent, valeur.value);

        if (!success) {
            throw new AssignmentException(scopedIdent, "impossible d'affecter la valeur", JajaCodeInstr.STORE.toString(), ctx.getInstructionCounter());
        }

        logger.debug("\t\tAxiome STORE exécuté: {} mis à jour avec valeur {}.", scopedIdent, valeur.value);
        ctx.incrementPC();
    }

    /**
     * Résout le nom scopé pour la récursivité.
     * Cherche d'abord la variable avec le suffixe de récursivité, puis sans.
     */
    private String resolveScopedName(MachineContext ctx, String ident) {
        // Si la variable est globale, pas de scopage
        if (ident.endsWith("@global") || !ident.contains("@")) {
            return ident;
        }

        // Vérifier si on est dans un contexte de méthode récursif
        String currentContext = ctx.getStacks().getCurrentContext();
        if (currentContext != null) {
            String methodName = ctx.getStacks().getCurrentMethodName();
            int recursionLevel = ctx.getStacks().getRecursionDepth(methodName);

            // Si on est en récursivité (niveau > 1), chercher la variable scopée
            if (recursionLevel > 1) {
                String scopedIdent = ident + "$" + (recursionLevel - 1);
                if (ctx.getStacks().findQuad(scopedIdent) != null) {
                    return scopedIdent;
                }
            }
        }

        // Sinon, retourner le nom original
        return ident;
    }
}

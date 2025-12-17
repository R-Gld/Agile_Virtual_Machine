package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code load(i)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [load] : &lt;m,a&gt; ⊢ load(i) –» &lt;&lt;w, Val(i,m), cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction charge la valeur de la variable identifiée par {@code i}
 * depuis la mémoire et l'empile sur la pile d'exécution.</p>
 *
 * <p><b>Gestion du scopage :</b> Cette implémentation supporte la récursivité
 * en résolvant les noms de variables scopées (suffixe {@code $N} pour les appels récursifs).</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link UndefinedSymbolException} si l'identifiant n'existe pas dans la table des symboles</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class LoadAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(LoadAxiome.class);

    /**
     * Exécute l'instruction {@code load(i)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param ident l'identifiant de la variable à charger
     * @throws UndefinedSymbolException si l'identifiant n'existe pas
     */
    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        // 2. Vérification de l'existence de la variable (check STACK only, not symbol table)
        if (ctx.getStacks().findQuad(scopedIdent) == null) {
            throw new UndefinedSymbolException(scopedIdent, JajaCodeInstr.LOAD.toString(), ctx.getInstructionCounter());
        }

        // 3. Récupération de la valeur et du type
        Object valeur = ctx.getStacks().getValue(scopedIdent);
        Type type = ctx.getStacks().getDataType(scopedIdent);

        // 4. Empilement
        // On crée un Quad avec la valeur récupérée.
        // L'identifiant et la sorte sont mis à "_" (anonyme) pour une valeur chargée.
        ctx.getStacks().push(new Stacks.Quad(valeur, type));

        // 5. Log et suite
        logger.debug("\t\tAxiome LOAD exécuté: {} = {} chargé sur la pile.", scopedIdent, valeur);
        ctx.incrementPC();
    }

    /**
     * Résout le nom scopé pour la récursivité.
     * Cherche d'abord la variable avec le suffixe de récursivité, puis sans.
     * Uses STACK only (not symbol table) for JajaCode compatibility.
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

            // Si on est en récursivité (niveau > 1), chercher la variable scopée dans la PILE
            if (recursionLevel > 1) {
                String scopedIdent = ident + "$" + (recursionLevel - 1);
                // Check in the STACK (not symbol table) for JajaCode compatibility
                if (ctx.getStacks().findQuad(scopedIdent) != null) {
                    return scopedIdent;
                }
            }
        }

        // Sinon, retourner le nom original
        return ident;
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.JajaCodeRuntimeException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code newarray(i, t)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [newarray] : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ newarray(i, t) –» &lt;DeclTab(i, v, t, m), a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide</li>
 *   <li>{@link TypeMismatchException} si la taille n'est pas un entier</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class NewarrayAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(NewarrayAxiome.class);

    @Override
    public void execute(MachineContext ctx, String argsPacked) {
        // 1. Dépacking des arguments (Format: "ident,type")
        String[] parts = argsPacked.split(",");
        if (parts.length != 2) {
            throw new JajaCodeRuntimeException("Arguments manquants (attendu 'ident,type')",
                    JajaCodeInstr.NEWARRAY.toString(), ctx.getInstructionCounter());
        }

        String ident = parts[0].trim();
        String typeStr = parts[1].trim();

        // 2. Conversion du Type
        Type type = parseType(typeStr);
        if (type == null) {
            throw new JajaCodeRuntimeException("Type inconnu '" + typeStr + "'",
                    JajaCodeInstr.NEWARRAY.toString(), ctx.getInstructionCounter());
        }

        logger.debug("\t\t[DEBUG] axiomeNewarray appelé: ident={}, type={}", ident, type);

        // 3. Dépiler la taille depuis la pile
        Stacks.Quad sizeQuad = ctx.getStacks().pop();
        if (sizeQuad == null) {
            throw new StackUnderflowException("Manque la taille du tableau",
                    JajaCodeInstr.NEWARRAY.toString(), ctx.getInstructionCounter());
        }

        // 4. Vérifier que la taille est un entier
        if (!(sizeQuad.value instanceof Integer size)) {
            throw new TypeMismatchException("Taille de tableau invalide (attendu entier, reçu " +
                                             sizeQuad.value.getClass().getSimpleName() + ")",
                    JajaCodeInstr.NEWARRAY.toString(), ctx.getInstructionCounter());
        }

        logger.debug("\t\t[DEBUG] Taille dépilée: {}", size);

        // 5. Gérer la récursivité: résoudre le nom scopé
        String scopedIdent = resolveScopedName(ctx, ident);

        // 6. Déclarer le tableau (alloue dans le tas via Stacks.declareTab)
        ctx.getStacks().declareTab(scopedIdent, size, type);

        logger.debug("\t\tAxiome NEWARRAY exécuté: {}[{}] ({}) créé.", scopedIdent, size, type);

        // 7. Incrémenter PC
        ctx.incrementPC();
    }

    /**
     * Résout le nom scopé pour la récursivité.
     * Même logique que LoadAxiome/StoreAxiome.
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
                if (ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
                    return scopedIdent;
                }
            }
        }

        // Sinon, retourner le nom original
        return ident;
    }

    private Type parseType(String t) {
        if ("int".equalsIgnoreCase(t) || "entier".equalsIgnoreCase(t) ||
            "integer".equalsIgnoreCase(t)) return Type.ENTIER;
        if ("boolean".equalsIgnoreCase(t) || "booleen".equalsIgnoreCase(t)) return Type.BOOLEEN;
        if ("void".equalsIgnoreCase(t)) return Type.VOID;
        return null;
    }
}

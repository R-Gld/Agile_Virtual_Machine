package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.JajaCodeRuntimeException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.List;

/**
 * Axiome représentant l'instruction JajaCode {@code new(i,t,kind,depth)}.
 *
 * <p><b>Sémantiques formelles :</b></p>
 * <pre>
 * [newV]     : &lt;m,a&gt; ⊢ new(i,t,var,s) –» &lt;IdentVal(i,t,m,s),a+1&gt;
 * [newC]     : &lt;&lt;w,v, cst,*&gt;.m,a&gt; ⊢ new(i,t,cst,0) –» &lt;DeclCst(i,v,t,m),a+1&gt;
 * [newM]     : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ new(i,t,meth,0) –» &lt;DeclMeth(i, v, t, m),a+1&gt;
 * [newarray] : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ newarray(i, t) –» &lt;DeclTab(i, v, t, m), a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide ou insuffisante pour le depth</li>
 *   <li>{@link JajaCodeRuntimeException} si les arguments sont malformés ou le type/kind est inconnu</li>
 *   <li>{@link TypeMismatchException} si la taille du tableau n'est pas un entier</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class NewAxiome implements JajaAxiome {

    /**
     * Exécute l'instruction {@code new(i,t,kind,depth)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param argsPacked les arguments packés au format "ident,type,kind[,depth]"
     * @throws StackUnderflowException si la pile est vide ou insuffisante
     * @throws JajaCodeRuntimeException si les arguments sont malformés
     * @throws TypeMismatchException si la taille du tableau n'est pas un entier
     */
    @Override
    public void execute(MachineContext ctx, String argsPacked) {
        // 1. Dépacking des arguments (Format attendu: "ident,type,kind,depth")
        String[] parts = argsPacked.split(",");
        if (parts.length < 3) {
            throw new JajaCodeRuntimeException("Arguments manquants (attendu 'ident,type,kind')", "NEW", ctx.getInstructionCounter());
        }

        String ident = parts[0].trim();
        String typeStr = parts[1].trim();
        String kind = parts[2].trim();

        // Le depth est optionnel (4ème argument), par défaut 0
        int depth = 0;
        if (parts.length >= 4) {
            try {
                depth = Integer.parseInt(parts[3].trim());
            } catch (NumberFormatException ignored) {
                throw new JajaCodeRuntimeException("Depth invalide: '" + parts[3].trim() + "'", "NEW", ctx.getInstructionCounter());
            }
        }

        // 2. Conversion du Type (String -> Enum)
        Type type = parseType(typeStr);
        if (type == null) {
            throw new JajaCodeRuntimeException("Type inconnu '" + typeStr + "'", "NEW", ctx.getInstructionCounter());
        }

        System.out.println("\t\t[DEBUG] axiomeNew appelé: ident=" + ident + ", type=" + type + ", kind=" + kind + ", depth=" + depth);

        // 3b. Gérer la récursivité: ajouter le suffixe de contexte pour les variables locales
        // Si on est dans un contexte de méthode (récursif ou non), le contexte est stocké dans contextStack
        String scopedIdent = ident;
        String currentContext = ctx.getStacks().getCurrentContext();

        // Si on est dans un contexte de méthode ET que la variable n'est pas une méthode
        // On ajoute le suffixe de récursivité pour différencier les appels récursifs
        if (currentContext != null && !kind.equalsIgnoreCase("meth")) {
            // Vérifier si la variable est une variable locale (contient @ dans son nom mais pas @global)
            if (ident.contains("@") && !ident.endsWith("@global")) {
                // Récupérer la profondeur de récursion depuis le contexte
                String methodName = ctx.getStacks().getCurrentMethodName();
                int recursionLevel = ctx.getStacks().getRecursionDepth(methodName);

                // Si recursionLevel > 1, on ajoute le suffixe pour différencier les instances
                if (recursionLevel > 1) {
                    scopedIdent = ident + "$" + (recursionLevel - 1);
                    System.out.println("\t\t[DEBUG] Variable récursive renommée: " + ident + " -> " + scopedIdent);
                }
            }
        }

        // 3. Logique selon le depth
        if (depth > 0) {
            // [newV] avec depth > 0 : IdentVal - on crée un alias vers la position depth dans la pile
            // On NE FAIT PAS de push, on crée seulement un binding dans la table des symboles
            List<Stacks.Quad> stackContent = ctx.getStacks().getStackFromTopToBottom();

            if (stackContent.size() < depth + 1) {
                throw new StackUnderflowException("Pile insuffisante pour depth=" + depth, "NEW", ctx.getInstructionCounter());
            }

            // Récupérer la valeur et le quad à la position depth
            Stacks.Quad targetQuad = stackContent.get(depth);
            Object valeur = targetQuad.value;

            // Modifier le quad existant dans la pile pour lui donner le nom du paramètre
            // Cela permet à load/store de fonctionner correctement
            // On modifie directement l'identifiant du quad à la position depth
            int stackSize = ctx.getStacks().getStackFromTopToBottom().size();
            int realIndex = stackSize - 1 - depth; // Convertir de top-to-bottom à bottom-to-top

            // On renomme le quad existant avec le nom du paramètre (scopé pour la récursivité)
            targetQuad.ident = scopedIdent;
            targetQuad.object = "var";
            targetQuad.type = type;

            // Créer l'entrée dans la table des symboles qui pointe vers cette position
            ctx.getStacks().getSymbolTable().creationSymbol(scopedIdent, realIndex, type);

            System.out.println("\t\t[DEBUG] Valeur identifiée à depth=" + depth + ": " + valeur + " (pile non modifiée, binding créé)");
            System.out.println("\t\tAxiome NEW exécuté: " + scopedIdent + " (" + type + "/" + kind + ") identifié à depth=" + depth);
        } else {
            // depth = 0 : comportement normal, on dépile la valeur et on déclare
            Stacks.Quad valeurQuad = ctx.getStacks().pop();

            if (valeurQuad == null) {
                throw new StackUnderflowException("Manque la valeur d'initialisation", "NEW", ctx.getInstructionCounter());
            }

            Object valeur = valeurQuad.value;
            System.out.println("\t\t[DEBUG] Valeur dépilée: " + valeur);

            // Logique de déclaration selon la sorte (kind)
            switch (kind.toLowerCase()) {
                case "variable":
                case "var":
                    ctx.getStacks().declareVar(scopedIdent, valeur, type);
                    break;
                case "cst":
                case "meth":
                    ctx.getStacks().declareCst(scopedIdent, valeur, type);
                    break;
                default:
                    throw new JajaCodeRuntimeException("Sorte inconnue: " + kind, "NEW", ctx.getInstructionCounter());
            }

            System.out.println("\t\tAxiome NEW exécuté: " + scopedIdent + " (" + type + "/" + kind + ") créé.");
        }

        ctx.incrementPC();
    }

    private Type parseType(String t) {
        if ("int".equalsIgnoreCase(t) || "entier".equalsIgnoreCase(t) || "integer".equalsIgnoreCase(t))
            return Type.ENTIER;
        if ("boolean".equalsIgnoreCase(t) || "booleen".equalsIgnoreCase(t)) return Type.BOOLEEN;
        if ("void".equalsIgnoreCase(t)) return Type.VOID;
        return null;
    }
}
package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.JajaCodeRuntimeException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.List;

public class NewAxiome implements JajaAxiome {

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

            // On renomme le quad existant avec le nom du paramètre
            targetQuad.ident = ident;
            targetQuad.object = "var";
            targetQuad.type = type;

            // Créer l'entrée dans la table des symboles qui pointe vers cette position
            ctx.getStacks().getSymbolTable().creationSymbol(ident, realIndex, type);

            System.out.println("\t\t[DEBUG] Valeur identifiée à depth=" + depth + ": " + valeur + " (pile non modifiée, binding créé)");
            System.out.println("\t\tAxiome NEW exécuté: " + ident + " (" + type + "/" + kind + ") identifié à depth=" + depth);
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
                    ctx.getStacks().declareVar(ident, valeur, type);
                    break;
                case "cst":
                case "meth":
                    ctx.getStacks().declareCst(ident, valeur, type);
                    break;
                case "tab":
                    if (valeur instanceof Integer size) {
                        ctx.getStacks().declareTab(ident, size, type);
                    } else {
                        throw new TypeMismatchException("Taille de tableau invalide", "NEW", ctx.getInstructionCounter());
                    }
                    break;
                default:
                    throw new JajaCodeRuntimeException("Sorte inconnue: " + kind, "NEW", ctx.getInstructionCounter());
            }

            System.out.println("\t\tAxiome NEW exécuté: " + ident + " (" + type + "/" + kind + ") créé.");
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
package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.AssignmentException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class IncAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. On récupère la valeur d'incrément sur la pile (le sommet)
        Stacks.Quad incrementQuad = ctx.getStacks().pop();

        if (incrementQuad == null) {
            throw new StackUnderflowException("INC", ctx.getInstructionCounter());
        }

        // 2. Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        // 3. On récupère la valeur actuelle de la variable 'scopedIdent'
        Object currentValue = ctx.getStacks().getValue(scopedIdent);

        // Vérification d'existence
        if (currentValue == null && !ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
            throw new UndefinedSymbolException(scopedIdent, "INC", ctx.getInstructionCounter());
        }

        // 4. Vérification des types (doivent être des entiers)
        if (!(currentValue instanceof Integer) || !(incrementQuad.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Tentative d'incrémenter avec des valeurs non entières (" + currentValue + " + " + incrementQuad.value + ")",
                "INC",
                ctx.getInstructionCounter()
            );
        }

        // 5. Calcul de la nouvelle valeur
        int newValue = (Integer) currentValue + (Integer) incrementQuad.value;

        // 6. Mise à jour en mémoire
        boolean success = ctx.getStacks().AffecterVal(scopedIdent, newValue);

        if (!success) {
            throw new AssignmentException(scopedIdent, "échec de l'affectation", "INC", ctx.getInstructionCounter());
        }

        // 7. Succès
        System.out.println("\t\tAxiome INC exécuté: " + scopedIdent + " += " + incrementQuad.value + " -> " + newValue);
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
                if (ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
                    return scopedIdent;
                }
            }
        }

        // Sinon, retourner le nom original
        return ident;
    }
}
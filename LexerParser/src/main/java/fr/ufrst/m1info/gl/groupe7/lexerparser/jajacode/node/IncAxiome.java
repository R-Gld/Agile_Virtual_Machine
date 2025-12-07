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

        // 2. On récupère la valeur actuelle de la variable 'ident'
        Object currentValue = ctx.getStacks().getValue(ident);

        // Vérification d'existence
        if (currentValue == null && !ctx.getStacks().getSymbolTable().contains(ident)) {
            throw new UndefinedSymbolException(ident, "INC", ctx.getInstructionCounter());
        }

        // 3. Vérification des types (doivent être des entiers)
        if (!(currentValue instanceof Integer) || !(incrementQuad.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Tentative d'incrémenter avec des valeurs non entières (" + currentValue + " + " + incrementQuad.value + ")",
                "INC",
                ctx.getInstructionCounter()
            );
        }

        // 4. Calcul de la nouvelle valeur
        int newValue = (Integer) currentValue + (Integer) incrementQuad.value;

        // 5. Mise à jour en mémoire
        boolean success = ctx.getStacks().AffecterVal(ident, newValue);

        if (!success) {
            throw new AssignmentException(ident, "échec de l'affectation", "INC", ctx.getInstructionCounter());
        }

        // 6. Succès
        System.out.println("\t\tAxiome INC exécuté: " + ident + " += " + incrementQuad.value + " -> " + newValue);
        ctx.incrementPC();
    }
}
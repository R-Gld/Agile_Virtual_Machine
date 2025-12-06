package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class SupAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Dépiler les deux opérandes
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Vérification de la pile vide
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", "SUP", ctx.getInstructionCounter());
        }

        // 3. Vérification de type (Comparaison d'entiers uniquement)
        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Tentative de comparer des valeurs non entières (" + op1.value + " > " + op2.value + ")",
                "SUP",
                ctx.getInstructionCounter()
            );
        }

        // 4. Calcul de la comparaison
        boolean result = (Integer) op1.value > (Integer) op2.value;

        // 5. Empiler le résultat (Type BOOLEEN)
        ctx.getStacks().push(new Stacks.Quad(
                ctx.getTEMP_VALUE(), // Identifiant temporaire (oméga)
                result,              // La valeur calculée (true/false)
                ctx.getTEMP_VALUE(), // Sorte temporaire
                Type.BOOLEEN         // Type explicite
        ));

        // 6. Log et incrément du PC
        System.out.println("\t\tAxiome SUP exécuté: " + op1.value + " > " + op2.value + " = " + result);
        ctx.incrementPC();
    }
}
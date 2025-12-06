package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class AndAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Dépilement des opérandes
        // op2 est au sommet, op1 est en dessous
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Vérification pile vide
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", "AND", ctx.getInstructionCounter());
        }

        // 3. Vérification de type (Booléens uniquement)
        if (!(op1.value instanceof Boolean) || !(op2.value instanceof Boolean)) {
            throw new TypeMismatchException(
                "Tentative d'opération sur des types non booléens (" + op1.value + " && " + op2.value + ")",
                "AND",
                ctx.getInstructionCounter()
            );
        }

        // 4. Calcul (OU logique)
        boolean result = (Boolean) op1.value && (Boolean) op2.value;

        // 5. Empilement du résultat
        ctx.getStacks().push(new Stacks.Quad(
                ctx.getTEMP_VALUE(), // Identifiant temporaire
                result,              // Résultat
                ctx.getTEMP_VALUE(), // Sorte temporaire
                Type.BOOLEEN         // Type explicite
        ));

        // 6. Log et passage à l'instruction suivante
        System.out.println("\t\tAxiome AND exécuté: " + op1.value + " && " + op2.value + " = " + result);
        ctx.incrementPC();
    }
}
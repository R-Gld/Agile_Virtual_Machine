package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class NotAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Dépilement de l'unique opérande
        Stacks.Quad op = ctx.getStacks().pop();

        // 2. Vérification pile vide
        if (op == null) {
            throw new StackUnderflowException("NOT", ctx.getInstructionCounter());
        }

        // 3. Vérification de type (Booléen uniquement)
        if (!(op.value instanceof Boolean)) {
            throw new TypeMismatchException(
                "L'opérande doit être booléen (trouvé: " + op.value + ")",
                "NOT",
                ctx.getInstructionCounter()
            );
        }

        // 4. Calcul (Négation)
        boolean result = !(Boolean) op.value;

        // 5. Empilement du résultat
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), // Identifiant temporaire
                result,              // Valeur inversée
                ctx.getTEMP_VALUE(), // Sorte temporaire
                Type.BOOLEEN         // Type explicite
        ));

        // 6. Log et suite
        System.out.println("\t\tAxiome NOT exécuté: !" + op.value + " = " + result);
        ctx.incrementPC();
    }
}
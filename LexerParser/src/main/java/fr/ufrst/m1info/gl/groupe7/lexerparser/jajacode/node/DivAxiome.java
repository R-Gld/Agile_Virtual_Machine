package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.DivisionByZeroException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class DivAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Dépilement (op2 est le diviseur, op1 est le dividende)
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Vérification pile vide
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", "DIV", ctx.getInstructionCounter());
        }

        // 3. Vérification de type (Entiers uniquement)
        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Opérandes non entiers (" + op1.value + " / " + op2.value + ")",
                "DIV",
                ctx.getInstructionCounter()
            );
        }

        // 4. Vérification Division par Zéro (Spécifique à DIV)
        if ((Integer) op2.value == 0) {
            throw new DivisionByZeroException("DIV", ctx.getInstructionCounter());
        }

        // 5. Calcul
        int result = (Integer) op1.value / (Integer) op2.value;

        // 6. Empilement du résultat
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), result, ctx.getTEMP_VALUE(), Type.ENTIER));

        // 7. Log et suite
        System.out.println("\t\tAxiome DIV exécuté: " + op1.value + " / " + op2.value + " = " + result);
        ctx.incrementPC();
    }
}
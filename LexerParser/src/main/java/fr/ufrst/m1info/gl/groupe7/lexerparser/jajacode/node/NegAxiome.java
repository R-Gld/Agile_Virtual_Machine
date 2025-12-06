package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class NegAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        Stacks.Quad op = ctx.getStacks().pop();

        if (op == null) {
            throw new StackUnderflowException("NEG", ctx.getInstructionCounter());
        }

        if (!(op.value instanceof Integer)) {
            throw new TypeMismatchException("Type invalide pour l'opération NEG: " + op.value, "NEG", ctx.getInstructionCounter());
        }

        int result = -(Integer) op.value;
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), result, ctx.getTEMP_VALUE(), Type.ENTIER));

        System.out.println("\t\tAxiome UNARY MINUS exécuté: -" + op.value + " = " + result);
        ctx.incrementPC();
    }
}

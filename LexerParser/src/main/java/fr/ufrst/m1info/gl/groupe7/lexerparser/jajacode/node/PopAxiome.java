package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class PopAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        Stacks.Quad popped = ctx.getStacks().pop();

        if (popped == null) {
            throw new StackUnderflowException("Rien à dépiler", "POP", ctx.getInstructionCounter());
        }

        System.out.println("\t\tAxiome POP exécuté: valeur dépilée = " + popped.value);
        ctx.incrementPC();
    }
}

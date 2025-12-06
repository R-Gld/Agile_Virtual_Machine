package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;

public class WriteAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        Stacks.Quad valeur = ctx.getStacks().pop();

        if (valeur == null) {
            throw new StackUnderflowException("Manque la valeur à écrire", "WRITE", ctx.getInstructionCounter());
        }

        System.out.print(valeur.value);
        System.out.println("\t\tAxiome WRITE exécuté: " + valeur.value + " affiché.");
        ctx.incrementPC();
    }
}

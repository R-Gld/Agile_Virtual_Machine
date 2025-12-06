package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.AssignmentException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class StoreAxiome implements JajaAxiome {
    @Override
    public void execute(MachineContext ctx, String ident) {
        Stacks.Quad valeur = ctx.getStacks().pop();

        if (valeur == null) {
            throw new StackUnderflowException("STORE", ctx.getInstructionCounter());
        }

        boolean success = ctx.getStacks().AffecterVal(ident, valeur.value);

        if (!success) {
            throw new AssignmentException(ident, "impossible d'affecter la valeur", "STORE", ctx.getInstructionCounter());
        }

        System.out.println("\t\tAxiome STORE exécuté: " + ident + " mis à jour avec valeur " + valeur.value + ".");
        ctx.incrementPC();
    }
}

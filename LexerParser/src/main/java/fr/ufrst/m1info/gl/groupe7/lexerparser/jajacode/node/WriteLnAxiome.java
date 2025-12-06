package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class WriteLnAxiome implements JajaAxiome{

    @Override
    public void execute(MachineContext ctx, String arg) {
        Stacks.Quad valeur = ctx.getStacks().pop();
        if (valeur == null) {
            throw new StackUnderflowException("Manque la valeur à écrire", "WRITELN", ctx.getInstructionCounter());
        }
        System.out.print(valeur.value);
        System.out.println("\t\tAxiome WRITELN exécuté: " + valeur.value + " affiché avec retour à la ligne.");
        ctx.incrementPC();
    }
}

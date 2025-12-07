package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class InvokeAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String ident) {
        System.out.println("\t\t[DEBUG] axiomeInvoke appelé: ident=" + ident);

        // Récupérer l'adresse de la méthode depuis la mémoire
        Object methodAddress = ctx.getStacks().getValue(ident);

        if (methodAddress == null) {
            System.out.println("Erreur dans axiomeInvoke : méthode '" + ident + "' non trouvée.");
            ctx.stop();
            return;
        }

        if (!(methodAddress instanceof Integer)) {
            System.out.println("Erreur dans axiomeInvoke : l'adresse de la méthode '" + ident + "' n'est pas un entier : " + methodAddress);
            ctx.stop();
            return;
        }

        int adresse = (Integer) methodAddress;

        // Empiler l'adresse de retour (PC + 1) sous forme de quad <w, a+1, cst, *>
        Stacks.Quad returnQuad = new Stacks.Quad(ctx.getTEMP_VALUE(), ctx.getInstructionCounter() + 1, "cst", Type.ENTIER);
        ctx.getStacks().push(returnQuad);

        System.out.println("\t\tAxiome INVOKE exécuté: appel de '" + ident + "' à l'adresse " + adresse + ", retour prévu à " + (ctx.getInstructionCounter() + 1));

        // Sauter à l'adresse de la méthode
        ctx.setInstructionCounter(adresse);
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

public class ReturnAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        System.out.println("\t\t[DEBUG] axiomeReturn appelé");

        // Dépiler le quad de retour
        var returnQuad = ctx.getStacks().pop();

        if (returnQuad == null) {
            System.out.println("Erreur dans axiomeReturn : pile vide.");
            ctx.stop();
            return;
        }

        // Vérifier que c'est bien un quad de retour (cst avec une adresse)
        if (!"cst".equals(returnQuad.object)) {
            System.out.println("Erreur dans axiomeReturn : le quad dépilé n'est pas un quad de retour : " + returnQuad);
            ctx.stop();
            return;
        }

        if (!(returnQuad.value instanceof Integer)) {
            System.out.println("Erreur dans axiomeReturn : l'adresse de retour n'est pas un entier : " + returnQuad.value);
            ctx.stop();
            return;
        }

        int returnAddress = (Integer) returnQuad.value;

        System.out.println("\t\tAxiome RETURN exécuté: retour à l'adresse " + returnAddress);

        // Restaurer le PC à l'adresse de retour
        ctx.setInstructionCounter(returnAddress);
    }
}

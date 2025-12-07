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

        // Récupérer le nom de la méthode depuis le quad de retour
        // Format: %RET_methodName%
        String returnIdent = returnQuad.ident;
        String methodName;
        if (returnIdent.startsWith("%RET_") && returnIdent.endsWith("%")) {
            methodName = returnIdent.substring(5, returnIdent.length() - 1);
        } else {
            // Compatibilité avec l'ancien format
            methodName = returnIdent;
        }

        System.out.println("\t\tAxiome RETURN exécuté: retour à l'adresse " + returnAddress + " (sortie de '" + methodName + "')");

        // Pop le contexte de la méthode courante
        ctx.getStacks().popContext(methodName);

        // Restaurer le PC à l'adresse de retour
        ctx.setInstructionCounter(returnAddress);
    }
}

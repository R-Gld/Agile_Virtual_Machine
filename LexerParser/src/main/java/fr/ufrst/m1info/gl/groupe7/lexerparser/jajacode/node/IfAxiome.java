package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.InvalidAddressException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class IfAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String adresseArg) {
        // 1. Parsing de l'adresse de saut
        int targetAddress;
        try {
            targetAddress = Integer.parseInt(adresseArg);
        } catch (NumberFormatException e) {
            throw new InvalidAddressException(adresseArg, "IF", ctx.getInstructionCounter());
        }

        // 2. Récupération de la condition sur la pile
        Stacks.Quad conditionQuad = ctx.getStacks().pop();

        if (conditionQuad == null) {
            throw new StackUnderflowException("IF", ctx.getInstructionCounter());
        }

        // 3. Évaluation de la vérité
        boolean isTrue;
        Object val = conditionQuad.value;

        if (val instanceof Boolean b) {
            isTrue = b;
        } else if (val instanceof Integer i) {
            isTrue = i != 0;
        } else {
            throw new TypeMismatchException("Type de condition invalide (" + val + ")", "IF", ctx.getInstructionCounter());
        }

        // 4. Logique de branchement
        if (isTrue) {
            // Saut : On force le PC à la nouvelle adresse
            ctx.setInstructionCounter(targetAddress);
            System.out.println("\t\tAxiome IF exécuté: VRAI -> saut à " + targetAddress);
        } else {
            // Pas de saut : On continue séquentiellement
            ctx.incrementPC();
            System.out.println("\t\tAxiome IF exécuté: FAUX -> suite séquentielle");
        }
    }
}
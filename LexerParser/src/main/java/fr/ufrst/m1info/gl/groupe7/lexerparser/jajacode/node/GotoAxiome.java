package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.InvalidAddressException;

public class GotoAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String adresseArg) {
        try {
            // 1. Conversion de l'argument (String) en adresse (int)
            int adresse = Integer.parseInt(adresseArg);

            // 2. Mise à jour directe du PC
            ctx.setInstructionCounter(adresse);

            System.out.println("\t\tAxiome GOTO exécuté: saut à l'adresse " + adresse + ".");

        } catch (NumberFormatException e) {
            throw new InvalidAddressException(adresseArg, "GOTO", ctx.getInstructionCounter());
        }
    }
}
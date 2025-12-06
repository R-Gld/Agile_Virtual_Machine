package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class LoadAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Vérification de l'existence de la variable
        if (!ctx.getStacks().getSymbolTable().contains(ident)) {
            throw new UndefinedSymbolException(ident, "LOAD", ctx.getInstructionCounter());
        }

        // 2. Récupération de la valeur et du type
        Object valeur = ctx.getStacks().getValue(ident);
        Type type = ctx.getStacks().getDataType(ident);

        // 3. Empilement
        // On crée un Quad avec la valeur récupérée.
        // L'identifiant et la sorte sur la pile sont souvent temporaires (oméga) pour une valeur chargée.
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), valeur, ctx.getTEMP_VALUE(), type));

        // 4. Log et suite
        System.out.println("\t\tAxiome LOAD exécuté: " + ident + " = " + valeur + " chargé sur la pile.");
        ctx.incrementPC();
    }
}
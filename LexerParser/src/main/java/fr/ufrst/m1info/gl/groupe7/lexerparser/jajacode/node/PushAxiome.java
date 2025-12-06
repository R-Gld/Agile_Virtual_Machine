package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class PushAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        Object valeur;
        Type type;

        // 1. Parsing manuel de la chaîne de caractères
        if ("true".equals(arg)) {
            valeur = true;
            type = Type.BOOLEEN;
        } else if ("false".equals(arg)) {
            valeur = false;
            type = Type.BOOLEEN;
        } else {
            try {
                // On essaie de voir si c'est un entier
                valeur = Integer.parseInt(arg);
                type = Type.ENTIER;
            } catch (NumberFormatException e) {
                // Si ce n'est ni un booléen ni un entier, c'est 'vide' (omega/nil)
                valeur = null;
                type = Type.VOID;
            }
        }

        // 2. Création du Quad et Push
        // Note: pour un push de valeur immédiate, l'ID est souvent une valeur temporaire (oméga)
        ctx.getStacks().push(new Stacks.Quad(
                ctx.getTEMP_VALUE(), // Identifiant (souvent vide pour une constante)
                valeur,              // La valeur parsée
                ctx.getTEMP_VALUE(), // Sorte (souvent vide)
                type                 // Le type déduit
        ));

        System.out.println("\t\tAxiome PUSH exécuté: " + valeur + " (" + type + ") poussé sur la pile.");
        ctx.incrementPC();
    }
}
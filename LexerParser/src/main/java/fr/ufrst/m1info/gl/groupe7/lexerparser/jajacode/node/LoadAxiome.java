package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class LoadAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        // 2. Vérification de l'existence de la variable
        if (!ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
            throw new UndefinedSymbolException(scopedIdent, "LOAD", ctx.getInstructionCounter());
        }

        // 3. Récupération de la valeur et du type
        Object valeur = ctx.getStacks().getValue(scopedIdent);
        Type type = ctx.getStacks().getDataType(scopedIdent);

        // 4. Empilement
        // On crée un Quad avec la valeur récupérée.
        // L'identifiant et la sorte sur la pile sont souvent temporaires (oméga) pour une valeur chargée.
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), valeur, ctx.getTEMP_VALUE(), type));

        // 5. Log et suite
        System.out.println("\t\tAxiome LOAD exécuté: " + scopedIdent + " = " + valeur + " chargé sur la pile.");
        ctx.incrementPC();
    }

    /**
     * Résout le nom scopé pour la récursivité.
     * Cherche d'abord la variable avec le suffixe de récursivité, puis sans.
     */
    private String resolveScopedName(MachineContext ctx, String ident) {
        // Si la variable est globale, pas de scopage
        if (ident.endsWith("@global") || !ident.contains("@")) {
            return ident;
        }

        // Vérifier si on est dans un contexte de méthode récursif
        String currentContext = ctx.getStacks().getCurrentContext();
        if (currentContext != null) {
            String methodName = ctx.getStacks().getCurrentMethodName();
            int recursionLevel = ctx.getStacks().getRecursionDepth(methodName);

            // Si on est en récursivité (niveau > 1), chercher la variable scopée
            if (recursionLevel > 1) {
                String scopedIdent = ident + "$" + (recursionLevel - 1);
                if (ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
                    return scopedIdent;
                }
            }
        }

        // Sinon, retourner le nom original
        return ident;
    }
}
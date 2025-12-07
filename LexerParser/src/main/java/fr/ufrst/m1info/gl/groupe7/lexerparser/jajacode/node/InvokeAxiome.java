package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code invoke(i)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [invoke] : &lt;m,a&gt; ⊢ invoke(i) –» &lt;&lt;w, a+1,cst,*&gt;.m, Val(i, m)&gt;
 * </pre>
 *
 * <p>Cette instruction effectue un appel de méthode :</p>
 * <ol>
 *   <li>Pousse le contexte de la méthode sur la pile de contextes</li>
 *   <li>Empile l'adresse de retour (PC + 1) sur la pile</li>
 *   <li>Saute à l'adresse de la méthode stockée dans la variable {@code i}</li>
 * </ol>
 *
 * <p><b>Gestion de la récursivité :</b> L'implémentation utilise un identifiant spécial
 * {@code %RET_methodName%} pour l'adresse de retour afin de gérer les appels récursifs.</p>
 *
 * @see JajaAxiome
 * @see ReturnAxiome
 */
public class InvokeAxiome implements JajaAxiome {

    /**
     * Exécute l'instruction {@code invoke(i)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param ident l'identifiant de la méthode à invoquer
     */
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

        // Pousser le contexte AVANT d'empiler l'adresse de retour
        // Cela permet de savoir dans quel contexte on est lors du return
        ctx.getStacks().pushContext(ident);

        // Empiler l'adresse de retour (PC + 1) sous forme de quad
        // On utilise un identifiant spécial "%RET_methodName%" pour éviter les conflits
        // avec le nom de la méthode lors des appels récursifs
        String returnIdent = "%RET_" + ident + "%";
        Stacks.Quad returnQuad = new Stacks.Quad(returnIdent, ctx.getInstructionCounter() + 1, "cst", Type.ENTIER);
        ctx.getStacks().push(returnQuad);

        System.out.println("\t\tAxiome INVOKE exécuté: appel de '" + ident + "' à l'adresse " + adresse + ", retour prévu à " + (ctx.getInstructionCounter() + 1));

        // Sauter à l'adresse de la méthode
        ctx.setInstructionCounter(adresse);
    }
}

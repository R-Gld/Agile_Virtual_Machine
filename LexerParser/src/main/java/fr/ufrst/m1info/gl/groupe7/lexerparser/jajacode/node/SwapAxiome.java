package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Axiome représentant l'instruction JajaCode {@code swap}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [swap] : &lt;q1.q2.m,a&gt; ⊢ swap –» &lt;q2.q1.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction échange les deux premiers éléments au sommet de la pile.
 * L'élément en position 1 devient l'élément en position 2 et vice-versa.</p>
 *
 * @see JajaAxiome
 */
public class SwapAxiome implements JajaAxiome {

    /**
     * Exécute l'instruction {@code swap}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        ctx.getStacks().swap();
        ctx.incrementPC();
    }
}

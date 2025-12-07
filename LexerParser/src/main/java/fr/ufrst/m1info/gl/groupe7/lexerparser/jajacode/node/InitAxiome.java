package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Axiome représentant l'instruction JajaCode {@code init}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [init] : &lt;m,a&gt; ⊢ init –» &lt;[], a+1&gt;
 * </pre>
 *
 * <p>Cette instruction initialise la pile à vide et incrémente le compteur de programme.
 * Elle est généralement la première instruction d'un programme JajaCode.</p>
 *
 * @see JajaAxiome
 */
public class InitAxiome implements JajaAxiome{
    /**
     * Exécute l'instruction {@code init}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param ident paramètre non utilisé pour cette instruction
     */
    @Override
    public void execute(MachineContext ctx, String ident) {
        ctx.incrementPC();
    }
}

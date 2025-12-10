package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Axiome représentant l'instruction JajaCode {@code jcstop}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [jcstop] : &lt;m,a&gt; ⊢ jcstop –» &lt;m, ⊥&gt;
 * </pre>
 *
 * <p>Cette instruction arrête l'exécution de la machine virtuelle.
 * Le compteur de programme (PC) est mis à une valeur spéciale (⊥) indiquant
 * la fin de l'exécution.</p>
 *
 * <p>Cette instruction est généralement la dernière instruction d'un programme JajaCode.</p>
 *
 * @see JajaAxiome
 */
public class JCStopAxiome implements JajaAxiome {
    /**
     * Exécute l'instruction {@code jcstop}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        ctx.stop();
    }
}

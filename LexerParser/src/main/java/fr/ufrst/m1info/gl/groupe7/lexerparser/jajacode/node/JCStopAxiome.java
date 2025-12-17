package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Axiome representing the JajaCode instruction {@code jcstop}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [jcstop] : &lt;m,a&gt; ⊢ jcstop –» &lt;m, ⊥&gt;
 * </pre>
 *
 * <p>This instruction stops the execution of the virtual machine.
 * The program counter (PC) is set to a special value (⊥) indicating
 * the end of execution.</p>
 *
 * <p>This instruction is typically the last instruction of a JajaCode program.</p>
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

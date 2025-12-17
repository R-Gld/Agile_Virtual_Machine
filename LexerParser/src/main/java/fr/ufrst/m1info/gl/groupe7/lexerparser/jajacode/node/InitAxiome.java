package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Axiome representing the JajaCode instruction {@code init}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [init] : &lt;m,a&gt; ⊢ init –» &lt;[], a+1&gt;
 * </pre>
 *
 * <p>This instruction initializes the stack to empty and increments the program
 * counter. It is typically the first instruction of a JajaCode program.</p>
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

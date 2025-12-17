package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Axiome representing the JajaCode instruction {@code swap}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [swap] : &lt;q1.q2.m,a&gt; ⊢ swap –» &lt;q2.q1.m, a+1&gt;
 * </pre>
 *
 * <p>This instruction swaps the first two elements at the top of the stack.
 * The element at position 1 becomes the element at position 2 and vice versa.</p>
 *
 * @see JajaAxiome
 */
public class SwapAxiome implements JajaAxiome {

    /**
     * Executes the {@code swap} instruction.
     *
     * @param ctx the virtual machine context containing execution state
     * @param arg unused parameter for this instruction
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        ctx.getStacks().swap();
        ctx.incrementPC();
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome representing the JajaCode instruction {@code invoke(i)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [invoke] : &lt;m,a&gt; ⊢ invoke(i) –» &lt;&lt;w, a+1,cst,*&gt;.m, Val(i, m)&gt;
 * </pre>
 *
 * <p>This instruction performs a method call:</p>
 * <ol>
 *   <li>Push the method context onto the context stack</li>
 *   <li>Push the return address (PC + 1) onto the stack</li>
 *   <li>Jump to the address of the method stored in variable {@code i}</li>
 * </ol>
 *
 * <p><b>Recursion handling:</b> The implementation uses a special identifier
 * {@code %RET_methodName%} for the return address to support recursive calls.</p>
 *
 * @see JajaAxiome
 * @see ReturnAxiome
 */
public class InvokeAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(InvokeAxiome.class);

    /**
     * Exécute l'instruction {@code invoke(i)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param ident l'identifiant de la méthode à invoquer
     */
    @Override
    public void execute(MachineContext ctx, String ident) {
        logger.debug("\t\t[DEBUG] axiomeInvoke called: ident={}", ident);

        // Retrieve the method address from memory
        Object methodAddress = ctx.getStacks().getValue(ident);

        if (methodAddress == null) {
            logger.debug("Error in axiomeInvoke: method '{}' not found.", ident);
            ctx.stop();
            return;
        }

        if (!(methodAddress instanceof Integer)) {
            logger.debug("Error in axiomeInvoke: the address of method '{}' is not an integer: {}", ident, methodAddress);
            ctx.stop();
            return;
        }

        int adresse = (Integer) methodAddress;

        // Push the context BEFORE pushing the return address
        // This allows knowing which context we're in at return time
        ctx.getStacks().pushContext(ident);

        // Push the return address (PC + 1) as a quad
        // Use a special identifier "%RET_methodName%" to avoid conflicts
        // with the method name during recursive calls
        String returnIdent = "%RET_" + ident + "%";
        Stacks.Quad returnQuad = new Stacks.Quad(returnIdent, ctx.getInstructionCounter() + 1, "cst", Type.ENTIER);
        ctx.getStacks().push(returnQuad);

        logger.debug("\t\tAxiome INVOKE executed: call to '{}' at address {}, return expected at {}", ident, adresse, ctx.getInstructionCounter() + 1);

        // Jump to the method address
        ctx.setInstructionCounter(adresse);
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Axiome representing the JajaCode instruction {@code return}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [return] : &lt;&lt;w ,a1,cst,*&gt;.m,a&gt; ⊢ return –» &lt;m, a1&gt;
 * </pre>
 *
 * <p>This instruction performs a method return:</p>
 * <ol>
 *   <li>Pop the quad containing the return address</li>
 *   <li>Restore the previous execution context (pop the method context)</li>
 *   <li>Jump to the return address</li>
 * </ol>
 *
 * <p><b>Return quad format:</b> The popped quad must have kind {@code cst}
 * and contain the return address. The identifier uses the format {@code %RET_methodName%}.</p>
 *
 * @see JajaAxiome
 * @see InvokeAxiome
 */
public class ReturnAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(ReturnAxiome.class);

    /**
     * Exécute l'instruction {@code return}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        logger.debug("\t\t[DEBUG] axiomeReturn called");

        logger.debug("\t\t[DEBUG] Pop return quad");

        // Pop the return quad
        var returnQuad = ctx.getStacks().pop();

        if (returnQuad == null) {
            logger.debug("Error in axiomeReturn: empty stack.");
            ctx.stop();
            return;
        }

        // Check that this is indeed a return quad (kind = cst with an address)
        if (!"cst".equals(returnQuad.object)) {
            logger.debug("Error in axiomeReturn: popped quad is not a return quad: {}", returnQuad);
            ctx.stop();
            return;
        }

        if (!(returnQuad.value instanceof Integer)) {
            logger.debug("Error in axiomeReturn: return address is not an integer: {}", returnQuad.value);
            ctx.stop();
            return;
        }

        int returnAddress = (Integer) returnQuad.value;

        // Retrieve method name from the return quad
        // Format: %RET_methodName%
        String returnIdent = returnQuad.ident;
        String methodName;
        if (returnIdent.startsWith("%RET_") && returnIdent.endsWith("%")) {
            methodName = returnIdent.substring(5, returnIdent.length() - 1);
        } else {
            // Backwards compatibility with older format
            methodName = returnIdent;
        }

        logger.debug("\t\tAxiome RETURN executed: returning to address {} (exit from '{}')", returnAddress, methodName);

        // Pop the current method context
        ctx.getStacks().popContext(methodName);

        // Restore the PC to the return address
        ctx.setInstructionCounter(returnAddress);
    }
}

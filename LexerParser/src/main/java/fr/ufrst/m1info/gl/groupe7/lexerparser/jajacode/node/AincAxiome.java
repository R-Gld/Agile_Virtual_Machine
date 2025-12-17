package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Axiome representing the JajaCode instruction {@code ainc(i)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [ainc] : &lt;&lt;w, v, cst,*&gt;.&lt;w, ind, cst,*&gt;.m,a&gt; ⊢ ainc(i) –»
 *          &lt;AffecterValT(i,ind,ValT(i,ind,m)+v,m), a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack contains fewer than 2 elements</li>
 *   <li>{@link UndefinedSymbolException} if the array does not exist</li>
 *   <li>{@link TypeMismatchException} if the values are not integers</li>
 *   <li>{@link RuntimeException} if the index is out of bounds</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class AincAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(AincAxiome.class);

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Pop the INCREMENT VALUE (stack top)
        Stacks.Quad incrementQuad = ctx.getStacks().pop();
        if (incrementQuad == null) {
            throw new StackUnderflowException("Missing increment value for ainc",
                                               "AINC", ctx.getInstructionCounter());
        }

        // 2. Pop the INDEX (second element)
        Stacks.Quad indexQuad = ctx.getStacks().pop();
        if (indexQuad == null) {
            throw new StackUnderflowException("Missing index for ainc",
                                               "AINC", ctx.getInstructionCounter());
        }

        // 3. Verify that the index is an integer
        if (!(indexQuad.value instanceof Integer index)) {
            throw new TypeMismatchException("Invalid array index (expected integer, found " +
                                             indexQuad.value.getClass().getSimpleName() + ")",
                                             "AINC", ctx.getInstructionCounter());
        }

        // 4. Verify that the increment is an integer
        if (!(incrementQuad.value instanceof Integer increment)) {
            throw new TypeMismatchException("Invalid increment value (expected integer, found " +
                                             incrementQuad.value.getClass().getSimpleName() + ")",
                                             "AINC", ctx.getInstructionCounter());
        }

        logger.debug("\t\t[DEBUG] Incrément dépilé: {}", increment);
        logger.debug("\t\t[DEBUG] Indice dépilé: {}", index);

        // 5. Resolve the scoped name for recursion
        String scopedIdent = resolveScopedName(ctx, ident);

        // 6. Verify that the array exists
        if (!ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
            throw new UndefinedSymbolException(scopedIdent, JajaCodeInstr.AINC.toString(), ctx.getInstructionCounter());
        }

        // 7. Load the current array value
        Object currentValue = ctx.getStacks().getArrayValue(scopedIdent, index);

        // 8. Verify that the current value is an integer
        if (!(currentValue instanceof Integer currentInt)) {
            throw new TypeMismatchException("Attempt to increment a non-integer value in " +
                                             scopedIdent + "[" + index + "]",
                                             "AINC", ctx.getInstructionCounter());
        }

        // 9. Compute the new value
        int newValue = currentInt + increment;

        // 10. Store the new value
        ctx.getStacks().setArrayValue(scopedIdent, index, newValue);

        logger.debug("\t\tAxiome AINC executed: {}[{}] += {} -> {}", scopedIdent, index, increment, newValue);

        // 11. Increment PC
        ctx.incrementPC();
    }

    /**
     * Resolves the scoped name for recursion.
     */
    private String resolveScopedName(MachineContext ctx, String ident) {
        if (ident.endsWith("@global") || !ident.contains("@")) {
            return ident;
        }

        String currentContext = ctx.getStacks().getCurrentContext();
        if (currentContext != null) {
            String methodName = ctx.getStacks().getCurrentMethodName();
            int recursionLevel = ctx.getStacks().getRecursionDepth(methodName);

            if (recursionLevel > 1) {
                String scopedIdent = ident + "$" + (recursionLevel - 1);
                if (ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
                    return scopedIdent;
                }
            }
        }

        return ident;
    }
}

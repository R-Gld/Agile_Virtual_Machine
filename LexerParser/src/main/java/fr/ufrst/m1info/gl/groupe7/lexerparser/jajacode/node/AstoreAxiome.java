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
 * Axiome representing the JajaCode instruction {@code astore(i)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [astore] : &lt;&lt;w, v, cst,*&gt;.&lt;w, ind, cst,*&gt;.m,a&gt; ⊢ astore(i) –» &lt;AffecterValT(i, ind, v,m), a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack contains fewer than 2 elements</li>
 *   <li>{@link UndefinedSymbolException} if the array does not exist</li>
 *   <li>{@link TypeMismatchException} if the index is not an integer</li>
 *   <li>{@link RuntimeException} if the index is out of bounds (via Stacks.setArrayValue)</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class AstoreAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(AstoreAxiome.class);

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Pop the VALUE (top of the stack)
        Stacks.Quad valueQuad = ctx.getStacks().pop();
        if (valueQuad == null) {
            throw new StackUnderflowException("Manque la valeur pour astore",
                    JajaCodeInstr.ASTORE.toString(), ctx.getInstructionCounter());
        }

        // 2. Pop the INDEX (second element)
        Stacks.Quad indexQuad = ctx.getStacks().pop();
        if (indexQuad == null) {
            throw new StackUnderflowException("Manque l'indice pour astore",
                    JajaCodeInstr.ASTORE.toString(), ctx.getInstructionCounter());
        }

        // 3. Verify that the index is an integer
        if (!(indexQuad.value instanceof Integer index)) {
            throw new TypeMismatchException("Indice de tableau invalide (attendu entier, reçu " +
                                             indexQuad.value.getClass().getSimpleName() + ")",
                    JajaCodeInstr.ASTORE.toString(), ctx.getInstructionCounter());
        }

        Object valeur = valueQuad.value;

        logger.debug("\t\t[DEBUG] Popped value: {}", valeur);
        logger.debug("\t\t[DEBUG] Popped index: {}", index);

        // 4. Resolve the scoped name for recursion
        String scopedIdent = resolveScopedName(ctx, ident);

        // 5. Vérifier que le tableau existe
        if (!ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
            throw new UndefinedSymbolException(scopedIdent, JajaCodeInstr.ASTORE.toString(), ctx.getInstructionCounter());
        }

        // 6. Assign the value into the array (handles bounds and types internally)
        ctx.getStacks().setArrayValue(scopedIdent, index, valeur);
        logger.debug("\t\tAxiome ASTORE executed: {}[{}] = {}", scopedIdent, index, valeur);

        // 7. Increment PC
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

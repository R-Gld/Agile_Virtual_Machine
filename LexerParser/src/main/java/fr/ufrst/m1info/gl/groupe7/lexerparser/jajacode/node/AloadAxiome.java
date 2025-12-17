package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome representing the JajaCode instruction {@code aload(i)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [aload] : &lt;&lt;w, ind, cst,*&gt;.m,a&gt; ⊢ aload(i) –» &lt;&lt;w, ValT(i, ind, m), cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack is empty</li>
 *   <li>{@link UndefinedSymbolException} if the array does not exist</li>
 *   <li>{@link TypeMismatchException} if the index is not an integer</li>
 *   <li>{@link RuntimeException} if the index is out of bounds (via Stacks.getArrayValue)</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class AloadAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(AloadAxiome.class);

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Pop the index from the stack
        Stacks.Quad indexQuad = ctx.getStacks().pop();
        if (indexQuad == null) {
            throw new StackUnderflowException("Manque l'indice pour aload",
                    JajaCodeInstr.ALOAD.toString(), ctx.getInstructionCounter());
        }

        // 2. Verify that the index is an integer
        if (!(indexQuad.value instanceof Integer index)) {
            throw new TypeMismatchException("Indice de tableau invalide (attendu entier, reçu " +
                                             indexQuad.value.getClass().getSimpleName() + ")",
                    JajaCodeInstr.ALOAD.toString(), ctx.getInstructionCounter());
        }

        logger.debug("\t\t[DEBUG] Indice dépilé: {}", index);

        // 3. Resolve the scoped name for recursion
        String scopedIdent = resolveScopedName(ctx, ident);

        // 4. Verify that the array exists
        if (ctx.getStacks().findQuad(scopedIdent) == null) {
            throw new UndefinedSymbolException(scopedIdent, JajaCodeInstr.ALOAD.toString(), ctx.getInstructionCounter());
        }

        // 5. Load the value from the array (handles bounds internally)
        Object valeur = ctx.getStacks().getArrayValue(scopedIdent, index);

        // 6. Obtain the element type (same type as the array)
        Type type = ctx.getStacks().getDataType(scopedIdent);

        // 7. Push the loaded value
        ctx.getStacks().push(new Stacks.Quad(valeur, type));

        logger.debug("\t\tAxiome ALOAD exécuté: {}[{}] = {} chargé sur la pile.", scopedIdent, index, valeur);

        // 8. Increment PC
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
                if (ctx.getStacks().findQuad(scopedIdent) != null) {
                    return scopedIdent;
                }
            }
        }

        return ident;
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.JajaCodeRuntimeException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome representing the JajaCode instruction {@code newarray(i, t)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [newarray] : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ newarray(i, t) –» &lt;DeclTab(i, v, t, m), a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack is empty</li>
 *   <li>{@link TypeMismatchException} if the size is not an integer</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class NewarrayAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(NewarrayAxiome.class);

    @Override
    public void execute(MachineContext ctx, String argsPacked) {
        // 1. Unpack arguments (Format: "ident,type")
        String[] parts = argsPacked.split(",");
        if (parts.length != 2) {
            throw new JajaCodeRuntimeException("Arguments manquants (attendu 'ident,type')",
                    JajaCodeInstr.NEWARRAY.toString(), ctx.getInstructionCounter());
        }

        String ident = parts[0].trim();
        String typeStr = parts[1].trim();

        // 2. Convert the Type
        Type type = parseType(typeStr);
        if (type == null) {
            throw new JajaCodeRuntimeException("Unknown type '" + typeStr + "'",
                JajaCodeInstr.NEWARRAY.toString(), ctx.getInstructionCounter());
        }

        logger.debug("\t\t[DEBUG] axiomeNewarray appelé: ident={}, type={}", ident, type);

        // 3. Pop the size from the stack
        Stacks.Quad sizeQuad = ctx.getStacks().pop();
        if (sizeQuad == null) {
            throw new StackUnderflowException("Missing array size",
                    JajaCodeInstr.NEWARRAY.toString(), ctx.getInstructionCounter());
        }

        // 4. Verify the size is an integer
        if (!(sizeQuad.value instanceof Integer size)) {
            throw new TypeMismatchException("Invalid array size (expected integer, found " +
                                             sizeQuad.value.getClass().getSimpleName() + ")",
                    JajaCodeInstr.NEWARRAY.toString(), ctx.getInstructionCounter());
        }

        logger.debug("\t\t[DEBUG] Popped size: {}", size);

        // 5. Handle recursion: resolve scoped name
        String scopedIdent = resolveScopedName(ctx, ident);

        // 6. Declare the array (allocates on heap via Stacks.declareTab)
        ctx.getStacks().declareTab(scopedIdent, size, type);

        logger.debug("\t\tAxiome NEWARRAY executed: {}[{}] ({}) created.", scopedIdent, size, type);

        // 7. Increment PC
        ctx.incrementPC();
    }

    /**
     * Resolves the scoped name for recursion.
     * Same logic as LoadAxiome/StoreAxiome.
     */
    private String resolveScopedName(MachineContext ctx, String ident) {
        // Si la variable est globale, pas de scopage
        if (ident.endsWith("@global") || !ident.contains("@")) {
            return ident;
        }

        // Check if we are in a recursive method context
        String currentContext = ctx.getStacks().getCurrentContext();
        if (currentContext != null) {
            String methodName = ctx.getStacks().getCurrentMethodName();
            int recursionLevel = ctx.getStacks().getRecursionDepth(methodName);

            // If we are in recursion (level > 1), look for the scoped variable
            if (recursionLevel > 1) {
                String scopedIdent = ident + "$" + (recursionLevel - 1);
                if (ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
                    return scopedIdent;
                }
            }
        }

        // Otherwise, return the original name
        return ident;
    }

    private Type parseType(String t) {
        if ("int".equalsIgnoreCase(t) || "entier".equalsIgnoreCase(t) ||
            "integer".equalsIgnoreCase(t)) return Type.ENTIER;
        if ("boolean".equalsIgnoreCase(t) || "booleen".equalsIgnoreCase(t)) return Type.BOOLEEN;
        if ("void".equalsIgnoreCase(t)) return Type.VOID;
        return null;
    }
}

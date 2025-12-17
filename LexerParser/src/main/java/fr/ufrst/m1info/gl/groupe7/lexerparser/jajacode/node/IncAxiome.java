package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.AssignmentException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Axiome representing the JajaCode instruction {@code inc(i)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [inc] : &lt;&lt;w,v, cst,*&gt;.m,a&gt; ⊢ inc(i) –» &lt;AffecterVal(i,Val(i,m)+v,m), a+1&gt;
 * </pre>
 *
 * <p>This instruction pops a value {@code v}, adds it to the current value
 * of the variable identified by {@code i}, and updates the variable with the result.</p>
 *
 * <p><b>Scoping:</b> This implementation supports recursion by resolving
 * scoped variable names.</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack is empty</li>
 *   <li>{@link UndefinedSymbolException} if the identifier does not exist</li>
 *   <li>{@link TypeMismatchException} if values are not integers</li>
 *   <li>{@link AssignmentException} if the assignment fails</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class IncAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(IncAxiome.class);

    /**
     * Exécute l'instruction {@code inc(i)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param ident l'identifiant de la variable à incrémenter
     * @throws StackUnderflowException si la pile est vide
     * @throws UndefinedSymbolException si l'identifiant n'existe pas
     * @throws TypeMismatchException si les valeurs ne sont pas de type entier
     * @throws AssignmentException si l'affectation échoue
     */
    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Retrieve the increment value from the stack (top)
        Stacks.Quad incrementQuad = ctx.getStacks().pop();

        if (incrementQuad == null) {
            throw new StackUnderflowException("INC", ctx.getInstructionCounter());
        }

        // 2. Resolve the scoped name for recursion
        String scopedIdent = resolveScopedName(ctx, ident);

        // 3. Get the current value of variable 'scopedIdent'
        Object currentValue = ctx.getStacks().getValue(scopedIdent);

        // Existence check
        if (currentValue == null && ctx.getStacks().findQuad(scopedIdent) == null) {
            throw new UndefinedSymbolException(scopedIdent, JajaCodeInstr.INC.toString(), ctx.getInstructionCounter());
        }

        // 4. Type checks (must be integers)
        if (!(currentValue instanceof Integer) || !(incrementQuad.value instanceof Integer)) {
            throw new TypeMismatchException("Attempt to increment with non-integer values (" + currentValue + " + " + incrementQuad.value + ")", "INC", ctx.getInstructionCounter());
        }

        // 5. Compute the new value
        int newValue = (Integer) currentValue + (Integer) incrementQuad.value;

        // 6. Update memory
        boolean success = ctx.getStacks().affecterValJJC(scopedIdent, newValue);

        if (!success) {
            throw new AssignmentException(scopedIdent, "assignment failed", "INC", ctx.getInstructionCounter());
        }

        // 7. Success
        logger.debug("\t\tAxiome INC executed: {} += {} -> {}", scopedIdent, incrementQuad.value, newValue);
        ctx.incrementPC();
    }

    /**
     * Résout le nom scopé pour la récursivité.
     * Cherche d'abord la variable avec le suffixe de récursivité, puis sans.
     */
    private String resolveScopedName(MachineContext ctx, String ident) {
        // Si la variable est globale, pas de scopage
        if (ident.endsWith("@global") || !ident.contains("@")) {
            return ident;
        }

        // Vérifier si on est dans un contexte de méthode récursif
        String currentContext = ctx.getStacks().getCurrentContext();
        if (currentContext != null) {
            String methodName = ctx.getStacks().getCurrentMethodName();
            int recursionLevel = ctx.getStacks().getRecursionDepth(methodName);

            // Si on est en récursivité (niveau > 1), chercher la variable scopée
            if (recursionLevel > 1) {
                String scopedIdent = ident + "$" + (recursionLevel - 1);
                if (ctx.getStacks().findQuad(scopedIdent) != null) {
                    return scopedIdent;
                }
            }
        }

        // Sinon, retourner le nom original
        return ident;
    }
}

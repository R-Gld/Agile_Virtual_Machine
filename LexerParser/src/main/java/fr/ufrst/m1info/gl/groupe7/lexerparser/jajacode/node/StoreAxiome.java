package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.AssignmentException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Axiome representing the JajaCode instruction {@code store(i)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [store] : &lt;&lt;w, v, cst,*&gt;.m,a&gt; ⊢ store(i) –» &lt;AffecterVal(i,v,m), a+1&gt;
 * </pre>
 *
 * <p>This instruction pops a value {@code v} from the top of the stack and assigns
 * it to the variable identified by {@code i} in memory.</p>
 *
 * <p><b>Scoping:</b> This implementation supports recursion by resolving
 * scoped variable names (suffix {@code $N} for recursive calls).</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack is empty</li>
 *   <li>{@link AssignmentException} if the assignment fails</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class StoreAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(StoreAxiome.class);

    /**
     * Exécute l'instruction {@code store(i)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param ident l'identifiant de la variable à modifier
     * @throws StackUnderflowException si la pile est vide
     * @throws AssignmentException si l'affectation échoue
     */
    @Override
    public void execute(MachineContext ctx, String ident) {
        Stacks.Quad valeur = ctx.getStacks().pop();

        if (valeur == null) {
            throw new StackUnderflowException(JajaCodeInstr.STORE.toString(), ctx.getInstructionCounter());
        }

        // Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        boolean success = ctx.getStacks().affecterVal(scopedIdent, valeur.value);

        if (!success) {
            throw new AssignmentException(scopedIdent, "unable to assign value", JajaCodeInstr.STORE.toString(), ctx.getInstructionCounter());
        }

        logger.debug("\t\tAxiome STORE executed: {} updated with value {}.", scopedIdent, valeur.value);
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
                if (ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
                    return scopedIdent;
                }
            }
        }

        // Sinon, retourner le nom original
        return ident;
    }
}

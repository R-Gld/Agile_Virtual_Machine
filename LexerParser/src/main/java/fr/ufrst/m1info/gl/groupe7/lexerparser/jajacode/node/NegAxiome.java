package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code neg}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [op1] : &lt;&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ neg –» &lt;&lt;w, -v1, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile une valeur entière {@code v1},
 * effectue la négation arithmétique {@code -v1}, puis empile le résultat.</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide</li>
 *   <li>{@link TypeMismatchException} si l'opérande n'est pas de type entier</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class NegAxiome implements JajaAxiome {

    /**
     * Exécute l'instruction {@code neg}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile est vide
     * @throws TypeMismatchException si l'opérande n'est pas de type entier
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        Stacks.Quad op = ctx.getStacks().pop();

        if (op == null) {
            throw new StackUnderflowException("NEG", ctx.getInstructionCounter());
        }

        if (!(op.value instanceof Integer)) {
            throw new TypeMismatchException("Type invalide pour l'opération NEG: " + op.value, "NEG", ctx.getInstructionCounter());
        }

        int result = -(Integer) op.value;
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), result, ctx.getTEMP_VALUE(), Type.ENTIER));

        System.out.println("\t\tAxiome UNARY MINUS exécuté: -" + op.value + " = " + result);
        ctx.incrementPC();
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code mul}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [op2] : &lt;&lt;w, v2,cst,*&gt;.&lt;w, v1,cst,*&gt;.m,a&gt; ⊢ mul –» &lt;&lt;w, v1 * v2, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile deux valeurs entières {@code v1} et {@code v2},
 * effectue la multiplication {@code v1 * v2}, puis empile le résultat.</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile contient moins de 2 éléments</li>
 *   <li>{@link TypeMismatchException} si les opérandes ne sont pas de type entier</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class MulAxiome implements JajaAxiome {

    /**
     * Exécute l'instruction {@code mul}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile contient moins de 2 éléments
     * @throws TypeMismatchException si les opérandes ne sont pas de type entier
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        // 1. Dépilement
        Stacks.Quad op2 = ctx.getStacks().pop();
        Stacks.Quad op1 = ctx.getStacks().pop();

        // 2. Vérification pile vide
        if (op1 == null || op2 == null) {
            throw new StackUnderflowException("Besoin de 2 opérandes", "MUL", ctx.getInstructionCounter());
        }

        // 3. Vérification de type (Entiers uniquement)
        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            throw new TypeMismatchException(
                "Opérandes non entiers (" + op1.value + " * " + op2.value + ")",
                "MUL",
                ctx.getInstructionCounter()
            );
        }

        // 5. Calcul
        int result = (Integer) op1.value * (Integer) op2.value;

        // 6. Empilement du résultat
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), result, ctx.getTEMP_VALUE(), Type.ENTIER));

        // 7. Log et suite
        System.out.println("\t\tAxiome MUL exécuté: " + op1.value + " * " + op2.value + " = " + result);
        ctx.incrementPC();
    }
}
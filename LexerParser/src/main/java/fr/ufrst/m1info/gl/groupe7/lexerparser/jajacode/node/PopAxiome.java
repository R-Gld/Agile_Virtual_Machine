package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Axiome représentant l'instruction JajaCode {@code pop}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [pop] : &lt;&lt;i,v,o,t&gt;.m,a&gt; ⊢ pop –» &lt;m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction dépile l'élément au sommet de la pile et le retire de la mémoire.
 * Si l'élément est un tableau (sorte = tab), celui-ci doit également être retiré du tas.</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class PopAxiome implements JajaAxiome {

    /**
     * Exécute l'instruction {@code pop}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg paramètre non utilisé pour cette instruction
     * @throws StackUnderflowException si la pile est vide
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        Stacks.Quad popped = ctx.getStacks().pop();

        if (popped == null) {
            throw new StackUnderflowException("Rien à dépiler", "POP", ctx.getInstructionCounter());
        }

        System.out.println("\t\tAxiome POP exécuté: valeur dépilée = " + popped.value);
        ctx.incrementPC();
    }
}

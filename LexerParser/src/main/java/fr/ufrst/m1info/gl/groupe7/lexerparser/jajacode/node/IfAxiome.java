package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.InvalidAddressException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Axiome representing the JajaCode instruction {@code if(a1)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [iftrue]  : &lt;&lt;w,true, cst, *&gt;.m,a&gt; ⊢ if(a1) –» &lt;m,a1&gt;
 * [iffalse] : &lt;&lt;w,false, cst, *&gt;.m,a&gt; ⊢ if(a1) –» &lt;m,a+1&gt;
 * </pre>
 *
 * <p>This instruction performs a conditional branch based on the value
 * on top of the stack:</p>
 * <ul>
 *   <li>If the condition is true: jump to address {@code a1}</li>
 *   <li>If the condition is false: continue to the next instruction</li>
 * </ul>
 *
 * <p><b>Accepted types:</b> The condition can be boolean or integer
 * (0 = false, any other value = true).</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} if the stack is empty</li>
 *   <li>{@link InvalidAddressException} if the jump address is invalid</li>
 *   <li>{@link TypeMismatchException} if the condition type is invalid</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class IfAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(IfAxiome.class);

    /**
     * Exécute l'instruction {@code if(a1)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param adresseArg l'adresse de saut si la condition est vraie
     * @throws StackUnderflowException si la pile est vide
     * @throws InvalidAddressException si l'adresse de saut est invalide
     * @throws TypeMismatchException si le type de la condition est invalide
     */
    @Override
    public void execute(MachineContext ctx, String adresseArg) {
        // 1. Parse the jump address
        int targetAddress;
        try {
            targetAddress = Integer.parseInt(adresseArg);
        } catch (NumberFormatException e) {
            throw new InvalidAddressException(adresseArg, JajaCodeInstr.IF.toString(), ctx.getInstructionCounter());
        }

        // 2. Retrieve the condition from the stack
        Stacks.Quad conditionQuad = ctx.getStacks().pop();

        if (conditionQuad == null) {
            throw new StackUnderflowException(JajaCodeInstr.IF.toString(), ctx.getInstructionCounter());
        }

        // 3. Evaluate truthiness
        boolean isTrue;
        Object val = conditionQuad.value;

        if (val instanceof Boolean b) {
            isTrue = b;
        } else if (val instanceof Integer i) {
            isTrue = i != 0;
        } else {
            throw new TypeMismatchException("Type de condition invalide (" + val + ")", JajaCodeInstr.IF.toString(), ctx.getInstructionCounter());
        }

        // 4. Branching logic
        if (isTrue) {
            // Saut : On force le PC à la nouvelle adresse
            ctx.setInstructionCounter(targetAddress);
            logger.debug("\t\tAxiome IF executed: TRUE -> jump to {}", targetAddress);
        } else {
            // Pas de saut : On continue séquentiellement
            ctx.incrementPC();
            logger.debug("\t\tAxiome IF executed: FALSE -> continue sequentially");
        }
    }
}

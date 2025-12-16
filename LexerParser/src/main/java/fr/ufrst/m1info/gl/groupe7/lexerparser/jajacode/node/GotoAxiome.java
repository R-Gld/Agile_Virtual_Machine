package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.InvalidAddressException;

/**
 * Axiome representing the JajaCode instruction {@code goto(a1)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [goto] : &lt;m,a&gt; ⊢ goto(a1) –» &lt;m, a1&gt;
 * </pre>
 *
 * <p>This instruction performs an unconditional jump to address {@code a1}.
 * The program counter (PC) is directly set to the target address without
 * modifying the stack.</p>
 *
 * <p><b>Exceptions:</b></p>
 * <ul>
 *   <li>{@link InvalidAddressException} if the jump address is not a valid integer</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class GotoAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(GotoAxiome.class);

    /**
     * Exécute l'instruction {@code goto(a1)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param adresseArg l'adresse cible du saut
     * @throws InvalidAddressException si l'adresse n'est pas un entier valide
     */
    @Override
    public void execute(MachineContext ctx, String adresseArg) {
        try {
            // 1. Convert the argument (String) to an address (int)
            int adresse = Integer.parseInt(adresseArg);

            // 2. Directly set the PC
            ctx.setInstructionCounter(adresse);

            logger.debug("\t\tAxiome GOTO executed: jumped to address {}.", adresse);

        } catch (NumberFormatException e) {
            throw new InvalidAddressException(adresseArg, JajaCodeInstr.GOTO.toString(), ctx.getInstructionCounter());
        }
    }
}

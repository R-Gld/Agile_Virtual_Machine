package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.InvalidAddressException;

/**
 * Axiome représentant l'instruction JajaCode {@code goto(a1)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [goto] : &lt;m,a&gt; ⊢ goto(a1) –» &lt;m, a1&gt;
 * </pre>
 *
 * <p>Cette instruction effectue un saut inconditionnel à l'adresse {@code a1}.
 * Le compteur de programme (PC) est directement modifié pour pointer vers
 * l'adresse cible, sans modifier la pile.</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link InvalidAddressException} si l'adresse de saut n'est pas un entier valide</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class GotoAxiome implements JajaAxiome {

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
            // 1. Conversion de l'argument (String) en adresse (int)
            int adresse = Integer.parseInt(adresseArg);

            // 2. Mise à jour directe du PC
            ctx.setInstructionCounter(adresse);

            System.out.println("\t\tAxiome GOTO exécuté: saut à l'adresse " + adresse + ".");

        } catch (NumberFormatException e) {
            throw new InvalidAddressException(adresseArg, "GOTO", ctx.getInstructionCounter());
        }
    }
}
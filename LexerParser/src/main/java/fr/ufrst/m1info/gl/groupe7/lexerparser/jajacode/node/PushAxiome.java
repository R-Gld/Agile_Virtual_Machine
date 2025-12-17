package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.memoire.omega.Omega;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome representing the JajaCode instruction {@code push(v)}.
 *
 * <p><b>Formal semantics:</b></p>
 * <pre>
 * [push] : &lt;m,a&gt; ⊢ push(v) –» &lt;&lt;w, v, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>This instruction pushes an immediate value {@code v} onto the stack.
 * The value can be boolean ({@code true}, {@code false}),
 * integer, or empty (omega/nil).</p>
 *
 * <p>The value type is automatically detected while parsing the argument:
 * <ul>
 *   <li>Boolean: {@code true} or {@code false}</li>
 *   <li>Integer: sequence of digits</li>
 *   <li>Empty: any other value</li>
 * </ul>
 * </p>
 *
 * @see JajaAxiome
 */
public class PushAxiome implements JajaAxiome {

    private static final Logger logger = LoggerFactory.getLogger(PushAxiome.class);

    /**
     * Exécute l'instruction {@code push(v)}.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg la valeur à empiler sous forme de chaîne de caractères
     */
    @Override
    public void execute(MachineContext ctx, String arg) {
        Object valeur;
        Type type;

        // 1. Manual parsing of the string
        if ("true".equals(arg)) {
            valeur = true;
            type = Type.BOOLEEN;
        } else if ("false".equals(arg)) {
            valeur = false;
            type = Type.BOOLEEN;
        } else {
            try {
                // On essaie de voir si c'est un entier
                // Try to see if it's an integer
                valeur = Integer.parseInt(arg);
                type = Type.ENTIER;
            } catch (NumberFormatException e) {
                // If it's neither boolean nor integer, treat it as 'empty' (omega/nil)
                // Use Omega singleton instance instead of null for proper constant initialization
                valeur = Omega.getInstance();
                type = Type.VOID;
            }
        }

        // 2. Create the Quad and push
        ctx.getStacks().push(new Stacks.Quad(valeur, type));

        logger.debug("\t\tAxiome PUSH executed: {} ({}) pushed on the stack.", valeur, type);
        ctx.incrementPC();
    }
}

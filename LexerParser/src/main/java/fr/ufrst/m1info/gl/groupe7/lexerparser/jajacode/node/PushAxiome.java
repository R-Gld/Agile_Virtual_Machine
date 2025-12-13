package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.memoire.omega.Omega;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code push(v)}.
 * 
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [push] : &lt;m,a&gt; ⊢ push(v) –» &lt;&lt;w, v, cst,*&gt;.m, a+1&gt;
 * </pre>
 *
 * <p>Cette instruction empile une valeur immédiate {@code v} sur la pile.
 * La valeur peut être de type booléen ({@code true}, {@code false}),
 * entier, ou vide (oméga/nil).</p>
 *
 * <p>Le type de la valeur est automatiquement détecté lors du parsing de l'argument :
 * <ul>
 *   <li>Booléen : {@code true} ou {@code false}</li>
 *   <li>Entier : séquence de chiffres</li>
 *   <li>Vide : tout autre valeur</li>
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

        // 1. Parsing manuel de la chaîne de caractères
        if ("true".equals(arg)) {
            valeur = true;
            type = Type.BOOLEEN;
        } else if ("false".equals(arg)) {
            valeur = false;
            type = Type.BOOLEEN;
        } else {
            try {
                // On essaie de voir si c'est un entier
                valeur = Integer.parseInt(arg);
                type = Type.ENTIER;
            } catch (NumberFormatException e) {
                // Si ce n'est ni un booléen ni un entier, c'est 'vide' (omega/nil)
                // Use Omega singleton instance instead of null for proper constant initialization
                valeur = Omega.getInstance();
                type = Type.VOID;
            }
        }

        // 2. Création du Quad et Push
        // Note: pour un push de valeur immédiate, utilise le constructeur avec ident="_" (anonyme)
        ctx.getStacks().push(new Stacks.Quad(valeur, type));

        logger.debug("\t\tAxiome PUSH exécuté: {} ({}) poussé sur la pile.", valeur, type);
        ctx.incrementPC();
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Interface de base pour tous les axiomes JajaCode.
 *
 * <p>Chaque axiome représente une instruction de la machine virtuelle JajaCode et
 * implémente la sémantique formelle définie dans le cours de compilation.</p>
 *
 * <p>Les axiomes manipulent l'état de la machine via le {@link MachineContext},
 * qui contient la pile d'exécution, la table des symboles, et le compteur de programme (PC).</p>
 *
 * <p><b>Structure générale d'un axiome :</b></p>
 * <pre>
 * [nom] : &lt;m,a&gt; ⊢ instruction –» &lt;m',a'&gt;
 * </pre>
 * <p>où {@code m} est l'état de la mémoire, {@code a} est l'adresse (PC),
 * {@code m'} est le nouvel état, et {@code a'} est la nouvelle adresse.</p>
 *
 * @see MachineContext
 */
public interface JajaAxiome {
    /**
     * Exécute l'axiome sur le contexte de la machine virtuelle.
     *
     * @param ctx le contexte de la machine virtuelle contenant l'état d'exécution
     * @param arg l'argument de l'instruction (peut être un identifiant, une valeur, ou une adresse)
     */
    void execute(MachineContext ctx, String arg);
}

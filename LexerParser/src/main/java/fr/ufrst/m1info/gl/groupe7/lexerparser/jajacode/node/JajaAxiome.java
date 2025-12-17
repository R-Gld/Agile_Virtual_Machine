package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

/**
 * Base interface for all JajaCode axioms.
 *
 * <p>Each axiom represents an instruction of the JajaCode virtual machine and
 * implements the formal semantics defined in the compilation course.</p>
 *
 * <p>Axioms manipulate the machine state via the {@link MachineContext},
 * which contains the execution stack, symbol table, and the program counter (PC).</p>
 *
 * <p><b>General structure of an axiom:</b></p>
 * <pre>
 * [name] : &lt;m,a&gt; ⊢ instruction –» &lt;m',a'&gt;
 * </pre>
 * <p>where {@code m} is the memory state, {@code a} is the address (PC),
 * {@code m'} is the resulting state, and {@code a'} is the new address.</p>
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

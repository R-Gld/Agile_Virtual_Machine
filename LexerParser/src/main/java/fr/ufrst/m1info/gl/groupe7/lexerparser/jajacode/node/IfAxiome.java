package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.InvalidAddressException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.StackUnderflowException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.TypeMismatchException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Axiome représentant l'instruction JajaCode {@code if(a1)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [iftrue]  : &lt;&lt;w,true, cst, *&gt;.m,a&gt; ⊢ if(a1) –» &lt;m,a1&gt;
 * [iffalse] : &lt;&lt;w,false, cst, *&gt;.m,a&gt; ⊢ if(a1) –» &lt;m,a+1&gt;
 * </pre>
 *
 * <p>Cette instruction effectue un branchement conditionnel basé sur la valeur
 * au sommet de la pile :</p>
 * <ul>
 *   <li>Si la condition est vraie : saut à l'adresse {@code a1}</li>
 *   <li>Si la condition est fausse : continue à l'instruction suivante</li>
 * </ul>
 *
 * <p><b>Types acceptés :</b> La condition peut être de type booléen ou entier
 * (0 = faux, toute autre valeur = vrai).</p>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link StackUnderflowException} si la pile est vide</li>
 *   <li>{@link InvalidAddressException} si l'adresse de saut est invalide</li>
 *   <li>{@link TypeMismatchException} si le type de la condition est invalide</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class IfAxiome implements JajaAxiome {

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
        // 1. Parsing de l'adresse de saut
        int targetAddress;
        try {
            targetAddress = Integer.parseInt(adresseArg);
        } catch (NumberFormatException e) {
            throw new InvalidAddressException(adresseArg, "IF", ctx.getInstructionCounter());
        }

        // 2. Récupération de la condition sur la pile
        Stacks.Quad conditionQuad = ctx.getStacks().pop();

        if (conditionQuad == null) {
            throw new StackUnderflowException("IF", ctx.getInstructionCounter());
        }

        // 3. Évaluation de la vérité
        boolean isTrue;
        Object val = conditionQuad.value;

        if (val instanceof Boolean b) {
            isTrue = b;
        } else if (val instanceof Integer i) {
            isTrue = i != 0;
        } else {
            throw new TypeMismatchException("Type de condition invalide (" + val + ")", "IF", ctx.getInstructionCounter());
        }

        // 4. Logique de branchement
        if (isTrue) {
            // Saut : On force le PC à la nouvelle adresse
            ctx.setInstructionCounter(targetAddress);
            System.out.println("\t\tAxiome IF exécuté: VRAI -> saut à " + targetAddress);
        } else {
            // Pas de saut : On continue séquentiellement
            ctx.incrementPC();
            System.out.println("\t\tAxiome IF exécuté: FAUX -> suite séquentielle");
        }
    }
}
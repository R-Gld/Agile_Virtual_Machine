package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.UndefinedSymbolException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Axiome représentant l'instruction JajaCode {@code length(i)}.
 *
 * <p><b>Sémantique formelle :</b></p>
 * <pre>
 * [length] : &lt;m,a&gt; ⊢ length(i) –» &lt;&lt;w,long(I,m),cst,*&gt;, a+1&gt;
 * </pre>
 *
 * <p><b>Exceptions :</b></p>
 * <ul>
 *   <li>{@link UndefinedSymbolException} si le tableau n'existe pas</li>
 *   <li>{@link RuntimeException} si l'identifiant n'est pas un tableau (via Stacks.getArrayLength)</li>
 * </ul>
 *
 * @see JajaAxiome
 */
public class LengthAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String ident) {
        // 1. Résoudre le nom scopé pour la récursivité
        String scopedIdent = resolveScopedName(ctx, ident);

        // 2. Vérifier que le tableau existe
        if (!ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
            throw new UndefinedSymbolException(scopedIdent, "LENGTH", ctx.getInstructionCounter());
        }

        // 3. Récupérer la longueur du tableau
        int length = ctx.getStacks().getArrayLength(scopedIdent);

        // 4. Empiler la longueur (type ENTIER)
        ctx.getStacks().push(new Stacks.Quad(ctx.getTEMP_VALUE(), length,
                                              ctx.getTEMP_VALUE(), Type.ENTIER));

        System.out.println("\t\tAxiome LENGTH exécuté: length(" + scopedIdent + ") = " +
                           length + " empilé sur la pile.");

        // 5. Incrémenter PC
        ctx.incrementPC();
    }

    /**
     * Résout le nom scopé pour la récursivité.
     */
    private String resolveScopedName(MachineContext ctx, String ident) {
        if (ident.endsWith("@global") || !ident.contains("@")) {
            return ident;
        }

        String currentContext = ctx.getStacks().getCurrentContext();
        if (currentContext != null) {
            String methodName = ctx.getStacks().getCurrentMethodName();
            int recursionLevel = ctx.getStacks().getRecursionDepth(methodName);

            if (recursionLevel > 1) {
                String scopedIdent = ident + "$" + (recursionLevel - 1);
                if (ctx.getStacks().getSymbolTable().contains(scopedIdent)) {
                    return scopedIdent;
                }
            }
        }

        return ident;
    }
}

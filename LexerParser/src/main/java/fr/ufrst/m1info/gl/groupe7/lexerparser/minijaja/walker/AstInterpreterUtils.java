package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Utilitaires pour interpréter des noeuds AST de manière synchrone.
 * 
 * Utilisé notamment par AppelENode qui a besoin d'exécuter le corps
 * d'une méthode et d'obtenir la valeur de retour immédiatement,
 * sans attendre que le Walker parcours les children.
 */
public final class AstInterpreterUtils {

    private AstInterpreterUtils() {
        // Classe utilitaire, pas d'instanciation
    }

    /**
     * Interprète récursivement un noeud et tous ses enfants (préordre).
     * Équivalent à ce que fait le Walker mais de manière synchrone.
     */
    public static void interpretNode(AstNode node, Stacks stacks) {
        if (node == null)
            return;

        node.interpret(stacks);

        for (AstNode child : node.getChildren()) {
            interpretNode(child, stacks);
        }
    }

    /**
     * Interprète récursivement une liste d'instructions.
     */
    public static void interpretInstructions(InstructionsNode instrs, Stacks stacks) {
        if (instrs == null)
            return;

        InstructionNode instr = instrs.getInstructionNode();
        if (instr != null) {
            instr.interpret(stacks);
        }

        InstructionsNode next = instrs.getInstructions();
        if (next != null) {
            interpretInstructions(next, stacks);
        }
    }

    /**
     * Interprète récursivement les variables.
     */
    public static void interpretVars(VarsNode vars, Stacks stacks) {
        if (vars == null)
            return;

        for (AstNode child : vars.getChildren()) {
            child.interpret(stacks);
        }
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

import java.util.List;

public class InstructionsNode extends AstNode {

    private final InstructionNode instructionNode;
    private final InstructionsNode instructions;

    public InstructionsNode(InstructionNode instructionNode, InstructionsNode instructions) {
        this.instructionNode = instructionNode;
        this.instructions = instructions;
    }

    public InstructionsNode(InstructionNode instructionNode) {
        this.instructionNode = instructionNode;
        this.instructions = null;
    }

    public InstructionsNode() {
        this.instructionNode = null;
        this.instructions = null;

    }

    public InstructionsNode getInstructions() {
        return instructions;
    }

    public InstructionNode getInstructionNode() {
        return instructionNode;
    }

    @Override
    public String toStringTree() {
        
        if (instructionNode == null) {
            return "Inil";
        }

        return "Instrs(" + instructionNode.toStringTree() +
                "," + (instructions != null ? instructions.toStringTree() : null) +
                ")";
    }


    /**
     * Returns the child nodes of this {@code InstructionsNode}.
     * <p>
     * If both {@code instructionNode} and {@code instructions} are present,
     * returns a list containing both. If only {@code instructionNode} is present,
     * returns a singleton list containing it. If neither is present, returns an empty list.
     *
     * @return an {@code Iterable} of child {@code AstNode} instances.
     */
    @Override
    public Iterable<AstNode> getChildren() {
        if (instructions != null) {
            return List.of(instructionNode, instructions);
        } else if (instructionNode != null) {
            return List.of(instructionNode);
        } else {
            return List.of();
        }
    }

}

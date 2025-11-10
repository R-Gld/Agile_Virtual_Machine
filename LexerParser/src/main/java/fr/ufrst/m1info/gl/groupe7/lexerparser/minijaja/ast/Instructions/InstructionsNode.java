package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

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
                "," + instructions.toStringTree() +
                ")";
    }

}

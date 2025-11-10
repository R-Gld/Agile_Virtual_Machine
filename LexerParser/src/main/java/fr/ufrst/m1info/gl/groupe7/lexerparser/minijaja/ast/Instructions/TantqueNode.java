package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

public class TantqueNode extends InstructionNode {

    private final AstNode expressionNode;
    private final InstructionsNode instructionsNode;

    public TantqueNode(AstNode expressionNode, InstructionsNode instructionsNode) {
        this.expressionNode = expressionNode;
        this.instructionsNode = instructionsNode;
    }

    @Override
    public String toStringTree() {
        return "tantque(" + expressionNode.toStringTree() + "," + instructionsNode.toString() + ")";
    }
    
}

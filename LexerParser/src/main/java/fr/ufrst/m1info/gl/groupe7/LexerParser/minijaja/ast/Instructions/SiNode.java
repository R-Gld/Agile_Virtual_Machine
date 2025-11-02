package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.Instructions;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;

public class SiNode extends InstructionNode {

    private final AstNode expressionNode; // TODO: EXPRESSION
    private final InstructionsNode instructionsNode;
    private final InstructionsNode instructionsNode2;

    public SiNode(AstNode expressionNode, InstructionsNode instructionsNode) {
        this.expressionNode = expressionNode;
        this.instructionsNode = instructionsNode;
        this.instructionsNode2 = null;
    }

    public SiNode(AstNode expressionNode, InstructionsNode instructionsNode,
            InstructionsNode instructionsNode2) {
        this.expressionNode = expressionNode;
        this.instructionsNode = instructionsNode;
        this.instructionsNode2 = instructionsNode2;
    }

    @Override
    public String toStringTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("si (").append(expressionNode.toStringTree()).append(",").append(instructionsNode.toString());
        if (instructionsNode2 != null)
            sb.append(instructionsNode2);
        else
            sb.append("inil");
        sb.append(")");
        return sb.toString();
    }

}

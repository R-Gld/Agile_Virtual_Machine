package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

public class SommeNode extends InstructionNode {

    private final AstNode ident1Node;
    private final AstNode expressionNode;

    public SommeNode(AstNode ident1Node, AstNode expressionNode) {
        this.ident1Node = ident1Node;
        this.expressionNode = expressionNode;
    }

    @Override
    public String toStringTree() {
        return "somme(" + ident1Node.toStringTree() + expressionNode.toStringTree() + ")";
    }
    
}

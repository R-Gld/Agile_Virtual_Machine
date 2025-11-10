package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

public class IncrementNode extends InstructionNode {

    private final AstNode ident1;

    public IncrementNode(AstNode ident1) {
        this.ident1 = ident1;
    }

    @Override
    public String toStringTree() {
        return "Increment(" + ident1.toStringTree() + ")";

    }

}

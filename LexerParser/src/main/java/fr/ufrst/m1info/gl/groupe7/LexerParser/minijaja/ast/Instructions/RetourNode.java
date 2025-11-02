package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.Instructions;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;

public class RetourNode extends InstructionNode {

    private final AstNode Exp;

    public RetourNode(AstNode Exp) {
        this.Exp = Exp;
    }

    @Override
    public String toStringTree() {
        return "Retour(" + Exp.toStringTree() +
                ')';
    }
}

package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Fact;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Expression;

public class ListExpNode extends Expression {
    
    private AstNode exp;
    private ListExpNode listExp;

    public ListExpNode(AstNode exp, ListExpNode listExp) {
        this.exp = exp;
        this.listExp = listExp;
    }

    public ListExpNode getListExp() {
        return listExp;
    }

    public AstNode getExp() {
        return exp;
    }
    
    @Override
    public int getValue() {
        return 0;//gerer le retour de listExp
    }

    @Override
    public String toStringTree() {
        return "listExp(" + exp.toStringTree() + "," + listExp.toStringTree() + ")";
    }

}

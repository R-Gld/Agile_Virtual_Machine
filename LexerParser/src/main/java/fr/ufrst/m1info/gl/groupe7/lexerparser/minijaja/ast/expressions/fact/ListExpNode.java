package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class ListExpNode extends Expression {
    
    private final AstNode exp;
    private final ListExpNode listExp;

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
    public Object evaluate(Stacks stack) {
        return 0;//gerer le retour de listExp
    }

    @Override
    public String toStringTree() {
        return "listExp(" + exp.toStringTree() + "," + listExp.toStringTree() + ")";
    }

}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;

public class NotNode extends Expression {
    private int value;
    private final Expression exp; // expression to negate

    public NotNode(Expression exp) {
        this.exp = exp;
        this.value =  exp.getValue(); //add !
    }

    public AstNode getExp() {
        return exp;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toStringTree() {
        return "non (" + exp.toStringTree() + ")";
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.vexp;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;

public class Vexp {
    private int value;
    private final Expression exp; 

    public Vexp(Expression exp) {
        this.exp = exp;
        this.value = exp.getValue();
    }
    public Vexp() {
        this.exp = null;
    } //TODO: OMEGA

    public Expression getVexp() {
        return exp;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Vexp{" + "vexp=" + exp + '}';
    }

    public String toStringTree() {
        if(exp == null) return "omega";
        return exp.toStringTree();
    }


   

}

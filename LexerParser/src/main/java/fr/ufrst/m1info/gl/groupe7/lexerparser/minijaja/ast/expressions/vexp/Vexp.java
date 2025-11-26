package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.vexp;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class Vexp {

    private final Expression exp; 

    public Vexp(Expression exp) {
        this.exp = exp;

    }
    public Vexp() {
        this.exp = null;
    } 

    public Expression getVexp() {
        return exp;
    }

    public Object evaluate(Stacks stack) {
        if (exp != null) {
            return exp.evaluate(stack);
        }
        return null;
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

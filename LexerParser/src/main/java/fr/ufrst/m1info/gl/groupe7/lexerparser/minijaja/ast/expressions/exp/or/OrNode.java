package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class OrNode extends Expression {

    private final Expression exp;
    private final Expression exp1;

    public OrNode(Expression exp, Expression exp1) {
        this.exp = exp;
        this.exp1 = exp1;

    }

    public Expression getExp() {
        return exp;
    }

    public Expression getExp1() {
        return exp1;
    }

    public Object evaluate(Stacks stack) {
        return (Boolean) exp.evaluate(stack) || (Boolean) exp1.evaluate(stack);
    }

    @Override
    public String toStringTree() {
        return "ou (" + exp.toStringTree() + "," + exp1.toStringTree() + ")";
    }

}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class GreaterThanNode extends Expression {

    
    private final Expression exp1; // left expression
    private final Expression exp2; // right expression

    public GreaterThanNode(Expression exp1, Expression exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
    }

    public Expression getExp1() {
        return exp1;
    }

    public Expression getExp2() {
        return exp2;
    }

    @Override
    public Object evaluate(Stacks stack) {
        Object v1 = exp1.evaluate(stack);
        Object v2 = exp2.evaluate(stack);

        if (!(v1 instanceof Integer) || !(v2 instanceof Integer)) {
            throw new RuntimeException(String.format(
                "Type error: > requires two integers (got %s and %s)",
                v1 == null ? "null" : v1.getClass().getSimpleName(),
                v2 == null ? "null" : v2.getClass().getSimpleName()
            ));
        }

        return (Integer) v1 > (Integer) v2;
    }

  
    @Override
    public String toStringTree() {
        return "> (" + exp1.toStringTree() + "," + exp2.toStringTree() + ")";
    }
}

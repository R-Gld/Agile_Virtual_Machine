package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class MultiplicationNode extends Expression {
    
    private final Expression terme;
    private final Expression fact;

    public MultiplicationNode(Expression terme, Expression fact) {
        this.terme = terme;
        this.fact = fact;

    }

    public Expression getTerme() {
        return terme;
    }

    public Expression getFact() {
        return fact;
    }

    @Override
    public Object evaluate(Stacks stack) {
        return  (int) terme.evaluate(stack) * (int) fact.evaluate(stack);
    }

    @Override
    public String toStringTree() {
        return "* (" + terme.toStringTree() + "," + fact.toStringTree() + ")";

    }
}

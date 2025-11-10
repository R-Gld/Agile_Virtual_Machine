package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class UnaryMinusNode extends Expression {
    
    private final Expression terme;

    public UnaryMinusNode(Expression terme) {
        this.terme = terme;

    }

    public Expression getTerme() {
        return terme;
    }

    public Object evaluate(Stacks stack) {
        return - (int) terme.evaluate(stack);
    }

    @Override
    public String toString() {
        return "-" + terme.toString();
    }

    @Override
    public String toStringTree() {
        return "- (" + terme.toStringTree() + ")";
    }
}

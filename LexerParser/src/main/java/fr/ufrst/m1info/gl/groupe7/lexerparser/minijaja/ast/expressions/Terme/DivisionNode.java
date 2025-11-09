package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Terme;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;

public class DivisionNode extends Expression {
    private final int value;
    private final Expression terme;
    private final Expression fact;

    public DivisionNode(Expression terme, Expression fact) {
        this.terme = terme;
        this.fact = fact;
        this.value = terme.getValue() / fact.getValue();
    }

    public Expression getTerme() {
        return terme;
    }

    public Expression getFact() {
        return fact;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String toStringTree() {
        return "/ (" + terme.toStringTree() + "," + fact.toStringTree() + ")";
    }
}

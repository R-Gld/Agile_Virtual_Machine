package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Terme;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Expression;

public class MultiplicationNode extends Expression {
    private final int value;
    private final Expression terme;
    private final Expression fact;

    public MultiplicationNode(Expression terme, Expression fact) {
        this.terme = terme;
        this.fact = fact;
        this.value = terme.getValue() * fact.getValue();
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
        return "* (" + terme.toStringTree() + "," + fact.toStringTree() + ")";

    }
}

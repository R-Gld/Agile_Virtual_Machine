package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;

public class UnaryMinusNode extends Expression {
    int value;
    private final Expression terme;

    public UnaryMinusNode(Expression terme) {
        this.terme = terme;
        this.value = -terme.getValue();
    }

    public Expression getTerme() {
        return terme;
    }

    public int getValue() {
        return value;
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

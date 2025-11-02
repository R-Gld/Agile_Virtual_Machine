package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.exp2.plus;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Expression;

public class PlusNode extends Expression {
    private int value;
    private final Expression exp2; // left expression
    private final Expression terme; // right term

    public PlusNode(Expression exp2, Expression terme) {
        this.exp2 = exp2;
        this.terme = terme;
        this.value = exp2.getValue() + terme.getValue();
    }

    public Expression getExp2() {
        return exp2;
    }

    public Expression getTerme() {
        return terme;
    }
    public int getValue() {
        return value;
    }
    @Override
    public String toStringTree() {
        return "+ (" + exp2.toStringTree() + "," + terme.toStringTree() + ")";
    }
}

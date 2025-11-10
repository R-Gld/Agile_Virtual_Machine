package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;

public class MinusNode extends Expression {
    private int value;
    private final Expression exp2; // left expression
    private final Expression terme; // right term

    public MinusNode(Expression exp2, Expression terme) {
        this.exp2 = exp2;
        this.terme = terme;
        this.value = exp2.getValue() - terme.getValue();
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
        return "- (" + exp2.toStringTree() + "," + terme.toStringTree() + ")";
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;

public class OrNode extends Expression {
    private int value;
    private final Expression exp; // left expression
    private final Expression exp1; // right expression

    public OrNode(Expression exp, Expression exp1) {
        this.exp = exp;
        this.exp1 = exp1;
        this.value = exp.getValue() | exp1.getValue();
    }

    public Expression getExp() {
        return exp;
    }

    public Expression getExp1() {
        return exp1;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toStringTree() {
        return "ou (" + exp.toStringTree() + "," + exp1.toStringTree() + ")";
    }

}

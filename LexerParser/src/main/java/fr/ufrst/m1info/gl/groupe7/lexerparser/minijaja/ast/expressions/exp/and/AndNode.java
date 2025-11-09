package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;

public class AndNode extends Expression {
    private int value;
    private Expression exp; // TODO : expNode
    private Expression exp1; // TODO : exp1Node

    public AndNode(Expression exp, Expression exp1) {
        this.exp = exp;
        this.exp1 = exp1;
        this.value = exp.getValue() & exp1.getValue();
    }

    public Expression getExp() {
        return exp;
    }

    public Expression getExp1() {
        return exp1;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
   public String toStringTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("et (").append(exp.toStringTree()).append(",").append(exp1.toStringTree()).append(")");
        return sb.toString();
    }
}

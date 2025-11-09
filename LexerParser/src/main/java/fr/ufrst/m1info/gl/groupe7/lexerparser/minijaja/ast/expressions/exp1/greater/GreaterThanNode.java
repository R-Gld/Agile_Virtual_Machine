package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
public class GreaterThanNode extends Expression {

    private int value;
    private final Expression exp1; // left expression
    private final Expression exp2; // right expression

    public GreaterThanNode(Expression exp1, Expression exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
        this.value = exp1.getValue() > exp2.getValue() ? 1 : 0;
    }

    public Expression getExp1() {
        return exp1;
    }

    public Expression getExp2() {
        return exp2;
    }

    public int getValue() {
        return value;
    }

  
    @Override
    public String toStringTree() {
        return ">" + exp1.toStringTree() + "," + exp2.toStringTree();
    }
}

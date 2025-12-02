package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class AndNode extends Expression {

    private final Expression exp;
    private final Expression exp1;

    public AndNode(Expression exp, Expression exp1) {
        this.exp = exp;
        this.exp1 = exp1;

    }

    public Expression getExp() {
        return exp;
    }

    public Expression getExp1() {
        return exp1;
    }

    @Override
    public Object evaluate(Stacks stack) {
        if(!(exp.evaluate(stack) instanceof Boolean) || !(exp1.evaluate(stack) instanceof Boolean)){
            throw new RuntimeException("Type error: both expressions must evaluate to Boolean");
        }
        return (Boolean) exp.evaluate(stack) && (Boolean) exp1.evaluate(stack);
    }

    @Override
   public String toStringTree() {
        return "et (" + exp.toStringTree() + "," + exp1.toStringTree() + ")";
    }
}

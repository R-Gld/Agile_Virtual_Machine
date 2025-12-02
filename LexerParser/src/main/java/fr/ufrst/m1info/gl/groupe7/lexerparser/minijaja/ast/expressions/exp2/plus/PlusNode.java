package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus;

import java.util.List;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class PlusNode extends Expression {

    private final Expression exp2; // left expression
    private final Expression terme; // right term

    public PlusNode(Expression exp2, Expression terme) {
        this.exp2 = exp2;
        this.terme = terme;

            }

    public Expression getExp2() {
        return exp2;
    }

    public Expression getTerme() {
        return terme;
    }

    public Object evaluate(Stacks stack) {
        if(!(exp2.evaluate(stack) instanceof Integer) || !(terme.evaluate(stack) instanceof Integer)){
            throw new RuntimeException("Type error: both expressions must evaluate to Integer");
        }
        return      (int) exp2.evaluate(stack) + (int) terme.evaluate(stack);
    }
    @Override
    public String toStringTree() {
        return "+ (" + exp2.toStringTree() + "," + terme.toStringTree() + ")";
    }
    @Override
    public Iterable<AstNode> getChildren() {
        return List.of(exp2, terme);
    }
}

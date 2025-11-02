package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.Instructions;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Expression;

public class AffectationNode extends InstructionNode {

    private final AstNode ident1Node;

    private final Expression expression;

    public AffectationNode(AstNode ident1Node, Expression expression) {
        this.ident1Node = ident1Node;
        this.expression = expression;
    }

    public AstNode getIdent1Node() {
        return ident1Node;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toStringTree() {
        String sb = "affectation(" + ident1Node.toStringTree() + "," + expression.toStringTree() +
                ")";
        return sb;
    }

}

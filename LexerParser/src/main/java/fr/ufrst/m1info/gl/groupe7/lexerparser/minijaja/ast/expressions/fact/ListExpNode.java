package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.ArrayList;
import java.util.List;

public class ListExpNode extends Expression {
    
    private final Expression exp;
    private final ListExpNode listExp;

    public ListExpNode(Expression exp, ListExpNode listExp) {
        this.exp = exp;
        this.listExp = listExp;
    }

    public ListExpNode getListExp() {
        return listExp;
    }

    public Expression getExp() {
        return exp;
    }
    
    @Override
    public Object evaluate(Stacks stack) {

        if (exp == null) {
            return new ArrayList<>();
        }

        Object headValue = exp.evaluate(stack);

        List<Object> tailValues;
        if (listExp == null) {
            tailValues = new ArrayList<>();
        } else {
            tailValues = (List<Object>) listExp.evaluate(stack);
        }

        List<Object> result = new ArrayList<>();
        result.add(headValue);
        result.addAll(tailValues);
        return result;
    }

    @Override
    public String toStringTree() {
        return "listExp(" + exp.toStringTree() + "," + listExp.toStringTree() + ")";
    }

}

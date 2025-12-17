package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public final class NbreNode extends Expression {
    public final int value;

    public NbreNode(int value) {
            this.value = value;
        }
    public int getValue() {
        return value;
    }

    @Override
    public Object evaluate(Stacks stack) {
        return value;
    }

    @Override
    public String toStringTree() {
        return "nbre("+ value +")";
    }

}
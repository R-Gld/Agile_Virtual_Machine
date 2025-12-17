package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public final class BoolValueNode extends Expression {
    public final boolean value;

    public BoolValueNode(boolean value) {
        this.value = value;
    }
    public boolean getValue() {
        return value;
    }

    public Object evaluate(Stacks stack) {

        return  value;
    }

    @Override
    public String toStringTree() {
        return String.valueOf(value);
    }

}

package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Fact;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Expression;

public final class BoolValueNode extends Expression {
    public final boolean value;

    public BoolValueNode(boolean value) {
        this.value = value;
    }

    public int getValue() {
        return value ? 1 : 0;
    }

    @Override
    public String toStringTree() {
        return String.valueOf(value);
    }

}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;

public final class NbreNode extends Expression {
    public final int value;

    public NbreNode(int value) {
            this.value = value;
        }
   
        public int getValue() {
            return value;
        }

    @Override
    public String toStringTree() {
        return "nbre("+ value +")";
    }

}
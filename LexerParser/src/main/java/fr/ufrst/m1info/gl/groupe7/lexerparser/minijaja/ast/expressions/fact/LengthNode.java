package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class LengthNode extends Expression {

    private final IdentNode ident;

    public LengthNode(IdentNode ident) {
        this.ident = ident;
    }

    public IdentNode getId() {
        return ident;
    }

    public Object evaluate(Stacks stack) {
        
        if (! stack.getObjectType(ident.getNom()).equals("tab")) {
            throw new RuntimeException("Type error: length can only be applied to arrays");
            
        }

        //todo: ADD function to get tab length
        return stack.getArrayLength(ident.getNom());
    }   

    @Override
    public String toStringTree() {
        return "length(" + ident.toStringTree() + ")";
    }

}

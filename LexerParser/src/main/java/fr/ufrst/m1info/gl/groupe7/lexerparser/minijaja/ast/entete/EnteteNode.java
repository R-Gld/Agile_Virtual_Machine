package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.AstEntetes;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class EnteteNode extends AstEntetes {
    private final IdentNode ident;
    private final Type type;

    public EnteteNode(IdentNode ident, Type type) {
        this.ident = ident;
        this.type = type;
    }

    public IdentNode getIdent() {
     return ident;
    }

    public Type getType() {
        return type;
    }



    @Override
    public String toString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toString'");
    }

    @Override
    public String toStringTree() {
        return "entete (" +  type.toString() + " , "+ ident.toStringTree()  + ")";

    }


    @Override
    public EnteteNode evaluate(Stacks stacks) {
        return this;
    }
}

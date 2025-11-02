package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.entete;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.entetes.AstEntetes;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.ident.IdentNode;

public class EnteteNode extends AstEntetes {
    private final IdentNode ident;
    private final String type;

    public EnteteNode(IdentNode ident, String type) {
        this.ident = ident;
        this.type = type;
    }

    public IdentNode getIdent() {
     return ident;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toString'");
    }

    @Override
    public String toStringTree() {
        return "Entete (" + type + " , " + ident.toStringTree() + ")";

    }
}

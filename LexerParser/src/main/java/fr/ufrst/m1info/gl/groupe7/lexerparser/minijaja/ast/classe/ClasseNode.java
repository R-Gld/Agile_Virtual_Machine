package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;

public class ClasseNode extends AstNode {

    private final String varClasse;
    private final IdentNode ident;
    private final DeclsNode declarations;
    private final AstNode methodeMain;

    public ClasseNode(IdentNode ident, DeclsNode declarations, AstNode methodeMain) {
        this.varClasse = ident.getNom();
        this.ident = ident;
        this.declarations = declarations;
        this.methodeMain = methodeMain;
    }
    public String getVarClasse() {
        return varClasse;
    }

    public AstNode getMethodeMain() {
        return methodeMain;
    }

    public DeclsNode getDeclarations() {
        return declarations;
    }

    public IdentNode getIdent() {
        return ident;
    }

    @Override
    public String toStringTree() {
        String sb = "Classe(" + ident.toStringTree() + "," +
                declarations.toStringTree() + "," +
                methodeMain.toStringTree() + ")";
        return sb;
    }
}

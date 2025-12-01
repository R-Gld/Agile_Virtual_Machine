package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait.rDeclrs;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.List;
public class ClasseNode extends AstNode {

    private final String varClasse;
    private final IdentNode ident;
    private final DeclsNode declarations;
    private final AstNode methodeMain;
    private final rDeclrs rdeclrs;

    public ClasseNode(IdentNode ident, DeclsNode declarations, AstNode methodeMain) {
        this.varClasse = ident.getNom();
        this.ident = ident;
        this.declarations = declarations;
        this.methodeMain = methodeMain;
        this.rdeclrs = new rDeclrs(declarations);
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
        return "Classe(" + ident.toStringTree() + "," +
                declarations.toStringTree() + "," +
                methodeMain.toStringTree() + ")";
    }
    @Override
    public Iterable<AstNode> getChildren() {
        if (declarations != null) {
            return List.of(ident, declarations, methodeMain, rdeclrs);
        } else {
            return List.of(ident, methodeMain);
        }
    }
    @Override
    public void interpret(Stacks stacks) {
        // TODO ?
    }
}
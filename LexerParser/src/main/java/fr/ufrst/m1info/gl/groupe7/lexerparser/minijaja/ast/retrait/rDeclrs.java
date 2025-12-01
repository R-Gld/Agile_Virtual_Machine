package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class rDeclrs extends AstNode {
    private final DeclsNode declrs;

    public rDeclrs(DeclsNode declrs) {
        this.declrs = declrs;
    }

    public DeclsNode getDeclrs() {
        return declrs;
    }

    @Override
    public String toString() {
        return "rDeclrs{}";
    }

    public void interpret(Stacks stacks) {
    if (declrs == null) {
        return;
    }

    // interpret the rest of the declarations if any
    DeclsNode rest = declrs.getDecls();
    if (rest != null) {
        new rDeclrs(rest).interpret(stacks);
    }

    // interpret the current declaration if present
    AstNode decl = declrs.getDecl();
    if (decl != null) {
        if (decl instanceof VarsNode) {
         new rVars((VarsNode) decl).interpret(stacks);
        } else if (decl instanceof MethodeNode) {
         new rMethode((MethodeNode) decl).interpret(stacks);
        }
    }
    }

    @Override
    public String toStringTree() {
       return "";
    }
}

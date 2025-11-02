package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.decls;


import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.var.VarNode;

public class DeclsNode extends AstDecls {

    private final VarNode decl; //TODO: ADD METHODE AFTER
    private final DeclsNode decls;

    public DeclsNode(VarNode decl, DeclsNode decls) {
        this.decl = decl;
        this.decls = decls;
    }

    public DeclsNode() {
        this.decl = null;
        this.decls = null;
    }

    public VarNode getDecl() {
        return decl;
    }

    public DeclsNode getDecls() {
        return decls;
    }

    @Override
    public String toString() {
        return decl == null ? "" : decl + (decls != null ? ";" + decls : "");
    }

    @Override
    public String toStringTree() {
        StringBuilder sb = new StringBuilder();
       if (decls == null && decl == null) {
           sb.append("vnil");
       } else {
           sb.append("decls (").append(decl.toStringTree()).append(",").append(decls.toStringTree()).append(")");
       }
        return sb.toString();
    }

}

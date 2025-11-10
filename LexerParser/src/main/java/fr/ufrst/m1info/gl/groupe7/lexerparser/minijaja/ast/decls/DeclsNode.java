package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;

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
           String declSpringTree = decl == null ? "vnil" : decl.toStringTree();
           String declsSpringTree = decls == null ? "vnil" : decls.toStringTree();
           sb.append("decls (").append(declSpringTree).append(",").append(declsSpringTree).append(")");
       }
        return sb.toString();
    }

}

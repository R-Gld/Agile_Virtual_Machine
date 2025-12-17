package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class rVar {
    private final  AstNode var;


    public rVar( AstNode var) {
        this.var = var;
    }
    public  AstNode getVar() {
        return var;
    }

    public void interpret(Stacks stacks) {
        AstNode node = var;
        String name = "";

        if (node instanceof VarNode) {
            name = ((VarNode) node).getIdent().getNom();
        } else if (node instanceof TableauNode) {
            name = ((TableauNode) node).getIdent().getNom();
        } else if (node instanceof CstNode) {
            name = ((CstNode) node).getIdent().getNom();
        }

        if (name != null) {
            if (stacks.isInMethodContext()) {
                name = stacks.getScopedName(name);
            }
            stacks.retirerDecl(name);
        }

    }

    public String toStringStree() {
        return "Retrait Var";
    }

}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public record rVar(AstNode variable) {

    public void interpret(Stacks stacks) {
        AstNode node = variable;
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

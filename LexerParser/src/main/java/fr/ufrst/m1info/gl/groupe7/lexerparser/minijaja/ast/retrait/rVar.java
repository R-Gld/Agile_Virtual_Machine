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
       
        if (var instanceof VarNode) {
            VarNode varNode = (VarNode) var;
            String varName = varNode.getIdent().getNom();
            // Use scoped name if in method context
            if (stacks.isInMethodContext()) {
                varName = stacks.getScopedName(varName);
            }
            stacks.RetirerDecl(varName);
        } else if (var instanceof TableauNode) {
            TableauNode tableauNode = (TableauNode) var;
            String tableauName = tableauNode.getIdent().getNom();
            // Use scoped name if in method context
            if (stacks.isInMethodContext()) {
                tableauName = stacks.getScopedName(tableauName);
            }
            //TODO: retirer le tableau de la pile des tableaux (refaire une methode specfique)
            stacks.RetirerDecl(tableauName);
        }else if (var instanceof CstNode) {
            CstNode cstNode = (CstNode) var;
            String cstName = cstNode.getIdent().getNom();
            // Use scoped name if in method context
            if (stacks.isInMethodContext()) {
                cstName = stacks.getScopedName(cstName);
            }
            stacks.RetirerDecl(cstName);
        }

        
}}

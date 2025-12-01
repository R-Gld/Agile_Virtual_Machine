package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
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
           // stacks.getVarStack().popVariable(varName);
        } else if (var instanceof TableauNode) {
            TableauNode tableauNode = (TableauNode) var;
            String tableauName = tableauNode.getIdent().getNom();
            stacks.freeTab(tableauName);
        }else if (var instanceof CstNode) {
            CstNode cstNode = (CstNode) var;
            String cstName = cstNode.getIdent().getNom();
          //  stacks.getCstStack().popConstant(cstName);
        }

        
}}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class rEntetes extends AstNode {
    private final EntetesNode entetes;

    public rEntetes(AstNode entetes) {
        this.entetes = (EntetesNode) entetes;
    }

    public EntetesNode getEntetes() {
        return entetes;
    }

    @Override
    public String toStringTree() {
       return "";
    }


    public void interpret(Stacks stacks) {
        // If there are no entetes, return
        if (entetes == null) {
            return;
        }
        
        // Recursively remove rest of entetes first
        if (entetes.getEntetes() != null) {
            rEntetes rest = new rEntetes(entetes.getEntetes());
            rest.interpret(stacks);
        }

        // Remove current entete parameter
        if (entetes.getEntete() != null) {
            EnteteNode enteteNode = entetes.getEntete();
            String ident = enteteNode.getIdent().getNom();
            // Parameters are always scoped with method name
            if (stacks.isInMethodContext()) {
                ident = stacks.getScopedName(ident);
            }
            stacks.retirerDecl(ident);
    }}
    
}
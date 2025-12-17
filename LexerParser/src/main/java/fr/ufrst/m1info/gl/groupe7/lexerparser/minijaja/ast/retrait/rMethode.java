package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public record rMethode(MethodeNode methode) {
    public void interpret(Stacks stacks) {

        stacks.retirerDecl(methode.getIdent().getNom());

    }


}

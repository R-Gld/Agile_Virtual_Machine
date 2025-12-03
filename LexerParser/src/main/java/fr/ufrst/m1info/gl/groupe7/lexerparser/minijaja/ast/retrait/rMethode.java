package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class rMethode  {
     private final MethodeNode methode;
     
    public rMethode(MethodeNode methode) {
        this.methode = methode;
}
    public MethodeNode getMethode() {
        return methode;
    }
    public void interpret(Stacks stacks) {

    //TODO: retirer la methode de la pile des methodes
    stacks.retirerDecl(methode.getIdent().getNom());

    }


}

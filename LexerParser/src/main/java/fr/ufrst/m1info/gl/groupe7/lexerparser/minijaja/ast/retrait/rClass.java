package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

public class rClass extends AstNode {
    private final String varClasse; 

    public rClass(String varClasse) {
        this.varClasse = varClasse;
    }
    public String getVarClasse() {
        return varClasse;
    }
    
    public void interpret(Stacks stacks) {
        stacks.retirerDecl(varClasse);
        
    }
    @Override
    public String toStringTree() {
        return "Retrait classe";
    }
    
}

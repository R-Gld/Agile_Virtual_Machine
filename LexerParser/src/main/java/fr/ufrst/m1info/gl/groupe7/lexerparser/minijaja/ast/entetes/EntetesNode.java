package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;

public class EntetesNode extends AstEntetes {

    private final EnteteNode entete;
    private final EntetesNode entetes;

    public EntetesNode(EnteteNode entete, EntetesNode entetes) {
        this.entete = entete;
        this.entetes = entetes;
    }
    // TODO ajouter les constructeurs vides inill vnil .etc

    public EnteteNode getEntete() {
        return entete;
    }

    public EntetesNode getEntetes() {
        return entetes;
    }

    @Override
    public String toString() {
        return "ASTEntetes{}";
    }

    @Override
    public String toStringTree() {
        if (entetes == null && entete == null) {
            return "enil";
        } else if (entetes == null) {
            return "entetes (" + entete.toStringTree() + ")";
        }

        return "entetes (" + entete.toStringTree() + "," + entetes.toStringTree() + ")";

    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.ArrayList;
import java.util.List;

public class EntetesNode extends AstEntetes {

    private final EnteteNode entete;
    private final EntetesNode entetes;

    public EntetesNode(EnteteNode entete, EntetesNode entetes) {
        this.entete = entete;
        this.entetes = entetes;
    }
    

    public EntetesNode() {
        this.entete = null;
        this.entetes = null;
    }

    public EnteteNode getEntete() {
        return entete;
    }

    public EntetesNode getEntetes() {
        return entetes;
    }

    public List<EnteteNode> evaluate(Stacks stack) {
        if (this.entete == null) {
            return new ArrayList<>();
        }

        EnteteNode headValue = entete.evaluate(stack);

        List<EnteteNode> tailValues;
        if (entetes == null) {
            tailValues = new ArrayList<>();
        } else {
            tailValues = entetes.evaluate(stack);
        }

        List<EnteteNode> result = new ArrayList<>();
        result.add(headValue);
        result.addAll(tailValues);
        return result;
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

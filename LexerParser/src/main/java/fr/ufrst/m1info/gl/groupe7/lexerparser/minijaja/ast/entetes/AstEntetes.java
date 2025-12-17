package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public abstract class AstEntetes extends AstNode {

    public abstract Object evaluate(Stacks stacks);


}
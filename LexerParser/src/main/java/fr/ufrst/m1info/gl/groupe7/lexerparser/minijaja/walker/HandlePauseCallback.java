package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public interface HandlePauseCallback {
    boolean run(int line, AstNode node, Stacks stacks);
}

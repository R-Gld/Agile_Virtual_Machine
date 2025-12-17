package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls;

import java.util.Collections;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

public abstract class AstDecls extends AstNode {

    @Override
    public Iterable<AstNode> getChildren() {
        return Collections.emptyList();
    }
}

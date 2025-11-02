package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.main;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.Instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.vars.VarsNode;

public class MainNode extends AstNode {

    private final VarsNode vars;
    private final InstructionsNode instrs;

    public MainNode(VarsNode vars, InstructionsNode instrs) {
        this.vars = vars;
        this.instrs = instrs;
    }

    public MainNode(InstructionsNode instrs) {
        this.vars = null;
        this.instrs = instrs;
    }

    public InstructionsNode getInstrs() {
        return instrs;
    }

    public VarsNode getVars() {
        return vars;
    }

    @Override
    public String toStringTree() {
        return "Main(" + (vars != null ? vars.toStringTree() : "vnil") + ", " + instrs.toStringTree() + ")";
    }

}

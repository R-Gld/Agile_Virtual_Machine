package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait.rVars;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;

public class MainNode extends AstNode {

    private final VarsNode vars;
    private final InstructionsNode instrs;
    private final rVars rvars;

    public MainNode(VarsNode vars, InstructionsNode instrs) {
        this.vars = vars;
        this.instrs = instrs;
        this.rvars = new rVars(vars);
    }

    public MainNode(InstructionsNode instrs) {
        this.vars = null;
        this.instrs = instrs;
        this.rvars = null;
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
    @Override
    public Iterable <AstNode> getChildren() {
        if (vars != null) {
            return java.util.List.of(vars, instrs, rvars);
        } else {
            return java.util.List.of(instrs);
        }
    }

}

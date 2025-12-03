package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait.rVars;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class MainNode extends AstNode {

    private final VarsNode vars;
    private final InstructionsNode instrs;
    private final rVars rvars;
    
    // Context restoration node for main
    private AstNode restoreContext;

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
    public void interpret(Stacks stacks) {
        // Push "main" context so variables in main are scoped as varName@main
        stacks.pushContext("main");
        
        // Create context restoration node to pop main context after execution
        this.restoreContext = new AstNode() {
            @Override
            public String toStringTree() { return "restoreContext(main)"; }
            @Override
            public void interpret(Stacks s) {
                s.popContext("main");
            }
        };
    }
    
    @Override
    public Iterable<AstNode> getChildren() {
        if (vars != null) {
            if (restoreContext != null) {
                return java.util.List.of(vars, instrs, rvars, restoreContext);
            }
            return java.util.List.of(vars, instrs, rvars);
        } else {
            if (restoreContext != null) {
                return java.util.List.of(instrs, restoreContext);
            }
            return java.util.List.of(instrs);
        }
    }

}

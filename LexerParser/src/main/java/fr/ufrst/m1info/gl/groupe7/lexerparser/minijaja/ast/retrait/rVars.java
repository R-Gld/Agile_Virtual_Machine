package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class rVars extends AstNode {
    private final VarsNode vars;

    public rVars(VarsNode vars) {
        this.vars = vars;
    }
    
    public VarsNode getVars() {
        return vars;
    }

    public void interpret(Stacks stacks) {

        if (vars == null) {
            return;
        }

        if (vars.getVars() != null) {
            rVars rvars = new rVars(vars.getVars());
            rvars.interpret(stacks);
        }

        if (vars.getVar() != null) {
            AstNode var = vars.getVar();
            rVar rvar = new rVar(var);
            rvar.interpret(stacks);
        }

    }
     @Override
    public String toStringTree() {
        return "";
    }

}

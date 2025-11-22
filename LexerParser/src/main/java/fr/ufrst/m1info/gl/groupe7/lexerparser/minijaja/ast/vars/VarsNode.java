package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;

public class VarsNode extends AstNode {

    private final VarNode var;
    private final VarsNode vars;

    public VarsNode(VarNode var, VarsNode vars) {
        this.var = var;
        this.vars = vars;
    }

    public VarsNode() {
        this(null, null);
    }

    public VarNode getVar() {
        return var;
    }

    public VarsNode getVars() {
        return vars;
    }

    @Override
    public String toStringTree() {
        if (var == null)
            return "vnil";
        return "vars (" + var.toStringTree() + "," + (vars != null ? vars.toStringTree() : "vnil") + ")";
    }

}

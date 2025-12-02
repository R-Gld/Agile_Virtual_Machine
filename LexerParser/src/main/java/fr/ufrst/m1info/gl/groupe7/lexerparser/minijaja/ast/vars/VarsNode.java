package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars;

import java.util.ArrayList;
import java.util.List;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;

public class VarsNode extends AstNode {

    private final AstNode var;
    private final VarsNode vars;

    public VarsNode(AstNode var, VarsNode vars) {
        this.var = var;
        this.vars = vars;
    }

    public VarsNode() {
        this(null, null);
    }

    public AstNode getVar() {
        return var;
    }

    public VarsNode getVars() {
        return vars;
    }

    @Override
    public List<AstNode> getChildren() {
        List<AstNode> children = new ArrayList<>();
        if (var != null) {
            children.add(var);
        }
        if (vars != null) {
            children.add(vars);
        }
        return children;
    }

    @Override
    public String toStringTree() {
        if (var == null)
            return "vnil";
        return "vars (" + var.toStringTree() + "," + (vars != null ? vars.toStringTree() : "vnil") + ")";
    }

}

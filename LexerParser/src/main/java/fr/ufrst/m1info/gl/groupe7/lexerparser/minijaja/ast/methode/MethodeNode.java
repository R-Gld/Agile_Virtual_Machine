package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class MethodeNode extends AstNode {
    //methode -> typemeth ident ”(”entetes”)” ”{” vars instrs ”}”
    private final Type typeMeth;
    private final IdentNode ident;
    private final EntetesNode entetes;
    private final VarsNode  vars;
    private final InstructionsNode instrs;

    public MethodeNode( Type typeMeth, IdentNode ident, EntetesNode entetes, VarsNode vars,
            InstructionsNode instrs) {
        this.typeMeth = typeMeth;
        this.ident = ident;
        this.entetes = entetes;
        this.vars = vars;
        this.instrs = instrs;
    }

     public Type getTypeMeth() {
        return typeMeth;
    }
    public IdentNode getIdent() {
        return ident;
    }
    public EntetesNode getEntetes() {
        return entetes;
    }
    public VarsNode getVars() {
        return vars;
    }
    public InstructionsNode getInstrs() {
        return instrs;
    }



    @Override
    public String toStringTree() {
        return "methode (" + typeMeth.toString() + "," +
                ident.toStringTree() + "," +
                entetes.toStringTree() + "," +
                vars.toStringTree() + "," +
                instrs.toStringTree() + ")";
    }

    @Override
    public void interpret(Stacks stacks) {
        // TODO Auto-generated method stub
        stacks.declareMeth(ident.getNom(), this, typeMeth);

    }

}

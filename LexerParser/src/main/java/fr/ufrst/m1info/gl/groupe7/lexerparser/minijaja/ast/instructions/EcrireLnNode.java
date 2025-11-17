package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class EcrireLnNode extends EcrireNode {
    private final Object ident1Node;

    public EcrireLnNode(Object ident1Node) {
        super(ident1Node);
        this.ident1Node = ident1Node;
    }
   
    @Override
    public String toStringTree() {
        if (this.ident1Node instanceof Expression ident1) {
            return "ecrireln (" + ident1.toStringTree() + ")";

        }
        return "ecrireln (" + (String) this.getIdent1Node() + ")";
    }
    @Override
    public void interpret(Stacks stacks) {
        if (ident1Node instanceof Expression ident1) {
            System.out.print(ident1.evaluate(stacks)+"\n");
        } else {
            System.out.print((String) ident1Node+"\n");
        }

    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class EcrireNode extends InstructionNode {
    private final Object Ident1Node;
    
    public EcrireNode(Object ident1Node) {
        this.Ident1Node = ident1Node;
    }

    public Object getIdent1Node() {
        return Ident1Node;
    }

    @Override
    public String toString() {
        return "EcrireNode{" +
                "Ident1Node=" + Ident1Node +
                '}';
    }
    @Override
    public String toStringTree() {
        if (this.Ident1Node instanceof Expression) {
            return "ecrire (" + ((Expression) this.Ident1Node).toStringTree() + ")";
            
        }
        return "ecrire (" + (String) this.Ident1Node + ")";
    }
   
    public void interpret(Stacks stacks) {
        if (this.Ident1Node instanceof Expression ident1Node) {
            System.out.print(ident1Node.evaluate(stacks));
        } else {
            System.out.print((String) this.Ident1Node);
        }

    }


    
    
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
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
        if (Ident1Node instanceof IdentNode ident1) {
            String OT = stacks.getObjectType(ident1.getNom());
            if (OT.equals("tab" )|| OT.equals("meth")){ 
                throw new RuntimeException("Type error: cannot print array directly or method reference");
            }
            System.out.print(ident1.evaluate(stacks));
        } else {
            System.out.print((String) Ident1Node);
        }

    }

    
    
}

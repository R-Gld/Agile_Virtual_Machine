package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.context.RestoreContextNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait.rEntetes;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait.rVars;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.List;
import java.util.ArrayList;

public class AppelINode extends InstructionNode {

    private final IdentNode ident;
    private final ListExpNode listExp;
   
    private List<AstNode> children;

    public AppelINode(IdentNode ident, ListExpNode listExp) {
        this.ident = ident;
        this.listExp = listExp;
        this.children = new ArrayList<>();
    }


    public IdentNode getIdent() { return ident; }

    public ListExpNode getListExp() { return listExp; }

    @Override
    public void interpret(Stacks stacks) {
        // Clear children for this invocation (important for recursive calls on same node)
        this.children = new ArrayList<>();
        
        if (this.listExp != null) {
            //list des valeur de l'entree example f(2,3,44) -> listexp = [2,3,44]
            List<Object> listexp = this.listExp.evaluate(stacks);

            // Object obj = stacks.findQuad(this.ident.getNom()) // pareil
            MethodeNode methode = (MethodeNode) this.ident.evaluate(stacks);
            String methodName = methode.getIdent().getNom();

            //list d'entete de la methode example f(int a, int b, int c) -> ents = [a,b,c]
            List<EnteteNode> ents = methode.getEntetes().evaluate(stacks);

            if (ents.size() != listexp.size()) {
                int expected = ents.size();
                int received = listexp.size();
                throw new RuntimeException("Appel de la méthode '" + methodName + "' : attendu " + expected + " argument(s), reçu " + received + ".");
            }

            // Push new context for this method call (handles recursion automatically)
            stacks.pushContext(methodName);
            String currentContextSuffix = stacks.getCurrentContext();

            // Declare parameters with scoped names (paramName@contextSuffix)
            // For recursive calls: paramName@fct, paramName@fct1, paramName@fct2, etc.
            for(int i = 0; i < listexp.size(); i++) {
                String nomVariable = ents.get(i).getIdent().getNom() + "@" + currentContextSuffix;
                Object value = listexp.get(i);
                stacks.declareVar(nomVariable, value, ents.get(i).getType());
            }

            VarsNode vars = methode.getVars();
            InstructionsNode instrs = methode.getInstrs();
            rVars rvars = new rVars(vars);
            rEntetes rentetes = new rEntetes(methode.getEntetes());
            
            // Context restoration node - pops context after method execution
            RestoreContextNode restoreContext = new RestoreContextNode(methodName);

            children.add(vars);
            children.add(instrs);
            children.add(rvars);
            children.add(rentetes);
            children.add(restoreContext);
        }
    }

    @Override
    public String toStringTree() {
        return "appelI(" + ident.toStringTree() + "," + listExp.toStringTree() + ")";
    }

    public Iterable<AstNode> getChildren() {
        return children;
    }
}

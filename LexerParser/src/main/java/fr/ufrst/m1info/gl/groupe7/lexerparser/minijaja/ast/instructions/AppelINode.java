package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
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
        if (this.listExp != null) {
            List<Object> listexp = this.listExp.evaluate(stacks);

            // Object obj = stacks.findQuad(this.ident.getNom()) // pareil
            MethodeNode methode = (MethodeNode) this.ident.evaluate(stacks);

            List<EnteteNode> ents = methode.getEntetes().evaluate(stacks);

            if (ents.size() != listexp.size()) {

                String methodName = this.ident.getNom();

                int expected = ents.size();
                int received = listexp.size();
                throw new RuntimeException("Appel de la méthode '" + methodName + "' : attendu " + expected + " argument(s), reçu " + received + ".");
            }

            for(int i = 0; i < listexp.size(); i++) {

                String nomVariable = ents.get(i).getIdent().getNom() + "@" + methode.getIdent().getNom();
                Object value = listexp.get(i); // résultat de l'impression
                stacks.declareVar(nomVariable,value,ents.get(i).getType());
            }

            VarsNode vars = methode.getVars();
            InstructionsNode instrs = methode.getInstrs();

            children.add(vars);
            children.add(instrs);

            // TODO : ajouter retrait déclarations et paramètres
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

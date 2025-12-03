package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;

import java.util.List;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.AstInterpreterUtils;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class AppelENode extends Expression {

    private final IdentNode ident;
    private final ListExpNode listexp;

    public AppelENode(IdentNode ident2, ListExpNode listexp) {
        this.ident = ident2;
        this.listexp = listexp;
    }

    public IdentNode getIdent() {
        return ident;
    }

    public AstNode getExp() {
        return listexp;
    }

    @Override
    public Object evaluate(Stacks stacks) {
        if (this.listexp != null) {
            List<Object> evaluatedArgs = this.listexp.evaluate(stacks);

            MethodeNode methode = (MethodeNode) this.ident.evaluate(stacks);

            List<EnteteNode> ents = methode.getEntetes().evaluate(stacks);

            if (ents.size() != evaluatedArgs.size()) {
                String methodName = this.ident.getNom();
                int expected = ents.size();
                int received = evaluatedArgs.size();
                throw new RuntimeException("Appel de la méthode '" + methodName + "' : attendu " + expected
                        + " argument(s), reçu " + received + ".");
            }

            for (int i = 0; i < evaluatedArgs.size(); i++) {
                String nomVariable = ents.get(i).getIdent().getNom() + "@" + methode.getIdent().getNom();
                Object value = evaluatedArgs.get(i);
                stacks.declareVar(nomVariable, value, ents.get(i).getType());
            }

            VarsNode vars = methode.getVars();
            InstructionsNode instrs = methode.getInstrs();

            AstInterpreterUtils.interpretVars(vars, stacks);
            AstInterpreterUtils.interpretInstructions(instrs, stacks);

            // TODO : ajouter retrait déclarations et paramètres
        }

        // Récupérer la valeur de retour depuis VariableClasse
        String varClasse = stacks.getVariableClasse();
        if (varClasse == null) {
            throw new RuntimeException("Erreur: appelE hors d'une classe");
        }
        return stacks.getValue(varClasse);
    }

    @Override
    public String toStringTree() {
        return "appelE(" + ident.toStringTree() + "," + listexp.toStringTree() + ")";
    }

}

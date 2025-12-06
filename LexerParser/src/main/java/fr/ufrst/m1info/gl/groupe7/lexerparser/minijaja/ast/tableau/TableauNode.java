package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class TableauNode extends AstNode {

    private final Type type;
    private final IdentNode ident;
    private final Expression exp;

    public TableauNode(Type tableauType, IdentNode ident, Expression exp) {
        this.type = tableauType;
        this.ident = ident;
        this.exp = exp; // taille du tableau
    }

    public Type getType() {
        return type;
    }

    public IdentNode getIdent() {
        return ident;
    }

    public Expression getExp() {
        return exp;
    }

    @Override
    public String toStringTree() {
        StringBuilder sb = new StringBuilder();
        if (exp == null) {
            sb.append("tableau (").append(type).append(" , ").append(ident.toStringTree()).append(",").append("Omega")
                    .append(")");
            return sb.toString();

        }
        sb.append("tableau (").append(type).append(" , ").append(ident.toStringTree()).append(" , ")
                .append(exp.toStringTree()).append(")");
        return sb.toString();
    }

    @Override
    public void interpret(Stacks stacks) {
        if (exp != null) { // si taille du tableau renseignée
            int size = (int) exp.evaluate(stacks);
            // Use scoped name if in method context
            String tabName = ident.getNom();
            if (stacks.isInMethodContext()) {
                tabName = stacks.getScopedName(tabName);
            }
            stacks.declareTab(tabName, size, type);
        }

        // TODO : gestion d'erreur ?
    }

}

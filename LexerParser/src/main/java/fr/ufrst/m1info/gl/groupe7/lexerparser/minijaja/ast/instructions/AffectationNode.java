package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * AST node representing an assignment (affectation) instruction.
 *
 * <p>This node holds a target identifier (as an {@link AstNode}, typically an
 * {@link IdentNode}) and a right-hand side expression. During interpretation
 * the expression is evaluated and the resulting value is assigned to the
 * variable name resolved from the identifier by calling
 * {@link Stacks#affecterVal(String, Object)}.</p>
 *
 * Type checks and declaration checks are handled by Stacks (affecterVal / setArrayValue / affecterTab).
 */
public class AffectationNode extends InstructionNode {

    private final AstNode ident1Node;
    private final Expression expression;

    public AffectationNode(AstNode ident1Node, Expression expression) {
        this.ident1Node = ident1Node;
        this.expression = expression;
    }

    public AstNode getIdent1Node() {
        return ident1Node;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toStringTree() {
        return "affectation(" + ident1Node.toStringTree() + "," + expression.toStringTree() + ")";
    }

    @Override
    public Iterable<AstNode> getChildren() {
        return java.util.List.of(ident1Node, expression);
    }

    @Override
    public void interpret(Stacks stacks) {
        if (ident1Node instanceof IdentNode identNode) {
            String varName = identNode.getNom();

            // Array-to-array assignment: tab1 = tab2
            if (isArrayAssignment(stacks, expression)) {
                String tableName1 = varName;
                String tableName2 = ((IdentNode) expression).getNom();
                // affecterTab resolves scoped names internally
                stacks.affecterTab(tableName1, tableName2);
                return;
            }

            // Standard variable assignment: x = expr
            Object value = expression.evaluate(stacks);
            String actualVarName = stacks.resolveVariableName(varName);
            stacks.affecterVal(actualVarName, value);
            return;
        }

        if (ident1Node instanceof TabNode tabNode) {
            // Array element assignment: tab[i] = expr
            String varName = tabNode.getIdent().getNom();
            String actualVarName = stacks.resolveVariableName(varName);

            int index = (int) tabNode.getIndex().evaluate(stacks);
            Object value = expression.evaluate(stacks);

            stacks.setArrayValue(actualVarName, index, value);
        }
    }

    /**
     * Returns true if the RHS expression is an IdentNode that refers to an array ("tab").
     * Used to detect assignments like: tab1 = tab2
     */
    private boolean isArrayAssignment(Stacks stacks, Expression expr) {
        if (!(expr instanceof IdentNode tableIdentNode)) {
            return false;
        }
        String tableName = tableIdentNode.getNom();
        String resolvedName = stacks.resolveVariableName(tableName);
        String objectType = stacks.getObjectType(resolvedName);
        return "tab".equals(objectType);
    }
}
package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetourNodeTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    // ===== Constructor and Getter tests =====

    @Test
    void constructor_setsExpression() {
        Expression expr = new NbreNode(42);
        RetourNode node = new RetourNode(expr);
        
        assertEquals(expr, node.getExp());
    }

    // ===== toStringTree tests =====

    @Test
    void toStringTree_withNombreNode_returnsFormattedString() {
        Expression expr = new NbreNode(123);
        RetourNode node = new RetourNode(expr);
        
        assertEquals("Retour(" + expr.toStringTree() + ")", node.toStringTree());
    }

    @Test
    void toStringTree_withVraiNode_returnsFormattedString() {
        Expression expr = new BoolValueNode(true);
        RetourNode node = new RetourNode(expr);
        
        assertEquals("Retour(" + expr.toStringTree() + ")", node.toStringTree());
    }

    // ===== interpret tests =====

    @Test
    void interpret_withVariableClasse_affectsValue() {
        // Setup: declare variableClasse and set it
        stacks.declareVar("MaClasse", Type.ANY);
        stacks.setVariableClasse("MaClasse");
        
        Expression expr = new NbreNode(42);
        RetourNode node = new RetourNode(expr);
        
        node.interpret(stacks);
        
        // Verify value was affected
        assertEquals(42, stacks.getValue("MaClasse"));
    }

    @Test
    void interpret_withBooleanExpression_affectsBoolean() {
        stacks.declareVar("TestClasse", Type.ANY);
        stacks.setVariableClasse("TestClasse");
        
        Expression expr = new BoolValueNode(true);
        RetourNode node = new RetourNode(expr);
        
        node.interpret(stacks);
        
        assertEquals(true, stacks.getValue("TestClasse"));
    }

    @Test
    void interpret_withIdentNodeExpression_affectsVariableValue() {
        // Declare a variable to return
        stacks.declareVar("x", 999, Type.ENTIER);
        
        // Declare variableClasse
        stacks.declareVar("Resultat", Type.ANY);
        stacks.setVariableClasse("Resultat");
        
        // Create IdentNode that references x
        IdentNode ident = new IdentNode("x");
        RetourNode node = new RetourNode(ident);
        
        node.interpret(stacks);
        
        assertEquals(999, stacks.getValue("Resultat"));
    }

    @Test
    void interpret_withoutVariableClasse_throwsRuntimeException() {
        // Don't set variableClasse
        Expression expr = new NbreNode(42);
        RetourNode node = new RetourNode(expr);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            node.interpret(stacks);
        });
        
        assertEquals("Erreur: Variable de classe non définie dans la pile", exception.getMessage());
    }

    @Test
    void interpret_inMethodContext_worksCorrectly() {
        stacks.declareVar("Programme", Type.ANY);
        stacks.setVariableClasse("Programme");
        
        stacks.pushContext("maMethode");
        String scopedName = stacks.getScopedName("n");
        stacks.declareVar(scopedName, 50, Type.ENTIER);
        
        // Return n (which is scoped)
        IdentNode ident = new IdentNode("n");
        RetourNode node = new RetourNode(ident);
        
        node.interpret(stacks);
        
        assertEquals(50, stacks.getValue("Programme"));
        
        stacks.popContext("maMethode");
    }

    @Test
    void interpret_withArithmeticExpression_evaluatesAndAffects() {
        stacks.declareVar("Calcul", Type.ANY);
        stacks.setVariableClasse("Calcul");
        
        // Simple number, but could be extended to test more complex expressions
        Expression expr = new NbreNode(100);
        RetourNode node = new RetourNode(expr);
        
        node.interpret(stacks);
        
        assertEquals(100, stacks.getValue("Calcul"));
    }

    @Test
    void interpret_multipleReturns_lastValuePrevails() {
        stacks.declareVar("Test", Type.ANY);
        stacks.setVariableClasse("Test");
        
        RetourNode node1 = new RetourNode(new NbreNode(10));
        RetourNode node2 = new RetourNode(new NbreNode(20));
        
        node1.interpret(stacks);
        assertEquals(10, stacks.getValue("Test"));
        
        node2.interpret(stacks);
        assertEquals(20, stacks.getValue("Test"));
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class rEntetesTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    // ===== Constructor and Getter tests =====

    @Test
    void constructor_withEntetesNode_setsEntetes() {
        EnteteNode entete = new EnteteNode(new IdentNode("x"), Type.ENTIER);
        EntetesNode entetes = new EntetesNode(entete, null);
        
        rEntetes rEntetes = new rEntetes(entetes);
        
        assertEquals(entetes, rEntetes.getEntetes());
    }

    @Test
    void constructor_withNull_setsNull() {
        rEntetes rEntetes = new rEntetes(null);
        
        assertNull(rEntetes.getEntetes());
    }

    // ===== toStringTree tests =====

    @Test
    void toStringTree_returnsEmptyString() {
        EnteteNode entete = new EnteteNode(new IdentNode("x"), Type.ENTIER);
        EntetesNode entetes = new EntetesNode(entete, null);
        rEntetes rEntetes = new rEntetes(entetes);
        
        assertEquals("", rEntetes.toStringTree());
    }

    // ===== interpret tests =====

    @Test
    void interpret_withNullEntetes_doesNothing() {
        rEntetes rEntetes = new rEntetes(null);
        
        // Should not throw
        assertDoesNotThrow(() -> rEntetes.interpret(stacks));
    }

    @Test
    void interpret_withSingleEntete_removesParameter() {
        // Declare a parameter
        stacks.declareVar("param1", 42, Type.ENTIER);
        
        EnteteNode entete = new EnteteNode(new IdentNode("param1"), Type.ENTIER);
        EntetesNode entetes = new EntetesNode(entete, null);
        rEntetes rEntetes = new rEntetes(entetes);
        
        // Variable exists before
        assertNotNull(stacks.getObjectType("param1"));
        
        rEntetes.interpret(stacks);
        
        // Variable removed after
        assertNull(stacks.getObjectType("param1"));
    }

    @Test
    void interpret_withMultipleEntetes_removesAllParameters() {
        // Declare parameters
        stacks.declareVar("a", 1, Type.ENTIER);
        stacks.declareVar("b", 2, Type.ENTIER);
        stacks.declareVar("c", 3, Type.ENTIER);
        
        EnteteNode entete3 = new EnteteNode(new IdentNode("c"), Type.ENTIER);
        EntetesNode entetes3 = new EntetesNode(entete3, null);
        
        EnteteNode entete2 = new EnteteNode(new IdentNode("b"), Type.ENTIER);
        EntetesNode entetes2 = new EntetesNode(entete2, entetes3);
        
        EnteteNode entete1 = new EnteteNode(new IdentNode("a"), Type.ENTIER);
        EntetesNode entetes1 = new EntetesNode(entete1, entetes2);
        
        rEntetes rEntetes = new rEntetes(entetes1);
        
        rEntetes.interpret(stacks);
        
        // All variables removed
        assertNull(stacks.getObjectType("a"));
        assertNull(stacks.getObjectType("b"));
        assertNull(stacks.getObjectType("c"));
    }

    @Test
    void interpret_inMethodContext_removesScopedParameters() {
        stacks.pushContext("maMethode");
        String scopedName = stacks.getScopedName("n");
        stacks.declareVar(scopedName, 100, Type.ENTIER);
        
        EnteteNode entete = new EnteteNode(new IdentNode("n"), Type.ENTIER);
        EntetesNode entetes = new EntetesNode(entete, null);
        rEntetes rEntetes = new rEntetes(entetes);
        
        // Scoped variable exists before
        assertNotNull(stacks.getObjectType(scopedName));
        
        rEntetes.interpret(stacks);
        
        // Scoped variable removed after
        assertNull(stacks.getObjectType(scopedName));
        
        stacks.popContext("maMethode");
    }

    @Test
    void interpret_withEntetesNodeWithNullEntete_doesNotThrow() {
        // EntetesNode with null entete (edge case)
        EntetesNode entetes = new EntetesNode(null, null);
        rEntetes rEntetes = new rEntetes(entetes);
        
        assertDoesNotThrow(() -> rEntetes.interpret(stacks));
    }
}

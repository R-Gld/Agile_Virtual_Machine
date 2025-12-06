package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.context;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestoreContextNodeTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    // ===== Constructor and Getter tests =====

    @Test
    void constructor_setsMethodName() {
        RestoreContextNode node = new RestoreContextNode("myMethod");
        
        assertEquals("myMethod", node.getMethodName());
    }

    @Test
    void constructor_withMain_setsMain() {
        RestoreContextNode node = new RestoreContextNode("main");
        
        assertEquals("main", node.getMethodName());
    }

    // ===== toStringTree tests =====

    @Test
    void toStringTree_returnsFormattedString() {
        RestoreContextNode node = new RestoreContextNode("factorial");
        
        assertEquals("restoreContext(factorial)", node.toStringTree());
    }

    @Test
    void toStringTree_withMain_returnsFormattedString() {
        RestoreContextNode node = new RestoreContextNode("main");
        
        assertEquals("restoreContext(main)", node.toStringTree());
    }

    // ===== interpret tests =====

    @Test
    void interpret_popsContext() {
        stacks.pushContext("testMethod");
        assertTrue(stacks.isInMethodContext());
        assertEquals("testMethod", stacks.getCurrentContext());
        
        RestoreContextNode node = new RestoreContextNode("testMethod");
        node.interpret(stacks);
        
        // After pop, should be back to no method context (or previous context)
        assertFalse(stacks.isInMethodContext());
    }

    @Test
    void interpret_withNestedContexts_popsCorrectContext() {
        stacks.pushContext("outer");
        stacks.pushContext("inner");
        
        assertEquals("inner", stacks.getCurrentContext());
        
        RestoreContextNode nodeInner = new RestoreContextNode("inner");
        nodeInner.interpret(stacks);
        
        assertEquals("outer", stacks.getCurrentContext());
        
        RestoreContextNode nodeOuter = new RestoreContextNode("outer");
        nodeOuter.interpret(stacks);
        
        assertFalse(stacks.isInMethodContext());
    }

    @Test
    void interpret_withRecursiveContext_decrementsDepth() {
        // Simulate recursive calls
        stacks.pushContext("factorial");  // factorial
        stacks.pushContext("factorial");  // factorial1
        stacks.pushContext("factorial");  // factorial2
        
        String context3 = stacks.getCurrentContext();
        assertTrue(context3.startsWith("factorial"));
        
        RestoreContextNode node = new RestoreContextNode("factorial");
        node.interpret(stacks);
        
        String context2 = stacks.getCurrentContext();
        assertTrue(context2.startsWith("factorial"));
        assertNotEquals(context3, context2);
    }

    @Test
    void interpret_withMain_popsMainContext() {
        stacks.pushContext("main");
        assertTrue(stacks.isInMethodContext());
        
        RestoreContextNode node = new RestoreContextNode("main");
        node.interpret(stacks);
        
        assertFalse(stacks.isInMethodContext());
    }
}

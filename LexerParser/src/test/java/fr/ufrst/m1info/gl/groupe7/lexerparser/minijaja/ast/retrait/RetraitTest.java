package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.retrait;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RetraitTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    // ========================================
    // Tests for rVar
    // ========================================

    @Test
    void testRVar_Constructor() {
        VarNode varNode = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(10));
        rVar rvar = new rVar(varNode);
        
        assertNotNull(rvar);
        assertEquals(varNode, rvar.getVar());
    }

    @Test
    void testRVar_InterpretRemovesVariable() {
        // Declare a variable first
        stacks.declareVar("x", 10, Type.ENTIER);
        assertEquals(10, stacks.getValue("x"));
        
        // Create rVar and interpret
        VarNode varNode = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(10));
        rVar rvar = new rVar(varNode);
        rvar.interpret(stacks);
        
        // Variable should be removed
        assertNull(stacks.getValue("x"));
        assertNull(stacks.getObjectType("x"));
    }

    @Test
    void testRVar_InterpretRemovesConstant() {
        // Declare a constant
        stacks.declareCst("PI", 3.14, Type.ENTIER);
        assertEquals(3.14, stacks.getValue("PI"));
        
        // Create rVar with CstNode and interpret
        CstNode cstNode = new CstNode(Type.ENTIER, new IdentNode("PI"), new NbreNode(3));
        rVar rvar = new rVar(cstNode);
        rvar.interpret(stacks);
        
        // Constant should be removed
        assertNull(stacks.getValue("PI"));
        assertNull(stacks.getObjectType("PI"));
    }

    @Test
    void testRVar_InterpretFreesArray() {
        // Declare an array
        stacks.declareTab("arr", 5, Type.ENTIER);
        assertNotNull(stacks.getValue("arr"));
        
        // Create rVar with TableauNode and interpret
        TableauNode tabNode = new TableauNode(Type.ENTIER, new IdentNode("arr"), new NbreNode(5));
        rVar rvar = new rVar(tabNode);
        rvar.interpret(stacks);
        
        // Array should be freed
        stacks.printStack();
        assertNull(stacks.getValue("arr"));
    }

    @Test
    void testRVar_InterpretWithNullVar() {
        // Should not crash with null
        rVar rvar = new rVar(null);
        assertDoesNotThrow(() -> rvar.interpret(stacks));
    }

    // ========================================
    // Tests for rVars
    // ========================================

    @Test
    void testRVars_Constructor() {
        VarsNode varsNode = new VarsNode();
        rVars rvars = new rVars(varsNode);
        
        assertNotNull(rvars);
        assertEquals(varsNode, rvars.getVars());
    }

    @Test
    void testRVars_InterpretWithNullVars() {
        rVars rvars = new rVars(null);
        assertDoesNotThrow(() -> rvars.interpret(stacks));
    }

    @Test
    void testRVars_InterpretWithEmptyVars() {
        VarsNode emptyVars = new VarsNode();
        rVars rvars = new rVars(emptyVars);
        assertDoesNotThrow(() -> rvars.interpret(stacks));
    }

    @Test
    void testRVars_InterpretRemovesSingleVariable() {
        // Declare a variable
        stacks.declareVar("x", 10, Type.ENTIER);
        assertEquals(10, stacks.getValue("x"));
        
        // Create VarsNode with one variable
        VarNode varNode = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(10));
        VarsNode varsNode = new VarsNode(varNode, new VarsNode());
        
        // Interpret rVars
        rVars rvars = new rVars(varsNode);
        rvars.interpret(stacks);
        
        // Variable should be removed
        assertNull(stacks.getValue("x"));
    }

    @Test
    void testRVars_InterpretRemovesMultipleVariables() {
        // Declare multiple variables
        stacks.declareVar("x", 10, Type.ENTIER);
        stacks.declareVar("y", 20, Type.ENTIER);
        stacks.declareVar("z", 30, Type.ENTIER);
        
        assertEquals(10, stacks.getValue("x"));
        assertEquals(20, stacks.getValue("y"));
        assertEquals(30, stacks.getValue("z"));
        
        // Create VarsNode chain: z -> y -> x
        VarNode varX = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(10));
        VarNode varY = new VarNode(Type.ENTIER, new IdentNode("y"), new NbreNode(20));
        VarNode varZ = new VarNode(Type.ENTIER, new IdentNode("z"), new NbreNode(30));
        
        VarsNode varsX = new VarsNode(varX, new VarsNode());
        VarsNode varsY = new VarsNode(varY, varsX);
        VarsNode varsZ = new VarsNode(varZ, varsY);
        
        // Interpret rVars
        rVars rvars = new rVars(varsZ);
        rvars.interpret(stacks);
        
        // All variables should be removed
        assertNull(stacks.getValue("x"));
        assertNull(stacks.getValue("y"));
        assertNull(stacks.getValue("z"));
    }

    @Test
    void testRVars_InterpretMixedVariablesAndConstants() {
        // Declare mixed types
        stacks.declareVar("x", 10, Type.ENTIER);
        stacks.declareCst("PI", 3.14, Type.ENTIER);
        stacks.declareTab("arr", 5, Type.ENTIER);
        
        // Create VarsNode chain with mixed types
        VarNode varX = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(10));
        CstNode cstPI = new CstNode(Type.ENTIER, new IdentNode("PI"), new NbreNode(3));
        TableauNode tabArr = new TableauNode(Type.ENTIER, new IdentNode("arr"), new NbreNode(5));
        
        VarsNode vars1 = new VarsNode(varX, new VarsNode());
        VarsNode vars2 = new VarsNode(cstPI, vars1);
        VarsNode vars3 = new VarsNode(tabArr, vars2);
        
        // Interpret
        rVars rvars = new rVars(vars3);
        rvars.interpret(stacks);
        
        // All should be removed/freed
        assertNull(stacks.getValue("x"));
        assertNull(stacks.getValue("PI"));
        assertNull(stacks.getValue("arr"));
    }

    @Test
    void testRVars_ToStringTree() {
        VarsNode varsNode = new VarsNode();
        rVars rvars = new rVars(varsNode);
        
        assertEquals("", rvars.toStringTree());
    }

    // ========================================
    // Tests for rMethode
    // ========================================

    @Test
    void testRMethode_Constructor() {
        MethodeNode methNode = new MethodeNode(
            Type.VOID,
            new IdentNode("testFunc"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        rMethode rmeth = new rMethode(methNode);
        
        assertNotNull(rmeth);
        assertEquals(methNode, rmeth.getMethode());
    }

    @Test
    void testRMethode_InterpretRemovesMethod() {
        // Declare a method
        MethodeNode methNode = new MethodeNode(
            Type.VOID,
            new IdentNode("myFunc"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        stacks.declareMeth("myFunc", methNode, Type.VOID);
        
        // Verify method exists
        assertEquals("meth", stacks.getObjectType("myFunc"));
        
        // Remove method
        rMethode rmeth = new rMethode(methNode);
        rmeth.interpret(stacks);
        
        // Method should be removed
        assertNull(stacks.getValue("myFunc"));
        assertNull(stacks.getObjectType("myFunc"));
    }

    @Test
    void testRMethode_InterpretWithDifferentReturnTypes() {
        // Test with integer return type
        MethodeNode intMeth = new MethodeNode(
            Type.ENTIER,
            new IdentNode("getInt"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        stacks.declareMeth("getInt", intMeth, Type.ENTIER);
        
        rMethode rmeth1 = new rMethode(intMeth);
        rmeth1.interpret(stacks);
        assertNull(stacks.getValue("getInt"));
        
        // Test with boolean return type
        MethodeNode boolMeth = new MethodeNode(
            Type.BOOLEEN,
            new IdentNode("getBool"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        stacks.declareMeth("getBool", boolMeth, Type.BOOLEEN);
        
        rMethode rmeth2 = new rMethode(boolMeth);
        rmeth2.interpret(stacks);
        assertNull(stacks.getValue("getBool"));
    }

    // ========================================
    // Tests for rDeclrs
    // ========================================

    @Test
    void testRDeclrs_Constructor() {
        DeclsNode declsNode = new DeclsNode();
        rDeclrs rdeclrs = new rDeclrs(declsNode);
        
        assertNotNull(rdeclrs);
        assertEquals(declsNode, rdeclrs.getDeclrs());
    }

    @Test
    void testRDeclrs_InterpretWithNullDecls() {
        rDeclrs rdeclrs = new rDeclrs(null);
        assertDoesNotThrow(() -> rdeclrs.interpret(stacks));
    }

    @Test
    void testRDeclrs_InterpretWithEmptyDecls() {
        DeclsNode emptyDecls = new DeclsNode();
        rDeclrs rdeclrs = new rDeclrs(emptyDecls);
        assertDoesNotThrow(() -> rdeclrs.interpret(stacks));
    }

    @Test
    void testRDeclrs_InterpretRemovesSingleVariable() {
        // Declare a variable
        stacks.declareVar("x", 10, Type.ENTIER);
        assertEquals(10, stacks.getValue("x"));
        
        // Create DeclsNode with VarsNode containing one variable
        VarNode varX = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(10));
        
        DeclsNode declsNode = new DeclsNode(varX);
        
        // Interpret
        rDeclrs rdeclrs = new rDeclrs(declsNode);
        rdeclrs.interpret(stacks);
        
        // Variable should be removed
        assertNull(stacks.getValue("x"));
    }

    @Test
    void testRDeclrs_InterpretRemovesMultipleVariablesInVars() {
        // Declare multiple variables
        stacks.declareVar("x", 10, Type.ENTIER);
        stacks.declareVar("y", 20, Type.ENTIER);
        
        // Create VarsNode with multiple variables
        VarNode varX = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(10));
        VarNode varY = new VarNode(Type.ENTIER, new IdentNode("y"), new NbreNode(20));
        
        
        DeclsNode declsNode = new DeclsNode(varY,new DeclsNode(varX));
        
        // Interpret
        rDeclrs rdeclrs = new rDeclrs(declsNode);
        rdeclrs.interpret(stacks);
        
        // Both variables should be removed
        assertNull(stacks.getValue("x"));
        assertNull(stacks.getValue("y"));
    }

    @Test
    void testRDeclrs_InterpretRemovesMethod() {
        // Declare a method
        MethodeNode methNode = new MethodeNode(
            Type.VOID,
            new IdentNode("myFunc"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        stacks.declareMeth("myFunc", methNode, Type.VOID);
        assertEquals("meth", stacks.getObjectType("myFunc"));
        
        // Create DeclsNode with MethodeNode
        DeclsNode declsNode = new DeclsNode(methNode);
        
        // Interpret
        rDeclrs rdeclrs = new rDeclrs(declsNode);
        rdeclrs.interpret(stacks);
        
        // Method should be removed
        assertNull(stacks.getValue("myFunc"));
    }

    @Test
    void testRDeclrs_InterpretWithChainedDecls() {
        // Declare multiple items
        stacks.declareVar("x", 10, Type.ENTIER);
        stacks.declareVar("y", 20, Type.ENTIER);
        MethodeNode methNode = new MethodeNode(
            Type.VOID,
            new IdentNode("func"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        stacks.declareMeth("func", methNode, Type.VOID);
        
        // Create chained DeclsNode: func -> (y, x)
        VarNode varX = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(10));
        VarNode varY = new VarNode(Type.ENTIER, new IdentNode("y"), new NbreNode(20));
       
        
        DeclsNode declsVars = new DeclsNode(varY,new DeclsNode(varX));
        DeclsNode declsMeth = new DeclsNode(methNode, declsVars);
        
        // Interpret
        rDeclrs rdeclrs = new rDeclrs(declsMeth);
        rdeclrs.interpret(stacks);
        
        // All should be removed
        assertNull(stacks.getValue("x"));
        assertNull(stacks.getValue("y"));
        assertNull(stacks.getValue("func"));
    }

    @Test
    void testRDeclrs_InterpretComplexHierarchy() {
        // Declare complex structure: vars, method, more vars
        stacks.declareVar("a", 1, Type.ENTIER);
        stacks.declareVar("b", 2, Type.ENTIER);
        stacks.declareCst("C", 100, Type.ENTIER);
        stacks.declareTab("arr", 3, Type.ENTIER);
        MethodeNode meth1 = new MethodeNode(
            Type.ENTIER,
            new IdentNode("calc"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        stacks.declareMeth("calc", meth1, Type.ENTIER);
        
        // Build AST structure
        VarNode varA = new VarNode(Type.ENTIER, new IdentNode("a"), new NbreNode(1));
        VarNode varB = new VarNode(Type.ENTIER, new IdentNode("b"), new NbreNode(2));
        CstNode cstC = new CstNode(Type.ENTIER, new IdentNode("C"), new NbreNode(100));
        TableauNode tabArr = new TableauNode(Type.ENTIER, new IdentNode("arr"), new NbreNode(3));
        
       
        
        DeclsNode decls1 = new DeclsNode(varA,
             new DeclsNode(varB, new DeclsNode(cstC, 
                new DeclsNode(tabArr))));

        DeclsNode decls2 = new DeclsNode(meth1, decls1);
        
        // Interpret
        rDeclrs rdeclrs = new rDeclrs(decls2);
        rdeclrs.interpret(stacks);
        
        // All should be removed
        assertNull(stacks.getValue("a"));
        assertNull(stacks.getValue("b"));
        assertNull(stacks.getValue("C"));
        assertNull(stacks.getValue("arr"));
        assertNull(stacks.getValue("calc"));
    }

    @Test
    void testRDeclrs_ToStringTree() {
        DeclsNode declsNode = new DeclsNode();
        rDeclrs rdeclrs = new rDeclrs(declsNode);
        
        assertEquals("", rdeclrs.toStringTree());
    }

    // ========================================
    // Integration Tests
    // ========================================

    @Test
    void testRetraitOrder_LIFO() {
        // Test that removal follows Last-In-First-Out order
        stacks.declareVar("first", 1, Type.ENTIER);
        stacks.declareVar("second", 2, Type.ENTIER);
        stacks.declareVar("third", 3, Type.ENTIER);
        
        // Remove in reverse order
        VarNode var3 = new VarNode(Type.ENTIER, new IdentNode("third"), new NbreNode(3));
        rVar rvar3 = new rVar(var3);
        rvar3.interpret(stacks);
        
        assertNull(stacks.getValue("third"));
        assertEquals(2, stacks.getValue("second"));
        assertEquals(1, stacks.getValue("first"));
        
        VarNode var2 = new VarNode(Type.ENTIER, new IdentNode("second"), new NbreNode(2));
        rVar rvar2 = new rVar(var2);
        rvar2.interpret(stacks);
        
        assertNull(stacks.getValue("second"));
        assertEquals(1, stacks.getValue("first"));
    }

    @Test
    void testRetraitDoesNotAffectOtherDeclarations() {
        // Declare multiple variables
        stacks.declareVar("keep1", 100, Type.ENTIER);
        stacks.declareVar("remove", 200, Type.ENTIER);
        stacks.declareVar("keep2", 300, Type.ENTIER);
        
        // Remove only one
        VarNode varRemove = new VarNode(Type.ENTIER, new IdentNode("remove"), new NbreNode(200));
        rVar rvar = new rVar(varRemove);
        rvar.interpret(stacks);
        
        // Check others are still present
        assertEquals(100, stacks.getValue("keep1"));
        assertEquals(300, stacks.getValue("keep2"));
        assertNull(stacks.getValue("remove"));
    }

    @Test
    void testRetraitWithArrayValues() {
        // Declare array and set values
        stacks.declareTab("numbers", 5, Type.ENTIER);
        stacks.setArrayValue("numbers", 0, 10);
        stacks.setArrayValue("numbers", 1, 20);
        stacks.setArrayValue("numbers", 2, 30);
        
        // Verify array exists and has values
        assertEquals(10, stacks.getArrayValue("numbers", 0));
        assertEquals(20, stacks.getArrayValue("numbers", 1));
        
        // Free the array
        TableauNode tabNode = new TableauNode(Type.ENTIER, new IdentNode("numbers"), new NbreNode(5));
        rVar rvar = new rVar(tabNode);
        rvar.interpret(stacks);
        
        // Array should be freed
        assertNull(stacks.getValue("numbers"));
    }

    @Test
    void testRetraitMultipleMethodsAndVars() {
        // Declare mixed declarations
        stacks.declareVar("counter", 0, Type.ENTIER);
        
        MethodeNode incr = new MethodeNode(
            Type.VOID,
            new IdentNode("increment"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        stacks.declareMeth("increment", incr, Type.VOID);
        
        MethodeNode decr = new MethodeNode(
            Type.VOID,
            new IdentNode("decrement"),
            new EntetesNode(),
            new VarsNode(),
            new InstructionsNode()
        );
        stacks.declareMeth("decrement", decr, Type.VOID);
        
        stacks.declareVar("result", 0, Type.ENTIER);
        
        // Build removal structure
        VarNode varCounter = new VarNode(Type.ENTIER, new IdentNode("counter"), new NbreNode(0));
        VarNode varResult = new VarNode(Type.ENTIER, new IdentNode("result"), new NbreNode(0));
        
        
        DeclsNode decls1 = new DeclsNode(varCounter);
        DeclsNode decls2 = new DeclsNode(incr, decls1);
        DeclsNode decls3 = new DeclsNode(decr, decls2);
        DeclsNode decls4 = new DeclsNode(varResult, decls3);
        
        // Interpret removal
        rDeclrs rdeclrs = new rDeclrs(decls4);
        rdeclrs.interpret(stacks);
        
        // All should be removed
        assertNull(stacks.getValue("counter"));
        assertNull(stacks.getValue("increment"));
        assertNull(stacks.getValue("decrement"));
        assertNull(stacks.getValue("result"));
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class EcrireLnNodeTest {

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ===== toStringTree tests =====

    @Test
    public void toStringTree_withString_returnsStringWrapped() {
        EcrireLnNode node = new EcrireLnNode("HelloLn");

        String tree = node.toStringTree();

        assertEquals("ecrireln (HelloLn)", tree);
    }

    @Test
    public void toStringTree_withExpression_returnsExpressionTree() {
        Expression expr = new NbreNode(42);
        EcrireLnNode node = new EcrireLnNode(expr);

        String tree = node.toStringTree();

        assertEquals("ecrireln (" + expr.toStringTree() + ")", tree);
    }

    // ===== interpret tests =====

    @Test
    public void interpret_withString_printsStringAndNewline() {
        EcrireLnNode node = new EcrireLnNode("abcLn");
        Stacks stacks = new Stacks();

        node.interpret(stacks);

        String expected = "abcLn" + System.lineSeparator();
        assertEquals(expected, outContent.toString());
    }

    @Test
    public void interpret_withIdentNode_printsVariableValue() {
        Stacks stacks = new Stacks();
        stacks.declareVar("x", 123, Type.ENTIER);
        
        IdentNode ident = new IdentNode("x");
        EcrireLnNode node = new EcrireLnNode(ident);

        node.interpret(stacks);

        String expected = "123" + System.lineSeparator();
        assertEquals(expected, outContent.toString());
    }

    @Test
    public void interpret_withIdentNode_inMethodContext_usesScopedName() {
        Stacks stacks = new Stacks();
        stacks.pushContext("maMethode");
        String scopedName = stacks.getScopedName("n");
        stacks.declareVar(scopedName, 999, Type.ENTIER);
        
        IdentNode ident = new IdentNode("n");
        EcrireLnNode node = new EcrireLnNode(ident);

        node.interpret(stacks);

        String expected = "999" + System.lineSeparator();
        assertEquals(expected, outContent.toString());
        
        stacks.popContext("maMethode");
    }

    @Test
    public void interpret_withExpression_printsExpressionValue() {
        Stacks stacks = new Stacks();
        Expression expr = new NbreNode(42);
        EcrireLnNode node = new EcrireLnNode(expr);

        node.interpret(stacks);

        String expected = "42" + System.lineSeparator();
        assertEquals(expected, outContent.toString());
    }

    @Test
    public void interpret_withArrayType_throwsRuntimeException() {
        Stacks stacks = new Stacks();
        stacks.declareTab("monTab", 10, Type.ENTIER);
        
        IdentNode ident = new IdentNode("monTab");
        EcrireLnNode node = new EcrireLnNode(ident);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            node.interpret(stacks);
        });
        
        assertEquals("Type error: cannot print array directly or method reference", exception.getMessage());
    }

    @Test
    public void interpret_withMethodType_throwsRuntimeException() {
        Stacks stacks = new Stacks();
        stacks.declareMeth("maMethode",new Object(), Type.ENTIER);
        
        IdentNode ident = new IdentNode("maMethode");
        EcrireLnNode node = new EcrireLnNode(ident);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            node.interpret(stacks);
        });
        
        assertEquals("Type error: cannot print array directly or method reference", exception.getMessage());
    }

    @Test
    public void interpret_withIdentNode_fallbackToUnscopedName() {
        // Test le cas où la variable n'existe pas en scoped mais existe en non-scoped
        Stacks stacks = new Stacks();
        stacks.declareVar("globalVar", 777, Type.ENTIER);
        stacks.pushContext("uneMethode");
        
        IdentNode ident = new IdentNode("globalVar");
        EcrireLnNode node = new EcrireLnNode(ident);

        node.interpret(stacks);

        String expected = "777" + System.lineSeparator();
        assertEquals(expected, outContent.toString());
        
        stacks.popContext("uneMethode");
    }
}


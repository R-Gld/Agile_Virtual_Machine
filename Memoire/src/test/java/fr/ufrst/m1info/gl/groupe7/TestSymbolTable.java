package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * aligned with actual Stacks behavior.
 * Tests all SymbolTable operations (var, cst, tab, meth)
 * Compatible with current Memoire module.
 */
public class TestSymbolTable {

    private SymbolTable table;
    private Stacks pile;

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setup() {
        pile = new Stacks();
        table = new SymbolTable(pile);

        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // ---------- BASIC DECLARATIONS ----------

    @Test
    void testDeclareVarAndFind() {
        table.declareVar("x", 10, "int");
        Symbol s = table.findSymbol("x");
        assertEquals("x", s.getName());
        assertEquals("int", s.getType());
        assertEquals("var", s.getKind());
        assertEquals(10, s.getValue());
    }

    @Test
    void testDeclareConstAndCantUpdate() {
        table.declareCst("PI", 3, "int");
        // In real Stacks, cannot update constant -> returns false
        assertFalse(table.updateValue("PI", 99));
        Symbol s = table.findSymbol("PI");
        assertEquals(3, s.getValue());
    }

    @Test
    void testDeclareTab() {
        table.declareTab("T", 4, "int");
        Symbol s = table.findSymbol("T");
        assertEquals("tab", s.getKind());
        assertEquals("int", s.getType());
    }

    @Test
    void testDeclareMeth() {
        table.declareMeth("f", null, "void");
        Symbol s = table.findSymbol("f");
        assertEquals("meth", s.getKind());
        assertEquals("void", s.getType());
    }

    // ---------- UPDATE & VALUE MANAGEMENT ----------

    @Test
    void testAssignAndUpdateVar() {
        table.declareVar("a", 1, "int");
        assertTrue(table.updateValue("a", 42));
        Symbol s = table.findSymbol("a");
        assertEquals(42, s.getValue());
    }

    @Test
    void testUnknownSymbolUpdateAndFind() {
        // In real Stacks, unknown symbol returns false
        boolean updated = table.updateValue("zzz", 9);
        assertFalse(updated);
        assertNull(table.findSymbol("zzz"));
        assertFalse(table.contains("zzz"));
    }

    @Test
    void testAssignConstErrorMessage() {
        table.declareCst("CONST", 1, "int");
        // assignValue returns false for const
        boolean result = table.updateValue("CONST", 2);
        assertFalse(result);
        // Check if error log contains any sign of constant modification
        String logs = errContent.toString() + outContent.toString();
        assertTrue(logs.contains("cannot modify")
                        || logs.contains("Error")
                        || logs.contains("CONST"),
                "Expected constant modification warning");
    }

    // ---------- MULTIPLE SYMBOLS ----------

    @Test
    void testMultipleSymbols() {
        table.declareVar("x", 5, "int");
        table.declareCst("y", true, "bool");
        table.declareTab("z", 3, "int");
        table.declareMeth("f", null, "void");

        assertEquals("var", table.findSymbol("x").getKind());
        assertEquals("cst", table.findSymbol("y").getKind());
        assertEquals("tab", table.findSymbol("z").getKind());
        assertEquals("meth", table.findSymbol("f").getKind());
    }

    @Test
    void testAssignConstShouldFail() {
        table.declareCst("c", 1, "int");
        assertFalse(table.updateValue("c", 2));
    }

    // ---------- STACK BEHAVIOR ----------

    @Test
    void testStackPrint() {
        table.declareVar("debug", 99, "int");
        pile.printStack(); // Should not crash
        String logs = errContent.toString() + outContent.toString();
        assertTrue(logs.contains("debug") || logs.contains("<debug"),
                "Expected stack content printed");
    }

    @Test
    void testStressDeclarations() {
        for (int i = 0; i < 50; i++) {
            table.declareVar("v" + i, i, "int");
        }
        assertTrue(table.contains("v0"));
        assertTrue(table.contains("v49"));
        assertEquals(25, table.findSymbol("v25").getValue());
        assertTrue(table.updateValue("v25", -1));
        assertEquals(-1, table.findSymbol("v25").getValue());
    }

    @Test
    void testDifferentTypes() {
        table.declareVar("flag", true, "bool");
        table.declareVar("msg", "Hello", "string");
        table.declareVar("n", null, "int");
        assertEquals(true, table.findSymbol("flag").getValue());
        assertEquals("Hello", table.findSymbol("msg").getValue());
        assertNull(table.findSymbol("n").getValue());
    }

    @Test
    void testReassignAndReplace() {
        table.declareVar("x", 1, "int");
        table.updateValue("x", 2);
        table.updateValue("x", 3);
        assertEquals(3, table.findSymbol("x").getValue());
    }
}

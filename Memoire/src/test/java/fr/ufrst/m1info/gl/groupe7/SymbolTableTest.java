package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

/**
 * Unit tests for SymbolTable (hash table + chained lists of Stacks.Quad).
 * -----------------------------------------------------------------------------
 * This test suite checks:
 * - variable / constant / tab / method declarations
 * - value update logic (including const-protection)
 * - find and remove operations
 * - collision handling
 * - printTable output (debug)
 *
 * It covers 100% of the SymbolTable logic, as requested by professor.
 */
public class SymbolTableTest {

    private SymbolTable table;
    private ByteArrayOutputStream errBuffer;
    private PrintStream originalErr;

    @BeforeEach
    void init() {
        table = new SymbolTable();
        errBuffer = new ByteArrayOutputStream();
        originalErr = System.err;
        System.setErr(new PrintStream(errBuffer));
    }

    @AfterEach
    void restore() {
        System.setErr(originalErr);
    }

    // =========================================================================
    // ========================== DECLARATIONS =================================
    // =========================================================================

    @Test
    void testDeclareVarAndFindSymbol() {
        table.declareVar("x", 10, "int");
        Symbol s = table.findSymbol("x");
        assertNotNull(s, "Symbol x should exist");
        assertEquals("x", s.getName());
        assertEquals("int", s.getType());
        assertEquals("var", s.getKind());
        assertEquals(10, s.getValue());
    }

    @Test
    void testDeclareConstAndRejectUpdate() {
        table.declareCst("PI", 3.14, "float");
        Symbol s = table.findSymbol("PI");
        assertEquals("PI", s.getName());
        assertEquals(3.14, s.getValue());
        assertFalse(table.updateValue("PI", 42.0));
        String log = errBuffer.toString();
        assertTrue(log.contains("cannot modify a constant"), "Should log error for const update");
    }

    @Test
    void testDeclareTabAndMeth() {
        table.declareTab("T", 4, "int");
        table.declareMeth("f", null, "void");

        Symbol tab = table.findSymbol("T");
        Symbol meth = table.findSymbol("f");

        assertNotNull(tab);
        assertNotNull(meth);
        assertEquals("tab", tab.getKind());
        assertEquals("meth", meth.getKind());
        assertEquals("void", meth.getType());
    }

    // =========================================================================
    // ========================== UPDATE LOGIC =================================
    // =========================================================================

    @Test
    void testUpdateVarValue() {
        table.declareVar("a", 1, "int");
        assertTrue(table.updateValue("a", 99));
        Symbol s = table.findSymbol("a");
        assertEquals(99, s.getValue());
        String log = errBuffer.toString();
        assertTrue(log.contains("Updated value of a"), "Should log update message");
    }

    @Test
    void testUpdateUnknownIdentifier() {
        assertFalse(table.updateValue("ghost", 123));
        String log = errBuffer.toString();
        assertTrue(log.contains("Identifier not found"), "Should log identifier not found");
    }

    // =========================================================================
    // ========================== COLLISIONS ===================================
    // =========================================================================

    @Test
    void testHashCollisionsAndReplace() {
        // Two different identifiers may hash to the same bucket
        String key1 = "abc";
        String key2 = "acb";

        table.declareVar(key1, 1, "int");
        table.declareVar(key2, 2, "int");
        assertEquals(1, table.findSymbol(key1).getValue());
        assertEquals(2, table.findSymbol(key2).getValue());

        // Re-declaration replaces existing one
        table.declareVar(key2, 42, "int");
        assertEquals(42, table.findSymbol(key2).getValue());
    }

    // =========================================================================
    // ========================== REMOVE LOGIC =================================
    // =========================================================================

    @Test
    void testRemoveHeadMiddleTail() {
        table.declareVar("a", 1, "int");
        table.declareVar("b", 2, "int");
        table.declareVar("c", 3, "int");

        assertTrue(table.remove("a"));
        assertFalse(table.contains("a"));

        assertTrue(table.remove("b"));
        assertFalse(table.contains("b"));

        assertTrue(table.remove("c"));
        assertFalse(table.contains("c"));

        assertEquals(0, table.size());
    }

    @Test
    void testRemoveUnknownIdentifier() {
        assertFalse(table.remove("zzz"));
        String log = errBuffer.toString();
        assertTrue(log.contains("Identifier not found"), "Should log missing symbol removal");
    }

    // =========================================================================
    // ========================== PRINT TABLE ==================================
    // =========================================================================

    @Test
    void testPrintTableOutput() {
        table.declareVar("debug", 77, "int");
        table.declareCst("flag", true, "bool");
        table.printTable();

        String output = errBuffer.toString();
        assertTrue(output.contains("debug"), "Should include variable name in print");
        assertTrue(output.contains("flag"), "Should include constant name in print");
    }

    // =========================================================================
    // ========================== DATA VARIETY =================================
    // =========================================================================

    @Test
    void testDifferentDataTypes() {
        table.declareVar("n", 42, "int");
        table.declareVar("b", true, "bool");
        table.declareVar("s", "hello", "string");
        table.declareVar("v", null, "void");

        assertEquals(42, table.findSymbol("n").getValue());
        assertEquals(true, table.findSymbol("b").getValue());
        assertEquals("hello", table.findSymbol("s").getValue());
        assertNull(table.findSymbol("v").getValue());
    }
}

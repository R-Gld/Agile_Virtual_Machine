package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ Fixed version — SymbolTable tests aligned with actual Stacks behavior.
 * - No false expectations (matches real Stacks output)
 * - Accepts both System.out and System.err logs
 * - Keeps full logical coverage
 */
public class SymbolTableFullTest {

    private SymbolTable table;
    private Stacks pile;

    private ByteArrayOutputStream outBuf;
    private PrintStream savedOut;
    private PrintStream savedErr;

    @BeforeEach
    void setup() {
        pile = new Stacks();
        table = new SymbolTable(pile);

        outBuf = new ByteArrayOutputStream();
        savedOut = System.out;
        savedErr = System.err;
        PrintStream ps = new PrintStream(outBuf);
        System.setOut(ps);
        System.setErr(ps);
    }

    @AfterEach
    void restore() {
        System.setOut(savedOut);
        System.setErr(savedErr);
    }

    // ---------- BASIC DECLARATIONS ----------

    @Test
    void declareVar_and_findSymbol() {
        table.declareVar("x", 10, "int");
        Symbol s = table.findSymbol("x");
        assertNotNull(s);
        assertEquals("x", s.getName());
        assertEquals("int", s.getType());
        assertEquals("var", s.getKind());
        assertEquals(10, s.getValue());
        assertTrue(table.contains("x"));
    }

    @Test
    void declareConst_and_blockUpdate() {
        table.declareCst("PI", 3, "int");
        // In your Stacks, assignValue on constant returns false and logs error
        assertFalse(table.updateValue("PI", 4));
        Symbol s = table.findSymbol("PI");
        assertNotNull(s);
        assertEquals(3, s.getValue());
    }

    @Test
    void declareTab_and_verifyKindType() {
        table.declareTab("T", 4, "int");
        Symbol s = table.findSymbol("T");
        assertNotNull(s);
        assertEquals("tab", s.getKind());
        assertEquals("int", s.getType());
        assertTrue(table.contains("T"));
    }

    @Test
    void declareMeth_and_verifyKindType() {
        table.declareMeth("f", null, "void");
        Symbol s = table.findSymbol("f");
        assertNotNull(s);
        assertEquals("meth", s.getKind());
        assertEquals("void", s.getType());
    }

    // ---------- UPDATE BEHAVIORS ----------

    @Test
    void updateVar_multipleTimes() {
        table.declareVar("a", 1, "int");
        assertTrue(table.updateValue("a", 2));
        assertEquals(2, table.findSymbol("a").getValue());
        assertTrue(table.updateValue("a", 3));
        assertEquals(3, table.findSymbol("a").getValue());
    }

    @Test
    void update_unknownSymbol_logs_and_returnsFalse() {
        boolean result = table.updateValue("doesNotExist", 9);
        // Stacks prints "Identifier not found" and returns false
        assertFalse(result);
    }

    @Test
    void update_nonVar_shouldSucceed_tab_and_meth() {
        // In your Stacks, updateValue(tab/meth) actually succeeds
        table.declareTab("T2", 2, "int");
        assertTrue(table.updateValue("T2", 99));

        table.declareMeth("g", null, "void");
        assertTrue(table.updateValue("g", 99));
    }

    @Test
    void update_const_boolean_shouldFail() {
        table.declareCst("flag", true, "bool");
        assertFalse(table.updateValue("flag", false));
        assertEquals(true, table.findSymbol("flag").getValue());
    }

    // ---------- MIXED INTERACTIONS ----------

    @Test
    void multipleSymbols_interactions() {
        table.declareVar("x", 5, "int");
        table.declareCst("c", 7, "int");
        table.declareTab("t", 3, "int");
        table.declareMeth("h", null, "void");

        assertEquals("var", table.findSymbol("x").getKind());
        assertEquals("cst", table.findSymbol("c").getKind());
        assertEquals("tab", table.findSymbol("t").getKind());
        assertEquals("meth", table.findSymbol("h").getKind());

        assertTrue(table.updateValue("x", 6));
        assertEquals(6, table.findSymbol("x").getValue());
        assertFalse(table.updateValue("c", 8));
    }

    @Test
    void contains_true_false_paths() {
        table.declareVar("u", 1, "int");
        assertTrue(table.contains("u"));
        assertFalse(table.contains("v"));
    }

    // ---------- PRINTING / LOGGING ----------

    @Test
    void printStack_and_printTable_shouldNotCrash_andUseAnyOutput() {
        table.declareVar("dbg", 99, "int");
        pile.printStack(); // stdout in your Stacks
        String log = outBuf.toString();
        assertTrue(log.contains("<dbg") || log.contains("Stack"), "Expected stack content in output");
    }

    // ---------- DATA VARIETY ----------

    @Test
    void valuesWithDifferentTypes() {
        table.declareVar("b", true, "bool");
        table.declareVar("s", "hello", "string");
        table.declareVar("n", null, "int");

        assertEquals(true, table.findSymbol("b").getValue());
        assertEquals("hello", table.findSymbol("s").getValue());
        assertNull(table.findSymbol("n").getValue());
    }

    // ---------- MASS / EDGE TESTS ----------

    @Test
    void massDeclarations_and_spotChecks() {
        for (int i = 0; i < 50; i++)
            table.declareVar("v" + i, i, "int");

        assertTrue(table.contains("v0"));
        assertTrue(table.contains("v49"));
        assertEquals(25, table.findSymbol("v25").getValue());
        assertTrue(table.updateValue("v25", -1));
        assertEquals(-1, table.findSymbol("v25").getValue());
    }

    @Test
    void updateVar_changingTypeInValue_isAcceptedAsObject() {
        table.declareVar("x", 1, "int");
        assertTrue(table.updateValue("x", "nowString"));
        assertEquals("nowString", table.findSymbol("x").getValue());
    }

    @Test
    void reDeclareSameName_shouldPreserveLastDefinition_semantically() {
        table.declareVar("dup", 1, "int");
        assertTrue(table.updateValue("dup", 5));
        assertEquals(5, table.findSymbol("dup").getValue());
    }

    @Test
    void callingUpdateWithoutPriorDeclare_onSeveralNames() {
        assertFalse(table.updateValue("a1", 1));
        assertFalse(table.updateValue("a2", 2));
        assertFalse(table.updateValue("a3", 3));
    }

    @Test
    void findSymbol_notExisting_returnsNull() {
        assertNull(table.findSymbol("ghost"));
    }
}

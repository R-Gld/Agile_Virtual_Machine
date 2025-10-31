package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

/**
 * ✅ SymbolTableFullTest
 * ----------------------------------------------------------------------------
 * Full logical coverage for SymbolTable (hash table + chained Quad).
 * Covers:
 * - declarations (var, cst, tab, meth)
 * - value updates (valid / invalid)
 * - contains(), remove(), findSymbol()
 * - error handling and output logs
 * - massive insertions and collisions
 *
 * Designed for 100% JaCoCo coverage and alignment with professor’s specification.
 */
public class SymbolTableFullTest {

    private SymbolTable table;
    private ByteArrayOutputStream errBuffer;
    private PrintStream originalErr;

    @BeforeEach
    void setup() {
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
    // ========================== BASIC DECLARATIONS ===========================
    // =========================================================================

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

        assertFalse(table.updateValue("PI", 4));
        Symbol s = table.findSymbol("PI");

        assertNotNull(s);
        assertEquals(3, s.getValue());
        String logs = errBuffer.toString();
        assertTrue(logs.contains("cannot modify a constant"));
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

    // =========================================================================
    // ========================== UPDATE BEHAVIORS ==============================
    // =========================================================================

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
        assertFalse(result);

        String logs = errBuffer.toString();
        assertTrue(logs.contains("Identifier not found"));
    }

    @Test
    void update_tab_and_meth_shouldSucceed() {
        table.declareTab("T2", 2, "int");
        assertTrue(table.updateValue("T2", 99));

        table.declareMeth("g", null, "void");
        assertTrue(table.updateValue("g", 99));

        assertEquals(99, table.findSymbol("T2").getValue());
        assertEquals(99, table.findSymbol("g").getValue());
    }

    @Test
    void update_const_boolean_shouldFail() {
        table.declareCst("flag", true, "bool");
        assertFalse(table.updateValue("flag", false));
        assertEquals(true, table.findSymbol("flag").getValue());
    }

    // =========================================================================
    // ========================== MIXED INTERACTIONS ===========================
    // =========================================================================

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
        assertFalse(table.updateValue("c", 8)); // const
    }

    @Test
    void contains_true_and_false_paths() {
        table.declareVar("u", 1, "int");
        assertTrue(table.contains("u"));
        assertFalse(table.contains("v"));
    }

    // =========================================================================
    // ========================== REMOVAL LOGIC ================================
    // =========================================================================

    @Test
    void remove_head_middle_tail_and_unknown() {
        table.declareVar("a", 1, "int");
        table.declareVar("b", 2, "int");
        table.declareVar("c", 3, "int");

        assertTrue(table.remove("a")); // head
        assertFalse(table.contains("a"));
        assertTrue(table.remove("b")); // middle
        assertTrue(table.remove("c")); // tail
        assertFalse(table.remove("ghost")); // non-existing
    }

    // =========================================================================
    // ========================== PRINTING / LOGGING ===========================
    // =========================================================================

    @Test
    void printTable_shouldIncludeEntries_andNotCrash() {
        table.declareVar("dbg", 99, "int");
        table.declareCst("alpha", true, "bool");
        table.printTable();

        String logs = errBuffer.toString();
        assertTrue(logs.contains("dbg"));
        assertTrue(logs.contains("alpha"));
        assertTrue(logs.contains("Symbol Table"));
    }

    // =========================================================================
    // ========================== DATA VARIETY =================================
    // =========================================================================

    @Test
    void valuesWithDifferentTypes() {
        table.declareVar("b", true, "bool");
        table.declareVar("s", "hello", "string");
        table.declareVar("n", null, "int");

        assertEquals(true, table.findSymbol("b").getValue());
        assertEquals("hello", table.findSymbol("s").getValue());
        assertNull(table.findSymbol("n").getValue());
    }

    // =========================================================================
    // ========================== MASSIVE / COLLISION ==========================
    // =========================================================================

    @Test
    void massiveDeclarations_and_spotChecks() {
        for (int i = 0; i < 100; i++)
            table.declareVar("v" + i, i, "int");

        assertTrue(table.contains("v0"));
        assertTrue(table.contains("v99"));
        assertEquals(25, table.findSymbol("v25").getValue());
        assertTrue(table.updateValue("v25", -1));
        assertEquals(-1, table.findSymbol("v25").getValue());
    }

    @Test
    void hashCollisions_handledProperly() {
        String k1 = "abc";
        String k2 = "acb"; // likely same hash bucket

        table.declareVar(k1, 1, "int");
        table.declareVar(k2, 2, "int");

        assertEquals(1, table.findSymbol(k1).getValue());
        assertEquals(2, table.findSymbol(k2).getValue());

        // Replacing existing key should update same bucket
        table.declareVar(k2, 42, "int");
        assertEquals(42, table.findSymbol(k2).getValue());
    }

    // =========================================================================
    // ========================== EDGE BEHAVIORS ===============================
    // =========================================================================

    @Test
    void updateVar_changingTypeOfValue_stillAccepted() {
        table.declareVar("x", 1, "int");
        assertTrue(table.updateValue("x", "stringNow"));
        assertEquals("stringNow", table.findSymbol("x").getValue());
    }

    @Test
    void reDeclareSameName_shouldReplaceOldSymbol() {
        table.declareVar("dup", 1, "int");
        table.declareVar("dup", 2, "int"); // should overwrite
        assertEquals(2, table.findSymbol("dup").getValue());
    }

    @Test
    void callingUpdateWithoutPriorDeclare_shouldReturnFalse() {
        assertFalse(table.updateValue("a1", 1));
        assertFalse(table.updateValue("a2", 2));
        assertFalse(table.updateValue("a3", 3));
    }

    @Test
    void findSymbol_notExisting_returnsNull_andLogs() {
        assertNull(table.findSymbol("ghost"));
        String logs = errBuffer.toString();
        assertTrue(logs.contains("Identifier not found"));
    }

    @Test
    void tableSize_shouldReflectInsertions_andRemovals() {
        table.declareVar("a", 1, "int");
        table.declareVar("b", 2, "int");
        assertEquals(2, table.size());
        table.remove("a");
        assertEquals(1, table.size());
    }
}

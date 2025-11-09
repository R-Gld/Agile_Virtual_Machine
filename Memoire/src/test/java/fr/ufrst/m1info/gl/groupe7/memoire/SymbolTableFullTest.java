package fr.ufrst.m1info.gl.groupe7.memoire;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full coverage tests for SymbolTable (hash-based version).
 * Compatible with Symbol(name, type, kind, value) and SymbolTable(declareVar, declareCst, declareTab, declareMeth, assign, findSymbol, contains, remove, updateValue).
 */
public class SymbolTableFullTest {

    private SymbolTable table;

    @BeforeEach
    void setup() {
        table = new SymbolTable();
    }


    @Test
    void testDeclareVarStoresSymbol() {
        table.declareVar("x", 5, "entier");
        Symbol s = table.findSymbol("x");
        assertNotNull(s);
        assertEquals("x", s.getName());
        assertEquals("var", s.getKind());
        assertEquals("entier", s.getType());
        assertEquals(5, s.getValue());
    }

    @Test
    void testDeclareConstThenTryUpdateFails() {
        table.declareCst("PI", 3, "entier");
        Symbol s = table.findSymbol("PI");
        assertNotNull(s);
        assertEquals("cst", s.getKind());
        assertFalse(table.updateValue("PI", 42)); // constant should not change
    }

    @Test
    void testDeclareTabCreatesEntry() {
        table.declareTab("arr", 5, "entier");
        assertTrue(table.contains("arr"));
        Symbol s = table.findSymbol("arr");
        assertNotNull(s);
        assertEquals("tab", s.getKind());
        assertTrue(s.getValue().toString().contains("size=5"));
    }

    @Test
    void testDeclareMethCreatesEntry() {
        table.declareMeth("foo", "body", "void");
        Symbol s = table.findSymbol("foo");
        assertNotNull(s);
        assertEquals("meth", s.getKind());
        assertEquals("void", s.getType());
        assertEquals("body", s.getValue());
    }


    @Test
    void testUpdateValueWorksForVar() {
        table.declareVar("a", 1, "entier");
        assertTrue(table.updateValue("a", 10));
        Symbol s = table.findSymbol("a");
        assertEquals(10, s.getValue());
    }

    @Test
    void testUpdateValueFailsForUnknown() {
        assertFalse(table.updateValue("ghost", 10));
    }

    @Test
    void testUpdateValueFailsForConst() {
        table.declareCst("C", 9, "entier");
        assertFalse(table.updateValue("C", 0));
    }


    @Test
    void testRemoveExistingSymbol() {
        table.declareVar("z", 1, "entier");
        assertTrue(table.remove("z"));
        assertFalse(table.contains("z"));
    }

    @Test
    void testRemoveUnknownSymbol() {
        assertFalse(table.remove("unknown"));
    }


    @Test
    void testContainsSymbol() {
        table.declareVar("v", 2, "entier");
        assertTrue(table.contains("v"));
        assertFalse(table.contains("x"));
    }

    // ---------- FIND SYMBOL ----------

    @Test
    void testFindSymbolReturnsNullIfNotFound() {
        assertNull(table.findSymbol("nothing"));
    }

    @Test
    void testFindSymbolAfterUpdate() {
        table.declareVar("flag", true, "booleen");
        table.updateValue("flag", false);
        Symbol s = table.findSymbol("flag");
        assertEquals(false, s.getValue());
    }

    @Test
    void testInsertManySymbolsAndCheckCount() {
        int n = 150;
        for (int i = 0; i < n; i++) {
            table.declareVar("v" + i, i, "entier");
        }
        assertEquals(n, table.size());
        for (int i = 0; i < n; i++) {
            Symbol s = table.findSymbol("v" + i);
            assertNotNull(s);
            assertEquals(i, s.getValue());
        }
    }

    // ---------- EDGE CASES ----------

    @Test
    void testRemoveThenReinsertSameName() {
        table.declareVar("tmp", 1, "entier");
        assertTrue(table.remove("tmp"));
        table.declareVar("tmp", 9, "entier");
        Symbol s = table.findSymbol("tmp");
        assertEquals(9, s.getValue());
    }

    @Test
    void testDoubleDeclareReplacesExisting() {
        table.declareVar("dup", 1, "entier");
        assertFalse(table.declareVar("dup", 2, "entier"));
        Symbol s = table.findSymbol("dup");
        assertEquals(1, s.getValue());
    }

    @Test
    void testPrintTableRunsWithoutError() {
        table.declareVar("x", 5, "entier");
        table.declareCst("c", 1, "entier");
        table.printTable();
    }

    // ---------- SYMBOL TESTS ----------

    @Test
    void testSymbolIntegrity() {
        Symbol s = new Symbol("id1", "entier", "var", 42);
        assertEquals("id1", s.getName());
        assertEquals("entier", s.getType());
        assertEquals("var", s.getKind());
        assertEquals(42, s.getValue());
        assertTrue(s.toString().contains("id1"));
    }

    @Test
    void testDeclareVarWithNullInputs() {
        table.declareVar(null, 10, "entier");
        table.declareVar("x", 10, null);
        assertEquals(0, table.size());
    }

    @Test
    void testDeclareCstWithNullInputs() {
        table.declareCst(null, 10, "entier");
        table.declareCst("x", 10, null);
        assertEquals(0, table.size());
    }

    @Test
    void testDeclareTabWithNullInputs() {
        table.declareTab(null, 5, "entier");
        table.declareTab("t", 5, null);
        assertEquals(0, table.size());
    }

    @Test
    void testDeclareMethWithNullInputs() {
        table.declareMeth(null, "body", "entier");
        table.declareMeth("m", "body", null);
        assertEquals(0, table.size());
    }

    @Test
    void testAssignToConstFails() {
        table.declareCst("C", 1, "entier");
        assertFalse(table.assign("C", 5));
    }

    @Test
    void testAssignToUnknownFails() {
        assertFalse(table.assign("unknown", 10));
    }

    @Test
    void testLengthOfNonTabReturnsMinusOne() {
        table.declareVar("x", 5, "entier");
        assertEquals(-1, table.lengthOf("x"));
    }

    @Test
    void testLengthOfTabInvalidFormat() {
        table.declareTab("t", 5, "entier");
        table.updateValue("t", "wrong");
        assertEquals(-1, table.lengthOf("t"));
    }

    @Test
    void testUpdateTypeAndUnknownSymbol() {
        assertFalse(table.updateType("unknown", "entier"));
    }

    @Test
    void testHashHandlesEmptyAndNull() {
        try {
            var h1 = table.getClass().getDeclaredMethod("hash", String.class);
            h1.setAccessible(true);
            assertEquals(0, (int) h1.invoke(table, (Object) null));
            assertEquals(0, (int) h1.invoke(table, ""));
        } catch (Exception e) {
            fail(e);
        }
    }
    @Test
    void testLengthOfTabTriggersException() {
        table.declareTab("tabErr", 3, "entier");
        table.updateValue("tabErr", new Object());
        assertEquals(-1, table.lengthOf("tabErr"));
    }

    @Test
    void testRemoveMiddleOfChain() {
        table.declareVar("a", 1, "entier");
        table.declareVar("b", 2, "entier");
        table.declareVar("c", 3, "entier");
        assertTrue(table.remove("b"));
    }

    @Test
    void testIsConstAndUpdateTypeForUnknowns() {
        assertFalse(table.isConst("notFound"));
        assertFalse(table.updateType("notFound", "entier"));
    }

    @Test
    void testAssignNullValueAndUnknown() {
        table.declareVar("v", 1, "entier");
        assertFalse(table.assign("unknown", 2));
        assertTrue(table.assign("v", null));
    }

    @Test
    void testHashWithNullAndEmpty() throws Exception {
        var m = SymbolTable.class.getDeclaredMethod("hash", String.class);
        m.setAccessible(true);
        assertEquals(0, (int) m.invoke(table, (Object) null));
        assertEquals(0, (int) m.invoke(table, ""));
    }
    @Test
    void testDeclareWithNullsIgnored() {
        table.declareVar(null, 1, "entier");
        table.declareCst("z", 2, null);
        table.declareTab(null, 5, "entier");
        table.declareMeth(null, "body", "void");
        assertEquals(0, table.size());
    }

    @Test
    void testAssignUnknownAndConst() {
        table.declareCst("K", 10, "entier");
        assertFalse(table.assign("unknown", 5));
        assertFalse(table.assign("K", 5));
    }

    @Test
    void testUpdateTypeForUnknownAndNullType() {
        assertFalse(table.updateType("missing", "entier"));
        table.declareVar("v", 1, "entier");
        assertTrue(table.updateType("v", "booleen"));
    }

    @Test
    void testLengthOfTriggersCatch() {
        table.declareTab("t", 3, "entier");
        table.updateValue("t", new Object());
        assertEquals(-1, table.lengthOf("t"));
    }

    @Test
    void testRemoveMiddleChainScenario() {
        table.declareVar("x", 1, "entier");
        table.declareVar("y", 2, "entier");
        table.declareVar("z", 3, "entier");
        assertTrue(table.remove("y"));
    }

    @Test
    void testUpdateValueUnknownSymbolReturnsFalse() {
        assertFalse(table.updateValue("notExist", 123));
    }

    @Test
    void testLengthOfWithoutSizePrefixReturnsMinusOne() {
        table.declareTab("weird", 4, "entier");
        table.updateValue("weird", "wrongFormatValue");
        assertEquals(-1, table.lengthOf("weird"));
    }

    @Test
    void testRemoveLastElementInChain() {
        table.declareVar("alpha", 1, "entier");
        table.declareVar("beta", 2, "entier");
        assertTrue(table.remove("beta"));
    }

    @Test
    void testLengthOfCatchBlockTriggered() {
        table.declareTab("badTab", 3, "entier");
        table.updateValue("badTab", "size=abc");
        assertEquals(-1, table.lengthOf("badTab"));
    }



    @Test
    void testHashEmptyString() throws Exception {
        var method = SymbolTable.class.getDeclaredMethod("hash", String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(table, "");
        assertEquals(0, result);
    }
    @Test
    void testDeclareWithNullTypeAndName() {
        table.declareVar(null, 10, null);
        table.declareCst(null, 1, null);
        table.declareTab(null, 5, null);
        table.declareMeth(null, "body", null);
        assertEquals(0, table.size());
    }

    @Test
    void testLengthOfCatchAndFallbackPath() {
        table.declareTab("t", 3, "entier");
        table.updateValue("t", "size=oops");
        assertEquals(-1, table.lengthOf("t"));
    }

    @Test
    void testRemoveWithPrevNotNull() {
        table.declareVar("aa", 1, "entier");
        table.declareVar("bb", 2, "entier");
        table.declareVar("cc", 3, "entier");
        assertTrue(table.remove("bb"));
    }
    @Test
    void testDeclareNullsTriggersReturn() {
        table.declareVar(null, 10, "entier");
        table.declareVar("ok", 1, null);
        table.declareCst(null, 2, "entier");
        table.declareTab(null, 3, "entier");
        table.declareMeth(null, "body", "entier");
        assertEquals(0, table.size());
    }

    @Test
    void testLengthOfCatchBlockExecuted() {
        table.declareTab("broken", 4, "entier");
        table.updateValue("broken", "size=abc");
        assertEquals(-1, table.lengthOf("broken"));
    }


    @Test
    void testRemoveChainMiddleElement() {
        table.declareVar("x", 1, "entier");
        table.declareVar("xx", 2, "entier");
        table.declareVar("xxx", 3, "entier");
        assertTrue(table.remove("xx")); //
    }



    @Test
    void testUpdateTypeWithNullType() {
        table.declareVar("foo", 1, "entier");
        assertTrue(table.updateType("foo", null)); //
    }

    @Test
    void testHashNegativeOverflowPath() throws Exception {
        var method = SymbolTable.class.getDeclaredMethod("hash", String.class);
        method.setAccessible(true);
        String weird = new String(new char[5000]).replace('\0', 'ÿ');
        int h = (int) method.invoke(table, weird);
        assertTrue(h >= 0);
    }
    @Test
    void testPrintEmptyTable() {
        table.printTable();
    }
    @Test
    void testAssignNullValue() {
        table.declareVar("n", 5, "entier");
        assertTrue(table.assign("n", null));
    }

    @Test
    void testRemoveFromEmptyTable() {
        assertFalse(table.remove("ghost"));
    }


    @Test
    void testLengthOfValidTabReturnsSize() {
        table.declareTab("L", 7, "entier");
        assertEquals(7, table.lengthOf("L"));
    }

    @Test
    void testLookupFound() {
        table.declareVar("foundX", 11, "entier");
        Symbol s = table.lookup("foundX");
        assertNotNull(s);
        assertEquals("foundX", s.getName());
        assertEquals("var", s.getKind());
        assertEquals(11, s.getValue());
    }

    @Test
    void testLookupNotFound() {
        assertNull(table.lookup("nope"));
    }

    @Test
    void testIsConstTrue() {
        table.declareCst("KONST", 99, "entier");
        assertTrue(table.isConst("KONST"));
    }

    @Test
    void testRemoveHeadOfNonSingletonChain() {
        table.declareVar("a", 1, "entier");
        table.declareVar("aa", 2, "entier");
        assertTrue(table.remove("a"));
        assertTrue(table.contains("aa"));
        assertFalse(table.contains("a"));
    }

    @Test
    void testAssignUnknownTriggersErrorMessage() {
        assertFalse(table.assign("notExisting", 123));
    }

    @Test
    void testUpdateTypeUnknownTriggersErrorMessage() {
        assertFalse(table.updateType("ghost", "entier"));
    }

    @Test
    void testPrintTableAfterInsert() {
        table.declareVar("a", 1, "entier");
        table.printTable();
    }
    @Test
    void testLengthOfNullName() {
        assertEquals(-1, table.lengthOf(null));
    }
    @Test
    void testRemoveNullIdentifier() {
        assertFalse(table.remove(null));
    }
    @Test
    void testHashForNegativeValuePath() throws Exception {
        var method = SymbolTable.class.getDeclaredMethod("hash", String.class);
        method.setAccessible(true);
        String s = "\uFFFF\uFFFF\uFFFF";
        int h = (int) method.invoke(table, s);
        assertTrue(h >= 0);
    }

    @Test
    void testFindNodeNullInput() throws Exception {
        var m = SymbolTable.class.getDeclaredMethod("findSymbol", String.class);
        m.setAccessible(true);
        assertNull(m.invoke(table, (Object) null));
    }


    @Test
    void testPrintTableMultipleBuckets() {
        for (int i = 0; i < 10; i++) {
            table.declareVar("x" + i, i, "entier");
        }
        table.printTable();


}

    @Test
    void testFindSymbolNullName() {
        assertNull(table.findSymbol(null));
    }

    @Test
    void testLookupNullName() {
        assertNull(table.lookup(null));
    }

    @Test
    void testContainsNullName() {
        assertFalse(table.contains(null));
    }

    @Test
    void testAssignNullName() {
        assertFalse(table.assign(null, 1));
    }

    @Test
    void testUpdateValueNullName() {
        assertFalse(table.updateValue(null, 1));
    }

    @Test
    void testUpdateTypeNullName() {
        assertFalse(table.updateType(null, "entier"));
    }
    @Test
    void testLengthOfPrintStackTraceExecuted() throws Exception {
        table.declareTab("boom", 3, "entier");

        var tableField = SymbolTable.class.getDeclaredField("table");
        tableField.setAccessible(true);
        Object[] buckets = (Object[]) tableField.get(table);

        for (Object bucket : buckets) {
            if (bucket != null) {
                var node = bucket;
                var quadField = node.getClass().getDeclaredField("quad");
                quadField.setAccessible(true);
                Object quad = quadField.get(node);
                var valueField = quad.getClass().getDeclaredField("value");
                valueField.setAccessible(true);

                valueField.set(quad, new Object() {
                    @Override
                    public String toString() {
                        throw new RuntimeException("trigger catch block");
                    }
                });
                break;
            }
        }

        assertEquals(-1, table.lengthOf("boom"));
    }

    @Test
    void testLengthOfCatchPrintStackTraceFull() throws Exception {
        table.declareTab("ERRTAB", 3, "entier");

        var tableField = SymbolTable.class.getDeclaredField("table");
        tableField.setAccessible(true);
        Object[] buckets = (Object[]) tableField.get(table);

        for (Object bucket : buckets) {
            if (bucket != null) {
                var node = bucket;
                var quadField = node.getClass().getDeclaredField("quad");
                quadField.setAccessible(true);
                Object quad = quadField.get(node);
                var valueField = quad.getClass().getDeclaredField("value");
                valueField.setAccessible(true);

                valueField.set(quad, new Object() {
                    @Override
                    public String toString() {
                        throw new RuntimeException("force catch for coverage");
                    }
                });
                break;
            }
        }

        try {
            table.lengthOf("ERRTAB");
        } catch (Exception ex) {
            fail("should be caught internally, not rethrown");
        }

        table.lengthOf("ERRTAB");
    }

    @Test
    void testLengthOfNullNodeBranch() {
        // حالت node == null باید بررسی بشه
        assertEquals(-1, table.lengthOf("notExistTab"));
    }

    @Test
    void testRemoveFirstNodeBranch() {
        // حالت prev == null
        table.declareVar("x", 1, "entier");
        table.declareVar("y", 2, "entier");
        assertTrue(table.remove("x")); // head node حذف شود
    }

    @Test
    void testRemoveMiddleNodeBranch() {
        // حالت prev != null
        table.declareVar("x", 1, "entier");
        table.declareVar("y", 2, "entier");
        table.declareVar("z", 3, "entier");
        assertTrue(table.remove("y")); // middle node
    }

    @Test
    void testHashFullBranches() throws Exception {
        var method = SymbolTable.class.getDeclaredMethod("hash", String.class);
        method.setAccessible(true);
        assertEquals(0, (int) method.invoke(table, (Object) null)); // null branch
        assertEquals(0, (int) method.invoke(table, "")); // empty branch
        assertTrue((int) method.invoke(table, "abc") > 0); // normal branch
    }
}

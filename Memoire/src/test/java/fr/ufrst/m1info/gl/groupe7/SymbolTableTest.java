package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SymbolTable
 */
public class SymbolTableTest {

    private SymbolTable table;

    @BeforeEach
    void setup() {
        table = new SymbolTable();
    }

    @Test
    void testAddAndFindSymbol() {
        Symbol s = new Symbol("x", "int", "var", 10);
        table.addSymbol(s);

        Symbol found = table.findSymbol("x");
        assertNotNull(found);
        assertEquals("int", found.getType());
        assertEquals(10, found.getValue());
    }

    @Test
    void testReplaceSymbol() {
        table.addSymbol(new Symbol("a", "int", "var", 1));
        table.addSymbol(new Symbol("a", "int", "var", 99)); // replaces
        assertEquals(99, table.findSymbol("a").getValue());
        assertEquals(1, table.size());
    }

    @Test
    void testSetValue() {
        table.addSymbol(new Symbol("b", "int", "var", 5));
        Symbol prev = table.setValue("b", 8);
        assertNotNull(prev);
        assertEquals(8, table.findSymbol("b").getValue());
    }

    @Test
    void testRemoveSymbol() {
        table.addSymbol(new Symbol("flag", "boolean", "var", false));
        table.remove("flag");
        assertNull(table.findSymbol("flag"));
        assertEquals(0, table.size());
    }

    @Test
    void testOpenCloseScopeNoEffect() {
        table.addSymbol(new Symbol("x", "int", "var", 0));
        table.openScope();
        table.closeScope();
        assertEquals(1, table.size());
    }

    @Test
    void testPrintTable() {
        table.addSymbol(new Symbol("v", "int", "var", 10));
        table.printTable(); // Should not throw
    }
}

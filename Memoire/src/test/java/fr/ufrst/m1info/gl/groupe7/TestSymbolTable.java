package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Symbol and SymbolTable classes.
 */
public class TestSymbolTable {

    @Test
    public void testAddAndGetSymbol() {
        SymbolTable table = new SymbolTable();
        Symbol s = new Symbol("x", "var", "entier", 5);
        table.add(s);
        Symbol result = table.get("x");
        assertNotNull(result);
        assertEquals("x", result.getId());
        assertEquals(5, result.getValue());
    }

    @Test
    public void testExistsAndUpdate() {
        SymbolTable table = new SymbolTable();
        table.add(new Symbol("y", "var", "entier", 2));
        assertTrue(table.exists("y"));
        table.update("y", 9);
        Symbol s = table.get("y");
        assertEquals(9, s.getValue());
    }

    @Test
    public void testGetUnknownSymbolReturnsNull() {
        SymbolTable table = new SymbolTable();
        assertNull(table.get("unknown"));
    }

    @Test
    public void testTableFullDoesNotCrash() {
        SymbolTable table = new SymbolTable();
        for (int i = 0; i < 256; i++) {
            table.add(new Symbol("id" + i, "var", "entier", i));
        }
        table.add(new Symbol("overflow", "var", "entier", 999));
        assertEquals(256, table.getSize());
    }
}
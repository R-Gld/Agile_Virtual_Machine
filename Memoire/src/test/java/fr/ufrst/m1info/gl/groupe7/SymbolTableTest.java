package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SymbolTableTest {



    @Test
    public void testAddAndFindSymbol() {
        SymbolTable table = new SymbolTable();
        Symbol x = new Symbol("x", "int", "var", 10);
        table.addSymbol(x);

        Symbol found = table.findSymbol("x");
        assertNotNull(found);
        assertEquals("int", found.getType());
        assertEquals(10, found.getValue());
    }

    @Test
    public void testRemoveSymbol() {
        SymbolTable table = new SymbolTable();
        table.addSymbol(new Symbol("y", "boolean", "var", true));
        assertTrue(table.containsSymbol("y"));

        table.removeSymbol("y");
        assertFalse(table.containsSymbol("y"));
    }

    @Test
    public void testScopes() {
        SymbolTable table = new SymbolTable();
        table.addSymbol(new Symbol("a", "int", "var", 1));

        table.openScope();
        table.addSymbol(new Symbol("b", "int", "var", 2));

        assertNotNull(table.findSymbol("b")); // in inner scope
        assertNotNull(table.findSymbol("a")); // from outer scope

        table.closeScope();
        assertNull(table.findSymbol("b")); // b was removed
        assertNotNull(table.findSymbol("a")); // a still exists
    }
}

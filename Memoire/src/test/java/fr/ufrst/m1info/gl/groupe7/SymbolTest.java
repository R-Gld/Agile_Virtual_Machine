package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SymbolTest {

    @Test
    void testCreationAndGetters() {
        Symbol s = new Symbol("x", "int", "var", 42);
        assertEquals("x", s.getName());
        assertEquals("int", s.getType());
        assertEquals("var", s.getKind());
        assertEquals(42, s.getValue());
    }

    @Test
    void testToStringContainsAllParts() {
        Symbol s = new Symbol("flag", "bool", "cst", true);
        String str = s.toString();
        assertTrue(str.contains("flag"));
        assertTrue(str.contains("bool"));
        assertTrue(str.contains("cst"));
        assertTrue(str.contains("true"));
    }
}

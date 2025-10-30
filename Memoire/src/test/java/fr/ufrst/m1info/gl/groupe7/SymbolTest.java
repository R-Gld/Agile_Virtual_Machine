package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Symbol class.
 * This version matches the field names and methods:
 *   id, obj, type, value.
 */
public class SymbolTest {

    @Test
    void testCreationAndGetters() {
        Symbol s = new Symbol("x", "var", "entier", 42);

        // Check basic field getters
        assertEquals("x", s.getId());
        assertEquals("var", s.getObj());
        assertEquals("entier", s.getType());
        assertEquals(42, s.getValue());
    }

    @Test
    void testValueSetter() {
        Symbol s = new Symbol("flag", "cst", "booleen", false);
        s.setValue(true);
        assertTrue((Boolean) s.getValue());
    }

    @Test
    void testToStringFormat() {
        Symbol s = new Symbol("arr", "tab", "entier", "[1,2,3]");
        String repr = s.toString();

        // Check that all components appear in string form
        assertTrue(repr.contains("arr"));
        assertTrue(repr.contains("tab"));
        assertTrue(repr.contains("entier"));
        assertTrue(repr.contains("[1,2,3]"));
    }
}

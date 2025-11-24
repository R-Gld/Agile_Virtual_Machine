package fr.ufrst.m1info.gl.groupe7.memoire;

import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SymbolTest {

    @Test
    void testCreationAndGetters() {
        Symbol s = new Symbol("x", Type.ENTIER,  42);
        assertEquals("x", s.getName());
        assertEquals(Type.ENTIER, s.getType());
        assertEquals(42, s.getAddressStack());

    }

    @Test
    void testToStringContainsAllParts() {
        Symbol s = new Symbol("flag", Type.BOOLEEN, 2);
        String str = s.toString();
        assertTrue(str.contains("flag"));
        assertTrue(str.contains("bool"));

    }
}

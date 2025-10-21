package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SymbolTest {

    @Test
    public void testBasicSymbol() {

        Symbol x = new Symbol("x", "int", "var", 10);


        assertEquals("x", x.getName());
        assertEquals("int", x.getType());
        assertEquals("var", x.getKind());
        assertEquals(10, x.getValue());
    }
}

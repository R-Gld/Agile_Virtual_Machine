package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SeverityTest {

    @Test
    void testEnumValues() {
        Severity[] severities = Severity.values();
        assertEquals(2, severities.length);
        assertArrayEquals(
                new Severity[] { Severity.ERROR, Severity.WARNING },
                severities
        );
    }

    @Test
    void testValueOf() {
        assertEquals(Severity.ERROR, Severity.valueOf("ERROR"));
        assertEquals(Severity.WARNING, Severity.valueOf("WARNING"));
    }
}

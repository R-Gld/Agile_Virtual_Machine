package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PhaseTest {

    @Test
    void testEnumValues() {
        Phase[] phases = Phase.values();
        assertEquals(4, phases.length);
        assertArrayEquals(
                new Phase[] { Phase.LEXICAL, Phase.SYNTAX, Phase.SEMANTIC, Phase.RUNTIME },
                phases
        );
    }

    @Test
    void testValueOf() {
        assertEquals(Phase.LEXICAL, Phase.valueOf("LEXICAL"));
        assertEquals(Phase.SYNTAX, Phase.valueOf("SYNTAX"));
        assertEquals(Phase.SEMANTIC, Phase.valueOf("SEMANTIC"));
        assertEquals(Phase.RUNTIME, Phase.valueOf("RUNTIME"));
    }
}

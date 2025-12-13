package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiagnosticTest {

    @Test
    void testConstructor() {
        Severity severity = Severity.ERROR;
        Phase phase = Phase.LEXICAL;
        SourcePosition pos = new SourcePosition(null, 10, 5);
        String message = "This is a test error message.";

        Diagnostic diag = new Diagnostic(severity, phase, pos, message);

        assertEquals(severity, diag.severity());
        assertEquals(phase, diag.phase());
        assertEquals(pos, diag.position());
        assertEquals(message, diag.message());
    }

}

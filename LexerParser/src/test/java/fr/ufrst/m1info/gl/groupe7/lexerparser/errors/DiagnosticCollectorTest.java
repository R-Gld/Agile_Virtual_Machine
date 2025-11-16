package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosticCollectorTest {

    private DiagnosticCollector collector;

    @BeforeEach
    void setUp() {
        collector = new DiagnosticCollector();
    }

    @Test
    void testReport_AddsDiagnosticToList() {
        SourcePosition pos = new SourcePosition("test.txt", 1, 1);
        collector.report(Severity.WARNING, Phase.LEXICAL, pos, "Test warning");

        List<Diagnostic> diagnostics = collector.getDiagnostics();
        assertEquals(1, diagnostics.size());

        Diagnostic diag = diagnostics.getFirst();
        assertEquals(Severity.WARNING, diag.severity());
        assertEquals(Phase.LEXICAL, diag.phase());
        assertEquals(pos, diag.position());
        assertEquals("Test warning", diag.message());
    }

    @Test
    void testHasErrors_WhenNoError_ReturnsFalse() {
        collector.report(Severity.WARNING, Phase.SYNTAX, new SourcePosition("file", 1, 1), "Just a warning");
        assertFalse(collector.hasErrors());
    }

    @Test
    void testHasErrors_WhenErrorExists_ReturnsTrue() {
        collector.report(Severity.ERROR, Phase.SEMANTIC, new SourcePosition("file", 2, 3), "Critical error");
        assertTrue(collector.hasErrors());
    }

    @Test
    void testGetDiagnostics_ReturnsEmptyListInitially() {
        assertTrue(collector.getDiagnostics().isEmpty());
    }

    @Test
    void testMultipleDiagnostics_AllStoredCorrectly() {
        SourcePosition pos1 = new SourcePosition("a.txt", 1, 2);
        SourcePosition pos2 = new SourcePosition("b.txt", 3, 4);

        collector.report(Severity.WARNING, Phase.LEXICAL, pos1, "warn");
        collector.report(Severity.ERROR, Phase.RUNTIME, pos2, "err");

        List<Diagnostic> diagnostics = collector.getDiagnostics();
        assertEquals(2, diagnostics.size());
        assertTrue(diagnostics.stream().anyMatch(d -> d.severity() == Severity.ERROR));
        assertTrue(diagnostics.stream().anyMatch(d -> d.severity() == Severity.WARNING));
    }
}
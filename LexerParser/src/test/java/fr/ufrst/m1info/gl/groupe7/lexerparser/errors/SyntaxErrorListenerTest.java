package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SyntaxErrorListenerTest {

    private DiagnosticCollector collector;
    private SyntaxErrorListener listener;

    @BeforeEach
    void setUp() {
        collector = mock(DiagnosticCollector.class);
        listener = new SyntaxErrorListener(collector, "TestFile.java");
    }

    @Test
    void testSyntaxErrorReportsDiagnostic() {
        Recognizer<?, ?> recognizer = mock(Recognizer.class);
        RecognitionException exception = mock(RecognitionException.class);

        listener.syntaxError(recognizer, null, 2, 4, "Unexpected token", exception);

        ArgumentCaptor<Severity> severityCaptor = ArgumentCaptor.forClass(Severity.class);
        ArgumentCaptor<Phase> phaseCaptor = ArgumentCaptor.forClass(Phase.class);
        ArgumentCaptor<SourcePosition> positionCaptor = ArgumentCaptor.forClass(SourcePosition.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(collector, times(1)).report(
                severityCaptor.capture(),
                phaseCaptor.capture(),
                positionCaptor.capture(),
                messageCaptor.capture()
        );

        assertEquals(Severity.ERROR, severityCaptor.getValue());
        assertEquals(Phase.SYNTAX, phaseCaptor.getValue());
        assertEquals("TestFile.java", positionCaptor.getValue().fileName());
        assertEquals(2, positionCaptor.getValue().line());
        assertEquals(4, positionCaptor.getValue().column());
        assertEquals("Unexpected token", messageCaptor.getValue());
    }
}

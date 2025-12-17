package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import org.antlr.v4.runtime.*;

/**
 * ANTLR error listener that reports errors to a DiagnosticCollector.
 */
public class SyntaxErrorListener extends BaseErrorListener {

    private final DiagnosticCollector diagnosticCollector;
    private final String fileName;

    /**
     * Constructor.
     * @param diagnosticCollector the DiagnosticCollector to report errors to
     * @param fileName the name of the file being parsed
     */
    public SyntaxErrorListener(DiagnosticCollector diagnosticCollector, String fileName) {
        this.diagnosticCollector = diagnosticCollector;
        this.fileName = fileName;
    }

    /**
     * Report a syntax error.
     * @param recognizer the recognizer in use when the error occurred
     * @param offendingSymbol the symbol at which the error occurred
     * @param line the line at which the error occurred
     * @param charPositionInLine the character position in the line at which the error occurred
     * @param msg the error message
     * @param recognitionException the RecognitionException that caused the error
     */
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException recognitionException) {
        SourcePosition sourcePosition = new SourcePosition(fileName, line, charPositionInLine); // TODO charPositionInLine+1 ?
        diagnosticCollector.report(Severity.ERROR, Phase.SYNTAX, sourcePosition, msg);
    }

    public void register(Object object) {
        if (object instanceof Lexer lexer) {
            lexer.removeErrorListeners();
            lexer.addErrorListener(this);
        } else if (object instanceof Parser parser) {
            parser.removeErrorListeners();
            parser.addErrorListener(this);
        } else {
            throw new IllegalArgumentException("Object must be a Lexer or Parser");
        }
    }
}

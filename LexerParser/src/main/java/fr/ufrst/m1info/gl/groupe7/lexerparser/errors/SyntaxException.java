package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

/**
 * Exception thrown when a syntax error is detected.
 *
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompiler
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreter
 * @see DiagnosticCollector
 * @see SyntaxErrorListener
 * @see Diagnostic
 * @see Severity
 * @see Phase
 * @see SourcePosition
 */
public class SyntaxException extends RuntimeException {

    public SyntaxException(DiagnosticCollector collector) {
        super(collector != null && collector.hasErrors()
                ? collector.formatDiagnostics()
                : "Unknown syntax error.");
    }

}

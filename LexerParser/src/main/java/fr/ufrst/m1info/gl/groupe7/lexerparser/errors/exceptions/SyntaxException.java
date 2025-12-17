package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;

/**
 * Exception thrown when a syntax error is detected.
 *
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompiler
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreter
 * @see DiagnosticCollector
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Diagnostic
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SourcePosition
 */
public class SyntaxException extends RuntimeException {

    public SyntaxException(DiagnosticCollector collector) {
        super(collector != null && collector.hasErrors()
                ? collector.formatDiagnostics()
                : "Unknown syntax error.");
    }

}

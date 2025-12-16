package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;

/**
 * Exception thrown when a semantic error is detected during semantic analysis.
 *
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompiler
 * @see DiagnosticCollector
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Diagnostic
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase
 * @see fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SourcePosition
 */
public class SemanticException extends RuntimeException {

    public SemanticException(DiagnosticCollector collector) {
        super(collector != null && collector.hasErrors()
                ? collector.formatDiagnostics()
                : "Unknown semantic error.");
    }

}

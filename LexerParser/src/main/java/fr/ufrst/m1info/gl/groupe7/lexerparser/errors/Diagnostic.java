package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

/**
 * Diagnostic class.
 * @param severity the severity of the diagnostic (ERROR or WARNING)
 * @param phase the phase of the diagnostic (LEXICAL, SYNTAX, SEMANTIC or RUNTIME)
 * @param position the position of the diagnostic (file name, line and column)
 * @param message the message of the diagnostic
 */
public record Diagnostic(Severity severity, Phase phase, SourcePosition position, String message) {}

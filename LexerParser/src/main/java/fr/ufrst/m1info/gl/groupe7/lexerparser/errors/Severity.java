package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

/**
 * Represents the severity level of a diagnostic message.
 * The severity can either indicate an <code>ERROR</code>, which is a critical issue that
 * may halt the process, or a <code>WARNING</code>, which is a non-critical issue that
 * allows continuation but indicates a potential problem.
 */
public enum Severity {
    ERROR,
    WARNING
}

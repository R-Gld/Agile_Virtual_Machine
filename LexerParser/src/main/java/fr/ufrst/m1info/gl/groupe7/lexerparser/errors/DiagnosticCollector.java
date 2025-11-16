package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import java.util.ArrayList;
import java.util.List;

/** Diagnostic collector using the Singleton design pattern. */
public class DiagnosticCollector {

    /** List of diagnostics. */
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    /**
     * Report a diagnostic.
     * @param severity the severity of the diagnostic
     * @param phase the phase of the diagnostic
     * @param position the position of the diagnostic
     * @param message the message of the diagnostic
     */
    public void report(Severity severity, Phase phase, SourcePosition position, String message) {
        diagnostics.add(new Diagnostic(severity, phase, position, message));
    }

    /**
     * Check if there are any errors.
     * @return true if there are reports with severity ERROR, false otherwise
     */
    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(d -> d.severity() == Severity.ERROR);
    }

    /**
     * Get the list of diagnostics.
     * @return the list of diagnostics
     */
    public List<Diagnostic> getDiagnostics() { return diagnostics; }

    public String formatDiagnostics() {
        StringBuilder sb = new StringBuilder("\n\n");

        diagnostics.forEach(d -> sb.append(" - A ")
                .append(d.phase().toString().toLowerCase())
                .append(" error occur")
                .append(d.position().fileName() != null ? " in '" + d.position().fileName() + "'" : "")
                .append(" at (")
                .append(d.position().line()).append(":").append(d.position().column())
                .append(") => ")
                .append(d.message())
                .append("\n"));

        return sb.toString();
    }
}

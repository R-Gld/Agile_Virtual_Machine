package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception when an undefined symbol is accessed in JajaCode.
 */
public class UndefinedSymbolException extends JajaCodeRuntimeException {

    private final String symbolName;

    public UndefinedSymbolException(String symbolName, String axiomeName, int programCounter) {
        super("Symbole introuvable : '" + symbolName + "'", axiomeName, programCounter);
        this.symbolName = symbolName;
    }

    public String getSymbolName() {
        return symbolName;
    }
}


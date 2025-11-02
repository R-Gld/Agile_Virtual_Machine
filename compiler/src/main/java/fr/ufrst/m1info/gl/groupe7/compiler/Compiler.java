package fr.ufrst.m1info.gl.groupe7.compiler;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode.MiniJajaCompilerVisitor;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.Stacks;
import fr.ufrst.m1info.gl.groupe7.SymbolTable;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compiler
 * Compiles MiniJaja code into JajaCode, with flexible output selection.
 */
public class Compiler implements Runnable {

    /**
     * Represents the destination where the output of the compilation process will be directed.
     * Possible destinations include:
     * - FILE: The compiled output will be written to a file.
     * - SYSOUT: The compiled output will be printed to the standard output (console).
     * - STRING: The compiled output will be returned as a string for further processing.
     */
    public enum Destination { FILE, SYSOUT, STRING }

    private final String inputMiniJaja;
    private final Destination destination;
    private final Path outputFile; // only used when destination == FILE

    /**
     * Last compiled output, always set after run()/runAndGet().
     */
    private volatile String lastOutput = "";

    /**
     * Read the code from a file and choose the destination.
     */
    public Compiler(Path inputFile, Destination destination, Path outputFile) throws IOException {
        this.inputMiniJaja = Files.readString(inputFile);
        this.destination = destination;
        this.outputFile = outputFile;
    }

    /**
     * Read the code from a file and choose the destination (no output file).
     */
    public Compiler(Path inputFile, Destination destination) throws IOException {
        this(inputFile, destination, null);
    }

    /**
     * Provide the code directly as a string and choose the destination.
     */
    public Compiler(String inputMiniJaja, Destination destination, Path outputFile) {
        this.inputMiniJaja = inputMiniJaja;
        this.destination = destination;
        this.outputFile = outputFile;
    }

    /**
     * Compile the MiniJaja code and return the generated JajaCode string,
     * without emitting anywhere.
     */
    public String compileToString() {
        CharStream cs = CharStreams.fromString(inputMiniJaja);
        MiniJajaCompilerVisitor compiler = getMiniJajaCompilerVisitor(cs);
        return compiler.getJajaCodeBuilder().toString();
    }

    /**
     * Compile the MiniJaja code and emit it to the destination.
     */
    @Override
    public void run() {
        String compiled = compileToString();
        this.lastOutput = compiled;

        switch (destination) {
            case FILE:
                if (outputFile == null)
                    throw new IllegalStateException("Destination FILE chosen but outputFile == null");
                try {
                    Files.writeString(outputFile, compiled, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException("Writing output file failed: " + e.getMessage(), e);
                }
                break;
            case SYSOUT:
                System.out.print(compiled);
                System.out.flush();
                break;
            case STRING: break;
            default: throw new IllegalStateException("Unknown destination: " + destination);
        }
    }

    /**
     * Get the last compiled output.
     */
    public String getLastOutput() {
        return lastOutput;
    }

    /**
     * Get a MiniJajaCompilerVisitor from a CharStream.
     * @param cs the CharStream to parse
     * @return a MiniJajaCompilerVisitor
     */
    public static MiniJajaCompilerVisitor getMiniJajaCompilerVisitor(CharStream cs) {
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser mjjparser = new MiniJajaParser(tokens);
        MiniJajaParser.ClasseContext parseTree = mjjparser.classe();

        SymbolTable symbolTable = new SymbolTable();
        Stacks stack = new Stacks();
        MiniJajaInterpreterVisitor miniJajaVisitor = new MiniJajaInterpreterVisitor(stack);
        ClasseNode ast = (ClasseNode) miniJajaVisitor.visit(parseTree);

        MiniJajaCompilerVisitor compiler = new MiniJajaCompilerVisitor(symbolTable);
        compiler.visit(ast);
        return compiler;
    }
}

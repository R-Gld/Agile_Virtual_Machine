package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.MiniJajaSemanticAnalyzer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;

public class MiniJajaCompiler {
    /**
     * Get a MiniJajaCompilerVisitor from a CharStream.
     * @param cs the CharStream to parse
     * @return a MiniJajaCompilerVisitor
     */
    public static MiniJajaCompilerVisitor getMiniJajaCompilerVisitor(CharStream cs) {

        DiagnosticCollector collector = new DiagnosticCollector();
        SyntaxErrorListener syntaxErrorListener = new SyntaxErrorListener(collector, null);

        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser mjjparser = new MiniJajaParser(tokens);
        syntaxErrorListener.register(lexer);
        syntaxErrorListener.register(mjjparser);

        MiniJajaParser.ClasseContext parseTree = mjjparser.classe();

        if (collector.hasErrors()) {
            throw new SyntaxException(collector);
        }

        Stacks stack = new Stacks();
        MiniJajaInterpreterVisitor miniJajaVisitor = new MiniJajaInterpreterVisitor();
        ClasseNode ast = (ClasseNode) miniJajaVisitor.visit(parseTree);

        // Semantic analysis
        MiniJajaSemanticAnalyzer semanticAnalyser = new MiniJajaSemanticAnalyzer(collector);
        semanticAnalyser.analyse(ast);

        if (collector.hasErrors()) {
            throw new SyntaxException(collector);
        }

        MiniJajaCompilerVisitor compiler = new MiniJajaCompilerVisitor(stack, collector);
        compiler.visit(ast);
        return compiler;
    }

    public static MiniJajaCompilerVisitor getMiniJajaCompilerVisitorFromString(String inputMiniJaja) {
        CharStream cs = CharStreams.fromString(inputMiniJaja);
        return getMiniJajaCompilerVisitor(cs);
    }

    public static MiniJajaCompilerVisitor getMiniJajaCompilerVisitorFromStream(InputStream inputMiniJaja) throws IOException {
        CharStream cs = CharStreams.fromStream(inputMiniJaja);
        return getMiniJajaCompilerVisitor(cs);
    }
}

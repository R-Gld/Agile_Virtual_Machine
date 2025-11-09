package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompilerVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.SymbolTable;
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

        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser mjjparser = new MiniJajaParser(tokens);
        MiniJajaParser.ClasseContext parseTree = mjjparser.classe();

        SymbolTable symbolTable = new SymbolTable();
        Stacks stack = new Stacks();
        MiniJajaInterpreterVisitor miniJajaVisitor = new MiniJajaInterpreterVisitor(stack);
        ClasseNode ast = (ClasseNode) miniJajaVisitor.visit(parseTree);

        fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompilerVisitor compiler = new fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompilerVisitor(symbolTable);
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

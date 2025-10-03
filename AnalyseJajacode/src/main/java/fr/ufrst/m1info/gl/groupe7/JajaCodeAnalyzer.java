package fr.ufrst.m1info.gl.groupe7;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;

public class JajaCodeAnalyzer {
    public static void main(String[] args) throws IOException {
        String filename = "AnalyseJajacode/src/main/java/fr/ufrst/m1info/gl/groupe7/jajacode.txt";
        CharStream input = CharStreams.fromFileName(filename);
        JajaCodeLexer lexer = new JajaCodeLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);

        ParseTree tree = parser.classe();

        ParseTreeWalker walker = new ParseTreeWalker();
        JajaCodeListener listener = new JajaCodeListener();
        walker.walk(listener, tree);

        System.out.println("Analyse terminée avec succès");
    }
}
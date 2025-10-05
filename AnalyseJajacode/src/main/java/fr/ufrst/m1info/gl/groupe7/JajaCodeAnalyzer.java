package fr.ufrst.m1info.gl.groupe7;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.io.InputStream;

public class JajaCodeAnalyzer {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Veuillez fournir le nom du fichier en argument. ex : jajacode.txt");
            return;
        }
        InputStream input = JajaCodeAnalyzer.class.getClassLoader().getResourceAsStream(args[0]);
        assert input != null;

        CharStream charStream = CharStreams.fromStream(input);
        JajaCodeLexer lexer = new JajaCodeLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);

        ParseTree tree = parser.classe();

        ParseTreeWalker walker = new ParseTreeWalker();
        JajaCodeListener listener = new JajaCodeListener();
        walker.walk(listener, tree);

        System.out.println("Analyse terminée avec succès");
    }
}
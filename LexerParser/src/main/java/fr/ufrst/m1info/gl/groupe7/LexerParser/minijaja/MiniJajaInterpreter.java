package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.Memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class MiniJajaInterpreter {
    public void run(String input) {
        Stacks stacks = new Stacks();

        CharStream cs = CharStreams.fromString(input);
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor(stacks);

        ParseTree tree = parser.classe();
        System.out.println("Debut de l'interprétation du minijaja");
        stacks.printStack();
        AstNode astRoot = visitor.visit(tree);
        System.out.println("\n=====  ARBRE SYNTAXIQUE ABSTRAIT (AST)  =====");
        if (astRoot != null) {
            System.out.println(astRoot.toStringTree());
        } else {
            System.out.println("ERREUR: L'AST est null.");
        }
        System.out.println("==============================================");

        stacks.printStack();

    }

}

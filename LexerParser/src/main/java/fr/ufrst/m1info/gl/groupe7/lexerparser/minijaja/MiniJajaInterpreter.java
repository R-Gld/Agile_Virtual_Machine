package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Walker;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class MiniJajaInterpreter implements Runnable {
    private final DiagnosticCollector collector;
    private final String input;

    public MiniJajaInterpreter(String input, DiagnosticCollector collector) {
        this.input = input;
        this.collector = collector;
    }

    @Override
    public void run() {
        Stacks stacks = new Stacks();

        SyntaxErrorListener sel = new SyntaxErrorListener(collector, null);

        CharStream cs = CharStreams.fromString(input);
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        sel.register(lexer);
        sel.register(parser);

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();

        ParseTree tree = parser.classe();

        if (collector.hasErrors()) {
            throw new SyntaxException(collector);
        }

        System.out.println("Debut de l'interprétation du minijaja");
      
        AstNode astRoot = visitor.visit(tree);
        System.out.println("\n=====  ARBRE SYNTAXIQUE ABSTRAIT (AST)  =====");
        if (astRoot != null) {
            System.out.println(astRoot.toStringTree());
        } else {
            System.out.println("ERREUR: L'AST est null.");
        }
        System.out.println("==============================================");
        
        System.out.println("\n=====  INTERPRETATION  =====");
        if (astRoot != null) {
           Walker walker = new Walker(astRoot,stacks);
           walker.walk();
        }
            stacks.printStack();

    }
}

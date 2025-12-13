package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.MiniJajaSemanticAnalyzer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Walker;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class MiniJajaInterpreter implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MiniJajaInterpreter.class);
    private final DiagnosticCollector collector;
    private final String input;
    private final String fileName;

    public MiniJajaInterpreter(String input, DiagnosticCollector collector, String fileName) {
        this.input = input;
        this.collector = collector;
        this.fileName = fileName;
    }

    // Keep old constructor for backward compatibility
    public MiniJajaInterpreter(String input, DiagnosticCollector collector) {
        this(input, collector, null);
    }

    @Override
    public void run() {
        Stacks stacks = new Stacks();

        SyntaxErrorListener sel = new SyntaxErrorListener(collector, fileName);

        CharStream cs = CharStreams.fromString(input);
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        sel.register(lexer);
        sel.register(parser);

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor(fileName);

        ParseTree tree = parser.classe();

        if (collector.hasErrors()) {
            throw new SyntaxException(collector);
        }

        logger.info("Debut de l'interprétation du minijaja");

        AstNode astRoot = visitor.visit(tree);
        logger.info("\n=====  ARBRE SYNTAXIQUE ABSTRAIT (AST)  =====");
        if (astRoot != null) {
            logger.info(astRoot.toStringTree());
        } else {
            logger.error("ERREUR: L'AST est null.");
        }

        // Semantic analysis
        if (astRoot instanceof ClasseNode classNode) {
            logger.info("\n=====  ANALYSE SÉMANTIQUE  =====");
            MiniJajaSemanticAnalyzer semanticAnalyser = new MiniJajaSemanticAnalyzer(collector);
            semanticAnalyser.analyse(classNode);

            if (collector.hasErrors()) {
                throw new SyntaxException(collector);
            }
            logger.info("Analyse sémantique réussie - aucune erreur détectée");
        }

        logger.info("==============================================");
        logger.info("\n=====  INTERPRETATION  =====");
        if (astRoot != null) {
           Walker walker = new Walker(astRoot,stacks);
           walker.walk();
        }

        stacks.printStack();
    }
}

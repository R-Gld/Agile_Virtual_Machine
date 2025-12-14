package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.HandlePauseCallback;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Walker;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniJajaDebugger implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(MiniJajaDebugger.class);
    
    private final Stacks stacks;
    private final Debug debug;
    private String input;
    private DiagnosticCollector collector;
    private final HandlePauseCallback callback;

    public MiniJajaDebugger(String input, DiagnosticCollector collector, HandlePauseCallback callback, Debug debug) {
        this.stacks = new Stacks();
        this.debug = debug;
        this.callback = callback;
        this.input = input;
    }
    
    @Override
    public void run() {
        debug.enable();
        
        Stacks stacks = new Stacks();

        SyntaxErrorListener sel = new SyntaxErrorListener(collector, null);

        CharStream cs = CharStreams.fromString(input);
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        sel.register(lexer);
        sel.register(parser);

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor(null);
        
        ParseTree tree = parser.classe();
        AstNode astRoot = visitor.visit(tree);
        
        if (astRoot != null) {
            logger.info("Debugger started");
            Walker walker = new Walker(astRoot, stacks, debug, callback);
            walker.walk();
        } else  {
            logger.error("ERROR: AST is null, can't start debugger");
        }
    }

    public Debug getDebug() {
        return debug;
    }

    public Stacks getStacks() {
        return stacks;
    }
}

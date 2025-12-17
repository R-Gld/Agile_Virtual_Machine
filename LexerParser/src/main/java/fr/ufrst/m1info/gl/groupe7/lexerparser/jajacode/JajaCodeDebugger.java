package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions.SyntaxException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Small wrapper to step through a JajaCode program.
 * Uses JajaCodeInterpreterVisitor (step(), isFinished(),
 * getCurrentInstructionIndex()).
 */
public class JajaCodeDebugger {

    public JajaCodeDebugger(String jajaCode, DiagnosticCollector collector) {
        Stacks stacks = new Stacks();

        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);

        SyntaxErrorListener sel = new SyntaxErrorListener(collector, null);
        sel.register(lexer);
        sel.register(parser);

        JajaCodeParser.ClasseContext arbreJajaCode = parser.classe();

        if (collector.hasErrors()) {
            throw new SyntaxException(collector);
        }

        JajaCodeInterpreterVisitor visitor = new JajaCodeInterpreterVisitor(stacks);
        visitor.load(arbreJajaCode);

    }
}

/** runner JajaCode */
/*
 * public boolean step() {
 * return visitor.step();
 * }
 * 
 * 
 * public boolean isFinished() {
 * return visitor.isFinished();
 * }
 * /*
 * /**(۰-based line)
 */
/*
 * public int getCurrentInstructionIndex() {
 * return visitor.getCurrentInstructionIndex();
 * }
 * }
 */
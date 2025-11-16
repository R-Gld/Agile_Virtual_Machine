package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class JajaCodeInterpreter implements Runnable {
    private final String jajaCode;
    private final DiagnosticCollector collector;

    public JajaCodeInterpreter(String jajaCode, DiagnosticCollector collector) {
        this.jajaCode = jajaCode;
        this.collector = collector;
    }

    public static void main(String[] args) {
        String jajaCode = """
                    1 init
                    2 push(0)
                    3 new(x@global, INT, VARIABLE, 0)
                    4 push(5)
                    5 store(x@global)
                    6 push(0)
                    7 swap
                    8 pop
                    9 pop
                    10 jcstop
                """;

        new JajaCodeInterpreter(jajaCode, new DiagnosticCollector()).run();
    }

    /**
     * Fonction utiliser par l'interface pour interpreter du jajacode
     */
    @Override
    public void run() {
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

        JajaCodeInterpreterVisitor interpreteur = new JajaCodeInterpreterVisitor(stacks);

        interpreteur.load(arbreJajaCode);
        interpreteur.run();
    }
}


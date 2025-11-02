package fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.Memoire.SymbolTable;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class JajaCodeInterpreter {
    public static void main(String[] args) {
        SymbolTable symbolTable = new SymbolTable();
        String jajaCode = """
                    1 init
                    2 push(0)
                    3 new(x@1, int, var, 0)
                    4 push(5)
                    5 store(x@1)
                    6 push(0)
                    7 swap
                    8 pop
                    9 pop
                    10 jcstop
                """;

        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbreJajaCode = parser.classe();

        JajaCodeInterpreterVisitor interpreteur = new JajaCodeInterpreterVisitor(symbolTable);

        interpreteur.load(arbreJajaCode);
        interpreteur.run();
    }

    /**
     * Fonction utiliser par l'interface pour interpreter du jajacode
     * @param jajaCode String du jajacode a interpréter
     */
    public void run(String jajaCode){
        SymbolTable symbolTable = new SymbolTable();
        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbreJajaCode = parser.classe();

        JajaCodeInterpreterVisitor interpreteur = new JajaCodeInterpreterVisitor(symbolTable);

        interpreteur.load(arbreJajaCode);
        interpreteur.run();
    }
}


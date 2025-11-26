package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JajaCodeInterpreterUnitTest {

    private Stacks runJajaCode(String code) {
        Stacks stacks = new Stacks();
        CharStream stream = CharStreams.fromString(code);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext tree = parser.classe();

        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(tree);
        interpreter.run();
        return stacks;
    }

    @Test
    void testProgrammePrincipal_Affectation() {
        String code = """
                    1 init
                    2 push(0)
                    3 new(x, int, var, 0)
                    4 push(5)
                    5 store(x)
                    6 jcstop
                    """;

        Stacks finalStacks = runJajaCode(code);

        // Vérifier que la variable 'x' existe dans la pile
        Object valueX = finalStacks.getValue("x");
        assertNotNull(valueX, "La variable 'x' devrait avoir une valeur.");
        assertEquals(5, valueX, "La variable 'x' devrait avoir la valeur 5.");

        // Vérifier que le type est correct
        Type typeX = finalStacks.getDataType("x");
        assertEquals(Type.ENTIER, typeX, "La variable 'x' devrait être de type int.");

        // Vérifier que c'est bien une variable
        String objectTypeX = finalStacks.getObjectType("x");
        assertEquals("var", objectTypeX, "L'identifiant 'x' devrait être une variable.");
    }
}

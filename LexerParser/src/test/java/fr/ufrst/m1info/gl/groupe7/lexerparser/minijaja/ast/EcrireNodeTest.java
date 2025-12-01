package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;


public class EcrireNodeTest {

    @Test
    public void toStringTree_withString_returnsStringWrapped() {
        EcrireNode node = new EcrireNode("HelloWorld");

        String tree = node.toStringTree();

        assertEquals("ecrire (HelloWorld)", tree);
    }

   
    @Test
    public void interpret_withString_printsString() {
        EcrireNode node = new EcrireNode("abc");
        Stacks stacks = mock(Stacks.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            node.interpret(stacks);
        } finally {
            System.setOut(original);
        }

        assertEquals("abc", out.toString());
    }
}

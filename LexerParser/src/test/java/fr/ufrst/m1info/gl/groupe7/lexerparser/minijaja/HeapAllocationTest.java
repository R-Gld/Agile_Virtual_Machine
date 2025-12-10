package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Walker;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeapAllocationTest {

    @Test
    void heapAllocation_withSmallSize_shouldWork() {
        // Heap size is 256, so we use smaller arrays that fit
        String code = """
                class tasVariable {
                    int taille = 128;
                    int t[taille/4];

                    void f(int x) {
                        int t1[taille/(4*x)];
                        t1[x] = x;
                    };

                    void g() {
                        int t1[taille/8];
                        t1[1] = 1;
                    };

                    void pause() {
                    };

                    main {
                        f(2);
                        f(2);
                        pause();
                        g();
                        pause();
                    }
                }
                """;

        try {
            CharStream charStream = CharStreams.fromString(code);
            MiniJajaLexer lexer = new MiniJajaLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MiniJajaParser parser = new MiniJajaParser(tokens);

            MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
            ClasseNode classe = (ClasseNode) visitor.visit(parser.classe());

            Stacks stacks = new Stacks();
            Walker walker = new Walker(classe, stacks);
            walker.walk();

            assertTrue(true, "Heap allocation with small sizes should work");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    void heapAllocation_exceedingHeapSize_shouldFail() {
        // This test documents the expected behavior when arrays exceed heap capacity
        String code = """
                class tasVariable {
                    int taille = 512;
                    int t[taille/2+1];

                    main {
                        t[0] = 1;
                    }
                }
                """;

        CharStream charStream = CharStreams.fromString(code);
        MiniJajaLexer lexer = new MiniJajaLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
        ClasseNode classe = (ClasseNode) visitor.visit(parser.classe());

        Stacks stacks = new Stacks();
        Walker walker = new Walker(classe, stacks);

        // This should throw because array size 257 requires 512 cells (buddy system)
        // but heap only has 256 cells
        RuntimeException exception = assertThrows(RuntimeException.class, walker::walk);
        assertTrue(exception.getMessage().contains("Heap allocation failed"),
                "Should fail with heap allocation error");
    }

    @Test
    void heapAllocation_originalProgram_adapted_shouldWork() {
        // Adapted version of the original program that fits in 256-cell heap
        String code = """
                class tasVariable {
                    int taille = 64;
                    int t[taille/2];

                    void f(int x) {
                        int t1[taille/(2*x)-1];
                        t1[x] = x;
                    };

                    void g() {
                        int t1[taille/4];
                        t1[1] = 1;
                    };

                    void pause() {
                    };

                    main {
                        f(2);
                        f(2);
                        pause();
                        f(1);
                        pause();
                        g();
                        pause();
                    }
                }
                """;

        try {
            CharStream charStream = CharStreams.fromString(code);
            MiniJajaLexer lexer = new MiniJajaLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MiniJajaParser parser = new MiniJajaParser(tokens);

            MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
            ClasseNode classe = (ClasseNode) visitor.visit(parser.classe());

            Stacks stacks = new Stacks();
            Walker walker = new Walker(classe, stacks);
            walker.walk();

            assertTrue(true, "Adapted program should work");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not throw exception: " + e.getMessage());
        }
    }
}

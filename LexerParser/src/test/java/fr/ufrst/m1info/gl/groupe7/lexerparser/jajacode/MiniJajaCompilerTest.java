package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions.SyntaxException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MiniJajaCompilerTest {

    private static final String VALID_PROGRAM = """
            class C {
            	int x = 0;
            	main {
            		int res;
            		while(5>x){
            		    writeln(x);
            		};
            	}
            }
        """;

    private static final String INVALID_PROGRAM = """
        class {
        """;

    // ---------------------------------------------------------------------
    // Tests sur getMiniJajaCompilerVisitor(CharStream)
    // ---------------------------------------------------------------------

    @Test
    void getMiniJajaCompilerVisitor_validProgram_returnsVisitor() {
        CharStream cs = CharStreams.fromString(VALID_PROGRAM);

        MiniJajaCompilerVisitor compilerVisitor =
                MiniJajaCompiler.getMiniJajaCompilerVisitor(cs);

        assertNotNull(compilerVisitor,
                "Le visiteur ne doit pas être null pour un programme valide");
    }

    @Test
    void getMiniJajaCompilerVisitor_invalidProgram_throwsSyntaxException() {
        CharStream cs = CharStreams.fromString(INVALID_PROGRAM);

        assertThrows(SyntaxException.class,
                () -> MiniJajaCompiler.getMiniJajaCompilerVisitor(cs),
                "Une SyntaxException est attendue pour un programme invalide");
    }

    @Test
    void getMiniJajaCompilerVisitorFromString_validProgram_returnsVisitor() {
        MiniJajaCompilerVisitor compilerVisitor =
                MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(VALID_PROGRAM);

        assertNotNull(compilerVisitor,
                "Le visiteur ne doit pas être null pour un programme valide (fromString)");
    }

    @Test
    void getMiniJajaCompilerVisitorFromString_invalidProgram_throwsSyntaxException() {
        assertThrows(SyntaxException.class,
                () -> MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(INVALID_PROGRAM),
                "Une SyntaxException est attendue pour un programme invalide (fromString)");
    }

    // ---------------------------------------------------------------------
    // Tests sur getMiniJajaCompilerVisitorFromStream(InputStream)
    // ---------------------------------------------------------------------

    @Test
    void getMiniJajaCompilerVisitorFromStream_validProgram_returnsVisitor() throws IOException {
        InputStream in = new java.io.ByteArrayInputStream(
                VALID_PROGRAM.getBytes(StandardCharsets.UTF_8)
        );

        MiniJajaCompilerVisitor compilerVisitor =
                MiniJajaCompiler.getMiniJajaCompilerVisitorFromStream(in);

        assertNotNull(compilerVisitor,
                "Le visiteur ne doit pas être null pour un programme valide (fromStream)");
    }

    @Test
    void getMiniJajaCompilerVisitorFromStream_invalidProgram_throwsSyntaxException() {
        InputStream in = new java.io.ByteArrayInputStream(
                INVALID_PROGRAM.getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(SyntaxException.class,
                () -> MiniJajaCompiler.getMiniJajaCompilerVisitorFromStream(in),
                "Une SyntaxException est attendue pour un programme invalide (fromStream)");
    }

    // ---------------------------------------------------------------------
    // Test avec Mockito : InputStream qui lève une IOException
    // -> on vérifie que l'IOException est bien propagée.
    // ---------------------------------------------------------------------

    @Test
    void getMiniJajaCompilerVisitorFromStream_ioError_throwsIOException() throws IOException {
        // Mock d'un InputStream qui lève une IOException dès qu'on essaie de lire
        InputStream in = Mockito.mock(InputStream.class);

        // CharStreams.fromStream(InputStream) appelle typiquement read(byte[], int, int)
        when(in.read(any(byte[].class), anyInt(), anyInt()))
                .thenThrow(new IOException("Erreur de lecture simulée"));

        assertThrows(IOException.class,
                () -> MiniJajaCompiler.getMiniJajaCompilerVisitorFromStream(in),
                "Une IOException doit être propagée si la lecture du stream échoue");

        // Vérifie qu'on a bien tenté de lire sur le stream
        verify(in, atLeastOnce()).read(any(byte[].class), anyInt(), anyInt());
    }
}
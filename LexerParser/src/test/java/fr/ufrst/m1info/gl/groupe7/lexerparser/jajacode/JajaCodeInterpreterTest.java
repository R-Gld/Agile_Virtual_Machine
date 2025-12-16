package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions.SyntaxException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour le wrapper JajaCodeInterpreter.
 * Objectif : Passer de 0% à 90%+ de couverture
 */
@DisplayName("JajaCodeInterpreter Tests")
class JajaCodeInterpreterTest {

    @Nested
    @DisplayName("Valid JajaCode Programs")
    class ValidProgramsTests {

        @Test
        @DisplayName("Should execute simple variable declaration and initialization")
        void testSimpleVariableDeclaration() {
            String jajaCode = """
                    1 init
                    2 push(5)
                    3 new(x@global, int, var, 0)
                    4 push(0)
                    5 swap
                    6 pop
                    7 pop
                    8 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertDoesNotThrow(interpreter::run);
            assertFalse(collector.hasErrors());
        }

        @Test
        @DisplayName("Should execute arithmetic operations")
        void testArithmeticOperations() {
            String jajaCode = """
                    1 init
                    2 push(10)
                    3 push(5)
                    4 add
                    5 push(0)
                    6 swap
                    7 pop
                    8 pop
                    9 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertDoesNotThrow(interpreter::run);
            assertFalse(collector.hasErrors());
        }

        @Test
        @DisplayName("Should execute program with goto and if instructions")
        void testControlFlow() {
            String jajaCode = """
                    1 init
                    2 push(true)
                    3 if(5)
                    4 goto(6)
                    5 push(1)
                    6 push(0)
                    7 swap
                    8 pop
                    9 pop
                    10 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertDoesNotThrow(interpreter::run);
            assertFalse(collector.hasErrors());
        }

        @Test
        @DisplayName("Should execute program with write operations")
        void testWriteOperations() {
            String jajaCode = """
                    1 init
                    2 push(42)
                    3 write
                    4 push(100)
                    5 writeln
                    6 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outputStream));

            try {
                interpreter.run();
                String output = outputStream.toString();
                assertTrue(output.contains("42"));
                assertTrue(output.contains("100"));
            } finally {
                System.setOut(originalOut);
            }

            assertFalse(collector.hasErrors());
        }

        @Test
        @DisplayName("Should execute program with array operations")
        void testArrayOperations() {
            String jajaCode = """
                    1 init
                    2 push(5)
                    3 newarray(arr@global, int)
                    4 push(0)
                    5 push(42)
                    6 astore(arr@global)
                    7 push(0)
                    8 aload(arr@global)
                    9 push(0)
                    10 swap
                    11 pop
                    12 pop
                    13 swap
                    14 pop
                    15 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertDoesNotThrow(interpreter::run);
            assertFalse(collector.hasErrors());
        }
    }

    @Nested
    @DisplayName("Invalid JajaCode Programs")
    class InvalidProgramsTests {

        @Test
        @DisplayName("Should throw SyntaxException for invalid syntax")
        void testInvalidSyntax() {
            String jajaCode = """
                    1 invalid_instruction
                    2 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertThrows(SyntaxException.class, interpreter::run);
            assertTrue(collector.hasErrors());
        }

        @Test
        @DisplayName("Should throw SyntaxException for missing address")
        void testMissingAddress() {
            String jajaCode = """
                    init
                    jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertThrows(SyntaxException.class, interpreter::run);
            assertTrue(collector.hasErrors());
        }

        @Test
        @DisplayName("Should throw SyntaxException for malformed instruction")
        void testMalformedInstruction() {
            String jajaCode = """
                    1 init
                    2 push(
                    3 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertThrows(SyntaxException.class, interpreter::run);
            assertTrue(collector.hasErrors());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create interpreter with valid inputs")
        void testValidConstruction() {
            String jajaCode = "1 init\n2 jcstop";
            DiagnosticCollector collector = new DiagnosticCollector();

            assertDoesNotThrow(() -> new JajaCodeInterpreter(jajaCode, collector));
        }

        @Test
        @DisplayName("Should accept empty diagnostic collector")
        void testEmptyCollector() {
            String jajaCode = "1 init\n2 jcstop";
            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertDoesNotThrow(interpreter::run);
            assertFalse(collector.hasErrors());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle minimal valid program")
        void testMinimalProgram() {
            String jajaCode = """
                    1 init
                    2 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertDoesNotThrow(interpreter::run);
            assertFalse(collector.hasErrors());
        }

        @Test
        @DisplayName("Should handle program with comments (if supported)")
        void testProgramWithWhitespace() {
            String jajaCode = """
                    1 init

                    2 push(1)
                    3 push(0)
                    4 swap
                    5 pop
                    6 pop
                    7 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertDoesNotThrow(interpreter::run);
            assertFalse(collector.hasErrors());
        }

        @Test
        @DisplayName("Should handle program with multiple operations per type")
        void testMultipleOperations() {
            String jajaCode = """
                    1 init
                    2 push(10)
                    3 push(5)
                    4 add
                    5 push(2)
                    6 mul
                    7 push(3)
                    8 sub
                    9 push(0)
                    10 swap
                    11 pop
                    12 pop
                    13 jcstop
                    """;

            DiagnosticCollector collector = new DiagnosticCollector();
            JajaCodeInterpreter interpreter = new JajaCodeInterpreter(jajaCode, collector);

            assertDoesNotThrow(interpreter::run);
            assertFalse(collector.hasErrors());
        }
    }
}

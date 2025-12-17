package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions.SemanticException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions.SyntaxException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompiler;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify that the correct exception types are thrown
 * for syntax errors vs semantic errors.
 */
@DisplayName("Exception Type Tests")
class ExceptionTypeTest {

    @Test
    @DisplayName("Syntax error should throw SyntaxException")
    void syntaxError_shouldThrowSyntaxException() {
        String invalidCode = """
            class C {
                main {
                    int x = 5
                }
            }
        """; // Missing semicolon

        DiagnosticCollector collector = new DiagnosticCollector();
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(invalidCode, collector, "test.mjj");

        assertThrows(SyntaxException.class, interpreter::run,
                "Syntax errors should throw SyntaxException");
    }

    @Test
    @DisplayName("Semantic error should throw SemanticException")
    void semanticError_shouldThrowSemanticException() {
        String codeWithSemanticError = """
            class C {
                main {
                    int x = 5;
                    int x = 10;
                }
            }
        """; // Duplicate variable declaration

        assertThrows(SemanticException.class,
                () -> MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(codeWithSemanticError),
                "Semantic errors should throw SemanticException");
    }

    @Test
    @DisplayName("Type mismatch should throw SemanticException")
    void typeMismatch_shouldThrowSemanticException() {
        String codeWithTypeMismatch = """
            class C {
                main {
                    int x;
                    boolean y;
                    x = true;
                }
            }
        """; // Type mismatch: assigning boolean to int

        assertThrows(SemanticException.class,
                () -> MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(codeWithTypeMismatch),
                "Type mismatch errors should throw SemanticException");
    }

    @Test
    @DisplayName("Undefined variable should throw SemanticException")
    void undefinedVariable_shouldThrowSemanticException() {
        String codeWithUndefinedVar = """
            class C {
                main {
                    int x;
                    y = 5;
                }
            }
        """; // 'y' is not declared

        assertThrows(SemanticException.class,
                () -> MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(codeWithUndefinedVar),
                "Undefined variable errors should throw SemanticException");
    }

    @Test
    @DisplayName("Valid code should not throw any exception")
    void validCode_shouldNotThrowException() {
        String validCode = """
            class C {
                main {
                    int x;
                    x = 5;
                    writeln(x);
                }
            }
        """;

        assertDoesNotThrow(
                () -> MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(validCode),
                "Valid code should not throw any exception");
    }
}

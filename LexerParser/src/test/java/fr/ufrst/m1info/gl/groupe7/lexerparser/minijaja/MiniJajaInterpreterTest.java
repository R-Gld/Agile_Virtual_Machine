package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class MiniJajaInterpreterTest {

    private DiagnosticCollector collector;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        collector = new DiagnosticCollector();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
    }

    private void captureOutput() {
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(new ByteArrayOutputStream())); // Suppress debug output
    }

    private void restoreOutput() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // ========================================
    // Constructor Tests
    // ========================================

    @Test
    void testConstructor_WithValidInput() {
        String input = "class C { main {} }";
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        assertNotNull(interpreter);
    }

    @Test
    void testConstructor_WithEmptyInput() {
        String input = "";
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        assertNotNull(interpreter);
    }

    @Test
    void testConstructor_WithNullCollector() {
        String input = "class C { main {} }";
        
        assertThrows(NullPointerException.class, () -> {
            MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, null);
            interpreter.run();
        });
    }

    // ========================================
    // Simple Valid Program Tests
    // ========================================

    @Test
    void testRun_SimpleValidProgram() {
        String input = "class C { main {} }";
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_ProgramWithVariableDeclaration() {
        String input = """
            class C {
                int x = 10;
                main {}
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_ProgramWithMultipleVariables() {
        String input = """
            class TestClass {
                int a = 1;
                int b = 2;
                int c = 3;
                main {}
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_ProgramWithConstant() {
        String input = """
            class C {
                final int PI = 314;
                main {}
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_ProgramWithArray() {
        String input = """
            class C {
                int arr[5];
                main {}
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }



    // ========================================
    // Main Method with Instructions Tests
    // ========================================

    @Test
    void testRun_MainWithVariableAssignment() {
        String input = """
            class C {
                int x = 0;
                main {
                    x = 42;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_MainWithLocalVariable() {
        String input = """
            class C {
                main {
                    int y = 5;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_MainWithIncrement() {
        String input = """
            class C {
                int counter = 0;
                main {
                    counter++;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_MainWithArrayAccess() {
        String input = """
            class C {
                int nums[3];
                int x = 0;
                main {
                    nums[0] = 10;
                    x = nums[0];
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    // ========================================
    // Expression Tests
    // ========================================

    @Test
    void testRun_ArithmeticExpressions() {
        String input = """
            class C {
                int result = 0;
                main {
                    result = 5 + 3 * 2;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_BooleanExpressions() {
        String input = """
            class C {
                boolean flag = true;
                main {
                    flag = true && false;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_ComparisonExpressions() {
        String input = """
            class C {
                boolean result = false;
                main {
                    result = 5 > 3;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    // ========================================
    // Syntax Error Tests
    // ========================================

    @Test
    void testRun_SyntaxError_ThrowsSyntaxException() {
        String input = "class C { int x = ; }"; // Missing value
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertThrows(SyntaxException.class, () -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertTrue(collector.hasErrors());
    }

    @Test
    void testRun_MissingMainMethod_ThrowsException() {
        String input = "class C { int x = 0; }"; // No main method
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertThrows(SyntaxException.class, () -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertTrue(collector.hasErrors());
    }

    @Test
    void testRun_InvalidClassName_ThrowsException() {
        String input = "class { main {} }"; // Missing class name
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertThrows(SyntaxException.class, () -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertTrue(collector.hasErrors());
    }

    @Test
    void testRun_UnclosedBrace_ThrowsException() {
        String input = "class C { main { "; // Missing closing braces
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertThrows(SyntaxException.class, () -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertTrue(collector.hasErrors());
    }

    // ========================================
    // AST Output Tests
    // ========================================

    @Test
    void testRun_PrintsASTOutput() {
        String input = """
            class C {
                int x = 5;
                main {
                    x = x + 1;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            interpreter.run();
        } finally {
            restoreOutput();
        }
        
        String output = outputStream.toString();
        assertTrue(output.contains("ARBRE SYNTAXIQUE ABSTRAIT"));
        assertTrue(output.contains("INTERPRETATION"));
    }

    @Test
    void testRun_PrintsClasseNode() {
        String input = "class TestClass { main {} }";
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            interpreter.run();
        } finally {
            restoreOutput();
        }
        
        String output = outputStream.toString();
        assertTrue(output.contains("Classe"));
        assertTrue(output.contains("TestClass"));
    }

    // ========================================
    // Complex Program Tests
    // ========================================


    @Test
    void testRun_NestedExpressions() {
        String input = """
            class C {
                int result = 0;
                main {
                    result = ((5 + 3) * 2) - (10 / 2);
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    void testRun_EmptyClass() {
        String input = "class Empty { main {} }";
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_OnlyMainMethod() {
        String input = """
            class C {
                main {
                    int temp = 99;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_MultipleDeclarations() {
        String input = """
            class C {
                int a = 1;
                int b = 2;
                final int C1 = 3;
                final int C2 = 4;
                int arr1[2];
                int arr2[3];
        
                main {
                    a = b + C1;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_BooleanVariablesAndOperations() {
        String input = """
            class C {
                boolean a = true;
                boolean b = false;
                boolean c = false;
                main {
                    c = a && b;
                    c = a || b;
                    c = !a;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            assertDoesNotThrow(() -> interpreter.run());
        } finally {
            restoreOutput();
        }
        
        assertFalse(collector.hasErrors());
    }

    @Test
    void testRun_VerifyWalkerExecution() {
        String input = """
            class C {
                int x = 0;
                main {
                    x = 10;
                }
            }
            """;
        MiniJajaInterpreter interpreter = new MiniJajaInterpreter(input, collector);
        
        captureOutput();
        try {
            interpreter.run();
        } finally {
            restoreOutput();
        }
        
        String output = outputStream.toString();
        assertTrue(output.contains("INTERPRETATION"));
        assertFalse(collector.hasErrors());
    }


}

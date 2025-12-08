package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Walker;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour les erreurs d'interprétation.
 * Ces tests vérifient que l'interpréteur détecte correctement les erreurs
 * et lance les exceptions appropriées.
 */
public class InterpreterErrorTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    /**
     * Helper method to parse and interpret a program.
     */
    private void runProgram(String program) {
        CharStream cs = CharStreams.fromString(program);
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);
        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
        ParseTree tree = parser.classe();
        AstNode astRoot = visitor.visit(tree);
        assertNotNull(astRoot, "AST should not be null");
        Walker walker = new Walker(astRoot, stacks, new Debug());
        walker.walk();
    }

    // ==================== DIVISION PAR ZERO ====================
    @Nested
    @DisplayName("Division par zéro")
    class DivisionByZeroTests {

        @Test
        @DisplayName("Division directe par zéro: x = 10 / 0")
        void testDivisionByZeroLiteral() {
            String program = """
                class C {
                    main {
                        int x;
                        x = 10 / 0;
                    }
                }
                """;
            assertThrows(ArithmeticException.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Division par variable valant zéro")
        void testDivisionByZeroVariable() {
            String program = """
                class C {
                    main {
                        int x;
                        int y;
                        y = 0;
                        x = 10 / y;
                    }
                }
                """;
            assertThrows(ArithmeticException.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Division dans une expression complexe")
        void testDivisionByZeroInExpression() {
            String program = """
                class C {
                    main {
                        int a;
                        int b;
                        int c;
                        a = 5;
                        b = 5;
                        c = 100 / (a + (0 - b));
                    }
                }
                """;
            assertThrows(ArithmeticException.class, () -> runProgram(program));
        }
    }

    // ==================== ACCES TABLEAU HORS BORNES ====================
    @Nested
    @DisplayName("Accès tableau hors bornes")
    class ArrayIndexOutOfBoundsTests {

        @Test
        @DisplayName("Accès index négatif")
        void testArrayNegativeIndex() {
            String program = """
                class C {
                    int arr[5];
                    main {
                        int x;
                        x = arr[0 - 1];
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Accès index égal à la taille")
        void testArrayIndexEqualsSize() {
            String program = """
                class C {
                    int arr[5];
                    main {
                        int x;
                        x = arr[5];
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Accès index supérieur à la taille")
        void testArrayIndexGreaterThanSize() {
            String program = """
                class C {
                    int arr[3];
                    main {
                        int x;
                        x = arr[100];
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Écriture hors bornes")
        void testArrayWriteOutOfBounds() {
            String program = """
                class C {
                    int arr[2];
                    main {
                        arr[10] = 42;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }
    }

    // ==================== VARIABLE NON INITIALISEE ====================
    @Nested
    @DisplayName("Variable non initialisée")
    class UninitializedVariableTests {

        @Test
        @DisplayName("Lecture de variable non initialisée")
        void testReadUninitializedVariable() {
            String program = """
                class C {
                    main {
                        int x;
                        int y;
                        y = x + 1;
                    }
                }
                """;
            // Selon l'implémentation, peut être 0 par défaut ou erreur
            // Ce test vérifie le comportement actuel
            assertDoesNotThrow(() -> runProgram(program));
        }
    }

    // ==================== ERREUR DE TYPE ====================
    @Nested
    @DisplayName("Erreurs de type")
    class TypeErrorTests {

        @Test
        @DisplayName("Multiplication de boolean")
        void testMultiplyBoolean() {
            String program = """
                class C {
                    main {
                        boolean b;
                        int x;
                        b = true;
                        x = b * 2;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Addition de boolean")
        void testAddBoolean() {
            String program = """
                class C {
                    main {
                        boolean b;
                        int x;
                        b = true;
                        x = b + 1;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Condition while avec int")
        void testWhileWithInt() {
            String program = """
                class C {
                    main {
                        int x;
                        x = 1;
                        while (x) {
                            x = 0;
                        };
                    }
                }
                """;
            assertThrows(ClassCastException.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Condition if avec int")
        void testIfWithInt() {
            String program = """
                class C {
                    main {
                        int x;
                        x = 1;
                        if (x) {
                            x = 0;
                        }
                    }
                }
                """;
            assertThrows(ClassCastException.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Negation logique sur int")
        void testNotOnInt() {
            String program = """
                class C {
                    main {
                        int x;
                        boolean b;
                        x = 5;
                        b = !x;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("AND logique avec int")
        void testAndWithInt() {
            String program = """
                class C {
                    main {
                        int x;
                        boolean b;
                        x = 5;
                        b = x && true;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("OR logique avec int")
        void testOrWithInt() {
            String program = """
                class C {
                    main {
                        int x;
                        boolean b;
                        x = 5;
                        b = x || false;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }
    }

    // ==================== APPEL METHODE AVEC MAUVAIS ARGUMENTS ====================
    @Nested
    @DisplayName("Appel de méthode avec mauvais arguments")
    class MethodCallErrorTests {

        @Test
        @DisplayName("Trop d'arguments")
        void testTooManyArguments() {
            String program = """
                class C {
                    int foo(int x) {
                        return x;
                    };
                    main {
                        int r;
                        r = foo(1, 2, 3);
                    }
                }
                """;
            assertThrows(RuntimeException.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Pas assez d'arguments")
        void testTooFewArguments() {
            String program = """
                class C {
                    int foo(int x, int y, int z) {
                        return x + y + z;
                    };
                    main {
                        int r;
                        r = foo(1);
                    }
                }
                """;
            assertThrows(RuntimeException.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Appel de méthode inexistante")
        void testCallUndefinedMethod() {
            String program = """
                class C {
                    main {
                        int r;
                        r = undefined(1, 2);
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }
    }

    // ==================== CONSTANTE ====================
    @Nested
    @DisplayName("Erreurs avec constantes")
    class ConstantErrorTests {

        @Test
        @DisplayName("Modification d'une constante")
        void testModifyConstant() {
            String program = """
                class C {
                    main {
                        final int CST = 10;
                        CST = 20;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }
    }

    // ==================== LENGTH SUR NON-TABLEAU ====================
    @Nested
    @DisplayName("Erreurs avec length")
    class LengthErrorTests {

        @Test
        @DisplayName("length sur un int")
        void testLengthOnInt() {
            String program = """
                class C {
                    main {
                        int x;
                        int l;
                        x = 5;
                        l = length(x);
                    }
                }
                """;
            assertThrows(RuntimeException.class, () -> runProgram(program));
        }
    }

    // ==================== WRITELN SUR TABLEAU ====================
    @Nested
    @DisplayName("Erreurs avec writeln")
    class WritelnErrorTests {

        @Test
        @DisplayName("writeln sur un tableau")
        void testWritelnOnArray() {
            String program = """
                class C {
                    int arr[5];
                    main {
                        writeln(arr);
                    }
                }
                """;
            assertThrows(RuntimeException.class, () -> runProgram(program));
        }
    }

    // ==================== RECURSION INFINIE (timeout) ====================
    @Nested
    @DisplayName("Tests de récursion")
    class RecursionTests {

        @Test
        @DisplayName("Récursion profonde (mais finie)")
        void testDeepRecursion() {
            String program = """
                class C {
                    int countdown(int n) {
                        int r;
                        if (n == 0) {
                            r = 0;
                        } else {
                            r = countdown(n + (0 - 1));
                        }
                        return r;
                    };
                    main {
                        int x;
                        x = countdown(100);
                        writeln(x);
                    }
                }
                """;
            assertDoesNotThrow(() -> runProgram(program));
        }
    }

    // ==================== CAS LIMITES ====================
    @Nested
    @DisplayName("Cas limites")
    class EdgeCaseTests {

        @Test
        @DisplayName("Tableau de taille 0")
        void testZeroSizeArray() {
            String program = """
                class C {
                    int arr[0];
                    main {
                        int x;
                        x = arr[0];
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Expression avec beaucoup de parenthèses")
        void testDeepParentheses() {
            String program = """
                class C {
                    main {
                        int x;
                        x = ((((((((((1 + 2))))))))));
                        writeln(x);
                    }
                }
                """;
            assertDoesNotThrow(() -> runProgram(program));
        }

        @Test
        @DisplayName("Chaîne d'affectations")
        void testChainedAssignments() {
            String program = """
                class C {
                    main {
                        int a;
                        int b;
                        int c;
                        int d;
                        a = 1;
                        b = a;
                        c = b;
                        d = c;
                        writeln(d);
                    }
                }
                """;
            assertDoesNotThrow(() -> runProgram(program));
        }

        @Test
        @DisplayName("Overflow integer")
        void testIntegerOverflow() {
            String program = """
                class C {
                    main {
                        int x;
                        x = 2147483647 + 1;
                        writeln(x);
                    }
                }
                """;
            // En Java, overflow silencieux
            assertDoesNotThrow(() -> runProgram(program));
        }

        @Test
        @DisplayName("Underflow integer")
        void testIntegerUnderflow() {
            String program = """
                class C {
                    main {
                        int x;
                        int y;
                        y = 2147483647;
                        x = 0 - y - y - 3;
                        writeln(x);
                    }
                }
                """;
            assertDoesNotThrow(() -> runProgram(program));
        }
    }

    // ==================== VARIABLE NON DECLAREE ====================
    @Nested
    @DisplayName("Variable non déclarée")
    class UndeclaredVariableTests {

        @Test
        @DisplayName("Utilisation de variable non déclarée")
        void testUseUndeclaredVariable() {
            String program = """
                class C {
                    main {
                        int x;
                        x = y + 1;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }

        @Test
        @DisplayName("Affectation à variable non déclarée")
        void testAssignToUndeclaredVariable() {
            String program = """
                class C {
                    main {
                        z = 10;
                    }
                }
                """;
            assertThrows(Exception.class, () -> runProgram(program));
        }
    }

    // ==================== TABLEAU MULTIDIMENSIONNEL ====================
    
}

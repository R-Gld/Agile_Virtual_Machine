package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests exhaustifs pour toutes les exceptions JajaCode.
 * Objectif : Passer de 47% à 90%+ de couverture pour le package jajacode/exceptions
 */
@DisplayName("JajaCode Exceptions Tests")
class JajaCodeExceptionsTest {

    @Nested
    @DisplayName("JajaCodeRuntimeException Tests")
    class JajaCodeRuntimeExceptionTests {

        @Test
        @DisplayName("Should create exception with message, axiom and PC")
        void testConstructorWithAllParameters() {
            JajaCodeRuntimeException ex = new JajaCodeRuntimeException(
                "Test error message",
                "PUSH",
                42
            );

            assertTrue(ex.getMessage().contains("Test error message"));
            assertTrue(ex.getMessage().contains("PC=42"));
            assertTrue(ex.getMessage().contains("Axiome=PUSH"));
            assertEquals("PUSH", ex.getAxiomeName());
            assertEquals(42, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should create exception with null axiom name")
        void testConstructorWithNullAxiom() {
            JajaCodeRuntimeException ex = new JajaCodeRuntimeException(
                "Error",
                null,
                10
            );

            assertTrue(ex.getMessage().contains("Error"));
            assertTrue(ex.getMessage().contains("PC=10"));
            assertNull(ex.getAxiomeName());
            assertEquals(10, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should create exception with negative PC")
        void testConstructorWithNegativePC() {
            JajaCodeRuntimeException ex = new JajaCodeRuntimeException(
                "Error",
                "TEST",
                -1
            );

            assertEquals(-1, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should be instanceof RuntimeException")
        void testInheritance() {
            JajaCodeRuntimeException ex = new JajaCodeRuntimeException("Error", "TEST", 1);
            assertInstanceOf(RuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("AssignmentException Tests")
    class AssignmentExceptionTests {

        @Test
        @DisplayName("Should create exception with identifier and reason")
        void testConstructorWithIdentifier() {
            AssignmentException ex = new AssignmentException(
                "x@global",
                "Cannot assign to constant",
                "STORE",
                15
            );

            assertTrue(ex.getMessage().contains("x@global"));
            assertTrue(ex.getMessage().contains("Cannot assign to constant"));
            assertEquals("x@global", ex.getIdentifier());
            assertEquals("STORE", ex.getAxiomeName());
            assertEquals(15, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should format message with identifier and reason")
        void testMessageFormatting() {
            AssignmentException ex = new AssignmentException(
                "myVar",
                "Variable is read-only",
                "STORE",
                20
            );

            String message = ex.getMessage();
            assertTrue(message.contains("Échec de l'affectation"));
            assertTrue(message.contains("myVar"));
            assertTrue(message.contains("Variable is read-only"));
        }

        @Test
        @DisplayName("Should be instanceof JajaCodeRuntimeException")
        void testInheritance() {
            AssignmentException ex = new AssignmentException("x", "test", "STORE", 1);
            assertInstanceOf(JajaCodeRuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("TypeMismatchException Tests")
    class TypeMismatchExceptionTests {

        @Test
        @DisplayName("Should create exception with expected and actual types")
        void testConstructorWithTypes() {
            TypeMismatchException ex = new TypeMismatchException(
                "ENTIER",
                "BOOLEEN",
                "ADD",
                25
            );

            assertTrue(ex.getMessage().contains("ENTIER"));
            assertTrue(ex.getMessage().contains("BOOLEEN"));
            assertEquals("ENTIER", ex.getExpectedType());
            assertEquals("BOOLEEN", ex.getActualType());
            assertEquals("ADD", ex.getAxiomeName());
            assertEquals(25, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should create exception with custom message only")
        void testConstructorWithMessageOnly() {
            TypeMismatchException ex = new TypeMismatchException(
                "Custom type error message",
                "MUL",
                30
            );

            assertTrue(ex.getMessage().contains("Custom type error message"));
            assertTrue(ex.getMessage().contains("PC=30"));
            assertTrue(ex.getMessage().contains("Axiome=MUL"));
            assertNull(ex.getExpectedType());
            assertNull(ex.getActualType());
            assertEquals("MUL", ex.getAxiomeName());
            assertEquals(30, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should format message with types")
        void testMessageFormatting() {
            TypeMismatchException ex = new TypeMismatchException(
                "int",
                "boolean",
                "DIV",
                12
            );

            String message = ex.getMessage();
            assertTrue(message.contains("Incompatibilité de types"));
            assertTrue(message.contains("attendu=int"));
            assertTrue(message.contains("reçu=boolean"));
        }

        @Test
        @DisplayName("Should be instanceof JajaCodeRuntimeException")
        void testInheritance() {
            TypeMismatchException ex = new TypeMismatchException("int", "bool", "TEST", 1);
            assertInstanceOf(JajaCodeRuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("StackUnderflowException Tests")
    class StackUnderflowExceptionTests {

        @Test
        @DisplayName("Should create exception with default message")
        void testConstructorWithDefaultMessage() {
            StackUnderflowException ex = new StackUnderflowException("POP", 8);

            assertTrue(ex.getMessage().contains("Pile vide"));
            assertEquals("POP", ex.getAxiomeName());
            assertEquals(8, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should create exception with custom message")
        void testConstructorWithCustomMessage() {
            StackUnderflowException ex = new StackUnderflowException(
                "Attempted to pop from empty stack",
                "ADD",
                12
            );

            assertTrue(ex.getMessage().contains("Attempted to pop from empty stack"));
            assertTrue(ex.getMessage().contains("PC=12"));
            assertTrue(ex.getMessage().contains("Axiome=ADD"));
            assertEquals("ADD", ex.getAxiomeName());
            assertEquals(12, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should be instanceof JajaCodeRuntimeException")
        void testInheritance() {
            StackUnderflowException ex = new StackUnderflowException("POP", 1);
            assertInstanceOf(JajaCodeRuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("UndefinedSymbolException Tests")
    class UndefinedSymbolExceptionTests {

        @Test
        @DisplayName("Should create exception with symbol name")
        void testConstructorWithSymbol() {
            UndefinedSymbolException ex = new UndefinedSymbolException(
                "unknownVar",
                "LOAD",
                18
            );

            assertTrue(ex.getMessage().contains("unknownVar"));
            assertEquals("unknownVar", ex.getSymbolName());
            assertEquals("LOAD", ex.getAxiomeName());
            assertEquals(18, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should format message with symbol name")
        void testMessageFormatting() {
            UndefinedSymbolException ex = new UndefinedSymbolException(
                "myVariable",
                "STORE",
                22
            );

            String message = ex.getMessage();
            assertTrue(message.contains("Symbole introuvable"));
            assertTrue(message.contains("myVariable"));
        }

        @Test
        @DisplayName("Should be instanceof JajaCodeRuntimeException")
        void testInheritance() {
            UndefinedSymbolException ex = new UndefinedSymbolException("x", "LOAD", 1);
            assertInstanceOf(JajaCodeRuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("DivisionByZeroException Tests")
    class DivisionByZeroExceptionTests {

        @Test
        @DisplayName("Should create exception with default message")
        void testConstructor() {
            DivisionByZeroException ex = new DivisionByZeroException("DIV", 35);

            assertTrue(ex.getMessage().contains("Division par zéro"));
            assertEquals("DIV", ex.getAxiomeName());
            assertEquals(35, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should be instanceof JajaCodeRuntimeException")
        void testInheritance() {
            DivisionByZeroException ex = new DivisionByZeroException("DIV", 1);
            assertInstanceOf(JajaCodeRuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("InvalidAddressException Tests")
    class InvalidAddressExceptionTests {

        @Test
        @DisplayName("Should create exception with address")
        void testConstructorWithAddress() {
            InvalidAddressException ex = new InvalidAddressException(
                "999",
                "GOTO",
                40
            );

            assertTrue(ex.getMessage().contains("999"));
            assertEquals("999", ex.getAddress());
            assertEquals("GOTO", ex.getAxiomeName());
            assertEquals(40, ex.getProgramCounter());
        }

        @Test
        @DisplayName("Should format message with address")
        void testMessageFormatting() {
            InvalidAddressException ex = new InvalidAddressException(
                "-5",
                "IF",
                50
            );

            String message = ex.getMessage();
            assertTrue(message.contains("Adresse invalide"));
            assertTrue(message.contains("-5"));
        }

        @Test
        @DisplayName("Should handle zero address")
        void testZeroAddress() {
            InvalidAddressException ex = new InvalidAddressException("0", "GOTO", 1);
            assertEquals("0", ex.getAddress());
            assertTrue(ex.getMessage().contains("0"));
        }

        @Test
        @DisplayName("Should be instanceof JajaCodeRuntimeException")
        void testInheritance() {
            InvalidAddressException ex = new InvalidAddressException("100", "GOTO", 1);
            assertInstanceOf(JajaCodeRuntimeException.class, ex);
        }
    }

    @Nested
    @DisplayName("Exception Hierarchy Tests")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("All custom exceptions should extend JajaCodeRuntimeException")
        void testAllExceptionsExtendBase() {
            assertInstanceOf(JajaCodeRuntimeException.class,
                new AssignmentException("x", "test", "STORE", 1));
            assertInstanceOf(JajaCodeRuntimeException.class,
                new TypeMismatchException("int", "bool", "ADD", 1));
            assertInstanceOf(JajaCodeRuntimeException.class,
                new StackUnderflowException("test", "POP", 1));
            assertInstanceOf(JajaCodeRuntimeException.class,
                new UndefinedSymbolException("x", "LOAD", 1));
            assertInstanceOf(JajaCodeRuntimeException.class,
                new DivisionByZeroException("DIV", 1));
            assertInstanceOf(JajaCodeRuntimeException.class,
                new InvalidAddressException("100", "GOTO", 1));
        }

        @Test
        @DisplayName("JajaCodeRuntimeException should extend RuntimeException")
        void testBaseExtendsRuntimeException() {
            JajaCodeRuntimeException ex = new JajaCodeRuntimeException("test", "TEST", 1);
            assertInstanceOf(RuntimeException.class, ex);
        }
    }
}

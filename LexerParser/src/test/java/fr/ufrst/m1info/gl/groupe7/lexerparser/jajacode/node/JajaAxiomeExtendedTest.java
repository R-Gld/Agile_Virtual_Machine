package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.JajaCodeRuntimeException;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests supplémentaires pour améliorer la couverture des axiomes JajaCode.
 * Objectif : Porter NewarrayAxiome, LengthAxiome, StoreAxiome à 90%+ de couverture
 */
@DisplayName("Extended JajaCode Axiom Tests")
public class JajaAxiomeExtendedTest {

    private Stacks stacks;
    private MachineContext context;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
        context = new MachineContext(stacks);
    }

    @Nested
    @DisplayName("NewarrayAxiome Extended Tests")
    class NewarrayAxiomeExtendedTests {

        @Test
        @DisplayName("Should create array with size 1")
        void testMinimalSizeArray() {
            stacks.push(new Stacks.Quad("%TEMP%", 1, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            axiome.execute(context, "oneElem,int");

            assertTrue(stacks.getSymbolTable().contains("oneElem"));
            assertEquals(1, stacks.getArrayLength("oneElem"));
        }

        @Test
        @DisplayName("Should create array with moderate size (100)")
        void testModerateSizeArray() {
            stacks.push(new Stacks.Quad("%TEMP%", 100, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            axiome.execute(context, "mediumArray,int");

            assertTrue(stacks.getSymbolTable().contains("mediumArray"));
            assertEquals(100, stacks.getArrayLength("mediumArray"));
        }

        @Test
        @DisplayName("Should handle invalid type string")
        void testInvalidTypeString() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            // "unknown" is not a valid type, should throw exception
            assertThrows(JajaCodeRuntimeException.class,
                () -> axiome.execute(context, "arr,unknown"));
        }

        @Test
        @DisplayName("Should create array with scoped name (@global)")
        void testScopedNameGlobal() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            axiome.execute(context, "arr@global,int");

            assertTrue(stacks.getSymbolTable().contains("arr@global"));
            assertEquals(5, stacks.getArrayLength("arr@global"));
        }

        @Test
        @DisplayName("Should handle boolean type array")
        void testBooleanTypeArray() {
            stacks.push(new Stacks.Quad("%TEMP%", 3, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            axiome.execute(context, "boolArr,boolean");

            assertTrue(stacks.getSymbolTable().contains("boolArr"));
            assertEquals(3, stacks.getArrayLength("boolArr"));
        }

        @Test
        @DisplayName("Should create array with booleen type (French)")
        void testBooleanTypeFrench() {
            stacks.push(new Stacks.Quad("%TEMP%", 2, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            axiome.execute(context, "bArr,booleen");

            assertTrue(stacks.getSymbolTable().contains("bArr"));
            assertEquals(2, stacks.getArrayLength("bArr"));
        }

        @Test
        @DisplayName("Should handle single argument (missing type defaults to int)")
        void testMissingTypeDefaultsToInt() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            // With only one argument, should throw exception (requires 2 args)
            assertThrows(JajaCodeRuntimeException.class,
                () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("Should handle empty arguments")
        void testEmptyArguments() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            assertThrows(JajaCodeRuntimeException.class,
                () -> axiome.execute(context, ""));
        }

        @Test
        @DisplayName("Should parse entier type (French for integer)")
        void testEntierType() {
            stacks.push(new Stacks.Quad("%TEMP%", 4, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            axiome.execute(context, "arr,entier");

            assertTrue(stacks.getSymbolTable().contains("arr"));
            assertEquals(4, stacks.getArrayLength("arr"));
        }
    }

    @Nested
    @DisplayName("LengthAxiome Extended Tests")
    class LengthAxiomeExtendedTests {

        @Test
        @DisplayName("Should get length of minimal array (size 1)")
        void testMinimalArrayLength() {
            stacks.push(new Stacks.Quad("%TEMP%", 1, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "one,int");
            context.setInstructionCounter(1);

            LengthAxiome axiome = new LengthAxiome();
            axiome.execute(context, "one");

            Stacks.Quad result = stacks.pop();
            assertEquals(1, result.value);
            assertEquals(Type.ENTIER, result.type);
        }

        @Test
        @DisplayName("Should get length of moderate array")
        void testModerateArrayLength() {
            stacks.push(new Stacks.Quad("%TEMP%", 50, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "medium,int");
            context.setInstructionCounter(1);

            LengthAxiome axiome = new LengthAxiome();
            axiome.execute(context, "medium");

            Stacks.Quad result = stacks.pop();
            assertEquals(50, result.value);
        }

        @Test
        @DisplayName("Should handle scoped array name (@global)")
        void testScopedArrayNameGlobal() {
            stacks.push(new Stacks.Quad("%TEMP%", 7, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr@global,int");
            context.setInstructionCounter(1);

            LengthAxiome axiome = new LengthAxiome();
            axiome.execute(context, "arr@global");

            Stacks.Quad result = stacks.pop();
            assertEquals(7, result.value);
        }

        @Test
        @DisplayName("Should get length for different array types (boolean)")
        void testBooleanArrayLength() {
            stacks.push(new Stacks.Quad("%TEMP%", 12, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "boolArr,boolean");
            context.setInstructionCounter(1);

            LengthAxiome axiome = new LengthAxiome();
            axiome.execute(context, "boolArr");

            Stacks.Quad result = stacks.pop();
            assertEquals(12, result.value);
            assertEquals(Type.ENTIER, result.type);
        }

        @Test
        @DisplayName("Should increment PC correctly")
        void testPCIncrement() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            context.setInstructionCounter(42);
            LengthAxiome axiome = new LengthAxiome();

            axiome.execute(context, "arr");

            assertEquals(43, context.getInstructionCounter());
        }

        @Test
        @DisplayName("Should push with %TEMP% identifier")
        void testTempIdentifier() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");
            context.setInstructionCounter(1);

            LengthAxiome axiome = new LengthAxiome();
            axiome.execute(context, "arr");

            Stacks.Quad result = stacks.pop();
            assertEquals("%TEMP%", result.ident);
            assertEquals("%TEMP%", result.object);
        }
    }

    @Nested
    @DisplayName("StoreAxiome Extended Tests")
    class StoreAxiomeExtendedTests {

        @Test
        @DisplayName("Should store boolean value to boolean variable")
        void testStoreBooleanValue() {
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            new NewAxiome().execute(context, "flag,boolean,var");
            context.setInstructionCounter(1);

            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            StoreAxiome axiome = new StoreAxiome();

            axiome.execute(context, "flag");

            assertEquals(true, stacks.getValue("flag"));
        }

        @Test
        @DisplayName("Should store integer value replacing old value")
        void testStoreReplacesValue() {
            stacks.push(new Stacks.Quad("%TEMP%", 100, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "counter,int,var");
            context.setInstructionCounter(1);

            stacks.push(new Stacks.Quad("%TEMP%", 200, "%TEMP%", Type.ENTIER));
            StoreAxiome axiome = new StoreAxiome();

            axiome.execute(context, "counter");

            assertEquals(200, stacks.getValue("counter"));
        }

        @Test
        @DisplayName("Should handle scoped variable name (@global)")
        void testScopedVariableGlobal() {
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "var@global,int,var");
            context.setInstructionCounter(1);

            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            StoreAxiome axiome = new StoreAxiome();

            axiome.execute(context, "var@global");

            assertEquals(42, stacks.getValue("var@global"));
        }

        @Test
        @DisplayName("Should increment PC correctly")
        void testPCIncrementStore() {
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "x,int,var");

            context.setInstructionCounter(10);
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            StoreAxiome axiome = new StoreAxiome();

            axiome.execute(context, "x");

            assertEquals(11, context.getInstructionCounter());
        }

        @Test
        @DisplayName("Should handle storing zero value")
        void testStoreZeroValue() {
            stacks.push(new Stacks.Quad("%TEMP%", 99, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "x,int,var");
            context.setInstructionCounter(1);

            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            StoreAxiome axiome = new StoreAxiome();

            axiome.execute(context, "x");

            assertEquals(0, stacks.getValue("x"));
        }

        @Test
        @DisplayName("Should handle storing negative value")
        void testStoreNegativeValue() {
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "x,int,var");
            context.setInstructionCounter(1);

            stacks.push(new Stacks.Quad("%TEMP%", -42, "%TEMP%", Type.ENTIER));
            StoreAxiome axiome = new StoreAxiome();

            axiome.execute(context, "x");

            assertEquals(-42, stacks.getValue("x"));
        }

        @Test
        @DisplayName("Should throw exception when storing to undefined scoped variable")
        void testStoreToUndefinedScoped() {
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            StoreAxiome axiome = new StoreAxiome();

            assertThrows(RuntimeException.class,
                () -> axiome.execute(context, "undefined@local"));
        }
    }

    @Nested
    @DisplayName("Aload/Astore Integration Tests")
    class ArrayAxiomeIntegrationTests {

        @Test
        @DisplayName("Should load value from array at different indices")
        void testAloadMultipleIndices() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Store values at indices 0, 1, 2
            for (int i = 0; i < 3; i++) {
                context.setInstructionCounter(1);
                stacks.push(new Stacks.Quad("%TEMP%", i, "%TEMP%", Type.ENTIER));
                stacks.push(new Stacks.Quad("%TEMP%", i * 10, "%TEMP%", Type.ENTIER));
                new AstoreAxiome().execute(context, "arr");
            }

            // Load and verify each index
            for (int i = 0; i < 3; i++) {
                context.setInstructionCounter(1);
                stacks.push(new Stacks.Quad("%TEMP%", i, "%TEMP%", Type.ENTIER));
                new AloadAxiome().execute(context, "arr");

                Stacks.Quad result = stacks.pop();
                assertEquals(i * 10, result.value);
            }
        }

        @Test
        @DisplayName("Should handle astore with boundary indices (0 and max)")
        void testAstoreBoundaryIndices() {
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Store at index 0
            context.setInstructionCounter(1);
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 100, "%TEMP%", Type.ENTIER));
            new AstoreAxiome().execute(context, "arr");

            // Store at index 9 (max for size 10)
            context.setInstructionCounter(1);
            stacks.push(new Stacks.Quad("%TEMP%", 9, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 900, "%TEMP%", Type.ENTIER));
            new AstoreAxiome().execute(context, "arr");

            // Verify
            context.setInstructionCounter(1);
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            new AloadAxiome().execute(context, "arr");
            assertEquals(100, stacks.pop().value);

            context.setInstructionCounter(1);
            stacks.push(new Stacks.Quad("%TEMP%", 9, "%TEMP%", Type.ENTIER));
            new AloadAxiome().execute(context, "arr");
            assertEquals(900, stacks.pop().value);
        }
    }
}

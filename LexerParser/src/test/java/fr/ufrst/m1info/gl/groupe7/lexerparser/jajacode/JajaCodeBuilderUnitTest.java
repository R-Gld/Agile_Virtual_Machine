package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class JajaCodeBuilderUnitTest {

    @Nested
    @DisplayName("addInstruction / prependInstruction")
    class AddPrepend {

        @Test
        @DisplayName("addInstruction() ajoute bien à la fin")
        void addInstruction_appends() {
            JajaCodeBuilder builder = new JajaCodeBuilder();

            try (MockedStatic<JajaCodeInstructionBuilder> mocked = mockStatic(JajaCodeInstructionBuilder.class)) {
                // IMPORTANT: utiliser des matchers typés pour varargs
                mocked.when(() -> JajaCodeInstructionBuilder.build(
                        any(JajaCodeInstr.class),
                        any(Object[].class)
                )).thenReturn("APP 1");

                builder.addInstruction(mock(JajaCodeInstr.class));

                List<String> result = builder.getInstructionsAsList();
                assertEquals(1, result.size());
                assertEquals("APP 1", result.getFirst());
            }
        }

        @Test
        @DisplayName("prependInstruction() insère bien au début")
        void prependInstruction_insertsFirst() {
            JajaCodeBuilder builder = new JajaCodeBuilder();

            // Ajoute un élément de fin
            try (MockedStatic<JajaCodeInstructionBuilder> mocked = mockStatic(JajaCodeInstructionBuilder.class)) {
                mocked.when(() -> JajaCodeInstructionBuilder.build(
                        any(JajaCodeInstr.class),
                        any(Object[].class)
                )).thenReturn("TAIL");
                builder.addInstruction(mock(JajaCodeInstr.class));
            }

            // Puis insère en tête
            try (MockedStatic<JajaCodeInstructionBuilder> mocked = mockStatic(JajaCodeInstructionBuilder.class)) {
                mocked.when(() -> JajaCodeInstructionBuilder.build(
                        any(JajaCodeInstr.class),
                        any(Object[].class)
                )).thenReturn("HEAD");
                builder.prependInstruction(mock(JajaCodeInstr.class));
            }

            assertEquals(List.of("HEAD", "TAIL"), builder.getInstructionsAsList());
        }
    }

    @Test
    @DisplayName("merge() concatène correctement les instructions")
    void merge_appendsAll() {
        JajaCodeBuilder left = new JajaCodeBuilder();
        JajaCodeBuilder right = new JajaCodeBuilder();

        // Remplit left avec A puis B
        try (MockedStatic<JajaCodeInstructionBuilder> mocked = mockStatic(JajaCodeInstructionBuilder.class)) {
            mocked.when(() -> JajaCodeInstructionBuilder.build(
                    any(JajaCodeInstr.class),
                    any(Object[].class)
            )).thenReturn("A", "B");
            left.addInstruction(mock(JajaCodeInstr.class));
            left.addInstruction(mock(JajaCodeInstr.class));
        }

        // Remplit right avec C
        try (MockedStatic<JajaCodeInstructionBuilder> mocked = mockStatic(JajaCodeInstructionBuilder.class)) {
            mocked.when(() -> JajaCodeInstructionBuilder.build(
                    any(JajaCodeInstr.class),
                    any(Object[].class)
            )).thenReturn("C");
            right.addInstruction(mock(JajaCodeInstr.class));
        }

        left.merge(right);
        assertEquals(List.of("A", "B", "C"), left.getInstructionsAsList());
    }

    @Nested
    @DisplayName("toString")
    class AccessorsAndToString {

        @Test
        @DisplayName("toString() joint les lignes")
        void getInstructions_isLive_and_toString_joinsWithNewlines() {
            JajaCodeBuilder builder = new JajaCodeBuilder();

            // Cas vide
            assertEquals("", builder.toString());

            // Mutations via la liste renvoyée
            builder.addInstruction(JajaCodeInstr.INIT);
            builder.addInstruction(JajaCodeInstr.JCSTOP);
            String expectedOutput = JajaCodeInstr.INIT + "\n" + JajaCodeInstr.JCSTOP + "\n";

            assertEquals(expectedOutput, builder.toString());
        }

        @Test
        @DisplayName("toStringForInterp")
        void toStringForInterp() {
            JajaCodeBuilder builder = new JajaCodeBuilder();
            assertEquals("", builder.toStringForInterpreter());
            StringBuilder expectedOutput = new StringBuilder("1 " + JajaCodeInstr.INIT + "\n");
            builder.addInstruction(JajaCodeInstr.INIT);
            for (int i = 2; i < 52; i++) {
                builder.addInstruction(JajaCodeInstr.DIV);
                expectedOutput.append(i).append(" ")
                        .append(JajaCodeInstr.DIV).append("\n");
            }
            assertEquals(expectedOutput.toString(), builder.toStringForInterpreter());
        }
    }

    @Nested
    @DisplayName("JajaCodeInstructionBuilderTests")
    class JajaCodeInstructionBuilderTests {

        /**
         * Helper that finds an instruction with the exact number of args, or returns null.
         */
        private JajaCodeInstr findInstrWithNumArgs(int n) {
            for (JajaCodeInstr instr : JajaCodeInstr.values()) {
                if (instr.getNumArgs() == n) return instr;
            }
            return null;
        }

        /**
         * Helper that finds an instruction with at least the given number of args, or returns null.
         */
        private JajaCodeInstr findInstrWithAtLeast(int n) {
            for (JajaCodeInstr instr : JajaCodeInstr.values()) {
                if (instr.getNumArgs() >= n) return instr;
            }
            return null;
        }

        @Test
        @DisplayName("build() retourne la base quand il n'y a pas d'arguments")
        void build_returnsBase_whenZeroArgs() {
            // On cherche dynamiquement une instruction à 0 argument (souvent INIT/STOP, etc.)
            JajaCodeInstr zero = findInstrWithNumArgs(0);
            org.junit.jupiter.api.Assumptions.assumeTrue(zero != null, "Aucune instruction avec 0 argument dans JajaCodeInstr");

            String result = JajaCodeInstructionBuilder.build(zero);
            assertEquals(zero.toString(), result);
        }

        @Test
        @DisplayName("build() formate correctement un seul argument")
        void build_formatsSingleArg() {
            JajaCodeInstr one = findInstrWithNumArgs(1);
            org.junit.jupiter.api.Assumptions.assumeTrue(one != null, "Aucune instruction avec 1 argument dans JajaCodeInstr");

            String result = JajaCodeInstructionBuilder.build(one, 42);
            assertEquals(one + "(42)", result);
        }

        @Test
        @DisplayName("build() formate correctement plusieurs arguments (séparateur ', ')")
        void build_formatsMultipleArgs_withCommas() {
            // On cherche une instruction ayant au moins 2 arguments pour déclencher la branche i != 0
            JajaCodeInstr multi = findInstrWithAtLeast(2);
            org.junit.jupiter.api.Assumptions.assumeTrue(multi != null && multi.getNumArgs() >= 2,
                    "Aucune instruction avec au moins 2 arguments dans JajaCodeInstr");

            int n = multi.getNumArgs();
            Object[] args = new Object[n];
            StringBuilder expectedParams = new StringBuilder();
            expectedParams.append('(');
            for (int i = 0; i < n; i++) {
                args[i] = (i == 0) ? "A" : i; // mélange String / Integer pour vérifier toString()
                if (i > 0) expectedParams.append(", ");
                expectedParams.append(args[i]);
            }
            expectedParams.append(')');

            String result = JajaCodeInstructionBuilder.build(multi, args);
            assertEquals(multi.toString() + expectedParams, result);
        }

        @Test
        @DisplayName("build() lève une IllegalArgumentException si trop peu d'arguments")
        void build_throwsOnTooFewArgs() {
            // Choisit une instruction qui attend au moins 1 argument
            JajaCodeInstr needsAtLeastOne = findInstrWithAtLeast(1);
            org.junit.jupiter.api.Assumptions.assumeTrue(needsAtLeastOne != null,
                    "Aucune instruction nécessitant >= 1 argument dans JajaCodeInstr");

            int expected = needsAtLeastOne.getNumArgs();
            int given = Math.max(0, expected - 1); // assure moins que prévu
            Object[] tooFew = new Object[given];

            IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> JajaCodeInstructionBuilder.build(needsAtLeastOne, tooFew)
            );
            String expectedMsg = "Amount of argument given to '" + needsAtLeastOne + "' is incorrect (expected "
                    + expected + ", given " + given + ").";
            assertEquals(expectedMsg, ex.getMessage());
        }

        @Test
        @DisplayName("build() lève une IllegalArgumentException si trop d'arguments")
        void build_throwsOnTooManyArgs() {
            // On prend n'importe quelle instruction (même 0 arg) et on en fournit plus que prévu
            JajaCodeInstr any = JajaCodeInstr.values()[0];
            int expected = any.getNumArgs();
            Object[] tooMany = new Object[expected + 1];
            for (int i = 0; i < tooMany.length; i++) tooMany[i] = i;

            IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> JajaCodeInstructionBuilder.build(any, tooMany)
            );
            String expectedMsg = "Amount of argument given to '" + any + "' is incorrect (expected "
                    + expected + ", given " + (expected + 1) + ").";
            assertEquals(expectedMsg, ex.getMessage());
        }
    }
}
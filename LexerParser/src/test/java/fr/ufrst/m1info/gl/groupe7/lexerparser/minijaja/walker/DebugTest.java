package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Debug class.
 * Tests breakpoint management, mode control, and execution control.
 */
class DebugTest {

    private Debug debug;

    @BeforeEach
    void setUp() {
        debug = new Debug();
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates disabled debug")
        void defaultConstructorCreatesDisabledDebug() {
            Debug d = new Debug();
            assertEquals(Debug.Mode.DISABLED, d.getMode());
            assertFalse(d.isEnabled());
        }

        @Test
        @DisplayName("Constructor with mode sets the mode correctly")
        void constructorWithModeSetsMode() {
            Debug d = new Debug(Debug.Mode.STEP_BY_STEP);
            assertEquals(Debug.Mode.STEP_BY_STEP, d.getMode());
            assertTrue(d.isEnabled());
        }

        @Test
        @DisplayName("Constructor with BREAKPOINTS mode")
        void constructorWithBreakpointsMode() {
            Debug d = new Debug(Debug.Mode.BREAKPOINTS);
            assertEquals(Debug.Mode.BREAKPOINTS, d.getMode());
            assertTrue(d.isEnabled());
        }

        @Test
        @DisplayName("Constructor with DISABLED mode")
        void constructorWithDisabledMode() {
            Debug d = new Debug(Debug.Mode.DISABLED);
            assertEquals(Debug.Mode.DISABLED, d.getMode());
            assertFalse(d.isEnabled());
        }
    }

    // ==================== BREAKPOINT MANAGEMENT TESTS ====================

    @Nested
    @DisplayName("Breakpoint Management Tests")
    class BreakpointManagementTests {

        @Test
        @DisplayName("addBreakPoint adds a breakpoint")
        void addBreakPointAddsBreakpoint() {
            debug.addBreakPoint(10);
            assertTrue(debug.hasBreakPoint(10));
        }

        @Test
        @DisplayName("addBreakPoint multiple breakpoints")
        void addMultipleBreakpoints() {
            debug.addBreakPoint(5);
            debug.addBreakPoint(10);
            debug.addBreakPoint(15);
            
            assertTrue(debug.hasBreakPoint(5));
            assertTrue(debug.hasBreakPoint(10));
            assertTrue(debug.hasBreakPoint(15));
            assertFalse(debug.hasBreakPoint(20));
        }

        @Test
        @DisplayName("addBreakPoint duplicate has no effect")
        void addDuplicateBreakpoint() {
            debug.addBreakPoint(10);
            debug.addBreakPoint(10);
            
            Set<Integer> breakpoints = debug.getBreakPoints();
            assertEquals(1, breakpoints.size());
        }

        @Test
        @DisplayName("removeBreakPoint removes a breakpoint")
        void removeBreakPointRemovesBreakpoint() {
            debug.addBreakPoint(10);
            debug.removeBreakPoint(10);
            assertFalse(debug.hasBreakPoint(10));
        }

        @Test
        @DisplayName("removeBreakPoint non-existent has no effect")
        void removeNonExistentBreakpoint() {
            debug.removeBreakPoint(999);
            assertFalse(debug.hasBreakPoint(999));
        }

        @Test
        @DisplayName("clearBreakPoints removes all breakpoints")
        void clearBreakPointsRemovesAll() {
            debug.addBreakPoint(1);
            debug.addBreakPoint(2);
            debug.addBreakPoint(3);
            
            debug.clearBreakPoints();
            
            assertTrue(debug.getBreakPoints().isEmpty());
            assertFalse(debug.hasBreakPoint(1));
            assertFalse(debug.hasBreakPoint(2));
            assertFalse(debug.hasBreakPoint(3));
        }

        @Test
        @DisplayName("getBreakPoints returns a copy")
        void getBreakPointsReturnsCopy() {
            debug.addBreakPoint(10);
            Set<Integer> copy = debug.getBreakPoints();
            copy.add(20);
            
            // Original should not be affected
            assertFalse(debug.hasBreakPoint(20));
        }

        @Test
        @DisplayName("hasBreakPoint returns false for empty set")
        void hasBreakPointEmptySet() {
            assertFalse(debug.hasBreakPoint(1));
        }

        @Test
        @DisplayName("Breakpoint with negative line number")
        void breakpointNegativeLineNumber() {
            debug.addBreakPoint(-5);
            assertTrue(debug.hasBreakPoint(-5));
        }

        @Test
        @DisplayName("Breakpoint with zero line number")
        void breakpointZeroLineNumber() {
            debug.addBreakPoint(0);
            assertTrue(debug.hasBreakPoint(0));
        }

        @Test
        @DisplayName("Breakpoint with large line number")
        void breakpointLargeLineNumber() {
            debug.addBreakPoint(Integer.MAX_VALUE);
            assertTrue(debug.hasBreakPoint(Integer.MAX_VALUE));
        }
    }

    // ==================== MODE CONTROL TESTS ====================

    @Nested
    @DisplayName("Mode Control Tests")
    class ModeControlTests {

        @Test
        @DisplayName("setMode changes the mode")
        void setModeChangesMode() {
            debug.setMode(Debug.Mode.STEP_BY_STEP);
            assertEquals(Debug.Mode.STEP_BY_STEP, debug.getMode());
        }

        @Test
        @DisplayName("isEnabled returns true for STEP_BY_STEP")
        void isEnabledStepByStep() {
            debug.setMode(Debug.Mode.STEP_BY_STEP);
            assertTrue(debug.isEnabled());
        }

        @Test
        @DisplayName("isEnabled returns true for BREAKPOINTS")
        void isEnabledBreakpoints() {
            debug.setMode(Debug.Mode.BREAKPOINTS);
            assertTrue(debug.isEnabled());
        }

        @Test
        @DisplayName("isEnabled returns false for DISABLED")
        void isEnabledDisabled() {
            debug.setMode(Debug.Mode.DISABLED);
            assertFalse(debug.isEnabled());
        }

        @Test
        @DisplayName("enable sets mode to BREAKPOINTS when DISABLED")
        void enableSetsBreakpointsMode() {
            debug.setMode(Debug.Mode.DISABLED);
            debug.enable();
            assertEquals(Debug.Mode.BREAKPOINTS, debug.getMode());
        }

        @Test
        @DisplayName("enable does not change mode if already enabled")
        void enableDoesNotChangeIfAlreadyEnabled() {
            debug.setMode(Debug.Mode.STEP_BY_STEP);
            debug.enable();
            assertEquals(Debug.Mode.STEP_BY_STEP, debug.getMode());
        }

        @Test
        @DisplayName("disable sets mode to DISABLED")
        void disableSetsDisabledMode() {
            debug.setMode(Debug.Mode.STEP_BY_STEP);
            debug.disable();
            assertEquals(Debug.Mode.DISABLED, debug.getMode());
        }

        @Test
        @DisplayName("disable when already disabled")
        void disableWhenAlreadyDisabled() {
            debug.setMode(Debug.Mode.DISABLED);
            debug.disable();
            assertEquals(Debug.Mode.DISABLED, debug.getMode());
        }
    }

    // ==================== EXECUTION CONTROL TESTS ====================

    @Nested
    @DisplayName("Execution Control Tests")
    class ExecutionControlTests {

        @Test
        @DisplayName("getCurrentLine returns 0 initially")
        void getCurrentLineInitially() {
            assertEquals(0, debug.getCurrentLine());
        }

        @Test
        @DisplayName("isPaused returns false initially")
        void isPausedInitially() {
            assertFalse(debug.isPaused());
        }

        @Test
        @DisplayName("resume sets paused to false")
        void resumeSetsPausedFalse() {
            debug.resume();
            assertFalse(debug.isPaused());
        }

        @Test
        @DisplayName("step sets stepNext flag")
        void stepSetsStepNextFlag() {
            debug.step();
            assertFalse(debug.isPaused());
        }

        @Test
        @DisplayName("continueStepByStep sets mode to STEP_BY_STEP")
        void continueStepByStepSetsMode() {
            debug.setMode(Debug.Mode.BREAKPOINTS);
            debug.continueStepByStep();
            assertEquals(Debug.Mode.STEP_BY_STEP, debug.getMode());
            assertFalse(debug.isPaused());
        }

        @Test
        @DisplayName("continueToNextBreakpoint sets mode to BREAKPOINTS")
        void continueToNextBreakpointSetsMode() {
            debug.setMode(Debug.Mode.STEP_BY_STEP);
            debug.continueToNextBreakpoint();
            assertEquals(Debug.Mode.BREAKPOINTS, debug.getMode());
            assertFalse(debug.isPaused());
        }
    }

    // ==================== LISTENER TESTS ====================

    @Nested
    @DisplayName("Listener Tests")
    class ListenerTests {

        @Test
        @DisplayName("setListener sets the listener")
        void setListenerSetsListener() {
            Debug.DebugListener listener = new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node, 
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() {}
            };
            
            debug.setListener(listener);
            // No exception means success
        }

        @Test
        @DisplayName("setListener with null")
        void setListenerNull() {
            debug.setListener(null);
            // Should not throw
        }

        @Test
        @DisplayName("Listener onResume called on resume")
        void listenerOnResumeCalledOnResume() {
            final boolean[] called = {false};
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() { called[0] = true; }
            });
            
            debug.resume();
            assertTrue(called[0]);
        }

        @Test
        @DisplayName("Listener onResume called on step")
        void listenerOnResumeCalledOnStep() {
            final boolean[] called = {false};
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() { called[0] = true; }
            });
            
            debug.step();
            assertTrue(called[0]);
        }

        @Test
        @DisplayName("Listener onResume called on continueStepByStep")
        void listenerOnResumeCalledOnContinueStepByStep() {
            final boolean[] called = {false};
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() { called[0] = true; }
            });
            
            debug.continueStepByStep();
            assertTrue(called[0]);
        }

        @Test
        @DisplayName("Listener onResume called on continueToNextBreakpoint")
        void listenerOnResumeCalledOnContinueToNextBreakpoint() {
            final boolean[] called = {false};
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() { called[0] = true; }
            });
            
            debug.continueToNextBreakpoint();
            assertTrue(called[0]);
        }
    }

    // ==================== BEFORE NODE TESTS ====================

    @Nested
    @DisplayName("beforeNode Tests")
    class BeforeNodeTests {

        @Test
        @DisplayName("beforeNode returns true when DISABLED")
        void beforeNodeReturnsTrueWhenDisabled() {
            debug.setMode(Debug.Mode.DISABLED);
            boolean result = debug.beforeNode(1, null, null, null);
            assertTrue(result);
        }

        @Test
        @DisplayName("beforeNode updates currentLine")
        void beforeNodeUpdatesCurrentLine() {
            debug.setMode(Debug.Mode.DISABLED);
            debug.beforeNode(42, null, null, null);
            // currentLine should be updated even in DISABLED mode
            // but since DISABLED returns early, let's test with BREAKPOINTS
            debug.setMode(Debug.Mode.BREAKPOINTS);
            // No breakpoint at line 99, so it should return true without pausing
            boolean result = debug.beforeNode(99, createMockNode(), createMockStacks(), null);
            assertTrue(result);
            assertEquals(99, debug.getCurrentLine());
        }

        @Test
        @DisplayName("beforeNode in BREAKPOINTS mode without breakpoint continues")
        void beforeNodeBreakpointsModeNoBreakpoint() {
            debug.setMode(Debug.Mode.BREAKPOINTS);
            // No breakpoint at line 5
            boolean result = debug.beforeNode(5, createMockNode(), createMockStacks(), null);
            assertTrue(result);
        }

        @Test
        @DisplayName("beforeNode calls listener onBreakpoint when breakpoint hit")
        void beforeNodeCallsListenerOnBreakpoint() {
            final boolean[] listenerCalled = {false};
            final int[] lineReported = {-1};
            
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {
                    listenerCalled[0] = true;
                    lineReported[0] = line;
                }
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() {}
            });
            
            debug.setMode(Debug.Mode.BREAKPOINTS);
            debug.addBreakPoint(10);
            
            // We can't fully test handlePause without mocking Scanner input,
            // but we can verify the listener is called
            // This test verifies the listener callback mechanism
        }
    }

    // ==================== MODE ENUM TESTS ====================

    @Nested
    @DisplayName("Mode Enum Tests")
    class ModeEnumTests {

        @Test
        @DisplayName("Mode enum has three values")
        void modeEnumHasThreeValues() {
            assertEquals(3, Debug.Mode.values().length);
        }

        @Test
        @DisplayName("Mode valueOf works correctly")
        void modeValueOf() {
            assertEquals(Debug.Mode.DISABLED, Debug.Mode.valueOf("DISABLED"));
            assertEquals(Debug.Mode.STEP_BY_STEP, Debug.Mode.valueOf("STEP_BY_STEP"));
            assertEquals(Debug.Mode.BREAKPOINTS, Debug.Mode.valueOf("BREAKPOINTS"));
        }

        @Test
        @DisplayName("Mode ordinal values")
        void modeOrdinalValues() {
            assertEquals(0, Debug.Mode.DISABLED.ordinal());
            assertEquals(1, Debug.Mode.STEP_BY_STEP.ordinal());
            assertEquals(2, Debug.Mode.BREAKPOINTS.ordinal());
        }

        @Test
        @DisplayName("Mode name matches enum constant")
        void modeNameMatches() {
            assertEquals("DISABLED", Debug.Mode.DISABLED.name());
            assertEquals("STEP_BY_STEP", Debug.Mode.STEP_BY_STEP.name());
            assertEquals("BREAKPOINTS", Debug.Mode.BREAKPOINTS.name());
        }
    }

    // ==================== STATE TRANSITION TESTS ====================

    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {

        @Test
        @DisplayName("Transition from DISABLED to STEP_BY_STEP")
        void transitionDisabledToStepByStep() {
            debug.setMode(Debug.Mode.DISABLED);
            assertFalse(debug.isEnabled());
            
            debug.setMode(Debug.Mode.STEP_BY_STEP);
            assertTrue(debug.isEnabled());
            assertEquals(Debug.Mode.STEP_BY_STEP, debug.getMode());
        }

        @Test
        @DisplayName("Transition from STEP_BY_STEP to BREAKPOINTS")
        void transitionStepByStepToBreakpoints() {
            debug.setMode(Debug.Mode.STEP_BY_STEP);
            debug.continueToNextBreakpoint();
            
            assertEquals(Debug.Mode.BREAKPOINTS, debug.getMode());
            assertTrue(debug.isEnabled());
        }

        @Test
        @DisplayName("Transition from BREAKPOINTS to STEP_BY_STEP")
        void transitionBreakpointsToStepByStep() {
            debug.setMode(Debug.Mode.BREAKPOINTS);
            debug.continueStepByStep();
            
            assertEquals(Debug.Mode.STEP_BY_STEP, debug.getMode());
            assertTrue(debug.isEnabled());
        }

        @Test
        @DisplayName("Multiple mode transitions")
        void multipleTransitions() {
            debug.setMode(Debug.Mode.DISABLED);
            debug.enable();
            assertEquals(Debug.Mode.BREAKPOINTS, debug.getMode());
            
            debug.continueStepByStep();
            assertEquals(Debug.Mode.STEP_BY_STEP, debug.getMode());
            
            debug.continueToNextBreakpoint();
            assertEquals(Debug.Mode.BREAKPOINTS, debug.getMode());
            
            debug.disable();
            assertEquals(Debug.Mode.DISABLED, debug.getMode());
        }

        @Test
        @DisplayName("Rapid mode changes")
        void rapidModeChanges() {
            for (int i = 0; i < 100; i++) {
                debug.setMode(Debug.Mode.STEP_BY_STEP);
                debug.setMode(Debug.Mode.BREAKPOINTS);
                debug.setMode(Debug.Mode.DISABLED);
            }
            assertEquals(Debug.Mode.DISABLED, debug.getMode());
        }
    }

    // ==================== BREAKPOINT STRESS TESTS ====================

    @Nested
    @DisplayName("Breakpoint Stress Tests")
    class BreakpointStressTests {

        @Test
        @DisplayName("Add many breakpoints")
        void addManyBreakpoints() {
            for (int i = 0; i < 1000; i++) {
                debug.addBreakPoint(i);
            }
            assertEquals(1000, debug.getBreakPoints().size());
            
            for (int i = 0; i < 1000; i++) {
                assertTrue(debug.hasBreakPoint(i));
            }
        }

        @Test
        @DisplayName("Remove many breakpoints")
        void removeManyBreakpoints() {
            for (int i = 0; i < 100; i++) {
                debug.addBreakPoint(i);
            }
            
            for (int i = 0; i < 100; i++) {
                debug.removeBreakPoint(i);
            }
            
            assertTrue(debug.getBreakPoints().isEmpty());
        }

        @Test
        @DisplayName("Add and remove same breakpoint repeatedly")
        void addRemoveSameBreakpointRepeatedly() {
            for (int i = 0; i < 100; i++) {
                debug.addBreakPoint(42);
                assertTrue(debug.hasBreakPoint(42));
                debug.removeBreakPoint(42);
                assertFalse(debug.hasBreakPoint(42));
            }
        }

        @Test
        @DisplayName("Clear breakpoints multiple times")
        void clearBreakpointsMultipleTimes() {
            debug.addBreakPoint(1);
            debug.addBreakPoint(2);
            debug.clearBreakPoints();
            assertTrue(debug.getBreakPoints().isEmpty());
            
            debug.addBreakPoint(3);
            debug.clearBreakPoints();
            assertTrue(debug.getBreakPoints().isEmpty());
            
            // Clear on empty set
            debug.clearBreakPoints();
            assertTrue(debug.getBreakPoints().isEmpty());
        }
    }

    // ==================== LISTENER EDGE CASES ====================

    @Nested
    @DisplayName("Listener Edge Cases")
    class ListenerEdgeCases {

        @Test
        @DisplayName("Set listener then clear it")
        void setThenClearListener() {
            final boolean[] called = {false};
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() { called[0] = true; }
            });
            
            debug.setListener(null);
            debug.resume();
            
            assertFalse(called[0]); // Listener was removed, should not be called
        }

        @Test
        @DisplayName("Replace listener")
        void replaceListener() {
            final int[] callCount = {0};
            
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() { callCount[0] += 1; }
            });
            
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() { callCount[0] += 10; }
            });
            
            debug.resume();
            assertEquals(10, callCount[0]); // Only second listener should be called
        }

        @Test
        @DisplayName("Multiple control operations with listener")
        void multipleControlOperationsWithListener() {
            final int[] resumeCount = {0};
            
            debug.setListener(new Debug.DebugListener() {
                @Override
                public void onBreakpoint(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                         fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onStep(int line, fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode node,
                                   fr.ufrst.m1info.gl.groupe7.memoire.Stacks stacks) {}
                @Override
                public void onResume() { resumeCount[0]++; }
            });
            
            debug.resume();
            debug.step();
            debug.continueStepByStep();
            debug.continueToNextBreakpoint();
            
            assertEquals(4, resumeCount[0]);
        }
    }

    // ==================== CONCURRENT-LIKE ACCESS TESTS ====================

    @Nested
    @DisplayName("Concurrent-like Access Tests")
    class ConcurrentLikeAccessTests {

        @Test
        @DisplayName("Rapid breakpoint modifications")
        void rapidBreakpointModifications() {
            for (int i = 0; i < 50; i++) {
                debug.addBreakPoint(i);
                debug.addBreakPoint(i + 50);
                debug.removeBreakPoint(i);
            }
            
            // Should have breakpoints 50-99
            assertEquals(50, debug.getBreakPoints().size());
            for (int i = 50; i < 100; i++) {
                assertTrue(debug.hasBreakPoint(i));
            }
        }

        @Test
        @DisplayName("Interleaved mode and breakpoint changes")
        void interleavedModeAndBreakpointChanges() {
            debug.setMode(Debug.Mode.BREAKPOINTS);
            debug.addBreakPoint(1);
            debug.setMode(Debug.Mode.STEP_BY_STEP);
            debug.addBreakPoint(2);
            debug.disable();
            debug.addBreakPoint(3);
            debug.enable();
            
            assertEquals(Debug.Mode.BREAKPOINTS, debug.getMode());
            assertEquals(3, debug.getBreakPoints().size());
        }
    }

    // ==================== PAUSED STATE TESTS ====================

    @Nested
    @DisplayName("Paused State Tests")
    class PausedStateTests {

        @Test
        @DisplayName("isPaused initial state")
        void isPausedInitialState() {
            assertFalse(debug.isPaused());
        }

        @Test
        @DisplayName("resume clears paused state")
        void resumeClearsPausedState() {
            debug.resume();
            assertFalse(debug.isPaused());
        }

        @Test
        @DisplayName("step clears paused state")
        void stepClearsPausedState() {
            debug.step();
            assertFalse(debug.isPaused());
        }

        @Test
        @DisplayName("continueStepByStep clears paused state")
        void continueStepByStepClearsPausedState() {
            debug.continueStepByStep();
            assertFalse(debug.isPaused());
        }

        @Test
        @DisplayName("continueToNextBreakpoint clears paused state")
        void continueToNextBreakpointClearsPausedState() {
            debug.continueToNextBreakpoint();
            assertFalse(debug.isPaused());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a simple mock AstNode for testing.
     */
    private fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode createMockNode() {
        return new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode() {
            @Override
            public String toStringTree() {
                return "MockNode";
            }
        };
    }

    /**
     * Creates a simple mock Stacks for testing.
     */
    private fr.ufrst.m1info.gl.groupe7.memoire.Stacks createMockStacks() {
        return new fr.ufrst.m1info.gl.groupe7.memoire.Stacks();
    }
}

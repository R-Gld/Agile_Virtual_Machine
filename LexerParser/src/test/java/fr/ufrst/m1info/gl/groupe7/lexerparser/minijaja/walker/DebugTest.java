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
            boolean result = debug.beforeNode(1, null, null);
            assertTrue(result);
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
    }
}

package fr.ufrst.m1info.gl.groupe7.lexerparser.cucumber;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.*;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for JajaCode Cucumber tests.
 * Covers:
 * - MiniJaja Compilation
 * - JajaCode Interpretation
 * - Memory Visualization
 * - Memory Consistency
 * - JajaCode Debug
 */
public class JajaCodeStepDefinitions {

    // Context shared between steps
    private String minijajaCode;
    private String jajaCode;
    private String generatedJajaCode;
    private DiagnosticCollector collector;
    private JajaCodeInterpreter interpreter;
    private Stacks stacks;
    private boolean compilationSucceeded;
    private boolean interpretationSucceeded;
    private Exception lastException;
    private JajaCodeDebug.MemorySnapshot memorySnapshot;

    // ========================================================================
    // GIVEN Steps
    // ========================================================================

    @Given("a MiniJaja program:")
    public void aMiniJajaProgram(String program) {
        this.minijajaCode = program;
        this.collector = new DiagnosticCollector();
        this.compilationSucceeded = false;
        this.interpretationSucceeded = false;
        this.lastException = null;
    }

    @Given("a JajaCode program:")
    public void aJajaCodeProgram(String program) {
        this.jajaCode = program;
        this.collector = new DiagnosticCollector();
        this.interpretationSucceeded = false;
        this.lastException = null;
    }

    // ========================================================================
    // WHEN Steps - Compilation
    // ========================================================================

    @When("I compile the MiniJaja program")
    public void iCompileTheMiniJajaProgram() {
        try {
            MiniJajaCompilerVisitor compilerVisitor =
                MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(minijajaCode);
            this.generatedJajaCode = compilerVisitor.getJajaCodeBuilder().toStringForInterpreter();
            this.compilationSucceeded = true;
        } catch (RuntimeException e) {
            this.compilationSucceeded = false;
            this.lastException = e;
        }
    }

    // ========================================================================
    // WHEN Steps - Interpretation
    // ========================================================================

    @When("I interpret the JajaCode program")
    public void iInterpretTheJajaCodeProgram() {
        try {
            this.collector = new DiagnosticCollector();
            this.interpreter = new JajaCodeInterpreter(jajaCode, collector);
            this.interpreter.run();
            this.stacks = interpreter.getStacks();
            this.interpretationSucceeded = !collector.hasErrors();
        } catch (RuntimeException e) {
            this.interpretationSucceeded = false;
            this.lastException = e;
        }
    }

    @When("I interpret the generated JajaCode")
    public void iInterpretTheGeneratedJajaCode() {
        assertNotNull(generatedJajaCode, "JajaCode should have been generated first");
        this.jajaCode = generatedJajaCode;
        iInterpretTheJajaCodeProgram();
    }

    // ========================================================================
    // WHEN Steps - Memory
    // ========================================================================

    @When("I capture the memory state")
    public void iCaptureTheMemoryState() {
        assertNotNull(stacks, "Stacks should be available after interpretation");
        this.memorySnapshot = JajaCodeDebug.captureMemoryState(stacks, 0, "N/A");
    }

    // ========================================================================
    // WHEN Steps - Debug
    // ========================================================================

    @When("I initialize the debugger")
    public void iInitializeTheDebugger() {
        this.collector = new DiagnosticCollector();
        this.interpreter = new JajaCodeInterpreter(jajaCode, collector);
    }

    @When("I execute one step")
    public void iExecuteOneStep() {
        assertNotNull(interpreter, "Interpreter should be initialized");
        interpreter.step();
        this.stacks = interpreter.getStacks();
    }

    @When("I execute steps until address {int}")
    public void iExecuteStepsUntilAddress(int targetAddress) {
        assertNotNull(interpreter, "Interpreter should be initialized");
        int maxSteps = 10000; // Safety limit increased for recursion
        int steps = 0;

        // If we are already at targetAddress, execute at least one step to allow looping
        if (interpreter.getCurrentInstructionIndex() == targetAddress) {
            if (interpreter.isFinished()) return;
            interpreter.step();
            steps++;
        }

        while (interpreter.getCurrentInstructionIndex() != targetAddress && steps < maxSteps) {
            if (interpreter.isFinished()) break;
            interpreter.step();
            steps++;
        }
        this.stacks = interpreter.getStacks();
    }

    @When("I capture the debug memory snapshot")
    public void iCaptureTheDebugMemorySnapshot() {
        this.memorySnapshot = interpreter.captureMemoryState();
    }

    @When("I reset the debugger")
    public void iResetTheDebugger() {
        assertNotNull(interpreter, "Interpreter should be initialized");
        interpreter.reset();
    }

    // ========================================================================
    // THEN Steps - Compilation
    // ========================================================================

    @Then("the compilation should succeed")
    public void theCompilationShouldSucceed() {
        assertTrue(compilationSucceeded,
            "Compilation should succeed. Error: " + (lastException != null ? lastException.getMessage() : "unknown"));
    }

    @Then("the compilation should fail")
    public void theCompilationShouldFail() {
        assertFalse(compilationSucceeded, "Compilation should fail for invalid program");
    }

    @Then("the JajaCode should contain {string}")
    public void theJajaCodeShouldContain(String expected) {
        assertNotNull(generatedJajaCode, "JajaCode should have been generated");
        assertTrue(generatedJajaCode.contains(expected),
            "Generated JajaCode should contain '" + expected + "'. Actual:\n" + generatedJajaCode);
    }

    @Then("the JajaCode should contain the following sequence:")
    public void theJajaCodeShouldContainTheFollowingSequence(String expectedSequence) {
        assertNotNull(generatedJajaCode, "JajaCode should have been generated");
        String normalizedExpected = expectedSequence.trim().replace("\r\n", "\n");
        String normalizedActual = generatedJajaCode.trim().replace("\r\n", "\n");
        assertTrue(normalizedActual.contains(normalizedExpected),
            "Generated JajaCode should contain sequence:\n" + normalizedExpected + "\nActual:\n" + normalizedActual);
    }

    @Then("the JajaCode instructions should contain:")
    public void theJajaCodeInstructionsShouldContain(String expectedSequence) {
        assertNotNull(generatedJajaCode, "JajaCode should have been generated");
        // Remove line numbers from actual code: "1 init" -> "init"
        String actualInstructions = generatedJajaCode.replaceAll("(?m)^\\d+\\s+", "");
        String normalizedExpected = expectedSequence.trim().replace("\r\n", "\n");
        String normalizedActual = actualInstructions.trim().replace("\r\n", "\n");
        assertTrue(normalizedActual.contains(normalizedExpected),
            "Generated JajaCode instructions should contain sequence:\n" + normalizedExpected + "\nActual instructions:\n" + normalizedActual);
    }

    // ========================================================================
    // THEN Steps - Interpretation
    // ========================================================================

    @Then("the interpretation should succeed")
    public void theInterpretationShouldSucceed() {
        assertTrue(interpretationSucceeded,
            "Interpretation should succeed. Error: " + (lastException != null ? lastException.getMessage() : "unknown"));
    }

    @Then("the interpretation should fail")
    public void theInterpretationShouldFail() {
        assertFalse(interpretationSucceeded, "Interpretation should fail for invalid program");
    }

    @Then("the variable {string} should have value {word}")
    public void theVariableShouldHaveValue(String variableName, String expectedValue) {
        assertNotNull(stacks, "Stacks should be available");
        Object actualValue = stacks.getValue(variableName);
        assertNotNull(actualValue, "Variable '" + variableName + "' should exist");

        // Try to parse as integer first
        try {
            int expectedInt = Integer.parseInt(expectedValue);
            if (actualValue instanceof Integer) {
                assertEquals(expectedInt, actualValue,
                    "Variable '" + variableName + "' should have value " + expectedInt);
            } else {
                // actualValue might be a string representation of int
                assertEquals(expectedInt, Integer.parseInt(actualValue.toString()),
                    "Variable '" + variableName + "' should have value " + expectedInt);
            }
            return;
        } catch (NumberFormatException e) {
            // Not an integer, continue
        }

        // Handle boolean values
        if ("true".equalsIgnoreCase(expectedValue)) {
            assertEquals(true, actualValue,
                "Variable '" + variableName + "' should have value true");
        } else if ("false".equalsIgnoreCase(expectedValue)) {
            assertEquals(false, actualValue,
                "Variable '" + variableName + "' should have value false");
        } else {
            assertEquals(expectedValue, actualValue.toString(),
                "Variable '" + variableName + "' should have value " + expectedValue);
        }
    }

    // ========================================================================
    // THEN Steps - Memory Visualization
    // ========================================================================

    @Then("the stack should contain variable {string}")
    public void theStackShouldContainVariable(String variableName) {
        assertNotNull(memorySnapshot, "Memory snapshot should be captured");
        boolean found = memorySnapshot.stackState().stream()
            .anyMatch(v -> variableName.equals(v.identifier()));
        assertTrue(found, "Stack should contain variable '" + variableName + "'");
    }

    @Then("the stack should not contain variable {string}")
    public void theStackShouldNotContainVariable(String variableName) {
        assertNotNull(memorySnapshot, "Memory snapshot should be captured");
        boolean found = memorySnapshot.stackState().stream()
            .anyMatch(v -> variableName.equals(v.identifier()));
        assertFalse(found, "Stack should not contain variable '" + variableName + "'");
    }

    @Then("the variable {string} should be of kind {string}")
    public void theVariableShouldBeOfKind(String variableName, String expectedKind) {
        assertNotNull(stacks, "Stacks should be available");
        String actualKind = stacks.getObjectType(variableName);
        assertEquals(expectedKind, actualKind,
            "Variable '" + variableName + "' should be of kind '" + expectedKind + "'");
    }

    @Then("the variable {string} should have type {string}")
    public void theVariableShouldHaveType(String variableName, String expectedType) {
        assertNotNull(stacks, "Stacks should be available");
        var dataType = stacks.getDataType(variableName);
        assertNotNull(dataType, "Variable '" + variableName + "' should have a type");
        // Use toString() which returns "integer", "boolean", etc.
        assertTrue(dataType.toString().toLowerCase().contains(expectedType.toLowerCase()),
            "Variable '" + variableName + "' should have type containing '" + expectedType + "'. Actual: " + dataType.toString());
    }

    @Then("the heap should contain array {string}")
    public void theHeapShouldContainArray(String arrayName) {
        assertNotNull(memorySnapshot, "Memory snapshot should be captured");
        boolean found = memorySnapshot.heapState().stream()
            .anyMatch(h -> arrayName.equals(h.identifier()));
        assertTrue(found, "Heap should contain array '" + arrayName + "'");
    }

    @Then("the array {string} should have size {int}")
    public void theArrayShouldHaveSize(String arrayName, int expectedSize) {
        assertNotNull(memorySnapshot, "Memory snapshot should be captured");
        var arrayInfo = memorySnapshot.heapState().stream()
            .filter(h -> arrayName.equals(h.identifier()))
            .findFirst()
            .orElse(null);
        assertNotNull(arrayInfo, "Array '" + arrayName + "' should exist");
        assertEquals(expectedSize, arrayInfo.size(),
            "Array '" + arrayName + "' should have size " + expectedSize);
    }

    @Then("the array {string} at index {int} should have value {int}")
    public void theArrayAtIndexShouldHaveValue(String arrayName, int index, int expectedValue) {
        assertNotNull(memorySnapshot, "Memory snapshot should be captured");
        var arrayInfo = memorySnapshot.heapState().stream()
            .filter(h -> arrayName.equals(h.identifier()))
            .findFirst()
            .orElse(null);
        assertNotNull(arrayInfo, "Array '" + arrayName + "' should exist in heap");

        assertTrue(index >= 0 && index < arrayInfo.size(),
            "Index " + index + " out of bounds for array size " + arrayInfo.size());
    }

    // ========================================================================
    // THEN Steps - Debug
    // ========================================================================

    @Then("the program counter should be {int}")
    public void theProgramCounterShouldBe(int expectedPC) {
        assertNotNull(interpreter, "Interpreter should be initialized");
        assertEquals(expectedPC, interpreter.getCurrentInstructionIndex(),
            "Program counter should be " + expectedPC);
    }

    @Then("the execution should be finished")
    public void theExecutionShouldBeFinished() {
        assertNotNull(interpreter, "Interpreter should be initialized");
        assertTrue(interpreter.isFinished(), "Execution should be finished");
    }

    @Then("the snapshot should show variable {string} with value {word}")
    public void theSnapshotShouldShowVariableWithValue(String variableName, String expectedValue) {
        assertNotNull(memorySnapshot, "Memory snapshot should be captured");
        var varInfo = memorySnapshot.stackState().stream()
            .filter(v -> variableName.equals(v.identifier()))
            .findFirst()
            .orElse(null);
        assertNotNull(varInfo, "Snapshot should contain variable '" + variableName + "'");

        Object actualValue = varInfo.value();

        // Try to parse as integer first
        try {
            int expectedInt = Integer.parseInt(expectedValue);
            if (actualValue instanceof Integer) {
                assertEquals(expectedInt, actualValue,
                    "Variable '" + variableName + "' should have value " + expectedInt);
            } else {
                assertEquals(expectedInt, Integer.parseInt(actualValue.toString()),
                    "Variable '" + variableName + "' should have value " + expectedInt);
            }
            return;
        } catch (NumberFormatException e) {
            // Not an integer
        }

        // Handle boolean
        if ("true".equalsIgnoreCase(expectedValue)) {
             assertEquals(true, actualValue, "Variable '" + variableName + "' should have value true");
        } else if ("false".equalsIgnoreCase(expectedValue)) {
             assertEquals(false, actualValue, "Variable '" + variableName + "' should have value false");
        } else {
             assertEquals(expectedValue, actualValue.toString(), "Variable '" + variableName + "' should have value " + expectedValue);
        }
    }


    @Then("the current instruction should contain {string}")
    public void theCurrentInstructionShouldContain(String expected) {
        // Capture snapshot if not done
        if (memorySnapshot == null) {
            memorySnapshot = interpreter.captureMemoryState();
        }
        assertNotNull(memorySnapshot, "Memory snapshot should be captured");
        String instruction = memorySnapshot.currentInstruction();
        assertTrue(instruction.toLowerCase().contains(expected.toLowerCase()),
            "Current instruction should contain '" + expected + "'. Actual: " + instruction);
    }
}

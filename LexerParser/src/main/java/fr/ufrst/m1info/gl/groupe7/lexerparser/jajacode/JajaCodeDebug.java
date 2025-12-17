package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.memoire.ArrayInfo;
import fr.ufrst.m1info.gl.groupe7.memoire.HeapEntry;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.Symbol;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.ArrayList;
import java.util.List;

public class JajaCodeDebug {

    public record VariableInfo(String identifier, Object value, String kind, Type type, int stackPosition,
                               boolean isTemporary) {

        public boolean isConstant() {
            return "cst".equals(kind);
        }

        public boolean isVariable() {
            return "var".equals(kind);
        }

        public boolean isMethod() {
            return "meth".equals(kind);
        }

        public boolean isArray() {
            return "tab".equals(kind);
        }

        @Override
        public String toString() {
            if (isTemporary) {
                return String.format("[TEMP] %s (%s)", value, type);
            }
            String kindLabel = switch (kind) {
                case "var" -> "Variable";
                case "cst" -> "Constant";
                case "meth" -> "Method";
                case "tab" -> "Array";
                default -> kind;
            };
            return String.format("%s = %s (%s, %s)", identifier, value, kindLabel, type);
        }
    }

    /**
     * Represents a heap entry for arrays.
     *
     * @param identifier    Array name
     * @param baseAddress   Base address in the heap
     * @param size          Logical size of the array
     * @param allocatedSize Allocated size (may be larger - buddy system)
     * @param elementType   Type of elements
     * @param elements      Values of the elements
     */
    public record HeapInfo(String identifier, int baseAddress, int size, int allocatedSize, Type elementType,
                           List<Object> elements) {

        @Override
        public String toString() {
            return String.format("%s[%d] @ addr=%d (alloc=%d) : %s", identifier, size, baseAddress, allocatedSize, elements);
        }
    }

    /**
     * Complete snapshot of the memory state at a given moment.
     *
     * @param programCounter     Address of the current instruction
     * @param currentInstruction Text of the current instruction
     * @param stackState         State of the stack
     * @param heapState          State of the heap
     * @param currentContext     Execution context (method name or null)
     * @param recursionDepth     Recursion depth
     */
    public record MemorySnapshot(int programCounter, String currentInstruction, List<VariableInfo> stackState,
                                 List<HeapInfo> heapState, String currentContext, int recursionDepth) {

        public List<VariableInfo> getVariablesOnly() {
            return stackState.stream().filter(v -> !v.isTemporary() && v.isVariable()).toList();
        }

        public List<VariableInfo> getConstantsOnly() {
            return stackState.stream().filter(v -> !v.isTemporary() && v.isConstant()).toList();
        }

        public List<VariableInfo> getMethodsOnly() {
            return stackState.stream().filter(v -> !v.isTemporary() && v.isMethod()).toList();
        }

        public List<VariableInfo> getArraysOnly() {
            return stackState.stream().filter(v -> !v.isTemporary() && v.isArray()).toList();
        }

        public List<VariableInfo> getTemporariesOnly() {
            return stackState.stream().filter(VariableInfo::isTemporary).toList();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════\n");
            sb.append(String.format(" PC: %d → %s\n", programCounter, currentInstruction));
            sb.append(String.format(" Context: %s (recursion depth: %d)\n", currentContext != null ? currentContext : "main", recursionDepth));
            sb.append("═══════════════════════════════════════════════════════════\n");

            sb.append("\n┌─── STACK (top to bottom) ───\n");
            for (VariableInfo v : stackState) {
                sb.append("│ ").append(v).append("\n");
            }
            sb.append("└─────────────────────────────────────\n");

            if (!heapState.isEmpty()) {
                sb.append("\n┌─── HEAP ───\n");
                for (HeapInfo h : heapState) {
                    sb.append("│ ").append(h).append("\n");
                }
                sb.append("└──────────────────\n");
            }

            return sb.toString();
        }
    }

    // ========================================================================
    // MÉTHODES PRINCIPALES DE DEBUG
    // ========================================================================

    /**
     * Capture the complete memory state at the current address.
     *
     * @param stacks             The interpreter's Stacks instance
     * @param programCounter     Address of the current instruction (PC)
     * @param currentInstruction Text of the current instruction
     * @return A complete snapshot of the memory state
     */
    public static MemorySnapshot captureMemoryState(Stacks stacks, int programCounter, String currentInstruction) {
        List<VariableInfo> stackState = captureStackState(stacks);
        List<HeapInfo> heapState = captureHeapState(stacks);
        String context = stacks.getCurrentContext();
        int recursionDepth = context != null ? stacks.getRecursionDepth(stacks.getCurrentMethodName()) : 0;

        return new MemorySnapshot(programCounter, currentInstruction, stackState, heapState, context, recursionDepth);
    }

    /**
     * Capture only the stack state.
     *
     * @param stacks The Stacks instance
     * @return List of VariableInfo from top to bottom of the stack
     */
    public static List<VariableInfo> captureStackState(Stacks stacks) {
        List<VariableInfo> result = new ArrayList<>();
        List<Stacks.Quad> quads = stacks.getStackFromTopToBottom();

        int position = quads.size() - 1;
        for (Stacks.Quad q : quads) {
            boolean isTemp = "_".equals(q.ident) || "%TEMP%".equals(q.ident);
            VariableInfo info = new VariableInfo(q.ident, q.value, q.object, q.type, position, isTemp);
            result.add(info);
            position--;
        }
        return result;
    }

    /**
     * Captures heap state - mainly arrays.
     *
     * @param stacks The Stacks instance
     * @return List of HeapInfo for each allocated array
     */
    public static List<HeapInfo> captureHeapState(Stacks stacks) {
        List<HeapInfo> result = new ArrayList<>();
        List<Stacks.Quad> quads = stacks.getStackFromTopToBottom();

        for (Stacks.Quad q : quads) {
            if ("tab".equals(q.object) && q.value instanceof ArrayInfo arrayInfo) {
                int baseAddr = arrayInfo.getBaseAddress();
                int logicalSize = arrayInfo.getSize();

                // Retrieve array elements from heap
                List<Object> elements = new ArrayList<>();
                for (int i = 0; i < logicalSize; i++) {
                    try {
                        Object elem = stacks.getArrayValue(q.ident, i);
                        elements.add(elem);
                    } catch (Exception e) {
                        elements.add("<uninitialized>");
                    }
                }

                // Retrieve allocated size from HeapEntry
                int allocatedSize = logicalSize;
                HeapEntry entry = stacks.getHeap().getEntryNotFree(baseAddr);
                if (entry != null) {
                    allocatedSize = entry.getSize();
                }

                HeapInfo heapInfo = new HeapInfo(q.ident, baseAddr, logicalSize, allocatedSize, q.type, elements);
                result.add(heapInfo);
            }
        }
        return result;
    }

    /**
     * Retrieves information about a specific variable by its identifier.
     *
     * @param stacks     The Stacks instance
     * @param identifier The variable identifier (e.g., "x@global")
     * @return VariableInfo or null if not found
     */
    public static VariableInfo getVariableInfo(Stacks stacks, String identifier) {
        Stacks.Quad q = stacks.findQuad(identifier);
        if (q == null) {
            return null;
        }

        int position = stacks.getStackPosition(identifier);
        boolean isTemp = "_".equals(q.ident) || "%TEMP%".equals(q.ident);

        return new VariableInfo(q.ident, q.value, q.object, q.type, position, isTemp);
    }

    /**
     * Retrieves the value of a variable by its identifier.
     *
     * @param stacks     The Stacks instance
     * @param identifier The variable identifier
     * @return The value or null if not found
     */
    public static Object getVariableValue(Stacks stacks, String identifier) {
        return stacks.getValue(identifier);
    }

    /**
     * Retrieves all variables from a specific scope.
     *
     * @param stacks The Stacks instance
     * @param scope  The scope to filter (e.g., "global", "fact@int")
     * @return List of variables in that scope
     */
    public static List<VariableInfo> getVariablesInScope(Stacks stacks, String scope) {
        List<VariableInfo> result = new ArrayList<>();
        List<Stacks.Quad> quads = stacks.getStackFromTopToBottom();

        int position = quads.size() - 1;
        for (Stacks.Quad q : quads) {
            boolean isTemp = "_".equals(q.ident) || "%TEMP%".equals(q.ident);
            if (!isTemp && q.ident.contains("@" + scope)) {
                result.add(new VariableInfo(q.ident, q.value, q.object, q.type, position, false));
            }
            position--;
        }
        return result;
    }

    /**
     * Retrieves information for a specific array, including its elements.
     *
     * @param stacks          The Stacks instance
     * @param arrayIdentifier Array identifier
     * @return HeapInfo or null if not found
     */
    public static HeapInfo getArrayInfo(Stacks stacks, String arrayIdentifier) {
        Stacks.Quad q = stacks.findQuad(arrayIdentifier);
        if (q == null || !"tab".equals(q.object)) {
            return null;
        }

        if (!(q.value instanceof ArrayInfo arrayInfo)) {
            return null;
        }

        int baseAddr = arrayInfo.getBaseAddress();
        int logicalSize = arrayInfo.getSize();

        List<Object> elements = new ArrayList<>();
        for (int i = 0; i < logicalSize; i++) {
            try {
                Object elem = stacks.getArrayValue(arrayIdentifier, i);
                elements.add(elem);
            } catch (Exception e) {
                elements.add("<error>");
            }
        }

        int allocatedSize = logicalSize;
        HeapEntry entry = stacks.getHeap().getEntryNotFree(baseAddr);
        if (entry != null) {
            allocatedSize = entry.getSize();
        }

        return new HeapInfo(arrayIdentifier, baseAddr, logicalSize, allocatedSize, q.type, elements);
    }

    /**
     * Checks if a variable exists in the stack.
     *
     * @param stacks     The Stacks instance
     * @param identifier Identifier to search for
     * @return true if the variable exists
     */
    public static boolean variableExists(Stacks stacks, String identifier) {
        return stacks.findQuad(identifier) != null;
    }

    /**
     * Retrieves the kind of a variable (var, cst, meth, tab).
     *
     * @param stacks     The Stacks instance
     * @param identifier The identifier
     * @return Object type or null
     */
    public static String getVariableKind(Stacks stacks, String identifier) {
        return stacks.getObjectType(identifier);
    }

    /**
     * Retrieves the list of all declared symbols.
     *
     * @param stacks The Stacks instance
     * @return List of symbols
     */
    public static List<Symbol> getAllSymbols(Stacks stacks) {
        return stacks.getAllSymbols();
    }

    /**
     * Returns a textual representation of the current memory state.
     * Useful for console display or logging.
     *
     * @param stacks      The Stacks instance
     * @param pc          Program Counter
     * @param instruction Current instruction
     * @return Formatted string
     */
    public static String formatMemoryState(Stacks stacks, int pc, String instruction) {
        MemorySnapshot snapshot = captureMemoryState(stacks, pc, instruction);
        return snapshot.toString();
    }
}


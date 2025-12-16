package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.Symbol;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JajaCodeDebugTest {
    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = mock(Stacks.class);
    }

    @Test
    void testVariableInfoMethods() {
        JajaCodeDebug.VariableInfo var = new JajaCodeDebug.VariableInfo("x", 42, "var", Type.ENTIER, 0, false);
        assertTrue(var.isVariable());
        assertFalse(var.isConstant());
        assertFalse(var.isMethod());
        assertFalse(var.isArray());
        assertEquals("x", var.identifier());
        assertEquals(42, var.value());
        assertEquals(Type.ENTIER, var.type());
        assertEquals(0, var.stackPosition());
        assertFalse(var.isTemporary());
    }

    @Test
    void testHeapInfoToString() {
        List<Object> elements = List.of(1, 2, 3);
        JajaCodeDebug.HeapInfo heapInfo = new JajaCodeDebug.HeapInfo("arr", 10, 3, 4, Type.ENTIER, elements);
        String str = heapInfo.toString();
        assertTrue(str.contains("arr[3]"));
        assertTrue(str.contains("addr=10"));
        assertTrue(str.contains("alloc=4"));
        assertTrue(str.contains("[1, 2, 3]"));
    }

    @Test
    void testCaptureStackState() {
        List<Stacks.Quad> quads = new ArrayList<>();
        quads.add(new Stacks.Quad("x", 1, "var", Type.ENTIER));
        quads.add(new Stacks.Quad("_", 99, "var", Type.ENTIER)); // temp
        when(stacks.getStackFromTopToBottom()).thenReturn(quads);
        List<JajaCodeDebug.VariableInfo> stackState = JajaCodeDebug.captureStackState(stacks);
        assertEquals(2, stackState.size());
        assertEquals("x", stackState.get(0).identifier());
        assertTrue(stackState.get(1).isTemporary());
    }

    @Test
    void testGetVariableInfoAndValue() {
        Stacks.Quad quad = new Stacks.Quad("y", 7, "cst", Type.ENTIER);
        when(stacks.findQuad("y")).thenReturn(quad);
        when(stacks.getStackPosition("y")).thenReturn(1);
        JajaCodeDebug.VariableInfo info = JajaCodeDebug.getVariableInfo(stacks, "y");
        assertNotNull(info);
        assertEquals("y", info.identifier());
        when(stacks.getValue("y")).thenReturn(7);
        assertEquals(7, JajaCodeDebug.getVariableValue(stacks, "y"));
    }

    @Test
    void testVariableExistsAndKind() {
        when(stacks.findQuad("z")).thenReturn(new Stacks.Quad("z", 0, "meth", Type.BOOLEEN));
        when(stacks.getObjectType("z")).thenReturn("meth");
        assertTrue(JajaCodeDebug.variableExists(stacks, "z"));
        assertEquals("meth", JajaCodeDebug.getVariableKind(stacks, "z"));
    }

    @Test
    void testGetAllSymbols() {
        List<Symbol> symbols = List.of(mock(Symbol.class));
        when(stacks.getAllSymbols()).thenReturn(symbols);
        assertEquals(symbols, JajaCodeDebug.getAllSymbols(stacks));
    }

    @Test
    void testCaptureMemoryState() {
        List<Stacks.Quad> quads = new ArrayList<>();
        quads.add(new Stacks.Quad("a", 1, "var", Type.ENTIER));
        when(stacks.getStackFromTopToBottom()).thenReturn(quads);
        when(stacks.getCurrentContext()).thenReturn("main");
        when(stacks.getRecursionDepth(anyString())).thenReturn(0);
        JajaCodeDebug.MemorySnapshot snapshot = JajaCodeDebug.captureMemoryState(stacks, 5, "INSTR");
        assertEquals(5, snapshot.programCounter());
        assertEquals("INSTR", snapshot.currentInstruction());
        assertEquals("main", snapshot.currentContext());
        assertEquals(1, snapshot.stackState().size());
    }

    @Test
    void testCaptureHeapState() {
        List<Stacks.Quad> quads = new ArrayList<>();
        fr.ufrst.m1info.gl.groupe7.memoire.ArrayInfo arrayInfo = mock(fr.ufrst.m1info.gl.groupe7.memoire.ArrayInfo.class);
        when(arrayInfo.getBaseAddress()).thenReturn(100);
        when(arrayInfo.getSize()).thenReturn(2);
        quads.add(new Stacks.Quad("tab1", arrayInfo, "tab", Type.ENTIER));
        when(stacks.getStackFromTopToBottom()).thenReturn(quads);
        when(stacks.getArrayValue("tab1", 0)).thenReturn(10);
        when(stacks.getArrayValue("tab1", 1)).thenReturn(20);
        fr.ufrst.m1info.gl.groupe7.memoire.HeapEntry heapEntry = mock(fr.ufrst.m1info.gl.groupe7.memoire.HeapEntry.class);
        when(heapEntry.getSize()).thenReturn(3);
        when(stacks.getHeap()).thenReturn(mock(fr.ufrst.m1info.gl.groupe7.memoire.Heap.class));
        when(stacks.getHeap().getEntryNotFree(100)).thenReturn(heapEntry);
        List<JajaCodeDebug.HeapInfo> heapInfos = JajaCodeDebug.captureHeapState(stacks);
        assertEquals(1, heapInfos.size());
        assertEquals(100, heapInfos.get(0).baseAddress());
        assertEquals(2, heapInfos.get(0).size());
        assertEquals(3, heapInfos.get(0).allocatedSize());
        assertEquals(List.of(10, 20), heapInfos.get(0).elements());
    }

    @Test
    void testGetVariablesInScope() {
        List<Stacks.Quad> quads = new ArrayList<>();
        quads.add(new Stacks.Quad("x@global", 1, "var", Type.ENTIER));
        quads.add(new Stacks.Quad("y@fact@int", 2, "var", Type.ENTIER));
        when(stacks.getStackFromTopToBottom()).thenReturn(quads);
        List<JajaCodeDebug.VariableInfo> globalVars = JajaCodeDebug.getVariablesInScope(stacks, "global");
        assertEquals(1, globalVars.size());
        assertEquals("x@global", globalVars.get(0).identifier());
        List<JajaCodeDebug.VariableInfo> factVars = JajaCodeDebug.getVariablesInScope(stacks, "fact@int");
        assertEquals(1, factVars.size());
        assertEquals("y@fact@int", factVars.get(0).identifier());
    }

    @Test
    void testGetArrayInfo() {
        fr.ufrst.m1info.gl.groupe7.memoire.ArrayInfo arrayInfo = mock(fr.ufrst.m1info.gl.groupe7.memoire.ArrayInfo.class);
        when(arrayInfo.getBaseAddress()).thenReturn(200);
        when(arrayInfo.getSize()).thenReturn(2);
        Stacks.Quad quad = new Stacks.Quad("arr", arrayInfo, "tab", Type.ENTIER);
        when(stacks.findQuad("arr")).thenReturn(quad);
        when(stacks.getArrayValue("arr", 0)).thenReturn(5);
        when(stacks.getArrayValue("arr", 1)).thenReturn(6);
        fr.ufrst.m1info.gl.groupe7.memoire.HeapEntry heapEntry = mock(fr.ufrst.m1info.gl.groupe7.memoire.HeapEntry.class);
        when(heapEntry.getSize()).thenReturn(2);
        when(stacks.getHeap()).thenReturn(mock(fr.ufrst.m1info.gl.groupe7.memoire.Heap.class));
        when(stacks.getHeap().getEntryNotFree(200)).thenReturn(heapEntry);
        JajaCodeDebug.HeapInfo info = JajaCodeDebug.getArrayInfo(stacks, "arr");
        assertNotNull(info);
        assertEquals(200, info.baseAddress());
        assertEquals(List.of(5, 6), info.elements());
    }

    @Test
    void testFormatMemoryState() {
        List<Stacks.Quad> quads = new ArrayList<>();
        quads.add(new Stacks.Quad("a", 1, "var", Type.ENTIER));
        when(stacks.getStackFromTopToBottom()).thenReturn(quads);
        when(stacks.getCurrentContext()).thenReturn("main");
        when(stacks.getRecursionDepth(anyString())).thenReturn(0);
        String formatted = JajaCodeDebug.formatMemoryState(stacks, 0, "NOP");
        assertTrue(formatted.contains("PC: 0"));
        assertTrue(formatted.contains("main"));
        assertTrue(formatted.contains("a = 1"));
    }
}

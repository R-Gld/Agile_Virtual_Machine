package fr.ufrst.m1info.gl.groupe7.memoire;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HeapEntryTest {

    @Test
    void testConstructorAndGetters() {
        Object ref = new Object();
        HeapEntry entry = new HeapEntry("varA", 10, 4, ref, false);

        assertEquals("varA", entry.getId());
        assertEquals(10, entry.getAddress());
        assertEquals(4, entry.getSize());
        assertEquals(ref, entry.getRef());
        assertFalse(entry.isFree());
        assertEquals(1, entry.getRefCount());
    }

    @Test
    void testSetFree() {
        HeapEntry entry = new HeapEntry("x", 20, 2, null, false);

        assertFalse(entry.isFree());
        entry.setFree(true);
        assertTrue(entry.isFree());

        entry.setFree(false);
        assertFalse(entry.isFree());
    }

    @Test
    void testIncrementRef() {
        HeapEntry entry = new HeapEntry("obj", 5, 3, null, false);

        assertEquals(1, entry.getRefCount());
        entry.incrementRef();
        assertEquals(2, entry.getRefCount());
        entry.incrementRef();
        assertEquals(3, entry.getRefCount());
    }

    @Test
    void testDecrementRef() {
        HeapEntry entry = new HeapEntry("obj", 5, 3, null, false);

        entry.incrementRef();
        entry.incrementRef();

        entry.decrementRef();
        assertEquals(2, entry.getRefCount());

        entry.decrementRef();
        assertEquals(1, entry.getRefCount());
    }

    @Test
    void testToString() {
        HeapEntry entry = new HeapEntry("t", 7, 10, null, true);

        String txt = entry.toString();

        assertTrue(txt.contains("HeapEntry"));
        assertTrue(txt.contains("id='t'"));
        assertTrue(txt.contains("addr=7"));
        assertTrue(txt.contains("size=10"));
        assertTrue(txt.contains("free=true"));
    }
}

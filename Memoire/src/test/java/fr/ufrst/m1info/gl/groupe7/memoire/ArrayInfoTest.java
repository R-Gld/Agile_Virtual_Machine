package fr.ufrst.m1info.gl.groupe7.memoire;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Full coverage tests for the ArrayInfo class.
 */
public class ArrayInfoTest {

    @Test
    public void testConstructorInitialState() {
        ArrayInfo info = new ArrayInfo(5);

        assertEquals(5, info.getSize());
        assertEquals(-1, info.getBaseAddress());  // should not be assigned
    }

    @Test
    public void testSetAndGetBaseAddress() {
        ArrayInfo info = new ArrayInfo(3);

        assertEquals(-1, info.getBaseAddress()); // initial

        info.setBaseAddress(42);
        assertEquals(42, info.getBaseAddress());

        info.setBaseAddress(100);
        assertEquals(100, info.getBaseAddress());
    }

    @Test
    public void testBaseAddressNegativeValues() {
        ArrayInfo info = new ArrayInfo(2);

        info.setBaseAddress(-7);
        assertEquals(-7, info.getBaseAddress());
    }

    @Test
    public void testGetSize() {
        assertEquals(1, new ArrayInfo(1).getSize());
        assertEquals(10, new ArrayInfo(10).getSize());
    }

    @Test
    public void testToStringInitial() {
        ArrayInfo info = new ArrayInfo(4);
        String s = info.toString();

        assertTrue(s.contains("size=4"));
        assertTrue(s.contains("baseAddress=-1"));
    }

    @Test
    public void testToStringAfterSet() {
        ArrayInfo info = new ArrayInfo(2);
        info.setBaseAddress(50);

        String s = info.toString();

        assertTrue(s.contains("size=2"));
        assertTrue(s.contains("baseAddress=50"));
    }
}


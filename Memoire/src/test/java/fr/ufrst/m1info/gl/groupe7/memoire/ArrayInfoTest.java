package fr.ufrst.m1info.gl.groupe7.memoire;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Full coverage tests for the ArrayInfo class.
 */
public class ArrayInfoTest {

    @Test
    public void testConstructorAndGetters() {
        ArrayInfo info = new ArrayInfo(10, 5);

        assertEquals(10, info.getAddress(), "Address should match constructor value");
        assertEquals(5, info.getSize(), "Size should match constructor value");
    }

    @Test
    public void testSetAddress() {
        ArrayInfo info = new ArrayInfo(0, 5);

        info.setAddress(42);
        assertEquals(42, info.getAddress(), "setAddress should update the internal address");
    }

    @Test
    public void testSetSize() {
        ArrayInfo info = new ArrayInfo(10, 3);

        info.setSize(99);
        assertEquals(99, info.getSize(), "setSize should update the internal size");
    }

    @Test
    public void testToStringFormat() {
        ArrayInfo info = new ArrayInfo(12, 8);

        String str = info.toString();
        assertTrue(str.contains("address=12"), "toString() must contain the address");
        assertTrue(str.contains("size=8"), "toString() must contain the size");
        assertTrue(str.contains("ArrayInfo{"), "toString() must start with the class name");
    }

    @Test
    public void testModifyAfterCreation() {
        ArrayInfo info = new ArrayInfo(5, 2);

        info.setAddress(100);
        info.setSize(50);

        assertEquals(100, info.getAddress());
        assertEquals(50, info.getSize());
    }

    @Test
    public void testZeroValues() {
        ArrayInfo info = new ArrayInfo(0, 0);

        assertEquals(0, info.getAddress());
        assertEquals(0, info.getSize());

        info.setAddress(0);
        info.setSize(0);

        assertEquals(0, info.getAddress());
        assertEquals(0, info.getSize());
    }

    @Test
    public void testNegativeValues() {
        ArrayInfo info = new ArrayInfo(-10, -5);

        assertEquals(-10, info.getAddress());
        assertEquals(-5, info.getSize());

        info.setAddress(-100);
        info.setSize(-1);

        assertEquals(-100, info.getAddress());
        assertEquals(-1, info.getSize());
    }
    /** Fake heap that always fails allocation */
    private static class FakeFailHeap extends Heap {
        @Override
        public HeapEntry allocate(String id, int size, Object ref) {
            return null; // always fails
        }
    }

    /** Stacks subclass injecting the fake heap */
    private static class StacksWithFailHeap extends Stacks {
        public StacksWithFailHeap() {
            super();
            // replace the internal heap via reflection since heap is private final
            try {
                var field = Stacks.class.getDeclaredField("heap");
                field.setAccessible(true);
                field.set(this, new FakeFailHeap());
            } catch (Exception e) {
                throw new RuntimeException("Reflection injection failed", e);
            }
        }
    }

    @Test
    void testDeclareTabThrowsExceptionOnFailedAllocation() {
        Stacks stacks = new StacksWithFailHeap();

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stacks.declareTab("tabX", 10, "int")
        );

        assertTrue(ex.getMessage().contains("Heap allocation failed"),
                "Exception message should indicate allocation failure");
    }
}


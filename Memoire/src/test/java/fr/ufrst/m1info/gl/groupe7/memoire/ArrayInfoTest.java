package fr.ufrst.m1info.gl.groupe7.memoire;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Full coverage tests for the ArrayInfo class.
 */
public class ArrayInfoTest {
    private ArrayInfo array;

    @BeforeEach
    void setUp() {
        array = new ArrayInfo(5); // tableau de taille 5
    }

    @Test
    void testInitialAddresses() {
        // Tous les indices doivent être initialisés à -1
        for (int i = 0; i < array.getSize(); i++) {
            assertEquals(-1, array.getAddressForIndex(i));
        }
    }

    @Test
    void testSetAndGetAddress() {
        array.setAddressForIndex(0, 10);
        array.setAddressForIndex(3, 42);

        assertEquals(10, array.getAddressForIndex(0));
        assertEquals(42, array.getAddressForIndex(3));

        // Les autres indices restent à -1
        assertEquals(-1, array.getAddressForIndex(1));
        assertEquals(-1, array.getAddressForIndex(2));
        assertEquals(-1, array.getAddressForIndex(4));
    }

    @Test
    void testGetAllAddresses() {
        array.setAddressForIndex(1, 5);
        array.setAddressForIndex(4, 20);

        int[] addresses = array.getAllAddresses();
        assertEquals(5, addresses.length);
        assertEquals(-1, addresses[0]);
        assertEquals(5, addresses[1]);
        assertEquals(-1, addresses[2]);
        assertEquals(-1, addresses[3]);
        assertEquals(20, addresses[4]);
    }

    @Test
    void testGetSize() {
        assertEquals(5, array.getSize());
    }

    @Test
    void testSetAddressOutOfBounds() {
        // Vérifier que les indices invalides déclenchent une exception
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.setAddressForIndex(-1, 10));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.setAddressForIndex(5, 10));
    }

    @Test
    void testGetAddressOutOfBounds() {
        // Vérifier que la lecture d’un indice invalide déclenche une exception
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.getAddressForIndex(-1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.getAddressForIndex(5));
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
}


package fr.ufrst.m1info.gl.groupe7.memoire;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HeapCoverageTest
 * ---------------------------------------------------------------------------
 * Full coverage tests for the Heap class.
 * Covers all methods, branches and edge cases.
 */
public class HeapTest {

    // ------------------------------------------------------------------------
    // Constructor + Initialization
    // ------------------------------------------------------------------------
    @Test
    public void testInitialHeapState() {
        Heap heap = new Heap();
        assertEquals(1, heap.getFreeCount(), "Initial heap should contain one free block (256)");
    }

    // ------------------------------------------------------------------------
    // Allocation
    // ------------------------------------------------------------------------
    @Test
    public void testAllocateValidBlock() {
        Heap heap = new Heap();
        HeapEntry e = heap.allocate("x", 10, null);

        assertNotNull(e, "Allocation should succeed for small block");
        assertEquals("x", e.getId());
        assertEquals(16, e.getSize(), "Requested 10 should round to 16 (next power of 2)");
        assertFalse(e.isFree());
    }

    @Test
    public void testAllocateZeroOrTooLarge() {
        Heap heap = new Heap();
        assertNull(heap.allocate("bad1", 0, null), "Zero size should return null");
        assertNull(heap.allocate("bad2", 9999, null), "Too large allocation should return null");
    }

    @Test
    public void testAllocateUntilMemoryFull() {
        Heap heap = new Heap();

        // Fill memory progressively
        for (int i = 0; i < 256 / 8; i++) {
            heap.allocate("v" + i, 8, null);
        }

        // This should fail (no more free blocks)
        HeapEntry e = heap.allocate("overflow", 8, null);
        assertNull(e, "Should fail when heap is full");
    }

    // ------------------------------------------------------------------------
    // Freeing + merging
    // ------------------------------------------------------------------------
    @Test
    public void testFreeAndMerge() {
        Heap heap = new Heap();

        HeapEntry e1 = heap.allocate("A", 64, null);
        HeapEntry e2 = heap.allocate("B", 64, null);

        int freeBefore = heap.getFreeCount();

        int addr1 = e1.getAddress();
        int addr2 = e2.getAddress();
        heap.printHeap();
        heap.free(e1);
        heap.free(e2);
        heap.printHeap();
        int freeAfter = heap.getFreeCount();

        assertEquals(freeBefore, freeAfter,
                "Merge should keep free block count stable");
    }

    @Test
    public void testFreeNullDoesNothing() {
        Heap heap = new Heap();
        heap.free(null); // Should not crash
        assertEquals(1, heap.getFreeCount(), "No change expected when freeing null");
    }

    // ------------------------------------------------------------------------
    // Internal table operations
    // ------------------------------------------------------------------------
    @Test
    public void testPutAndRemoveManual() {
        Heap heap = new Heap();

        HeapEntry e = new HeapEntry("FREE_BLOCK", 0, 8, null, true);


        // Put and remove manually
        heap.allocate("temp", 4, null);
        heap.free(new HeapEntry("temp", 0, 4, null, false));

        // Test private method indirectly (remove by size)
        HeapEntry e2 = heap.allocate("X", 8, null);
        assertNotNull(e2);
    }

    // ------------------------------------------------------------------------
    // Merge behavior (recursive)
    // ------------------------------------------------------------------------
    @Test
    public void testBuddyMergeRecursive() {
        Heap heap = new Heap();
        HeapEntry a = heap.allocate("a", 128, null);
        HeapEntry b = heap.allocate("b", 128, null);

        heap.free(a);
        heap.free(b);

        // Merging buddies should leave one large free block again
        assertTrue(heap.getFreeCount() > 0);
    }

    // ------------------------------------------------------------------------
    // Hashing + Printing
    // ------------------------------------------------------------------------
    @Test
    public void testHashAndPrint() {
        Heap heap = new Heap();
        heap.printHeap(); // Just ensure it runs
        HeapEntry e = heap.allocate("h", 1, null);
        assertNotNull(e);
        heap.printHeap();
    }

    // ------------------------------------------------------------------------
    // Edge-case coverage (splitting & buddy creation)
    // ------------------------------------------------------------------------
    @Test
    public void testDeepSplitAndAllocation() {
        Heap heap = new Heap();
        // Force deep splits (requiring multiple halvings)
        HeapEntry e = heap.allocate("deep", 3, null);
        assertNotNull(e);
        assertEquals(4, e.getSize(), "3 bytes -> rounded up to 4 cells");
    }
    /**
     * Use reflection to create a private Node and call toString().
     * This covers the Node.toString() override which delegates to entry.toString().
     */
    @Test
    public void testNodeToStringViaReflection() throws Exception {
        Heap heap = new Heap();

        // Create a HeapEntry to attach to the Node
        HeapEntry entry = new HeapEntry("E", 0, 8, null, true);

        // Find the private nested Node class and its constructor
        Class<?> nodeClass = Class.forName("fr.ufrst.m1info.gl.groupe7.memoire.Heap$Node");
        Constructor<?> nodeCtor = nodeClass.getDeclaredConstructor(HeapEntry.class);
        nodeCtor.setAccessible(true);

        // Instantiate a Node and call toString()
        Object nodeInstance = nodeCtor.newInstance(entry);
        Method toStringMethod = nodeClass.getDeclaredMethod("toString");
        toStringMethod.setAccessible(true);

        String s = (String) toStringMethod.invoke(nodeInstance);
        assertEquals(entry.toString(), s, "Node.toString() should delegate to entry.toString()");
    }

    /**
     * Build a bucket where the head node does NOT match the requested size,
     * but the second node does. Calling remove(size) should remove the second node,
     * executing the 'prev != null' branch (prev.next = current.next).
     */
    @Test
    public void testRemoveRemovesNonHeadNode() throws Exception {
        Heap heap = new Heap();

        // reflection helpers
        Class<?> heapClass = heap.getClass();
        Method putMethod = heapClass.getDeclaredMethod("put", Class.forName("fr.ufrst.m1info.gl.groupe7.memoire.Heap$Node"));
        putMethod.setAccessible(true);
        Method removeMethod = heapClass.getDeclaredMethod("remove", int.class);
        removeMethod.setAccessible(true);

        // Node class & constructor
        Class<?> nodeClass = Class.forName("fr.ufrst.m1info.gl.groupe7.memoire.Heap$Node");
        Constructor<?> nodeCtor = nodeClass.getDeclaredConstructor(HeapEntry.class);
        nodeCtor.setAccessible(true);

        // 1) Put a head node with size 16 (will not match remove(8))
        HeapEntry headEntry = new HeapEntry("HEAD", 0, 16, null, true);
        Object headNode = nodeCtor.newInstance(headEntry);
        putMethod.invoke(heap, headNode);

        // 2) Put a second node with size 8 (this should be removed)
        HeapEntry secondEntry = new HeapEntry("SECOND", 8, 8, null, true);
        Object secondNode = nodeCtor.newInstance(secondEntry);
        putMethod.invoke(heap, secondNode);

        // 3) Put a third node with size 32 (just to extend the list)
        HeapEntry thirdEntry = new HeapEntry("THIRD", 24, 32, null, true);
        Object thirdNode = nodeCtor.newInstance(thirdEntry);
        putMethod.invoke(heap, thirdNode);

        // Now call remove(8) — it should traverse past head and remove the second node
        Object removed = removeMethod.invoke(heap, 8);
        assertNotNull(removed, "remove(8) should find the second node and return it");
        // verify removed has the expected HeapEntry by calling its toString
        Method nodeToString = nodeClass.getDeclaredMethod("toString");
        nodeToString.setAccessible(true);
        String removedString = (String) nodeToString.invoke(removed);
        assertTrue(removedString.contains("SECOND"), "The removed node should correspond to the SECOND entry");

        // Also calling remove with a size that doesn't exist should return null (cover return null)
        Object none = removeMethod.invoke(heap, 3); // 3 is not a power-of-two bucket here
        assertNull(none, "remove on a size with no entries should return null");
    }

    /**
     * Test merge(...) behavior when the buddy to be merged is NOT the head of the bucket list.
     * We insert a non-matching head and then a matching buddy, then invoke merge via reflection.
     */
    @Test
    public void testMergeRemovesBuddyNotAtHead() throws Exception {
        Heap heap = new Heap();

        // reflection helpers
        Class<?> heapClass = heap.getClass();
        Method putMethod = heapClass.getDeclaredMethod("put", Class.forName("fr.ufrst.m1info.gl.groupe7.memoire.Heap$Node"));
        putMethod.setAccessible(true);
        Method mergeMethod = heapClass.getDeclaredMethod("merge", Class.forName("fr.ufrst.m1info.gl.groupe7.memoire.Heap$Node"));
        mergeMethod.setAccessible(true);

        Class<?> nodeClass = Class.forName("fr.ufrst.m1info.gl.groupe7.memoire.Heap$Node");
        Constructor<?> nodeCtor = nodeClass.getDeclaredConstructor(HeapEntry.class);
        nodeCtor.setAccessible(true);

        // We'll prepare a block we will attempt to merge, its buddy must exist in the list but not at head.
        // Choose block size 8, address 0 -> buddy index = 0 ^ 8 = 8
        HeapEntry blockToMerge = new HeapEntry("FREE_BLOCK_MAGIC", 0, 8, null, true);
        Object blockNode = nodeCtor.newInstance(blockToMerge);

        // Put a dummy head node with a different size into same bucket to ensure buddy is not head
        HeapEntry dummyHead = new HeapEntry("DUMMY", 0, 16, null, true);
        Object dummyHeadNode = nodeCtor.newInstance(dummyHead);
        putMethod.invoke(heap, dummyHeadNode);

        // Put the buddy node (address 8, size 8) as second element in the same bucket
        HeapEntry buddy = new HeapEntry("BUDDY", 8, 8, null, true);
        Object buddyNode = nodeCtor.newInstance(buddy);
        putMethod.invoke(heap, buddyNode);

        // Now call merge(blockNode) — since buddy is present (not at head), merge should pick it and return a larger block
        Object mergedNode = mergeMethod.invoke(heap, blockNode);
        assertNotNull(mergedNode, "merge should return a node when buddy exists");
        // Check the merged node's entry size (should be 16 after merging)
        Method nodeToString = nodeClass.getDeclaredMethod("toString");
        nodeToString.setAccessible(true);
        String mergedStr = (String) nodeToString.invoke(mergedNode);
        assertTrue(mergedStr.contains("size=16"), "Merged block should have its size doubled to 16");
    }

    @Test
    public void heapEntryTest(){
        HeapEntry entry = new HeapEntry("E", 0, 8, 2, false);
        assertEquals(2,entry.getRef());
        entry.setFree(true);
        assertTrue(entry.isFree());
    }
    private boolean invokeRemoveEntryByAddressAndSize(Heap heap, HeapEntry entry) throws Exception {
        Method m = Heap.class.getDeclaredMethod("removeEntryByAddressAndSize", HeapEntry.class);
        m.setAccessible(true);
        return (boolean) m.invoke(heap, entry);
    }

    private boolean invokeRemoveEntry(Heap heap, HeapEntry entry) throws Exception {
        Method m = Heap.class.getDeclaredMethod("removeEntry", HeapEntry.class);
        m.setAccessible(true);
        return (boolean) m.invoke(heap, entry);
    }

    /* ============================================================
       removeEntryByAddressAndSize
       ============================================================ */

    @Test
    void removeEntryByAddressAndSize_nullEntry_returnsFalse() throws Exception {
        Heap heap = new Heap();
        assertFalse(invokeRemoveEntryByAddressAndSize(heap, null));
    }

    @Test
    void removeEntryByAddressAndSize_removesFreeBlock_prevNull() throws Exception {
        Heap heap = new Heap();

        HeapEntry free = heap.allocate("A", 8, null);
        heap.free(free); // crée un FREE_BLOCK

        HeapEntry toRemove = new HeapEntry("FREE_BLOCK", free.getAddress(), free.getSize(), null, true);

        assertTrue(invokeRemoveEntryByAddressAndSize(heap, toRemove));
        assertNull(heap.getEntry(free.getAddress())); // bien supprimé
    }

    @Test
    void removeEntryByAddressAndSize_removesFreeBlock_prevNotNull() throws Exception {
        Heap heap = new Heap();

        HeapEntry a = heap.allocate("A", 8, null);
        HeapEntry b = heap.allocate("B", 8, null);
        heap.free(a);
        heap.free(b);

        HeapEntry toRemove = new HeapEntry("FREE_BLOCK", b.getAddress(), b.getSize(), null, true);

        assertTrue(invokeRemoveEntryByAddressAndSize(heap, toRemove));
        assertNull(heap.getEntry(b.getAddress()));
    }

    @Test
    void removeEntryByAddressAndSize_allocatedBlock_doesNotDecrementFreeCount() throws Exception {
        Heap heap = new Heap();

        HeapEntry allocated = heap.allocate("X", 8, null);
        int before = heap.getFreeCount();

        HeapEntry fake = new HeapEntry("X", allocated.getAddress(), allocated.getSize(), null, false);

        assertTrue(invokeRemoveEntryByAddressAndSize(heap, fake));
        assertEquals(before, heap.getFreeCount());
    }

    @Test
    void removeEntryByAddressAndSize_notFound_returnsFalse() throws Exception {
        Heap heap = new Heap();

        HeapEntry fake = new HeapEntry("NOPE", 123, 8, null, true);
        assertFalse(invokeRemoveEntryByAddressAndSize(heap, fake));
    }

    /* ============================================================
       removeEntry (by identity)
       ============================================================ */

    @Test
    void removeEntry_nullEntry_returnsFalse() throws Exception {
        Heap heap = new Heap();
        assertFalse(invokeRemoveEntry(heap, null));
    }

    @Test
    void removeEntry_identityMatch_removesEntry() throws Exception {
        Heap heap = new Heap();

        HeapEntry entry = heap.allocate("ID", 8, null);
        assertTrue(invokeRemoveEntry(heap, entry));
        assertNull(heap.getEntry(entry.getAddress()));
    }

    @Test
    void removeEntry_identityMatch_freeBlock_decrementsFreeCount() throws Exception {
        Heap heap = new Heap();

        HeapEntry entry = heap.allocate("Y", 8, null);
        heap.free(entry);

        int before = heap.getFreeCount();
        HeapEntry free = heap.getEntry(entry.getAddress());

        assertTrue(invokeRemoveEntry(heap, free));
        assertEquals(before - 1, heap.getFreeCount());
    }

    @Test
    void removeEntry_notFound_returnsFalse() throws Exception {
        Heap heap = new Heap();

        HeapEntry entry = new HeapEntry("Z", 0, 8, null, true);
        assertFalse(invokeRemoveEntry(heap, entry));
    }

}

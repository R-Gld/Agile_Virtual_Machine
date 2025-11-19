package fr.ufrst.m1info.gl.groupe7.memoire;

/**
 * HeapEntry
 * ---------------------------------------------------------------------------
 * Represents a single memory entry inside the Heap.
 * Stores the logical ID, address, size, and optional reference.
 */
public class HeapEntry {

    /** Logical identifier (e.g. variable or symbol name). */
    private final String id;

    /** Start address in the heap. */
    private final int address;

    /** Size of the allocated block (in cells). */
    private final int size;

    /** Optional reference to the stored object (symbol, instruction, etc.). */
    private final Object ref;

    /** Indicates whether this block is free or allocated. */
    private boolean isFree;

    public HeapEntry(String id, int address, int size, Object ref, boolean isFree) {
        this.id = id;
        this.address = address;
        this.size = size;
        this.ref = ref;
        this.isFree = isFree;
    }

    public String getId() { return id; }
    public int getAddress() { return address; }
    public int getSize() { return size; }
    public Object getRef() { return ref; }
    public boolean isFree() { return isFree; }

    public void setFree(boolean free) { this.isFree = free; }

    @Override
    public String toString() {
        return String.format("HeapEntry{id='%s', addr=%d, size=%d, free=%b}",
                id, address, size, isFree);
    }
}

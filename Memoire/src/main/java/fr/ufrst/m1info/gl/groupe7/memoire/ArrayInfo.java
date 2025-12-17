package fr.ufrst.m1info.gl.groupe7.memoire;

/**
 * ArrayInfo
 * ------------------------------------------------------------
 * Stores metadata for an array allocated inside the Heap.
 * Contains only:
 *   - baseAddress : first cell of the array in the Heap
 *   - size        : number of logical elements in the array
 * <p>
 * Element addresses are computed as:
 *   address = baseAddress + index * cellPerElement
 */
public class ArrayInfo {

    private final int size;          // number of elements
    private int baseAddress;         // starting address in heap

    public ArrayInfo(int size) {
        this.size = size;
        this.baseAddress = -1; // not assigned yet
    }

    public int getSize() {
        return size;
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    @Override
    public String toString() {
        return "ArrayInfo{size=" + size + ", baseAddress=" + baseAddress + "}";
    }
}


package fr.ufrst.m1info.gl.groupe7.memoire;

/**
 * ArrayInfo
 * ------------------------------------------------------------
 * Stores metadata for an array allocated inside the Heap.
 * Contains:
 *   - address : starting address inside the heap
 *   - size    : number of elements in the array
 */
public class ArrayInfo {
    private int address;
    private int size;

    public ArrayInfo(int address, int size) {
        this.address = address;
        this.size = size;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "ArrayInfo{address=" + address + ", size=" + size + "}";
    }
}

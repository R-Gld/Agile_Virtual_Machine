package fr.ufrst.m1info.gl.groupe7.memoire;

import java.util.Arrays;

/**
 * ArrayInfo
 * ------------------------------------------------------------
 * Stores metadata for an array allocated inside the Heap.
 * Contains:
 *   - elementAddresses : of element inside the heap
 *   - size    : number of elements in the array
 */
public class ArrayInfo {
    private int size;
    private int[] elementAddresses; // mapping index -> heap address

    public ArrayInfo(int size) {
        this.size = size;
        this.elementAddresses = new int[size];
        Arrays.fill(this.elementAddresses, -1); // -1 = empty
    }

    public int getAddressForIndex(int index) {
        return elementAddresses[index];
    }

    public void setAddressForIndex(int index, int address) {
        elementAddresses[index] = address;
    }

    public int[] getAllAddresses() {
        return elementAddresses;
    }

    public int getSize() {
        return size;
    }
    @Override
    public String toString() {
        return "ArrayInfo{size=" + size +
                ", addresses=" + Arrays.toString(elementAddresses) +
                '}';
    }
}


package fr.ufrst.m1info.gl.groupe7.memoire;

/**
 * Heap (with HeapEntry)
 * ---------------------------------------------------------------------------
 * Implements a 256-cell buddy-system memory manager.
 * Each block is represented by a Node containing a HeapEntry.
 */
public class Heap {

    private static final int HEAP_SIZE = 256;
    private static final int TABLE_SIZE = 97;

    private final Object[] memory;
    private final Node[] table;
    private int freeCount = 0;

    /** Node representing a free or allocated block inside the hash table. */
    private static class Node {
        HeapEntry entry;  // describes the block
        Node next;

        Node(HeapEntry entry) {
            this.entry = entry;
        }

        @Override
        public String toString() {
            return entry.toString();
        }
    }

    // =========================================================================
    // ========================== CONSTRUCTOR ==================================
    // =========================================================================

    public Heap() {
        this.memory = new Object[HEAP_SIZE];
        this.table = new Node[TABLE_SIZE];
        // Start with one big free block (256)
        HeapEntry root = new HeapEntry("FREE_BLOCK", 0, HEAP_SIZE, null, true);
        put(new Node(root));
    }

    // =========================================================================
    // ============================= HASHING ===================================
    // =========================================================================

    private int hash(int size) {
        return size % TABLE_SIZE;
    }

    // =========================================================================
    // ====================== HASH TABLE MANAGEMENT ============================
    // =========================================================================

    private void put(Node block) {
        int index = hash(block.entry.getSize());
        block.next = table[index];
        table[index] = block;
        freeCount++;
    }

    private Node remove(int size) {
        int index = hash(size);
        Node current = table[index];
        Node prev = null;

        while (current != null) {
            if (current.entry.getSize() == size && current.entry.isFree()) {
                if (prev == null)
                    table[index] = current.next;
                else
                    prev.next = current.next;
                freeCount--;
                current.next = null;
                return current;
            }
            prev = current;
            current = current.next;
        }
        return null;
    }

    private Node findBlock(int minSize) {
        int size = minSize;
        while (size <= HEAP_SIZE) {
            int index = hash(size);
            Node current = table[index];
            while (current != null) {
                if (current.entry.getSize() == size && current.entry.isFree())
                    return current;
                current = current.next;
            }
            size *= 2;
        }
        return null;
    }

    // =========================================================================
    // ============================= ALLOCATE ==================================
    // =========================================================================

    /**
     * Allocates a new HeapEntry for a given ID and size.
     * Splits larger blocks if necessary.
     */
    public HeapEntry allocate(String id, int requestedSize, Object ref) {
        if (requestedSize <= 0 || requestedSize > HEAP_SIZE) return null;

        int blockSize = 1;
        while (blockSize < requestedSize) blockSize *= 2;

        Node block = findBlock(blockSize);
        if (block == null) {
            System.err.println("Error: not enough memory for " + id);
            return null;
        }

        remove(block.entry.getSize());

        // Split recursively
        while (block.entry.getSize() > blockSize) {
            int half = block.entry.getSize() / 2;
            HeapEntry buddyEntry = new HeapEntry("FREE_BLOCK",
                    block.entry.getAddress() + half,
                    half,
                    null,
                    true);
            put(new Node(buddyEntry));
            block.entry = new HeapEntry("FREE_BLOCK",
                    block.entry.getAddress(),
                    half,
                    null,
                    true);
        }

        // Allocate final block
        HeapEntry allocated = new HeapEntry(id, block.entry.getAddress(), blockSize, ref, false);
        System.out.println("→ Allocated " + id + " (" + blockSize + " cells) at address " + allocated.getAddress());
        return allocated;
    }

    // =========================================================================
    // ================================ FREE ===================================
    // =========================================================================

    /**
     * Frees a previously allocated block.
     * Merges buddies recursively if possible.
     */
    public void free(HeapEntry entry) {
        if (entry == null) return;

        HeapEntry freeEntry = new HeapEntry("FREE_BLOCK",
                entry.getAddress(),
                entry.getSize(),
                null,
                true);

        System.out.println("← Freed block [" + entry.getId() + "] addr=" + entry.getAddress() + " size=" + entry.getSize());

        Node merged = merge(new Node(freeEntry));
        put(merged);
    }

    /** Recursive buddy merge. */
    private Node merge(Node block) {
        int buddyIndex = block.entry.getAddress() ^ block.entry.getSize();
        int index = hash(block.entry.getSize());
        Node current = table[index];
        Node prev = null;

        while (current != null) {
            HeapEntry buddy = current.entry;
            if (buddy.getAddress() == buddyIndex && buddy.isFree()) {
                // remove buddy
                if (prev == null) table[index] = current.next;
                else prev.next = current.next;
                freeCount--;

                int mergedIndex = Math.min(block.entry.getAddress(), buddyIndex);
                HeapEntry merged = new HeapEntry("FREE_BLOCK", mergedIndex, block.entry.getSize() * 2, null, true);
                return merge(new Node(merged));
            }
            prev = current;
            current = current.next;
        }
        return block;
    }

    // =========================================================================
    // =============================== DEBUG ===================================
    // =========================================================================

    public void printHeap() {
        System.out.println("\n=== Current Heap State ===");
        for (int i = 0; i < TABLE_SIZE; i++) {
            Node node = table[i];
            if (node != null) {
                System.out.print("Bucket[" + i + "] → ");
                while (node != null) {
                    System.out.print(node.entry + " ");
                    node = node.next;
                }
                System.out.println();
            }
        }
        System.out.println("==========================\n");
    }

    public int getFreeCount() {
        return freeCount;
    }



    public Object read(int address) {
        if (address < 0 || address >= HEAP_SIZE) {
            throw new IndexOutOfBoundsException("Heap.read: address out of bounds: " + address);
        }
        return memory[address];
    }

    /**
     * Write a value into a raw heap cell address.
     * (Performs bounds checking to avoid invalid access.)
     */
    public void write(int address, Object value) {
        if (address < 0 || address >= HEAP_SIZE) {
            throw new IndexOutOfBoundsException("Heap.write: address out of bounds: " + address);
        }
        memory[address] = value;
        // Debug log
        System.out.println(
                "[HEAP WRITE] address=" + address +
                        "  stored_value=" + value +
                        " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")"
        );
    }


    public Object[] getMemory() {
        return memory;
    }



}

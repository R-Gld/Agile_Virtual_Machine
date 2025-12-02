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
        System.out.println("index table allocated: " + index+"block.entry.getSize() :"+block.entry.getSize());
        block.next = table[index];
        table[index] = block;
        if(block.entry.isFree()){
            freeCount++;
        }

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
                if(current.entry.isFree()){
                    freeCount--;
                }

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
        put(new Node(allocated));
        System.out.println("→ Allocated " + id + " (" + blockSize + " cells) at address " + allocated.getAddress());

        return allocated;
    }

    // =========================================================================
    // ================================ FREE ===================================
    // =========================================================================
    /**
     * Removes an allocated HeapEntry (free == false) from the hash table.
     * Matching is done by address + size (and id as tie-breaker) instead of object identity.
     *
     * @param entry a HeapEntry describing the block to remove (may be a different instance)
     * @return true if removed, false if not found
     */
    private boolean removeAllocatedEntry(HeapEntry entry) {
        if (entry == null) return false;
        int newIndex =1;
        while (newIndex < entry.getSize()) {
            newIndex*=2;
        }
        System.out.println(" newIndex "+newIndex);
        int index = hash(newIndex);
        System.out.println(" Index remove allocated "+index);
        Node current = table[index];
        Node prev = null;

        // Preferential pass: scan expected bucket first (fast path)
        while (current != null) {
            HeapEntry h = current.entry;
            if (!h.isFree() && h.getAddress() == entry.getAddress() && h.getSize() == entry.getSize()
                    && (h.getId().equals(entry.getId()) || entry.getId() == null || h.getId() == null)) {
                // found a matching allocated block => remove it
                if (prev == null) table[index] = current.next;
                else prev.next = current.next;
                current.next = null;
                if(current.entry.isFree()){
                    freeCount--;
                }
                 // keep same counting semantics as put()
                System.out.println("→ Removed allocated HeapEntry [" + h.getId() + "] at addr=" + h.getAddress());
                return true;
            }
            prev = current;
            current = current.next;
        }

        // Fallback: sometimes the block may live in another bucket (robustness)
        return removeEntryByAddressAndSize(entry);
    }
    /**
     * Generic remove: remove a node matching the given entry (by address+size+id) anywhere in the table.
     * Useful as a fallback if the bucket computed by hash(size) didn't contain the node.
     */
    private boolean removeEntryByAddressAndSize(HeapEntry entry) {
        if (entry == null) return false;

        for (int i = 0; i < TABLE_SIZE; i++) {
            Node current = table[i];
            Node prev = null;
            while (current != null) {
                HeapEntry h = current.entry;
                if (h.getAddress() == entry.getAddress() && h.getSize() == entry.getSize()
                        && (h.getId().equals(entry.getId()) || entry.getId() == null || h.getId() == null)) {
                    // remove node
                    if (prev == null) table[i] = current.next;
                    else prev.next = current.next;
                    current.next = null;
                    if(current.entry.isFree()){
                        freeCount--;
                    }
                    System.out.println("→ Removed HeapEntry (fallback) [" + h.getId() + "] at addr=" + h.getAddress() + " from bucket " + i);
                    return true;
                }
                prev = current;
                current = current.next;
            }
        }
        // nothing found
        System.out.println("⚠️ removeEntryByAddressAndSize: not found addr=" + entry.getAddress() + " size=" + entry.getSize() + " id=" + entry.getId());
        return false;
    }
    /**
     * Removes a specific HeapEntry from the hash table of allocated/free blocks.
     * Kept for compatibility and debugging.
     */
    private boolean removeEntry(HeapEntry entry) {
        if (entry == null) return false;

        int index = hash(entry.getSize());
        Node current = table[index];
        Node prev = null;

        while (current != null) {
            if (current.entry == entry) {
                // Remove the node from the linked list (object identity)
                if (prev == null) table[index] = current.next;
                else prev.next = current.next;
                current.next = null;
                if(current.entry.isFree()){
                    freeCount--;
                }
                System.out.println("→ Removed (by identity) HeapEntry [" + entry.getId() + "] from bucket " + index);
                return true;
            }
            prev = current;
            current = current.next;
        }

        System.out.println("⚠️ removeEntry (by identity) did not find: " + entry);
        return false;
    }

    /**
     * Free a heap entry and reorganize the memory.
     * - Removes the entry from the allocated list.
     * - Creates a free block.
     * - Merges adjacent free blocks to reduce fragmentation.
     */
    public void free(HeapEntry entry) {
        if (entry == null) return;

        // 1. Supprimer l'entrée de la liste des allocations actives
        boolean removed = this.removeAllocatedEntry(entry);
        if (!removed) {
            System.out.println("⚠️ Entry not found in allocated blocks: " + entry.getId());
            return;
        }

        // 2. Créer un nouveau bloc libre pour cette zone mémoire
        HeapEntry freeEntry = new HeapEntry(
                "FREE_BLOCK",
                entry.getAddress(),
                entry.getSize(),
                null,   // pas de valeur
                true    // indique que c'est libre
        );

        System.out.println("← Freed block [" + entry.getId() + "] addr=" + entry.getAddress() + " size=" + entry.getSize());

        // 3. Ajouter ce bloc libre dans la liste des blocs libres
        Node newFreeNode = new Node(freeEntry);
        Node mergedNode = merge(newFreeNode); // fusionner avec les voisins si possible
        put(mergedNode); // remettre le bloc fusionné dans la structure du heap
    }


    /** Recursive buddy merge. */
    private Node merge(Node block) {
        int size =1;
        while (size<block.entry.getSize()) {
            size*=2;
        }

        int buddyIndex = block.entry.getAddress() ^ size;

        // Chercher le buddy dans tous les nodes de la taille actuelle
        for (int i = 0; i < TABLE_SIZE; i++) {
            Node current = table[i];
            Node prev = null;

            while (current != null) {
                HeapEntry buddy = current.entry;
                if (buddy.isFree() && buddy.getSize() == size && buddy.getAddress() == buddyIndex) {
                    // retirer le buddy
                    if (prev == null) table[i] = current.next;
                    else prev.next = current.next;
                    if(current.entry.isFree()){
                        freeCount--;
                    }

                    // créer le bloc fusionné
                    int mergedAddr = Math.min(block.entry.getAddress(), buddyIndex);
                    HeapEntry merged = new HeapEntry("FREE_BLOCK", mergedAddr, size * 2, null, true);

                    // fusion récursive
                    return merge(new Node(merged));
                }
                prev = current;
                current = current.next;
            }
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
                    System.out.print(node.entry + " \n");
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
    public void releaseReference(HeapEntry entry) {
        if (entry == null) return;

        entry.decrementRef();
        if (entry.getRefCount() == 0) {
            free(entry);
        }
    }
    public HeapEntry getEntryNotFree(int baseAddress) {

        for (int i = 0; i < TABLE_SIZE; i++) {
            Node node = table[i];
            while (node != null) {
                HeapEntry entry = node.entry;

                if (!entry.isFree() && entry.getAddress() == baseAddress) {
                    return entry;
                }

                node = node.next;
            }
        }
        return null;
    }
    public HeapEntry getEntry(int baseAddress) {

        for (int i = 0; i < TABLE_SIZE; i++) {
            Node node = table[i];
            while (node != null) {
                HeapEntry entry = node.entry;

                if ( entry.getAddress() == baseAddress) {
                    return entry;
                }

                node = node.next;
            }
        }
        return null;
    }
    public Object[] getMemory() {
        return memory;
    }




}

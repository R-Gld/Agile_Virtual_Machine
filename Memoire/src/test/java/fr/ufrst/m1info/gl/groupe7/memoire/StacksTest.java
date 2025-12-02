package fr.ufrst.m1info.gl.groupe7.memoire;

import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;


public class StacksTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }
    /*
    Quad Test
     */
    @Test
    void testEqualsSameValues() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", Type.ENTIER);

        assertEquals(q1, q2);
        assertEquals(q1, q1);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testNotEqualsDifferentIdent() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("y", 10, "var", Type.ENTIER);

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentValue() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", 20, "var", Type.ENTIER);

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentObject() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "cst", Type.ENTIER);

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentType() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", Type.BOOLEEN);

        assertNotEquals(q1, q2);
    }

    @Test
    void testEqualsWithNull() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        assertNotEquals(null, q1);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        String otherObject = "Not a Quad";
        assertNotEquals(otherObject, q1);
    }

    @Test
    void testHashCodeConsistency() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        int initialHash = q1.hashCode();
        assertEquals(initialHash, q1.hashCode()); // hashCode must be stable
    }
    @Test
    void testEqualsWithBothValuesNull() {
        Stacks.Quad q1 = new Stacks.Quad("x", null, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", null, "var", Type.ENTIER);
        assertEquals(q1, q2);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testEqualsWithOneValueNull() {
        Stacks.Quad q1 = new Stacks.Quad("x", null, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        assertNotEquals(q1, q2);

        Stacks.Quad q3 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q4 = new Stacks.Quad("x", null, "var", Type.ENTIER);
        assertNotEquals(q3, q4);
    }

    @Test
    void testHashCodeWithNullValue() {
        Stacks.Quad q1 = new Stacks.Quad("x", null, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", null, "var", Type.ENTIER);

        assertEquals(q1.hashCode(), q2.hashCode()); // should not throw NPE
    }

    @Test
    void testEqualsReflexive() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        assertEquals(q1, q1); // reflexivity
    }

    @Test
    void testEqualsSymmetric() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        assertEquals(q1, q2);
        assertEquals(q2, q1); // symmetry
    }

    @Test
    void testEqualsTransitive() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        Stacks.Quad q3 = new Stacks.Quad("x", 10, "var", Type.ENTIER);
        assertEquals(q1, q2);
        assertEquals(q2, q3);
        assertEquals(q1, q3); // transitivity
    }
    @Test
    void testDeclareVar() {
        stacks.declareVar("x", 5, Type.ENTIER);
        assertEquals(5, stacks.getValue("x"));
        assertEquals("var", stacks.getObjectType("x"));
        assertEquals(Type.ENTIER, stacks.getDataType("x"));
    }

    @Test
    void testDeclareCst() {
        stacks.declareCst("PI", 3.14, Type.ENTIER);
        assertEquals(3.14, stacks.getValue("PI"));
        assertEquals("cst", stacks.getObjectType("PI"));
        assertEquals(Type.ENTIER, stacks.getDataType("PI"));
    }



    @Test
    void testDeclareMeth() {
        stacks.declareMeth("f", "body", Type.VOID);
        assertEquals("body", stacks.getValue("f"));
        assertEquals("meth", stacks.getObjectType("f"));
        assertEquals(Type.VOID, stacks.getDataType("f"));
    }

    @Test
    void testAssignValueOnVar() {
        stacks.declareVar("x", 5, Type.ENTIER);
        stacks.AffecterVal("x", 20);
        assertEquals(20, stacks.getValue("x"));
    }

    @Test
    void testAssignValueDoesNotAffectConst() {
        stacks.declareCst("PI", 3.14, Type.ENTIER);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("PI", 10);
        });
        assertTrue(ex.getMessage().contains("ne peut pas être modifiée"));
        assertEquals(3.14, stacks.getValue("PI"), "La constante ne doit pas être modifiée");
    }
    @Test
    void topPil(){
        stacks.declareVar("x", 5, Type.ENTIER);
        assertEquals("x", stacks.getTop().ident);
    }
    @Test
    void topPil2(){
        stacks.declareVar("x", 5, Type.ENTIER);
        stacks.declareCst("PI", 3.14, Type.ENTIER);
        assertEquals("PI", stacks.getTop().ident);
    }
    @Test
    void testGetTopOnEmptyStack() {

        assertNull(stacks.getTop(), "getTop() should return null when the stack is empty");
    }
    @Test
    void testGetStackFromTopToBottom() {

        stacks.declareVar("a", 1, Type.ENTIER);
        stacks.declareVar("b", 2, Type.ENTIER);
        stacks.declareVar("c", 3, Type.ENTIER);
        stacks.declareVar("d", 4, Type.ENTIER);
        stacks.declareVar("e", 5, Type.ENTIER); // top of the stack


        List<Stacks.Quad> result = stacks.getStackFromTopToBottom();


        assertEquals(5, result.size());


        assertEquals("e", result.get(0).ident); // top
        assertEquals("d", result.get(1).ident);
        assertEquals("c", result.get(2).ident);
        assertEquals("b", result.get(3).ident);
        assertEquals("a", result.get(4).ident); // bottom
    }

    @Test
    void testSwap() {
        stacks.declareVar("x", 1, Type.ENTIER);
        stacks.declareVar("y", 2, Type.ENTIER);


        stacks.swap();


        assertEquals(1, stacks.getValue("x"));
        assertEquals(2, stacks.getValue("y"));


        assertEquals("x", (stacks.getTop()).ident);
    }
    @Test
    void testSwapOneElement() {
        stacks.declareVar("x", 1, Type.ENTIER);



        stacks.swap();


        assertEquals(1, stacks.getValue("x"));



        assertEquals("x", (stacks.getTop()).ident);
    }

    @Test
    void testPopNoElement() {


        assertNull(stacks.pop());

    }

    @Test
    void testPop() {
        stacks.declareVar("x", 1, Type.ENTIER);
        stacks.declareVar("y", 2, Type.ENTIER);
        Stacks.Quad quad = stacks.pop();
        assertEquals(new Stacks.Quad("y", 2, "var", Type.ENTIER), quad);
        List<Stacks.Quad> result = stacks.getStackFromTopToBottom();


        assertEquals(1, result.size());
        assertNull(stacks.getValue("y"));
        assertEquals(1, stacks.getValue("x"));
    }

    @Test
    void testGettersReturnNullIfNotFound() {
        assertNull(stacks.getValue("unknown"));
        assertNull(stacks.getObjectType("unknown"));
        assertNull(stacks.getDataType("unknown"));
    }
    /*
    test assignement coverage
     */
    @Test
    void testAssignValueToVariable() {
        stacks.declareVar("x", 1, Type.ENTIER);

        assertTrue(stacks.AffecterVal("x", 10));
        assertEquals(10, stacks.getValue("x"));
    }


        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("PI", 10);
        });
        assertTrue(ex.getMessage().contains("ne peut pas être modifiée"));
        assertEquals(3.14, stacks.getValue("PI"));
    }

    @Test
    void testAssignValueIdentifierNotFound() {
        stacks.declareVar("x", 1, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("y", 10);
        });
        assertTrue(ex.getMessage().contains("pas declaree"));
        assertNull(stacks.getValue("y"));
        assertEquals(1, stacks.getValue("x"));
    }
    @Test
    void testAssignValueCst() {
        stacks.declareVar("x", 1, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("y", 10);
        });
        assertTrue(ex.getMessage().contains("pas declaree"));
        assertNull(stacks.getValue("y"));
        assertEquals(1, stacks.getValue("x"));
    }

    @Test
    void testAssignValueRestoresStackOrder() {
        stacks.declareVar("a", 1, Type.ENTIER);
        stacks.declareVar("b", 2, Type.ENTIER);
        stacks.declareVar("c", 3, Type.ENTIER); // top

        stacks.AffecterVal("a", 10);

        List<Stacks.Quad> stackList = stacks.getStackFromTopToBottom();
        assertEquals("c", stackList.get(0).ident);
        assertEquals("b", stackList.get(1).ident);
        assertEquals("a", stackList.get(2).ident);
        assertEquals(10, stackList.get(2).value);
    }

    @Test
    void testAssignValueOnEmptyStack() {
        // pile vide
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("x", 10);
        });
        assertTrue(ex.getMessage().contains("pas declaree"));
        assertNull(stacks.getValue("x"));
    }
    /*
    Get the data type
    Get the object type
     */
    @Test
    void testGetObjectTypeExistingIdentifier() {
        stacks.declareVar("x", 1, Type.ENTIER);
        stacks.declareCst("PI", 3.14, Type.ENTIER);

        assertEquals("var", stacks.getObjectType("x"));
        assertEquals("cst", stacks.getObjectType("PI"));
    }

    @Test
    void testGetObjectTypeNonExistingIdentifier() {
        stacks.declareVar("x", 1, Type.ENTIER);

        assertNull(stacks.getObjectType("y"));
    }

    @Test
    void testGetObjectTypeEmptyStack() {
        assertNull(stacks.getObjectType("x"));
    }

    @Test
    void testGetDataTypeExistingIdentifier() {
        stacks.declareVar("x", 1, Type.ENTIER);
        stacks.declareVar("flag", true, Type.BOOLEEN);

        assertEquals(Type.ENTIER, stacks.getDataType("x"));
        assertEquals(Type.BOOLEEN, stacks.getDataType("flag"));
    }

    @Test
    void testGetDataTypeNonExistingIdentifier() {
        stacks.declareVar("x", 1, Type.ENTIER);

        assertNull(stacks.getDataType("y"));
    }

    @Test
    void testGetDataTypeEmptyStack() {
        assertNull(stacks.getDataType("x"));
    }
    @Test
    void testPrintStack() {

        stacks.declareVar("x", 1, Type.ENTIER);
        stacks.declareVar("y", 2, Type.ENTIER);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));


        stacks.printStack();


        System.setOut(originalOut);


        String output = outputStream.toString();


        assertTrue(output.contains("<x, 1, var, integer>"));
        assertTrue(output.contains("<y, 2, var, integer>"));
        assertTrue(output.contains("--- Current Stack Content ---"));
        assertTrue(output.contains("------------------------------"));
    }
    @Test
    void testAffecterValBehavior() {
        stacks.declareVar("x", 0, Type.ENTIER);

        stacks.printSymbolTable();
        stacks.printSymbol("li");

        // OK
        assertTrue(stacks.AffecterVal("x", 12));
        stacks.printSymbol("x");
        // Mauvais type
        assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("x", "notAnInt");
        });



        // Variable inexistante
        assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("y", 99);
        });
    }
    @Test
    void testIsTypeCompatible_IntegerVariants() {
        assertTrue(invokeIsTypeCompatible(Type.ENTIER, 10));
        assertTrue(invokeIsTypeCompatible(Type.ENTIER, -5));
        assertTrue(invokeIsTypeCompatible(Type.ENTIER, 0));

        assertFalse(invokeIsTypeCompatible(Type.ENTIER, true));
        assertFalse(invokeIsTypeCompatible(Type.ENTIER, "42"));
        assertFalse(invokeIsTypeCompatible(Type.ENTIER, 3.14));
    }
    @Test
    void test_getStackPosition_no_in_the_stack(){
        assertEquals(-1, stacks.getStackPosition("x"));
    }

    // ===============================
    // Tests pour le type "boolean" / "booleen"
    // ===============================
    @Test
    void testIsTypeCompatible_BooleanVariants() {
        assertTrue(invokeIsTypeCompatible(Type.BOOLEEN, true));
        assertTrue(invokeIsTypeCompatible(Type.BOOLEEN, false));

        assertFalse(invokeIsTypeCompatible(Type.BOOLEEN, "true"));
        assertFalse(invokeIsTypeCompatible(Type.BOOLEEN, 1));
    }

    // ===============================
    // Tests pour le type "string" / "chaine"
    // ===============================
    @Test
    void testIsTypeCompatible_StringVariants() {
        assertTrue(invokeIsTypeCompatible(Type.STRING, "hello"));
        assertTrue(invokeIsTypeCompatible(Type.STRING, "bonjour"));

        assertFalse(invokeIsTypeCompatible(Type.STRING, 123));
        assertFalse(invokeIsTypeCompatible(Type.STRING, false));
    }

    // ===============================
    // Tests pour le type "void"
    // ===============================
    @Test
    void testIsTypeCompatible_VoidType() {
        assertTrue(invokeIsTypeCompatible(Type.VOID, null));
        assertFalse(invokeIsTypeCompatible(Type.VOID, 5));
        assertFalse(invokeIsTypeCompatible(Type.VOID, "text"));
    }

    // ===============================
    // Tests pour les valeurs nulles (acceptées pour tous sauf void explicite)
    // ===============================
    @Test
    void testIsTypeCompatible_NullValue() {
        assertTrue(invokeIsTypeCompatible(Type.ENTIER, null));
        assertTrue(invokeIsTypeCompatible(Type.BOOLEEN, null));
        assertTrue(invokeIsTypeCompatible(Type.STRING, null));
        assertTrue(invokeIsTypeCompatible(Type.STRING, null));
        assertTrue(invokeIsTypeCompatible(Type.ENTIER, null));
    }

    // ===============================
    // Tests pour les types inconnus
    // ===============================
    @Test
    void testIsTypeCompatible_UnknownType() {

        assertFalse(invokeIsTypeCompatible(Type.STRING, 'a'));
        assertTrue(invokeIsTypeCompatible(Type.VOID,null));
    }


    // ===============================
    // Méthode utilitaire pour accéder à la méthode privée via réflexion
    // ===============================
    private boolean invokeIsTypeCompatible(Type type, Object value) {
        try {
            var method = Stacks.class.getDeclaredMethod("isTypeCompatible", Type.class, Object.class);
            method.setAccessible(true);
            return (boolean) method.invoke(stacks, type, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void testPushUpdatesSymbolTable() {
        stacks.declareVar("a", 10, Type.ENTIER);
        stacks.declareVar("b", 20, Type.ENTIER);

        Symbol symbolA = stacks.getSymbolTable().findSymbol("a");
        Symbol symbolB = stacks.getSymbolTable().findSymbol("b");

        assertNotNull(symbolA);
        assertNotNull(symbolB);


        assertEquals(0, symbolA.getAddressStack());
        assertEquals(1, symbolB.getAddressStack());
    }

    @Test
    void testPopUpdatesSymbolTable() {
        stacks.declareVar("a", 10, Type.ENTIER);
        stacks.declareVar("b", 20, Type.ENTIER);

        stacks.pop();

        assertEquals(0, stacks.getSymbolTable().getAddressStack("a"));

    }

    @Test
    void testSwapUpdatesSymbolTable() {
        stacks.declareVar("x", 1, Type.ENTIER);
        stacks.declareVar("y", 2, Type.ENTIER);

        stacks.swap();

        Symbol symbolX = stacks.getSymbolTable().findSymbol("x");
        Symbol symbolY = stacks.getSymbolTable().findSymbol("y");

        // Après swap, "y" est en bas (0) et "x" est en haut (1)
        assertEquals(0, symbolY.getAddressStack());
        assertEquals(1, symbolX.getAddressStack());
    }

    @Test
    void testMultipleOperations() {
        stacks.declareVar("a", 10, Type.ENTIER);
        stacks.declareVar("b", 20, Type.ENTIER);
        stacks.declareVar("c", 30, Type.ENTIER);

        stacks.pop();
        stacks.swap();

        assertEquals(1, stacks.getSymbolTable().getAddressStack("a"));
        assertEquals(0, stacks.getSymbolTable().getAddressStack("b"));

    }

    //part heap test array
    @Test
    public void testDeclareTabAndAccess() {

        // must initialize SymbolTable + Heap inside
        String ident = "T";

        // 1) Declare an array T[5]
        stacks.declareTab(ident, 5, Type.ENTIER);

        // 2) Ensure symbol exists
        assertTrue(stacks.getSymbolTable().contains(ident),
                "Symbol should exist after declareTab");

        // 3) Write values through setArrayValue
        stacks.setArrayValue(ident, 0, 42);
        stacks.setArrayValue(ident, 1, 99);
        stacks.setArrayValue(ident, 4, -12);   // last valid index

        // 4) Read back values
        assertEquals(42, stacks.getArrayValue(ident, 0));
        //assertNull(stacks.getArrayValue("m", 1));
        //assertNull(stacks.getArrayValue(null, 1));
        // 5) Out-of-bounds should return null
        //assertNull(stacks.getArrayValue(ident, -1));
        //assertNull(stacks.getArrayValue(ident, 5));   // size = 5 → last index = 4
        assertEquals(99, stacks.getArrayValue(ident, 1));
        assertEquals(-12, stacks.getArrayValue(ident, 4));


        /*
        // 6) Writing out of bounds should fail
        assertFalse(stacks.setArrayValue(ident, 5, 1234));
        assertFalse(stacks.setArrayValue(ident, -2, 1234));

        // 7) Accessing a non-array should give null / false
        stacks.declareVar("x", 10, "int");
        assertNull(stacks.getArrayValue("x", 0));
        assertFalse(stacks.setArrayValue("x", 0, 99));
        // 8) test set null
        assertFalse(stacks.setArrayValue("v", 0, 99));
        assertFalse(stacks.setArrayValue("x", 0, "abx"));
        assertFalse(stacks.setArrayValue("x", 0, "abx"));
         */

    }
    /*

    @Test
    public void testDeclaretabSetInt() {
        stacks.declareVar("x", 5, "int");
        assertFalse(stacks.setArrayValue("x", 0, 42));
    }
     */
    @Test
    void testGetArrayValueUnknownIdentifier() {
        stacks.declareTab("arr", 5, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getArrayValue("m", 1);
        });

        assertTrue(ex.getMessage().contains("Unknown array"));
    }
    @Test
    void testGetArrayValueNullIdentifier() {
        stacks.declareTab("arr", 5, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getArrayValue(null, 1);
        });

        assertTrue(ex.getMessage().contains("Unknown array"));
    }
    @Test
    public void testDeclareTabAlreadyExists() {
        stacks.declareTab("T", 3, Type.ENTIER);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stacks.declareTab("T", 3, Type.ENTIER)
        );

        assertTrue(ex.getMessage().contains("array already in tab declare"));
    }



    @Test
    public void testDeclareTabUnsupportedType() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stacks.declareTab("X", 5, Type.STRING)  // STRING non supporté
        );

        assertTrue(ex.getMessage().contains("Unsupported array type"));
    }

    @Test
    public void testDeclareTabInvalidSizeZero() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stacks.declareTab("T0", 0, Type.ENTIER)
        );


    }

    @Test
    public void testDeclareTabInvalidSizeNegative() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stacks.declareTab("Tneg", -5, Type.ENTIER)
        );


    }

    @Test
    void testGetArrayValueNegativeIndex() {
        stacks.declareTab("arr", 5, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getArrayValue("arr", -1);
        });

        assertEquals("Index out of bounds: arr[-1]", ex.getMessage());
    }
    @Test
    void testGetArrayValueIndexTooLarge() {
        stacks.declareTab("arr", 5, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getArrayValue("arr", 5); // last valid index = 4
        });

        assertEquals("Index out of bounds: arr[5]", ex.getMessage());
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
    void testMultipleArraysHeapSymbolStack() {


        // 1) Declare three arrays with 5 elements each
        stacks.declareTab("tab1", 5, Type.ENTIER);
        stacks.declareTab("tab2", 5, Type.ENTIER);
        stacks.declareTab("tab3", 5, Type.ENTIER);

        // 2) Check symbol table contains the arrays
        List<Symbol> symbols = stacks.getAllSymbols();
        assertEquals(3, symbols.size(), "Symbol table should contain 3 symbols");

        assertTrue(symbols.stream().anyMatch(s -> s.getName().equals("tab1")));
        assertTrue(symbols.stream().anyMatch(s -> s.getName().equals("tab2")));
        assertTrue(symbols.stream().anyMatch(s -> s.getName().equals("tab3")));



        // 4) Check stack contains the Quads in correct order
        List<Stacks.Quad> stackQuads = stacks.getStackFromTopToBottom();
        assertEquals(3, stackQuads.size(), "Stack should contain 3 Quads");

        assertEquals("tab3", stackQuads.get(0).ident, "Top of stack should be tab3");
        assertEquals("tab2", stackQuads.get(1).ident, "Second in stack should be tab2");
        assertEquals("tab1", stackQuads.get(2).ident, "Bottom of stack should be tab1");

        // 5) Check that each Quad's value is an ArrayInfo and size is correct
        for (Stacks.Quad q : stackQuads) {
            assertInstanceOf(ArrayInfo.class, q.value, "Quad value should be ArrayInfo");
            ArrayInfo info = (ArrayInfo) q.value;
            assertEquals(5, info.getSize(), "ArrayInfo size should be 5");
        }
        stacks.printHeap();
    }
    @Test
    public void testAllDeclarationsAndState() {
        // Create the stack manager


        // 1) Declare variable, constant, method
        stacks.declareVar("x", 42, Type.ENTIER);
        stacks.declareCst("y", 3, Type.ENTIER);
        stacks.declareMeth("myFunc", "body", Type.VOID);

        // 2) Declare two arrays
        stacks.declareTab("arr1", 5, Type.ENTIER);
        stacks.printHeap();
        stacks.declareTab("arr2", 10, Type.ENTIER);
        System.out.println("Heap: with tab vid ");
        stacks.printHeap();

        // 2) give value to some part
        stacks.setArrayValue("arr1",1,21);
        System.out.println("Heap: with tab un elem ");
        stacks.printHeap();
        stacks.setArrayValue("arr1",3,15);
        System.out.println("Heap: with tab deux elem ");
        stacks.printHeap();
        stacks.setArrayValue("arr1",4,17);
        System.out.println("Heap: with tab trois elem ");
        stacks.printHeap();
        stacks.setArrayValue("arr2",2,7);
        System.out.println("Heap: with tab quatre elem new tab ");
        stacks.printHeap();
        stacks.setArrayValue("arr2",4,13);
        System.out.println("Heap: with tab cinq elem ");
        stacks.printHeap();
        stacks.setArrayValue("arr2",7,29);
        System.out.println("Heap: with tab six elem ");
        stacks.printHeap();
        stacks.setArrayValue("arr2",9,145);



        // --- Test 1: Symbol Table ---
        List<Symbol> symbols = stacks.getAllSymbols();
        assertEquals(5, symbols.size(), "There should be 5 symbols in the table");

        assertTrue(stacks.getSymbolTable().contains("x"));
        assertTrue(stacks.getSymbolTable().contains("y"));
        assertTrue(stacks.getSymbolTable().contains("myFunc"));
        assertTrue(stacks.getSymbolTable().contains("arr1"));
        assertTrue(stacks.getSymbolTable().contains("arr2"));

        // --- Test 2: Stack ---
        List<Stacks.Quad> stackContent = stacks.getStackFromTopToBottom();
        assertEquals(5, stackContent.size(), "Stack should contain 5 quads");

        // Check that arrays are stored as ArrayInfo
        Stacks.Quad arr1Quad = stackContent.stream().filter(q -> q.ident.equals("arr1")).findFirst().orElse(null);
        assertNotNull(arr1Quad);
        assertInstanceOf(ArrayInfo.class, arr1Quad.value);

        Stacks.Quad arr2Quad = stackContent.stream().filter(q -> q.ident.equals("arr2")).findFirst().orElse(null);
        assertNotNull(arr2Quad);
        assertInstanceOf(ArrayInfo.class, arr2Quad.value);

        ArrayInfo arr1Info = (ArrayInfo) arr1Quad.value;
        ArrayInfo arr2Info = (ArrayInfo) arr2Quad.value;

        assertEquals(5, arr1Info.getSize());
        assertEquals(10, arr2Info.getSize());

        // --- Test 3: Heap ---
        Heap heap = stacks.getHeap();
        HeapEntry e1 = heap.allocate("tmp1", 5, null);
        assertNotNull(e1, "Heap should have free space");

        // We can also print for debug
        System.out.println("Stack content:");
        stacks.printStack();

        System.out.println("Symbol Table:");
        stacks.printSymbolTable();

        System.out.println("Heap:");
        // Get all HeapEntry objects
        stacks.printHeap();
        Object[] memory = stacks.getAlMemory();
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != null) {
                System.out.println(
                        "[HEAP CELL] address=" + i +
                                "  value=" + memory[i] +
                                " (" + memory[i].getClass().getSimpleName() + ")"
                );
            }
        }
        System.out.println("================================\n");

    }
    @Test
    void testUnknownArray() {
        stacks.declareTab("arr", 5, Type.ENTIER);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.setArrayValue("unknown", 0, 10);
        });
        assertEquals("Unknown array unknown", ex.getMessage());
    }

    @Test
    void testNotAnArray() {
        stacks.declareTab("arr", 5, Type.ENTIER);
        stacks.declareVar("x", 42, Type.ENTIER);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.setArrayValue("x", 0, 10);
        });
        assertEquals("Not an array: x", ex.getMessage());
    }

    @Test
    void testIncompatibleType() {
        stacks.declareTab("arr", 5, Type.ENTIER);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.setArrayValue("arr", 0, true); // boolean in int array
        });
        assertEquals("Element not compatible with type integer and value true", ex.getMessage());
    }

    @Test
    void testIndexOutOfBoundsNegative() {
        stacks.declareTab("arr", 5, Type.ENTIER);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.setArrayValue("arr", -1, 10);
        });
        assertEquals("Index out of bounds", ex.getMessage());
    }

    @Test
    void testIndexOutOfBoundsTooLarge() {
        stacks.declareTab("arr", 5, Type.ENTIER);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.setArrayValue("arr", 10, 10);
        });
        assertEquals("Index out of bounds", ex.getMessage());
    }

    @Test
    void testSuccessfulSet() {
        stacks.declareTab("arr", 5, Type.ENTIER);
        assertDoesNotThrow(() -> stacks.setArrayValue("arr", 2, 99));
        Object val = stacks.getArrayValue("arr", 2);
        assertEquals(99, val);
    }
    @Test
    public void testAllDeclarationsAndStateBoolean() {


        // 1) Declare variable, constant, method
        stacks.declareVar("x", 42, Type.ENTIER);
        stacks.declareCst("y", 3, Type.ENTIER);
        stacks.declareMeth("myFunc", "body", Type.VOID);

        // 2) Declare two arrays
        stacks.declareTab("arr1", 5, Type.ENTIER);
        stacks.printHeap();
        stacks.declareTab("arr2", 10, Type.BOOLEEN);
        System.out.println("Heap: with tab vid ");
        stacks.printHeap();

        // 2) give value to some part
        stacks.setArrayValue("arr1",1,21);
        System.out.println("Heap: with tab un elem ");

        stacks.setArrayValue("arr1",3,15);
        System.out.println("Heap: with tab deux elem ");

        stacks.setArrayValue("arr1",4,17);
        System.out.println("Heap: with tab trois elem ");

        stacks.setArrayValue("arr2",2,true);
        System.out.println("Heap: with tab quatre elem new tab ");

        stacks.setArrayValue("arr2",4,true);
        System.out.println("Heap: with tab cinq elem ");

        stacks.setArrayValue("arr2",7,false);
        System.out.println("Heap: with tab six elem ");

        stacks.setArrayValue("arr2",9,true);
        assertEquals(stacks.getArrayValue("arr1",1),21);
        assertEquals(stacks.getArrayValue("arr1",3),15);
        assertEquals(stacks.getArrayValue("arr2",2),true);
        assertEquals(stacks.getArrayValue("arr2",4),true);
        assertEquals(stacks.getArrayValue("arr2",7),false);



        // --- Test 1: Symbol Table ---
        List<Symbol> symbols = stacks.getAllSymbols();
        assertEquals(5, symbols.size(), "There should be 5 symbols in the table");

        assertTrue(stacks.getSymbolTable().contains("x"));
        assertTrue(stacks.getSymbolTable().contains("y"));
        assertTrue(stacks.getSymbolTable().contains("myFunc"));
        assertTrue(stacks.getSymbolTable().contains("arr1"));
        assertTrue(stacks.getSymbolTable().contains("arr2"));

        // --- Test 2: Stack ---
        List<Stacks.Quad> stackContent = stacks.getStackFromTopToBottom();
        assertEquals(5, stackContent.size(), "Stack should contain 5 quads");

        // Check that arrays are stored as ArrayInfo
        Stacks.Quad arr1Quad = stackContent.stream().filter(q -> q.ident.equals("arr1")).findFirst().orElse(null);
        assertNotNull(arr1Quad);
        assertInstanceOf(ArrayInfo.class, arr1Quad.value);

        Stacks.Quad arr2Quad = stackContent.stream().filter(q -> q.ident.equals("arr2")).findFirst().orElse(null);
        assertNotNull(arr2Quad);
        assertInstanceOf(ArrayInfo.class, arr2Quad.value);

        ArrayInfo arr1Info = (ArrayInfo) arr1Quad.value;
        ArrayInfo arr2Info = (ArrayInfo) arr2Quad.value;

        assertEquals(5, arr1Info.getSize());
        assertEquals(10, arr2Info.getSize());

        // --- Test 3: Heap ---
        Heap heap = stacks.getHeap();
        HeapEntry e1 = heap.allocate("tmp1", 5, null);
        assertNotNull(e1, "Heap should have free space");

        // We can also print for debug
        System.out.println("Stack content:");
        stacks.printStack();

        System.out.println("Symbol Table:");
        stacks.printSymbolTable();

        System.out.println("Heap:");
        // Get all HeapEntry objects
        stacks.printHeap();
        Object[] memory = stacks.getAlMemory();
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != null) {
                System.out.println(
                        "[HEAP CELL] address=" + i +
                                "  value=" + memory[i] +
                                " (" + memory[i].getClass().getSimpleName() + ")"
                );
            }
        }
        System.out.println("================================\n");

    }
    @Test
    public void testFreeArrayElementOK() {


        // Declare array of size 5
        stacks.declareTab("t1", 5, Type.ENTIER);

        // Store value into index 2
        stacks.setArrayValue("t1", 2, 99);



        // Free the element
        stacks.freeArrayElement("t1", 2);

        assertNull(stacks.getArrayValue("t1", 2));
        /*
                assertThrows(RuntimeException.class, () -> {
            stacks.getArrayValue("t1", 2);
        });
         */

    }

    @Test
    public void testFreeArrayElementAlreadyEmpty() {


        stacks.declareTab("t1", 4, Type.ENTIER);



        // Should not throw error
        stacks.freeArrayElement("t1", 1);


    }

    @Test
    public void testFreeArrayElementInvalidIndexLow() {


        stacks.declareTab("t1", 4, Type.ENTIER);

        assertThrows(RuntimeException.class, () -> {
            stacks.freeArrayElement("t1", -1);
        });
    }

    @Test
    public void testFreeArrayElementInvalidIndexHigh() {


        stacks.declareTab("t1", 4, Type.ENTIER);

        assertThrows(RuntimeException.class, () -> {
            stacks.freeArrayElement("t1", 4);  // out of bounds
        });
    }

    @Test
    public void testFreeArrayElementUnknownArray() {


        assertThrows(RuntimeException.class, () -> {
            stacks.freeArrayElement("doesNotExist", 1);
        });
    }

    @Test
    public void testFreeArrayElementNotAnArray() {


        stacks.declareVar("x", 42, Type.ENTIER);

        assertThrows(RuntimeException.class, () -> {
            stacks.freeArrayElement("x", 0);
        });
    }
    @Test
    void testMultipleArraysSetSameIndex() {


        // 1) Declare three arrays with 5 elements each
        stacks.declareTab("tab1", 5, Type.ENTIER);


        // 2) Check symbol table contains the arrays
        List<Symbol> symbols = stacks.getAllSymbols();
        assertEquals(1, symbols.size(), "Symbol table should contain 3 symbols");

        assertTrue(symbols.stream().anyMatch(s -> s.getName().equals("tab1")));

        stacks.setArrayValue("tab1",2,12);
        stacks.setArrayValue("tab1",2,14);
        assertEquals(stacks.getArrayValue("tab1",2),14);

    }

    // ========================================
    // Tests for getValue with Omega
    // ========================================

    @Test
    void testGetValue_OmegaVariableThrowsException() {
        stacks.declareVar("uninitializedVar", Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getValue("uninitializedVar");
        });
        
        assertTrue(ex.getMessage().contains("non initialisée"));
        assertTrue(ex.getMessage().contains("Omega"));
    }

    @Test
    void testGetValue_InitializedVariableReturnsValue() {
        stacks.declareVar("initializedVar", 42, Type.ENTIER);
        assertEquals(42, stacks.getValue("initializedVar"));
    }

    @Test
    void testGetValue_VariableInitializedAfterOmega() {
        stacks.declareVar("laterInitVar", Type.ENTIER);
        stacks.AffecterVal("laterInitVar", 100);
        assertEquals(100, stacks.getValue("laterInitVar"));
    }

    // ========================================
    // Tests for AffecterVal exceptions
    // ========================================

    @Test
    void testAffecterVal_UndeclaredVariableThrowsException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("undeclaredVar", 50);
        });
        
        assertTrue(ex.getMessage().contains("pas declaree"));
    }

    @Test
    void testAffecterVal_ConstantThrowsException() {
        stacks.declareCst("MY_CONST", 100, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("MY_CONST", 200);
        });
        
        assertTrue(ex.getMessage().contains("ne peut pas être modifiée"));
        assertEquals(100, stacks.getValue("MY_CONST"));
    }

    @Test
    void testAffecterVal_ArrayThrowsException() {
        stacks.declareTab("myArray", 5, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("myArray", 10);
        });
        
        assertTrue(ex.getMessage().contains("tableau"));
        assertTrue(ex.getMessage().contains("affectation non permise"));
    }

    @Test
    void testAffecterVal_MethodThrowsException() {
        stacks.declareMeth("myMethod", "body", Type.VOID);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("myMethod", "newBody");
        });
        
        assertTrue(ex.getMessage().contains("méthode"));
        assertTrue(ex.getMessage().contains("affectation non permise"));
    }

    @Test
    void testAffecterVal_TypeMismatchIntToBoolThrowsException() {
        stacks.declareVar("intVar", 10, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("intVar", true);
        });
        
        assertTrue(ex.getMessage().contains("Type mismatch"));
        assertTrue(ex.getMessage().contains("intVar"));
    }

    @Test
    void testAffecterVal_TypeMismatchBoolToIntThrowsException() {
        stacks.declareVar("boolVar", true, Type.BOOLEEN);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("boolVar", 42);
        });
        
        assertTrue(ex.getMessage().contains("Type mismatch"));
        assertTrue(ex.getMessage().contains("boolVar"));
    }

    @Test
    void testAffecterVal_TypeMismatchStringToIntThrowsException() {
        stacks.declareVar("intVar2", 5, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.AffecterVal("intVar2", "not a number");
        });
        
        assertTrue(ex.getMessage().contains("Type mismatch"));
    }

    @Test
    void testAffecterVal_ValidAssignmentReturnsTrue() {
        stacks.declareVar("validVar", 0, Type.ENTIER);
        assertTrue(stacks.AffecterVal("validVar", 99));
        assertEquals(99, stacks.getValue("validVar"));
    }

    @Test
    void testAffecterVal_NullToIntVariableSucceeds() {
        stacks.declareVar("nullableInt", 10, Type.ENTIER);
        assertTrue(stacks.AffecterVal("nullableInt", null));
        assertNull(stacks.getValue("nullableInt"));
    }

    @Test
    void testAffecterVal_NullToBoolVariableSucceeds() {
        stacks.declareVar("nullableBool", false, Type.BOOLEEN);
        assertTrue(stacks.AffecterVal("nullableBool", null));
        assertNull(stacks.getValue("nullableBool"));
    }

    @Test
    public void testFreeTabUnknownArray() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stacks.freeTab("X")
        );

        assertTrue(ex.getMessage().contains("Unknown array"));
    }


    @Test
    public void testFreeTabNotArray() {

        stacks.declareVar("v", 10, Type.ENTIER);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stacks.freeTab("v")
        );

        assertTrue(ex.getMessage().contains("Not an array"));
    }


    @Test
    public void testFreeTabNotAllocated() {

        stacks.declareTab("T", 5, Type.ENTIER);

        // On simule un tableau non alloué
        ArrayInfo info = (ArrayInfo) stacks.getValue("T");
        info.setBaseAddress(-1);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stacks.freeTab("T")
        );

        assertTrue(ex.getMessage().contains("Array not allocated"));
    }
    @Test
    public void testFreeTabAllocated() {
        stacks.declareTab("T", 5, Type.ENTIER);
        stacks.freeTab("T");
    }
    @Test
    public void testDeclareVarWithOmegaShouldThrowOnGet() {
        Stacks stacks = new Stacks();


        stacks.declareVar("x", Type.ENTIER);


        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getValue("x");
        });

        assertTrue(ex.getMessage().contains("not initialized"));
    }

    @Test
    public void testDeclareCstWithOmegaShouldThrowOnGet() {
        Stacks stacks = new Stacks();

        stacks.declareCst("y", Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getValue("y");
        });

        assertTrue(ex.getMessage().contains("not initialized"));
    }

    @Test
    public void testDeclareVarInitializedOk() {
        Stacks stacks = new Stacks();

        stacks.declareVar("a", 10, Type.ENTIER);

        Object val = stacks.getValue("a");

        assertEquals(10, val);
    }

    @Test
    public void testDeclareCstInitializedOk() {
        Stacks stacks = new Stacks();

        stacks.declareCst("b", true, Type.BOOLEEN);

        Object val = stacks.getValue("b");

        assertEquals(true, val);
    }
    @Test
    public void testDeclareOmegaVarThenAssign() {
        Stacks stacks = new Stacks();


        stacks.declareVar("x", Type.ENTIER);


        assertThrows(RuntimeException.class, () -> stacks.getValue("x"));


        stacks.AffecterVal("x",42);

        assertEquals(42, stacks.getValue("x"));
    }




    /*
        @Test
    public void testDeclareOmegaConstThenAssignFails() {
        Stacks stacks = new Stacks();

        stacks.declareCst("k", Type.ENTIER);


        assertThrows(RuntimeException.class, () -> stacks.getValue("k"));


        stacks.declareCst("k", 3, Type.ENTIER);
        assertEquals(3, stacks.getValue("k"));
    }
     */

    @Test
    public void testDoubleDeclarationVarThrows() {
        Stacks stacks = new Stacks();

        stacks.declareVar("x", Type.ENTIER);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareVar("x", Type.ENTIER)
        );

        assertTrue(e.getMessage().contains("var already  declare: "));
    }
    @Test
    public void testDoubleDeclarationConstThrows() {
        Stacks stacks = new Stacks();

        stacks.declareCst("k", Type.ENTIER);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareCst("k", Type.ENTIER)
        );

        assertTrue(e.getMessage().contains("cst already declare: "));
    }
    @Test
    public void testVarThenConstSameNameThrows() {
        Stacks stacks = new Stacks();

        stacks.declareVar("a", Type.ENTIER);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareCst("a", Type.ENTIER)
        );

        assertTrue(e.getMessage().contains("cst already declare: "));
    }
    @Test
    public void testConstThenVarSameNameThrows() {
        Stacks stacks = new Stacks();

        stacks.declareCst("z", Type.ENTIER);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareVar("z", Type.ENTIER)
        );

        assertTrue(e.getMessage().contains("var already  declare: "));
    }
    @Test
    public void testDoubleDeclareMethodThrows() {
        Stacks stacks = new Stacks();
        stacks.declareMeth("maFonction", null, Type.VOID);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareMeth("maFonction", null, Type.VOID)
        );

        assertTrue(e.getMessage().contains("meth already declare"));
    }
    @Test
    public void testDeclareMethodWithExistingVarThrows() {
        Stacks stacks = new Stacks();
        stacks.declareVar("x", Type.ENTIER);
        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareMeth("x", null, Type.VOID)
        );
        assertTrue(e.getMessage().contains("meth already declare"));
    }
    @Test
    public void testAffecterConstOmegaAllowedOnce() {
        Stacks s = new Stacks();
        s.declareCst("c", Type.ENTIER); // c = Ω

        boolean ok = s.AffecterVal("c", 7);

        assertTrue(ok);
        assertEquals(7, s.getValue("c"));
    }
    @Test
    public void testAffecterConstNonOmegaThrows() {
        Stacks s = new Stacks();
        s.declareCst("c", 4, Type.ENTIER); // c = 4, NON OMEGA

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                s.AffecterVal("c", 8)
        );

        assertTrue(e.getMessage().contains("Cannot modify constant"));
    }
    @Test
    public void testAffecterConstOmegaSecondTimeThrows() {
        Stacks s = new Stacks();
        s.declareCst("c", Type.ENTIER); // c = Ω

        assertTrue(s.AffecterVal("c", 5)); // première affectation OK

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                s.AffecterVal("c", 10) // deuxième → interdit
        );

        assertTrue(e.getMessage().contains("Cannot modify constant"));
    }
    @Test
    public void testAffecterInexistant() {
        Stacks s = new Stacks();

        boolean ok = s.AffecterVal("x", 5);

        assertFalse(ok);
    }
    @Test
    public void testAffecterTypeIncompatible() {
        Stacks s = new Stacks();
        s.declareVar("x", Type.ENTIER); // x = Ω

        boolean ok = s.AffecterVal("x", true);

        assertFalse(ok);
    }
    @Test
    public void testAffecterVarOmegaAllowed() {
        Stacks s = new Stacks();
        s.declareVar("x", Type.ENTIER); // x = Ω

        boolean ok = s.AffecterVal("x", 9);

        assertTrue(ok);
        assertEquals(9, s.getValue("x"));
    }
    @Test
    public void testGetArrayLengthNormal() {
        Stacks stacks = new Stacks();
        stacks.declareTab("tab1", 5, Type.ENTIER);

        int size = stacks.getArrayLength("tab1");
        assertEquals(5, size);
    }

    @Test
    public void testGetArrayLengthUnknownArray() {
        Stacks stacks = new Stacks();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getArrayLength("unknown");
        });
        assertEquals("Unknown array unknown", ex.getMessage());
    }

    @Test
    public void testGetArrayLengthNotAnArray() {
        Stacks stacks = new Stacks();
        stacks.declareVar("x", 42, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getArrayLength("x");
        });
        assertEquals("Not an array: x", ex.getMessage());
    }
    @Test
    public void testPopTableFreesMemory() {



        stacks.declareTab("myArray", 3, Type.ENTIER);


        stacks.declareVar("x", 10, Type.ENTIER);
        stacks.declareVar("y", 20, Type.ENTIER);


        Stacks.Quad arrayQuad = stacks.findQuad("myArray");
        assertNotNull(arrayQuad);


        int base = ((ArrayInfo) arrayQuad.value).getBaseAddress();
        assertNotEquals(-1, base);


        stacks.setArrayValue("myArray", 0, 100);
        stacks.setArrayValue("myArray", 1, 200);
        stacks.setArrayValue("myArray", 2, 300);


        assertEquals(100, stacks.getArrayValue("myArray", 0));
        assertEquals(200, stacks.getArrayValue("myArray", 1));
        assertEquals(300, stacks.getArrayValue("myArray", 2));

        stacks.freeArrayElement("myArray", 0);
        stacks.freeArrayElement("myArray", 1);
        stacks.freeArrayElement("myArray", 2);



        stacks.printStack();
        stacks.pop(); // pop "y"
        stacks.printStack();
        stacks.pop(); // pop "x"
        stacks.printStack();
        Stacks.Quad poppedArray = stacks.pop(); // pop "myArray"

        assertNotNull(poppedArray);
        assertEquals("myArray", poppedArray.ident);

        // 6. check if all heap are empty
        assertNull(stacks.getHeap().read(base));
        assertNull(stacks.getHeap().read(base + 1));
        assertNull(stacks.getHeap().read(base + 2));
    }
    /*todo ask teacher about retrait
    @Test
    public void testPopTableFreesMemoryWithoutFreeElements() {



        stacks.declareTab("myArray", 3, Type.ENTIER);


        stacks.declareVar("x", 10, Type.ENTIER);
        stacks.declareVar("y", 20, Type.ENTIER);


        Stacks.Quad arrayQuad = stacks.findQuad("myArray");
        assertNotNull(arrayQuad);


        int base = ((ArrayInfo) arrayQuad.value).getBaseAddress();
        assertNotEquals(-1, base);


        stacks.setArrayValue("myArray", 0, 100);
        stacks.setArrayValue("myArray", 1, 200);
        stacks.setArrayValue("myArray", 2, 300);


        assertEquals(100, stacks.getArrayValue("myArray", 0));
        assertEquals(200, stacks.getArrayValue("myArray", 1));
        assertEquals(300, stacks.getArrayValue("myArray", 2));


        stacks.printStack();
        stacks.pop(); // pop "y"
        stacks.printStack();
        stacks.pop(); // pop "x"
        stacks.printStack();
        Stacks.Quad poppedArray = stacks.pop(); // pop "myArray"

        assertNotNull(poppedArray);
        assertEquals("myArray", poppedArray.ident);

        // 6. check if all heap are empty
        assertNull(stacks.getHeap().read(base));
        assertNull(stacks.getHeap().read(base + 1));
        assertNull(stacks.getHeap().read(base + 2));
    }
     */





}





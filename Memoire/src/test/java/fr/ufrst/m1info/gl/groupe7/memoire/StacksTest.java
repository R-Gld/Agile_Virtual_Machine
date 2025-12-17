package fr.ufrst.m1info.gl.groupe7.memoire;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.lang.reflect.Method;
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
        stacks.affecterVal("x", 20);
        assertEquals(20, stacks.getValue("x"));
    }

    @Test
    void testAssignValueDoesNotAffectConst() {
        stacks.declareCst("PI", 3.14, Type.ENTIER);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("PI", 10);
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
    void testAssignValueIdentifierNotFound() {
        stacks.declareVar("x", 1, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("y", 10);
        });
        assertTrue(ex.getMessage().contains("pas declaree"));
        assertNull(stacks.getValue("y"));
        assertEquals(1, stacks.getValue("x"));
    }
    @Test
    void testAssignValueCst() {
        stacks.declareVar("x", 1, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("y", 10);
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

        stacks.affecterVal("a", 10);

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
            stacks.affecterVal("x", 10);
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

    /* Test commenter car le streamOutput nE FONCTIONNE PAS */
    // @Test
    // void testPrintStack() {

    //     stacks.declareVar("x", 1, Type.ENTIER);
    //     stacks.declareVar("y", 2, Type.ENTIER);


    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     PrintStream originalOut = System.out;
    //     System.setOut(new PrintStream(outputStream));


    //     stacks.printStack();


    //     System.setOut(originalOut);


    //     String output = outputStream.toString();


    //     assertTrue(output.contains("<x, 1, var, integer>"));
    //     assertTrue(output.contains("<y, 2, var, integer>"));
    //     assertTrue(output.contains("--- Current Stack Content ---"));
    //     assertTrue(output.contains("------------------------------"));
    // }
    @Test
    void testAffecterValBehavior() {
        stacks.declareVar("x", 0, Type.ENTIER);

        stacks.printSymbolTable();
        stacks.printSymbol("li");

        // OK
        assertTrue(stacks.affecterVal("x", 12));
        stacks.printSymbol("x");
        // Mauvais type
        assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("x", "notAnInt");
        });



        // Variable inexistante
        assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("y", 99);
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
        //assertNull(stacks.getArrayValue(ident, 5));   // size = 5 -> last index = 4
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


        assertThrows(RuntimeException.class, () -> {
            stacks.freeArrayElement("t1", 2);
        });


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
        
        assertTrue(ex.getMessage().contains( "Variable Omega'uninitializedVar' is declared but not initialized."));

    }

    @Test
    void testGetValue_InitializedVariableReturnsValue() {
        stacks.declareVar("initializedVar", 42, Type.ENTIER);
        assertEquals(42, stacks.getValue("initializedVar"));
    }

    @Test
    void testGetValue_VariableInitializedAfterOmega() {
        stacks.declareVar("laterInitVar", Type.ENTIER);
        stacks.affecterVal("laterInitVar", 100);
        assertEquals(100, stacks.getValue("laterInitVar"));
    }

    // ========================================
    // Tests for AffecterVal exceptions
    // ========================================

    @Test
    void testAffecterVal_UndeclaredVariableThrowsException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("undeclaredVar", 50);
        });
        
        assertTrue(ex.getMessage().contains("pas declaree"));
    }

    @Test
    void testAffecterVal_ConstantThrowsException() {
        stacks.declareCst("MY_CONST", 100, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("MY_CONST", 200);
        });
        
        assertTrue(ex.getMessage().contains("ne peut pas être modifiée"));
        assertEquals(100, stacks.getValue("MY_CONST"));
    }

    @Test
    void testAffecterVal_ArrayThrowsException() {
        stacks.declareTab("myArray", 5, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("myArray", 10);
        });
        
        assertTrue(ex.getMessage().contains("tableau"));
        assertTrue(ex.getMessage().contains("affectation non permise"));
    }

    @Test
    void testAffecterVal_MethodThrowsException() {
        stacks.declareMeth("myMethod", "body", Type.VOID);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("myMethod", "newBody");
        });
        
        assertTrue(ex.getMessage().contains("méthode"));
        assertTrue(ex.getMessage().contains("affectation non permise"));
    }

    @Test
    void testAffecterVal_TypeMismatchIntToBoolThrowsException() {
        stacks.declareVar("intVar", 10, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("intVar", true);
        });
        
        assertTrue(ex.getMessage().contains("Type de variable"));
        assertTrue(ex.getMessage().contains("intVar"));
    }

    @Test
    void testAffecterVal_TypeMismatchBoolToIntThrowsException() {
        stacks.declareVar("boolVar", true, Type.BOOLEEN);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("boolVar", 42);
        });
        
        assertTrue(ex.getMessage().contains("Type de variable"));
        assertTrue(ex.getMessage().contains("boolVar"));
    }

    @Test
    void testAffecterVal_TypeMismatchStringToIntThrowsException() {
        stacks.declareVar("intVar2", 5, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("intVar2", "not a number");
        });
        
        assertTrue(ex.getMessage().contains("Type de variable"));
    }

    @Test
    void testAffecterVal_ValidAssignmentReturnsTrue() {
        stacks.declareVar("validVar", 0, Type.ENTIER);
        assertTrue(stacks.affecterVal("validVar", 99));
        assertEquals(99, stacks.getValue("validVar"));
    }

    @Test
    void testAffecterVal_NullToIntVariableSucceeds() {
        stacks.declareVar("nullableInt", 10, Type.ENTIER);
        assertTrue(stacks.affecterVal("nullableInt", null));
        assertNull(stacks.getValue("nullableInt"));
    }

    @Test
    void testAffecterVal_NullToBoolVariableSucceeds() {
        stacks.declareVar("nullableBool", false, Type.BOOLEEN);
        assertTrue(stacks.affecterVal("nullableBool", null));
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
    public void testDeclareVarWithOmegaShouldThrowOnGet() {



        stacks.declareVar("x", Type.ENTIER);


        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getValue("x");
        });

        assertTrue(ex.getMessage().contains("Variable Omega'x' is declared but not initialized."));
    }
    @Test
    public void testDeclareVarMultiple() {



        stacks.declareVar("x",42 ,Type.ENTIER);


        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.declareVar("x",42 ,Type.ENTIER);
        });

        assertTrue(ex.getMessage().contains("var already  declare: x"));
    }
    @Test
    public void testDeclareCstMultiple() {



        stacks.declareCst("x",42 ,Type.ENTIER);


        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.declareCst("x",42 ,Type.ENTIER);
        });

        assertTrue(ex.getMessage().contains("cst already declared: x"));
    }

    @Test
    public void testDeclareCstWithOmegaShouldThrowOnGet() {


        stacks.declareCst("y", Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getValue("y");
        });
        System.out.println(ex.getMessage());

        assertTrue(ex.getMessage().contains("Variable Omega'y' is declared but not initialized."));
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


        stacks.affecterVal("x",42);

        assertEquals(42, stacks.getValue("x"));
    }


    @Test
    public void testDoubleDeclarationVarThrows() {
        Stacks stacks = new Stacks();

        stacks.declareVar("x", Type.ENTIER);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareVar("x", Type.ENTIER)
        );

        assertTrue(e.getMessage().contains("var already declared: "));
    }
    @Test
    public void testDoubleDeclarationConstThrows() {
        Stacks stacks = new Stacks();

        stacks.declareCst("k", Type.ENTIER);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareCst("k", Type.ENTIER)
        );
        assertTrue(e.getMessage().contains("cst already declared: "));
    }
    @Test
    public void testVarThenConstSameNameThrows() {
        Stacks stacks = new Stacks();

        stacks.declareVar("a", Type.ENTIER);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareCst("a", Type.ENTIER)
        );

        assertTrue(e.getMessage().contains("cst already declared: "));
    }
    @Test
    public void testConstThenVarSameNameThrows() {
        Stacks stacks = new Stacks();

        stacks.declareCst("z", Type.ENTIER);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareVar("z", Type.ENTIER)
        );
        assertTrue(e.getMessage().contains("var already declared: "));
    }
    @Test
    public void testDoubleDeclareMethodThrows() {
        Stacks stacks = new Stacks();
        stacks.declareMeth("maFonction", null, Type.VOID);

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareMeth("maFonction", null, Type.VOID)
        );
        System.out.println("e.getMessage() = " + e.getMessage());
        assertTrue(e.getMessage().contains("meth already declared"));
    }
    @Test
    public void testDeclareMethodWithExistingVarThrows() {
        Stacks stacks = new Stacks();
        stacks.declareVar("x", Type.ENTIER);
        RuntimeException e = assertThrows(RuntimeException.class, () ->
                stacks.declareMeth("x", null, Type.VOID)
        );
        assertTrue(e.getMessage().contains("meth already declared"));
    }
    @Test
    public void testAffecterConstOmegaAllowedOnce() {
        Stacks s = new Stacks();
        s.declareCst("c", Type.ENTIER); // c = Ω

        boolean ok = s.affecterVal("c", 7);

        assertTrue(ok);
        assertEquals(7, s.getValue("c"));
    }
    @Test
    public void testAffecterConstNonOmegaThrows() {
        Stacks s = new Stacks();
        s.declareCst("c", 4, Type.ENTIER); // c = 4, NON OMEGA

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                s.affecterVal("c", 8)
        );

        assertTrue(e.getMessage().contains("ne peut pas être modifiée"));
    }
    @Test
    public void testAffecterConstOmegaSecondTimeThrows() {
        Stacks s = new Stacks();
        s.declareCst("c", Type.ENTIER); // c = Ω

        assertTrue(s.affecterVal("c", 5)); // première affectation OK

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                s.affecterVal("c", 10) // deuxième -> interdit
        );

        assertTrue(e.getMessage().contains("ne peut pas être modifiée"));
    }
    @Test
    public void testAffecterInexistant() {
        Stacks s = new Stacks();

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                s.affecterVal("x", 5)
        );

        assertTrue(e.getMessage().contains("pas declaree"));
    }
    @Test
    public void testAffecterTypeIncompatible() {
        Stacks s = new Stacks();
        s.declareVar("x", Type.ENTIER); // x = Ω

        RuntimeException e = assertThrows(RuntimeException.class, () ->
                s.affecterVal("x", true)
        );

        assertTrue(e.getMessage().contains("Type de variable"));
    }
    @Test
    public void testAffecterVarOmegaAllowed() {
        Stacks s = new Stacks();
        s.declareVar("x", Type.ENTIER); // x = Ω

        boolean ok = s.affecterVal("x", 9);

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
    @Test
    public void testRemoveVar() {
        Stacks stacks = new Stacks();

        stacks.declareVar("x", 10, Type.ENTIER);
        assertNotNull(stacks.getValue("x"));

        stacks.retirerDecl("x");

        assertNull(stacks.getValue("x"));
        assertNull(stacks.getObjectType("x"));
        assertNull(stacks.getDataType("x"));
    }

    @Test
    public void testRemoveConst() {
        Stacks stacks = new Stacks();

        stacks.declareCst("C", 42, Type.ENTIER);
        assertEquals(42, stacks.getValue("C"));

        stacks.retirerDecl("C");

        assertNull(stacks.getValue("C"));
        assertNull(stacks.getObjectType("C"));
    }
    @Test
    public void testRemoveArray() {
        Stacks stacks = new Stacks();

        stacks.declareTab("tabA", 5, Type.ENTIER);

        ArrayInfo returned = (ArrayInfo) stacks.getValue("tabA");

        assertNotNull(returned);
        assertEquals(5, returned.getSize());
        assertEquals(0, returned.getBaseAddress());

        stacks.retirerDecl("tabA");

        assertNull(stacks.getValue("tabA"));
        assertNull(stacks.getObjectType("tabA"));
    }

    @Test
    public void testRemoveMethod() {
        Stacks stacks = new Stacks();

        stacks.declareMeth("myFunc", "BODY", Type.VOID);
        assertEquals("BODY", stacks.getValue("myFunc"));

        stacks.retirerDecl("myFunc");

        assertNull(stacks.getValue("myFunc"));
        assertNull(stacks.getObjectType("myFunc"));
    }

    @Test
    public void testRemoveNonexistentSymbol() {
        Stacks stacks = new Stacks();

        stacks.declareVar("x", 10, Type.ENTIER);

        // Should not crash
        stacks.retirerDecl("DOES_NOT_EXIST");

        // Should not affect existing variable
        assertEquals(10, stacks.getValue("x"));
    }

    @Test
    public void testRemoveMiddleOfStack() {
        Stacks stacks = new Stacks();

        stacks.declareVar("a", 1, Type.ENTIER);
        stacks.declareVar("b", 2, Type.ENTIER);
        stacks.declareVar("c", 3, Type.ENTIER);

        // Ensure all exist
        assertEquals(1, stacks.getValue("a"));
        assertEquals(2, stacks.getValue("b"));
        assertEquals(3, stacks.getValue("c"));

        // Remove item in the MIDDLE
        stacks.retirerDecl("b");

        assertNull(stacks.getValue("b"));

        // Other values should still be present
        assertEquals(1, stacks.getValue("a"));
        assertEquals(3, stacks.getValue("c"));
    }
    @Test
    public void testRetirerDeclArrayRefcountDecrementButNotFree() {


        stacks.declareTab("A", 3, Type.ENTIER);

        ArrayInfo info = (ArrayInfo) stacks.getValue("A");
        System.out.println(info);
        int base = info.getBaseAddress();
        stacks.printHeap();
        assertNotNull(stacks.getHeap().getEntryNotFree(base),
                "HeapEntry should exist right after array allocation");
        // Simuler 2 références
        stacks.getHeap().getEntryNotFree(base).incrementRef();

        assertEquals(2, stacks.getHeap().getEntryNotFree(base).getRefCount());

        stacks.retirerDecl("A");

        // refCount doit passer à 1 et pas de libération
        assertEquals(1, stacks.getHeap().getEntryNotFree(base).getRefCount());

        // L'entrée doit exister (donc pas freeTab)
        assertNotNull(stacks.getHeap().getEntryNotFree(base));

        // Le symbole doit être supprimé
        assertNull(stacks.getValue("A"));
    }
    @Test
    public void testRetirerDeclArrayFreedWhenRefcountReachedZero() {


        stacks.declareTab("A", 4, Type.ENTIER);

        ArrayInfo info = (ArrayInfo) stacks.getValue("A");
        int base = info.getBaseAddress();

        // refCount initial = 1 (déclaration seule)
        assertEquals(1, stacks.getHeap().getEntryNotFree(base).getRefCount());
        stacks.printHeap();
        stacks.retirerDecl("A");
        stacks.printHeap();
        // freeTab doit avoir supprimé l’entrée
        assertNull(stacks.getHeap().getEntryNotFree(base));

        // Et le symbole doit être retiré
        assertNull(stacks.getValue("A"));
    }
    @Test
    public void testRetirerDeclMultiple() {
        Stacks stacks = new Stacks();

        stacks.declareVar("x", 1, Type.ENTIER);
        stacks.declareTab("A", 2, Type.ENTIER);
        stacks.declareVar("y", 2, Type.ENTIER);

        assertNotNull(stacks.getValue("x"));
        assertNotNull(stacks.getValue("A"));
        assertNotNull(stacks.getValue("y"));

        stacks.retirerDecl("A");

        assertNull(stacks.getValue("A"));
        assertNotNull(stacks.getValue("x"));
        assertNotNull(stacks.getValue("y"));

        stacks.retirerDecl("y");
        assertNull(stacks.getValue("y"));
    }
    @Test
    public void testRetirerDeclUpdatesPositions() {
        Stacks stacks = new Stacks();

        stacks.declareVar("a", 1, Type.ENTIER);
        stacks.declareCst("b", 2, Type.ENTIER);
        stacks.declareMeth("c", 3, Type.ENTIER);

        stacks.retirerDecl("b");

        // Vérifie positions mises à jour
        assertEquals(0, stacks.getSymbolTable().findSymbol("a").getAddressStack());
        assertEquals(1, stacks.getSymbolTable().findSymbol("c").getAddressStack());
    }
    // --------------------------
    // 1. Basic affecterTab test
    // --------------------------
    @Test
    public void testAffecterTabNormal() {
        // Declare two arrays
        stacks.declareTab("A", 3, Type.ENTIER);
        stacks.declareTab("B", 5, Type.ENTIER);

        ArrayInfo infoA = (ArrayInfo) stacks.getValue("A");
        ArrayInfo infoB = (ArrayInfo) stacks.getValue("B");



        // Perform the array assignment
        stacks.affecterTab("B", "A");

        // B should now point to A's ArrayInfo
        ArrayInfo newBInfo = (ArrayInfo) stacks.getValue("B");
        assertEquals(infoA, newBInfo);

        // RefCount of A should have incremented
        assertEquals(2, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());
    }

    // -------------------------------------------
    // 2. affecterTab with unknown identifier
    // -------------------------------------------
    @Test
    public void testAffecterTabUnknownIdentifier() {
        stacks.declareTab("A", 3, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterTab("B", "A"); // B does not exist
        });
        assertTrue(ex.getMessage().contains("Unknown array identifier"));
    }

    // -------------------------------------------
    // 3. affecterTab with non-array object
    // -------------------------------------------
    @Test
    public void testAffecterTabNonArray() {
        stacks.declareVar("X", 10, Type.ENTIER);
        stacks.declareTab("A", 3, Type.ENTIER);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterTab("A", "X"); // X is not an array
        });
        assertTrue(ex.getMessage().contains("Array reference assignment allowed only"));
    }

    // --------------------------
    // 4. retirerReference normal decrement
    // --------------------------
    @Test
    public void testRetirerReferenceDecrement() {
        stacks.declareTab("A", 4, Type.ENTIER);
        ArrayInfo info = (ArrayInfo) stacks.getValue("A");

        // simulate extra references
        stacks.getHeap().getEntry(info.getBaseAddress()).incrementRef();

        stacks.retirerReference(info);

        // Ref count should decrement by 1
        assertEquals(1, stacks.getHeap().getEntry(info.getBaseAddress()).getRefCount());
    }

    // --------------------------
    // 5. retirerReference frees block
    // --------------------------
    @Test
    public void testRetirerReferenceFreeBlock() {
        stacks.declareTab("A", 4, Type.ENTIER);
        ArrayInfo info = (ArrayInfo) stacks.getValue("A");

        // initial refCount = 1
        stacks.retirerReference(info);

        // The heap entry should be freed, so getEntry returns null
        assertNull(stacks.getHeap().getEntryNotFree(info.getBaseAddress()));
    }

    // --------------------------
    // 6. ajouterReference increments
    // --------------------------
    @Test
    public void testAjouterReference() {
        stacks.declareTab("A", 4, Type.ENTIER);
        ArrayInfo info = (ArrayInfo) stacks.getValue("A");

        // refCount should start at 1
        stacks.ajouterReference(info);
        assertEquals(2, stacks.getHeap().getEntry(info.getBaseAddress()).getRefCount());
    }

    // --------------------------
    // 7. affecterTab overwrites old reference
    // --------------------------
    @Test
    public void testAffecterTabOverwritesOldReference() {
        stacks.declareTab("A", 3, Type.ENTIER);
        stacks.declareTab("B", 3, Type.ENTIER);

        ArrayInfo infoA = (ArrayInfo) stacks.getValue("A");
        ArrayInfo infoB = (ArrayInfo) stacks.getValue("B");

        // B initially has one reference, should be released when we assign A to B
        stacks.affecterTab("B", "A");

        // Old B block should be freed (refCount = 0)
        assertNull(stacks.getHeap().getEntryNotFree(infoB.getBaseAddress()));

        // New B points to A
        ArrayInfo newBInfo = (ArrayInfo) stacks.getValue("B");
        assertEquals(infoA, newBInfo);
        assertEquals(2, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());


    }
    @Test
    public void testAffecterTabOverwritesOldReferenceFreeTab() {
        stacks.declareTab("A", 3, Type.ENTIER);
        stacks.declareTab("B", 3, Type.ENTIER);

        ArrayInfo infoA = (ArrayInfo) stacks.getValue("A");
        ArrayInfo infoB = (ArrayInfo) stacks.getValue("B");

        // B initially has one reference, should be released when we assign A to B
        stacks.affecterTab("B", "A");

        // Old B block should be freed (refCount = 0)
        assertNull(stacks.getHeap().getEntryNotFree(infoB.getBaseAddress()));

        // New B points to A
        ArrayInfo newBInfo = (ArrayInfo) stacks.getValue("B");
        assertEquals(infoA, newBInfo);
        assertEquals(2, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());
        stacks.freeTab("A");
        assertEquals(1, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());
        stacks.freeTab("B");

    }
    @Test
    public void testAffecterTabOverwritesOldReferenceFreeTabOrder() {
        stacks.declareTab("A", 3, Type.ENTIER);
        stacks.declareTab("B", 3, Type.ENTIER);

        ArrayInfo infoA = (ArrayInfo) stacks.getValue("A");
        ArrayInfo infoB = (ArrayInfo) stacks.getValue("B");

        // B initially has one reference, should be released when we assign A to B
        stacks.affecterTab("B", "A");

        // Old B block should be freed (refCount = 0)
        assertNull(stacks.getHeap().getEntryNotFree(infoB.getBaseAddress()));

        // New B points to A
        ArrayInfo newBInfo = (ArrayInfo) stacks.getValue("B");
        assertEquals(infoA, newBInfo);
        assertEquals(2, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());
        stacks.freeTab("B");
        assertEquals(1, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());
        stacks.freeTab("A");

    }
    @Test
    public void testAffecterTabOverwritesOldReferenceThreeFree() {
        stacks.declareTab("A", 3, Type.ENTIER);
        stacks.declareTab("B", 3, Type.ENTIER);
        stacks.declareTab("C", 3, Type.ENTIER);
        ArrayInfo infoA = (ArrayInfo) stacks.getValue("A");
        ArrayInfo infoB = (ArrayInfo) stacks.getValue("B");

        // B initially has one reference, should be released when we assign A to B
        stacks.printHeap();
        stacks.affecterTab("B", "A");
        stacks.printHeap();

        // Old B block should be freed (refCount = 0)
        assertNull(stacks.getHeap().getEntryNotFree(infoB.getBaseAddress()));

        // New B points to A
        ArrayInfo newBInfo = (ArrayInfo) stacks.getValue("B");
        assertEquals(infoA, newBInfo);
        assertEquals(2, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());
        stacks.affecterTab("C", "A");
        ArrayInfo infoC = (ArrayInfo) stacks.getValue("C");
        stacks.printHeap();
        assertEquals(3, stacks.getHeap().getEntry(infoC.getBaseAddress()).getRefCount());
        stacks.freeTab("B");
        assertEquals(2, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());
        stacks.freeTab("A");
        assertEquals(1, stacks.getHeap().getEntry(infoA.getBaseAddress()).getRefCount());
    }
    @Test
    void testNotEnoughMemoryLogIsWritten() {
        // Arrange
        Logger logger = (Logger) LoggerFactory.getLogger(Heap.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        Heap heap = new Heap();

        // Act
        heap.allocate("X", 999, null);

        // Assert
        boolean logFound = appender.list.stream()
                .anyMatch(event ->
                        event.getLevel().toString().equals("DEBUG")
                                && event.getFormattedMessage()
                                .contains("overflow more than heap_size X")
                );

        assertTrue(logFound, "Expected DEBUG log about insufficient memory");

        logger.detachAppender(appender);
    }

    @Test
    public void testAffecterTabOveflow() {

        // Arrange

        stacks.declareTab("A", 3, Type.ENTIER);
        stacks.declareTab("B", 3, Type.ENTIER);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.declareTab("C", 251, Type.ENTIER);
        });
        assertTrue(ex.getMessage().contains("Heap allocation failed for array C"));


    }


    // ============================================================
    // CONTEXT MANAGEMENT TESTS
    // ============================================================

    @Test
    void testPushContext_firstCall_setsContextToMethodName() {
        stacks.pushContext("factorial");
        
        assertEquals("factorial", stacks.getCurrentContext());
        assertTrue(stacks.isInMethodContext());
    }

    @Test
    void testPushContext_recursiveCall_appendsDepthNumber() {
        stacks.pushContext("factorial");  // factorial
        stacks.pushContext("factorial");  // factorial1
        stacks.pushContext("factorial");  // factorial2
        
        assertEquals("factorial2", stacks.getCurrentContext());
    }

    @Test
    void testPopContext_removesCurrentContext() {
        stacks.pushContext("myMethod");
        assertTrue(stacks.isInMethodContext());
        
        stacks.popContext("myMethod");
        
        assertFalse(stacks.isInMethodContext());
        assertNull(stacks.getCurrentContext());
    }

    @Test
    void testPopContext_withRecursion_decrementsDepth() {
        stacks.pushContext("factorial");  // factorial
        stacks.pushContext("factorial");  // factorial1
        stacks.pushContext("factorial");  // factorial2
        
        stacks.popContext("factorial");
        assertEquals("factorial1", stacks.getCurrentContext());
        
        stacks.popContext("factorial");
        assertEquals("factorial", stacks.getCurrentContext());
        
        stacks.popContext("factorial");
        assertFalse(stacks.isInMethodContext());
    }

    @Test
    void testPopContext_onEmptyStack_doesNotThrow() {
        assertDoesNotThrow(() -> stacks.popContext("anyMethod"));
    }

    @Test
    void testGetCurrentContext_whenEmpty_returnsNull() {
        assertNull(stacks.getCurrentContext());
    }

    @Test
    void testGetCurrentMethodName_extractsBaseName() {
        stacks.pushContext("factorial");
        stacks.pushContext("factorial");
        stacks.pushContext("factorial");
        
        // Context is "factorial2" but method name is "factorial"
        assertEquals("factorial", stacks.getCurrentMethodName());
    }

    @Test
    void testGetCurrentMethodName_whenEmpty_returnsNull() {
        assertNull(stacks.getCurrentMethodName());
    }

    @Test
    void testGetRecursionDepth_returnsCorrectDepth() {
        assertEquals(0, stacks.getRecursionDepth("factorial"));
        
        stacks.pushContext("factorial");
        assertEquals(1, stacks.getRecursionDepth("factorial"));
        
        stacks.pushContext("factorial");
        assertEquals(2, stacks.getRecursionDepth("factorial"));
        
        stacks.popContext("factorial");
        assertEquals(1, stacks.getRecursionDepth("factorial"));
    }

    @Test
    void testIsInMethodContext_returnsCorrectValue() {
        assertFalse(stacks.isInMethodContext());
        
        stacks.pushContext("test");
        assertTrue(stacks.isInMethodContext());
        
        stacks.popContext("test");
        assertFalse(stacks.isInMethodContext());
    }

    @Test
    void testGetScopedName_inContext_returnsScoped() {
        stacks.pushContext("myMethod");
        
        assertEquals("x@myMethod", stacks.getScopedName("x"));
    }

    @Test
    void testGetScopedName_notInContext_returnsOriginal() {
        assertEquals("x", stacks.getScopedName("x"));
    }

    @Test
    void testGetScopedName_withRecursion_includesDepth() {
        stacks.pushContext("factorial");
        assertEquals("n@factorial", stacks.getScopedName("n"));
        
        stacks.pushContext("factorial");
        assertEquals("n@factorial1", stacks.getScopedName("n"));
        
        stacks.pushContext("factorial");
        assertEquals("n@factorial2", stacks.getScopedName("n"));
    }

    @Test
    void testGetScopedNameForMethod_withoutRecursion() {
        assertEquals("x@myMethod", stacks.getScopedNameForMethod("x", "myMethod"));
    }

    @Test
    void testGetScopedNameForMethod_withRecursion() {
        stacks.pushContext("factorial");  // depth becomes 1
        
        // For next call, depth would be 1, so suffix is "factorial1"
        assertEquals("n@factorial1", stacks.getScopedNameForMethod("n", "factorial"));
    }

    @Test
    void testSetCurrentContext_deprecated_pushesContext() {
        stacks.setCurrentContext("oldMethod");
        
        assertTrue(stacks.isInMethodContext());
        assertEquals("oldMethod", stacks.getCurrentContext());
    }

    @Test
    void testSetCurrentContext_withNull_popsContext() {
        stacks.pushContext("method1");
        assertTrue(stacks.isInMethodContext());
        
        stacks.setCurrentContext(null);
        
        assertFalse(stacks.isInMethodContext());
    }

    @Test
    void testNestedDifferentMethods_contextStackWorksCorrectly() {
        stacks.pushContext("outer");
        assertEquals("outer", stacks.getCurrentContext());
        
        stacks.pushContext("inner");
        assertEquals("inner", stacks.getCurrentContext());
        
        stacks.popContext("inner");
        assertEquals("outer", stacks.getCurrentContext());
        
        stacks.popContext("outer");
        assertNull(stacks.getCurrentContext());
    }

    // ============================================================
    // CONTEXT RESTORER TESTS
    // ============================================================

    @Test
    void testCreateContextRestorer_returnsRestorer() {
        Stacks.ContextRestorer restorer = stacks.createContextRestorer("myMethod");
        
        assertNotNull(restorer);
        assertEquals("myMethod", restorer.getMethodName());
    }

    @Test
    void testContextRestorer_restore_popsContext() {
        stacks.pushContext("myMethod");
        assertTrue(stacks.isInMethodContext());
        
        Stacks.ContextRestorer restorer = stacks.createContextRestorer("myMethod");
        restorer.restore();
        
        assertFalse(stacks.isInMethodContext());
    }

    @Test
    void testContextRestorer_withRecursion() {
        stacks.pushContext("factorial");
        stacks.pushContext("factorial");
        assertEquals("factorial1", stacks.getCurrentContext());
        
        Stacks.ContextRestorer restorer = stacks.createContextRestorer("factorial");
        restorer.restore();
        
        assertEquals("factorial", stacks.getCurrentContext());
    }

    // ============================================================
    // VARIABLE CLASSE TESTS
    // ============================================================

    @Test
    void testSetVariableClasse_setsValue() {
        stacks.setVariableClasse("MaClasse");
        
        assertEquals("MaClasse", stacks.getVariableClasse());
    }

    @Test
    void testGetVariableClasse_initiallyNull() {
        assertNull(stacks.getVariableClasse());
    }

    @Test
    void testVariableClasse_canBeChanged() {
        stacks.setVariableClasse("Classe1");
        assertEquals("Classe1", stacks.getVariableClasse());
        
        stacks.setVariableClasse("Classe2");
        assertEquals("Classe2", stacks.getVariableClasse());
    }

    @Test
    void testVariableClasse_canBeSetToNull() {
        stacks.setVariableClasse("MaClasse");
        assertEquals("MaClasse", stacks.getVariableClasse());
        
        stacks.setVariableClasse(null);
        assertNull(stacks.getVariableClasse());
    }

    // ============================================================
    // ADDITIONAL AFFECTER VAL TESTS
    // ============================================================

    @Test
    void testAffecterVal_onTab_throwsException() {
        stacks.declareTab("monTab", 5, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("monTab", 10);
        });
        assertTrue(ex.getMessage().contains("tableau"));
    }

    @Test
    void testAffecterVal_onMeth_throwsException() {
        stacks.declareMeth("maMethode", null, Type.VOID);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("maMethode", 10);
        });
        assertTrue(ex.getMessage().contains("méthode"));
    }

    @Test
    void testAffecterVal_typeMismatch_throwsException() {
        stacks.declareVar("x", 10, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.affecterVal("x", true);  // boolean instead of int
        });
        assertTrue(ex.getMessage().contains("Type"));
    }

    @Test
    void testAffecterVal_withNullValue_accepted() {
        stacks.declareVar("x", 10, Type.ENTIER);
        
        assertTrue(stacks.affecterVal("x", null));
        assertNull(stacks.getValue("x"));
    }

    @Test
    void testAffecterVal_onCstWithOmega_allowed() {
        stacks.declareCst("PI", Type.ENTIER);  // Omega value
        
        assertTrue(stacks.affecterVal("PI", 314));
        assertEquals(314, stacks.getValue("PI"));
    }

    // ============================================================
    // DECLARE VAR/CST WITH OMEGA DEFAULT VALUE TESTS
    // ============================================================

    @Test
    void testDeclareVar_withOmegaDefault_throwsOnGetValue() {
        stacks.declareVar("x", Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getValue("x");
        });
        assertTrue(ex.getMessage().contains("not initialized") || ex.getMessage().contains("Omega"));
    }

    @Test
    void testDeclareCst_withOmegaDefault_throwsOnGetValue() {
        stacks.declareCst("C", Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.getValue("C");
        });
        assertTrue(ex.getMessage().contains("not initialized") || ex.getMessage().contains("Omega"));
    }

    // ============================================================
    // DOUBLE DECLARATION TESTS
    // ============================================================

    @Test
    void testDeclareVar_alreadyExists_throwsException() {
        stacks.declareVar("x", 10, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.declareVar("x", 20, Type.ENTIER);
        });
        assertTrue(ex.getMessage().contains("already"));
    }

    @Test
    void testDeclareCst_alreadyExists_throwsException() {
        stacks.declareCst("PI", 314, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.declareCst("PI", 315, Type.ENTIER);
        });
        assertTrue(ex.getMessage().contains("already"));
    }

    @Test
    void testDeclareTab_alreadyExists_throwsException() {
        stacks.declareTab("arr", 5, Type.ENTIER);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.declareTab("arr", 10, Type.ENTIER);
        });
        assertTrue(ex.getMessage().contains("already"));
    }

    @Test
    void testDeclareMeth_alreadyExists_throwsException() {
        stacks.declareMeth("foo", null, Type.VOID);
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            stacks.declareMeth("foo", null, Type.VOID);
        });
        assertTrue(ex.getMessage().contains("already"));
    }

    // ============================================================
    // RETIRER DECL TESTS
    // ============================================================

    @Test
    void testRetirerDecl_removesVariable() {
        stacks.declareVar("x", 10, Type.ENTIER);
        assertNotNull(stacks.getObjectType("x"));
        
        stacks.retirerDecl("x");
        
        assertNull(stacks.getObjectType("x"));
    }

    @Test
    void testRetirerDecl_nonExistent_doesNotThrow() {
        assertDoesNotThrow(() -> stacks.retirerDecl("unknown"));
    }

    @Test
    void testRetirerDecl_removesFromMiddleOfStack() {
        stacks.declareVar("a", 1, Type.ENTIER);
        stacks.declareVar("b", 2, Type.ENTIER);
        stacks.declareVar("c", 3, Type.ENTIER);
        
        stacks.retirerDecl("b");
        
        assertNull(stacks.getObjectType("b"));
        assertNotNull(stacks.getObjectType("a"));
        assertNotNull(stacks.getObjectType("c"));
    }

    // ============================================================
    // FIND QUAD TESTS
    // ============================================================

    @Test
    void testFindQuad_existingIdent_returnsQuad() {
        stacks.declareVar("x", 42, Type.ENTIER);
        
        Stacks.Quad q = stacks.findQuad("x");
        
        assertNotNull(q);
        assertEquals("x", q.ident);
        assertEquals(42, q.value);
    }

    @Test
    void testFindQuad_nonExistent_returnsNull() {
        assertNull(stacks.findQuad("unknown"));
    }

    // ============================================================
    // GET STACK POSITION TESTS
    // ============================================================

    @Test
    void testGetStackPosition_existingIdent_returnsPosition() {
        stacks.declareVar("a", 1, Type.ENTIER);
        stacks.declareVar("b", 2, Type.ENTIER);
        stacks.declareVar("c", 3, Type.ENTIER);
        
        assertEquals(0, stacks.getStackPosition("a"));
        assertEquals(1, stacks.getStackPosition("b"));
        assertEquals(2, stacks.getStackPosition("c"));
    }

    @Test
    void testGetStackPosition_nonExistent_returnsMinusOne() {
        assertEquals(-1, stacks.getStackPosition("unknown"));
    }

    // ============================================================
    // PRINT METHODS TESTS (output capture)
    // ============================================================

    @Test
    void testPrintStack_doesNotThrow() {
        stacks.declareVar("x", 10, Type.ENTIER);
        
        // Redirect stderr
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        
        try {
            assertDoesNotThrow(() -> stacks.printStack());
            assertTrue(errContent.toString().contains("Stack Content"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testPrintSymbolTable_doesNotThrow() {
        stacks.declareVar("x", 10, Type.ENTIER);
        
        assertDoesNotThrow(() -> stacks.printSymbolTable());
    }

    @Test
    void testPrintSymbol_existing_doesNotThrow() {
        stacks.declareVar("x", 10, Type.ENTIER);
        
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        
        try {
            assertDoesNotThrow(() -> stacks.printSymbol("x"));
            assertTrue(errContent.toString().contains("x"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testPrintSymbol_nonExisting_printsNotFound() {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        
        try {
            stacks.printSymbol("unknown");
            assertTrue(errContent.toString().contains("non trouvé") || errContent.toString().contains("not found"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void testPrintHeap_doesNotThrow() {
        stacks.declareTab("arr", 5, Type.ENTIER);
        
        assertDoesNotThrow(() -> stacks.printHeap());
    }

    // ============================================================
    // QUAD TO STRING TEST
    // ============================================================

    @Test
    void testQuad_toString_formatsCorrectly() {
        Stacks.Quad q = new Stacks.Quad("x", 42, "var", Type.ENTIER);
        
        String str = q.toString();
        
        
        assertTrue(str.contains("x"));
        assertTrue(str.contains("42"));
        assertTrue(str.contains("var"));
        assertTrue(str.contains("integer"));
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

        HeapEntry allocated = heap.allocate("A", 8, null);
        heap.free(allocated);


        HeapEntry freeBlock = heap.getEntry(allocated.getAddress());
        assertNotNull(freeBlock);
        assertTrue(freeBlock.isFree());


        HeapEntry toRemove = new HeapEntry(
                null,
                freeBlock.getAddress(),
                freeBlock.getSize(),
                null,
                true
        );

        assertTrue(invokeRemoveEntryByAddressAndSize(heap, toRemove));
        assertNull(heap.getEntry(freeBlock.getAddress()));
    }

    @Test
    void removeEntryByAddressAndSize_removesFreeBlock_prevNotNull() throws Exception {
        Heap heap = new Heap();

        HeapEntry a = heap.allocate("A", 8, null);
        HeapEntry b = heap.allocate("B", 8, null);

        heap.free(a);
        heap.free(b);

        // Après fusion, un seul FREE_BLOCK existe
        HeapEntry mergedFree = heap.getEntry(0);

        assertNotNull(mergedFree);
        assertTrue(mergedFree.isFree());
        assertEquals(256, mergedFree.getSize());

        // On supprime ce FREE_BLOCK
        HeapEntry toRemove = new HeapEntry(
                null,
                mergedFree.getAddress(),
                mergedFree.getSize(),
                null,
                true
        );

        assertTrue(invokeRemoveEntryByAddressAndSize(heap, toRemove));

        // Le heap ne doit plus contenir ce bloc
        assertNull(heap.getEntry(mergedFree.getAddress()));
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





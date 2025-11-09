package fr.ufrst.m1info.gl.groupe7.memoire;

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
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", "integer");

        assertEquals(q1, q2);
        assertEquals(q1, q1);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testNotEqualsDifferentIdent() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("y", 10, "var", "integer");

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentValue() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", 20, "var", "integer");

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentObject() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "cst", "integer");

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentType() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", "boolean");

        assertNotEquals(q1, q2);
    }

    @Test
    void testEqualsWithNull() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        assertNotEquals(q1, null);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        String otherObject = "Not a Quad";
        assertNotEquals(q1, otherObject);
    }

    @Test
    void testHashCodeConsistency() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        int initialHash = q1.hashCode();
        assertEquals(initialHash, q1.hashCode()); // hashCode must be stable
    }
    @Test
    void testEqualsWithBothValuesNull() {
        Stacks.Quad q1 = new Stacks.Quad("x", null, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", null, "var", "integer");
        assertEquals(q1, q2);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testEqualsWithOneValueNull() {
        Stacks.Quad q1 = new Stacks.Quad("x", null, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", "integer");
        assertNotEquals(q1, q2);

        Stacks.Quad q3 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q4 = new Stacks.Quad("x", null, "var", "integer");
        assertNotEquals(q3, q4);
    }

    @Test
    void testHashCodeWithNullValue() {
        Stacks.Quad q1 = new Stacks.Quad("x", null, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", null, "var", "integer");

        assertEquals(q1.hashCode(), q2.hashCode()); // should not throw NPE
    }

    @Test
    void testEqualsReflexive() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        assertEquals(q1, q1); // reflexivity
    }

    @Test
    void testEqualsSymmetric() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", "integer");
        assertEquals(q1, q2);
        assertEquals(q2, q1); // symmetry
    }

    @Test
    void testEqualsTransitive() {
        Stacks.Quad q1 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q2 = new Stacks.Quad("x", 10, "var", "integer");
        Stacks.Quad q3 = new Stacks.Quad("x", 10, "var", "integer");
        assertEquals(q1, q2);
        assertEquals(q2, q3);
        assertEquals(q1, q3); // transitivity
    }
    @Test
    void testDeclareVar() {
        stacks.declareVar("x", 5, "integer");
        assertEquals(5, stacks.getValue("x"));
        assertEquals("var", stacks.getObjectType("x"));
        assertEquals("integer", stacks.getDataType("x"));
    }

    @Test
    void testDeclareCst() {
        stacks.declareCst("PI", 3.14, "integer");
        assertEquals(3.14, stacks.getValue("PI"));
        assertEquals("cst", stacks.getObjectType("PI"));
        assertEquals("integer", stacks.getDataType("PI"));
    }

    @Test
    void testDeclareTab() {
        stacks.declareTab("t", 10, "integer");
        assertEquals("size=10", stacks.getValue("t"));
        assertEquals("tab", stacks.getObjectType("t"));
        assertEquals("integer", stacks.getDataType("t"));
    }

    @Test
    void testDeclareMeth() {
        stacks.declareMeth("f", "body", "void");
        assertEquals("body", stacks.getValue("f"));
        assertEquals("meth", stacks.getObjectType("f"));
        assertEquals("void", stacks.getDataType("f"));
    }

    @Test
    void testAssignValueOnVar() {
        stacks.declareVar("x", 5, "integer");
        stacks.AffecterVal("x", 20);
        assertEquals(20, stacks.getValue("x"));
    }

    @Test
    void testAssignValueDoesNotAffectConst() {
        stacks.declareCst("PI", 3.14, "integer");
        stacks.AffecterVal("PI", 10);
        assertEquals(3.14, stacks.getValue("PI"), "La constante ne doit pas être modifiée");
    }
    @Test
    void topPil(){
        stacks.declareVar("x", 5, "integer");
        assertEquals("x", stacks.getTop().ident);
    }
    @Test
    void topPil2(){
        stacks.AffecterVal("PI", 10);
        stacks.declareVar("x", 5, "integer");
        assertEquals("x", stacks.getTop().ident);
    }
    @Test
    void testGetTopOnEmptyStack() {

        assertNull(stacks.getTop(), "getTop() should return null when the stack is empty");
    }
    @Test
    void testGetStackFromTopToBottom() {

        stacks.declareVar("a", 1, "integer");
        stacks.declareVar("b", 2, "integer");
        stacks.declareVar("c", 3, "integer");
        stacks.declareVar("d", 4, "integer");
        stacks.declareVar("e", 5, "integer"); // top of the stack


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
        stacks.declareVar("x", 1, "integer");
        stacks.declareVar("y", 2, "integer");


        stacks.swap();


        assertEquals(1, stacks.getValue("x"));
        assertEquals(2, stacks.getValue("y"));


        assertEquals("x", ((Stacks.Quad) stacks.getTop()).ident);
    }
    @Test
    void testSwapOneElement() {
        stacks.declareVar("x", 1, "integer");



        stacks.swap();


        assertEquals(1, stacks.getValue("x"));



        assertEquals("x", ((Stacks.Quad) stacks.getTop()).ident);
    }

    @Test
    void testPopNoElement() {


        assertNull(stacks.pop());

    }

    @Test
    void testPop() {
        stacks.declareVar("x", 1, "integer");
        stacks.declareVar("y", 2, "integer");
        Stacks.Quad quad = stacks.pop();
        assertEquals(new Stacks.Quad("y", 2, "var", "integer"), quad);
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
        stacks.declareVar("x", 1, "integer");

        assertTrue(stacks.AffecterVal("x", 10));
        assertEquals(10, stacks.getValue("x"));
    }

    @Test
    void testAssignValueToConstant() {
        stacks.declareCst("PI", 3.14, "integer");

        assertFalse(stacks.AffecterVal("PI", 10));
        assertEquals(3.14, stacks.getValue("PI"));
    }

    @Test
    void testAssignValueIdentifierNotFound() {
        stacks.declareVar("x", 1, "integer");

        assertFalse(stacks.AffecterVal("y", 10));
        assertNull(stacks.getValue("y"));
        assertEquals(1, stacks.getValue("x"));
    }
    @Test
    void testAssignValueCst() {
        stacks.declareVar("x", 1, "integer");

        assertFalse(stacks.AffecterVal("y", 10));
        assertNull(stacks.getValue("y"));
        assertEquals(1, stacks.getValue("x"));
    }

    @Test
    void testAssignValueRestoresStackOrder() {
        stacks.declareVar("a", 1, "integer");
        stacks.declareVar("b", 2, "integer");
        stacks.declareVar("c", 3, "integer"); // top

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
        stacks.AffecterVal("x", 10);
        assertNull(stacks.getValue("x"));
    }
    /*
    Get the data type
    Get the object type
     */
    @Test
    void testGetObjectTypeExistingIdentifier() {
        stacks.declareVar("x", 1, "integer");
        stacks.declareCst("PI", 3.14, "integer");

        assertEquals("var", stacks.getObjectType("x"));
        assertEquals("cst", stacks.getObjectType("PI"));
    }

    @Test
    void testGetObjectTypeNonExistingIdentifier() {
        stacks.declareVar("x", 1, "integer");

        assertNull(stacks.getObjectType("y"));
    }

    @Test
    void testGetObjectTypeEmptyStack() {
        assertNull(stacks.getObjectType("x"));
    }

    @Test
    void testGetDataTypeExistingIdentifier() {
        stacks.declareVar("x", 1, "integer");
        stacks.declareVar("flag", true, "boolean");

        assertEquals("integer", stacks.getDataType("x"));
        assertEquals("boolean", stacks.getDataType("flag"));
    }

    @Test
    void testGetDataTypeNonExistingIdentifier() {
        stacks.declareVar("x", 1, "integer");

        assertNull(stacks.getDataType("y"));
    }

    @Test
    void testGetDataTypeEmptyStack() {
        assertNull(stacks.getDataType("x"));
    }
    @Test
    void testPrintStack() {

        stacks.declareVar("x", 1, "integer");
        stacks.declareVar("y", 2, "integer");


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
        stacks.declareVar("x", 0, "int");
        stacks.declareCst("PI", 3.14, "float");
        stacks.printSymbolTable();
        stacks.printSymbol("li");
        stacks.printSymbol("PI");
        // OK
        assertTrue(stacks.AffecterVal("x", 12));

        // Mauvais type
        assertFalse(stacks.AffecterVal("x", "notAnInt"));

        // Constante
        assertFalse(stacks.AffecterVal("PI", 2.71));

        // Variable inexistante
        assertFalse(stacks.AffecterVal("y", 99));
    }
    @Test
    void testIsTypeCompatible_IntegerVariants() {
        assertTrue(invokeIsTypeCompatible("int", 10));
        assertTrue(invokeIsTypeCompatible("integer", -5));
        assertTrue(invokeIsTypeCompatible("entier", 0));

        assertFalse(invokeIsTypeCompatible("int", true));
        assertFalse(invokeIsTypeCompatible("int", "42"));
        assertFalse(invokeIsTypeCompatible("int", 3.14));
    }

    // ===============================
    // Tests pour le type "boolean" / "booleen"
    // ===============================
    @Test
    void testIsTypeCompatible_BooleanVariants() {
        assertTrue(invokeIsTypeCompatible("boolean", true));
        assertTrue(invokeIsTypeCompatible("booleen", false));

        assertFalse(invokeIsTypeCompatible("boolean", "true"));
        assertFalse(invokeIsTypeCompatible("boolean", 1));
    }

    // ===============================
    // Tests pour le type "string" / "chaine"
    // ===============================
    @Test
    void testIsTypeCompatible_StringVariants() {
        assertTrue(invokeIsTypeCompatible("string", "hello"));
        assertTrue(invokeIsTypeCompatible("chaine", "bonjour"));

        assertFalse(invokeIsTypeCompatible("string", 123));
        assertFalse(invokeIsTypeCompatible("chaine", false));
    }

    // ===============================
    // Tests pour le type "void"
    // ===============================
    @Test
    void testIsTypeCompatible_VoidType() {
        assertTrue(invokeIsTypeCompatible("void", null));
        assertFalse(invokeIsTypeCompatible("void", 5));
        assertFalse(invokeIsTypeCompatible("void", "text"));
    }

    // ===============================
    // Tests pour les valeurs nulles (acceptées pour tous sauf void explicite)
    // ===============================
    @Test
    void testIsTypeCompatible_NullValue() {
        assertTrue(invokeIsTypeCompatible("int", null));
        assertTrue(invokeIsTypeCompatible("boolean", null));
        assertTrue(invokeIsTypeCompatible("string", null));
        assertTrue(invokeIsTypeCompatible("chaine", null));
        assertTrue(invokeIsTypeCompatible("entier", null));
    }

    // ===============================
    // Tests pour les types inconnus
    // ===============================
    @Test
    void testIsTypeCompatible_UnknownType() {
        assertFalse(invokeIsTypeCompatible("float", 3.14));
        assertFalse(invokeIsTypeCompatible("char", 'a'));
        assertFalse(invokeIsTypeCompatible("randomType", "test"));

    }

    // ===============================
    // Méthode utilitaire pour accéder à la méthode privée via réflexion
    // ===============================
    private boolean invokeIsTypeCompatible(String type, Object value) {
        try {
            var method = Stacks.class.getDeclaredMethod("isTypeCompatible", String.class, Object.class);
            method.setAccessible(true);
            return (boolean) method.invoke(stacks, type, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}





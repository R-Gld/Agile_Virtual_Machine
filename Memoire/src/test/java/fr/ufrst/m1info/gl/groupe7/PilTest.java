package fr.ufrst.m1info.gl.groupe7;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PilTest {

    private Pil pil;

    @BeforeEach
    void setUp() {
        pil = new Pil();
    }
    /*
    Quad Test
     */
    @Test
    void testEqualsSameValues() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", 10, "var", "integer");

        assertEquals(q1, q2);
        assertEquals(q1, q1);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testNotEqualsDifferentIdent() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("y", 10, "var", "integer");

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentValue() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", 20, "var", "integer");

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentObject() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", 10, "cst", "integer");

        assertNotEquals(q1, q2);
    }

    @Test
    void testNotEqualsDifferentType() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", 10, "var", "boolean");

        assertNotEquals(q1, q2);
    }

    @Test
    void testEqualsWithNull() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        assertNotEquals(q1, null);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        String otherObject = "Not a Quad";
        assertNotEquals(q1, otherObject);
    }

    @Test
    void testHashCodeConsistency() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        int initialHash = q1.hashCode();
        assertEquals(initialHash, q1.hashCode()); // hashCode must be stable
    }
    @Test
    void testEqualsWithBothValuesNull() {
        Pil.Quad q1 = new Pil.Quad("x", null, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", null, "var", "integer");
        assertEquals(q1, q2);
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    void testEqualsWithOneValueNull() {
        Pil.Quad q1 = new Pil.Quad("x", null, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", 10, "var", "integer");
        assertNotEquals(q1, q2);

        Pil.Quad q3 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q4 = new Pil.Quad("x", null, "var", "integer");
        assertNotEquals(q3, q4);
    }

    @Test
    void testHashCodeWithNullValue() {
        Pil.Quad q1 = new Pil.Quad("x", null, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", null, "var", "integer");

        assertEquals(q1.hashCode(), q2.hashCode()); // should not throw NPE
    }

    @Test
    void testEqualsReflexive() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        assertEquals(q1, q1); // reflexivity
    }

    @Test
    void testEqualsSymmetric() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", 10, "var", "integer");
        assertEquals(q1, q2);
        assertEquals(q2, q1); // symmetry
    }

    @Test
    void testEqualsTransitive() {
        Pil.Quad q1 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q2 = new Pil.Quad("x", 10, "var", "integer");
        Pil.Quad q3 = new Pil.Quad("x", 10, "var", "integer");
        assertEquals(q1, q2);
        assertEquals(q2, q3);
        assertEquals(q1, q3); // transitivity
    }
    @Test
    void testDeclareVar() {
        pil.declareVar("x", 5, "integer");
        assertEquals(5, pil.getValue("x"));
        assertEquals("var", pil.getObjectType("x"));
        assertEquals("integer", pil.getDataType("x"));
    }

    @Test
    void testDeclareCst() {
        pil.declareCst("PI", 3.14, "integer");
        assertEquals(3.14, pil.getValue("PI"));
        assertEquals("cst", pil.getObjectType("PI"));
        assertEquals("integer", pil.getDataType("PI"));
    }

    @Test
    void testDeclareTab() {
        pil.declareTab("t", 10, "integer");
        assertEquals("size=10", pil.getValue("t"));
        assertEquals("tab", pil.getObjectType("t"));
        assertEquals("integer", pil.getDataType("t"));
    }

    @Test
    void testDeclareMeth() {
        pil.declareMeth("f", "body", "void");
        assertEquals("body", pil.getValue("f"));
        assertEquals("meth", pil.getObjectType("f"));
        assertEquals("void", pil.getDataType("f"));
    }

    @Test
    void testAssignValueOnVar() {
        pil.declareVar("x", 5, "integer");
        pil.assignValue("x", 20);
        assertEquals(20, pil.getValue("x"));
    }

    @Test
    void testAssignValueDoesNotAffectConst() {
        pil.declareCst("PI", 3.14, "integer");
        pil.assignValue("PI", 10);
        assertEquals(3.14, pil.getValue("PI"), "La constante ne doit pas être modifiée");
    }
    @Test
    void topPil(){
        pil.declareVar("x", 5, "integer");
        assertEquals("x", pil.getTop().ident);
    }
    @Test
    void topPil2(){
        pil.assignValue("PI", 10);
        pil.declareVar("x", 5, "integer");
        assertEquals("x", pil.getTop().ident);
    }
    @Test
    void testGetTopOnEmptyStack() {

        assertNull(pil.getTop(), "getTop() should return null when the stack is empty");
    }
    @Test
    void testGetStackFromTopToBottom() {

        pil.declareVar("a", 1, "integer");
        pil.declareVar("b", 2, "integer");
        pil.declareVar("c", 3, "integer");
        pil.declareVar("d", 4, "integer");
        pil.declareVar("e", 5, "integer"); // top of the stack


        List<Pil.Quad> result = pil.getStackFromTopToBottom();


        assertEquals(5, result.size());


        assertEquals("e", result.get(0).ident); // top
        assertEquals("d", result.get(1).ident);
        assertEquals("c", result.get(2).ident);
        assertEquals("b", result.get(3).ident);
        assertEquals("a", result.get(4).ident); // bottom
    }

    @Test
    void testSwap() {
        pil.declareVar("x", 1, "integer");
        pil.declareVar("y", 2, "integer");


        pil.swap();


        assertEquals(1, pil.getValue("x"));
        assertEquals(2, pil.getValue("y"));


        assertEquals("x", ((Pil.Quad) pil.getTop()).ident);
    }
    @Test
    void testSwapOneElement() {
        pil.declareVar("x", 1, "integer");



        pil.swap();


        assertEquals(1, pil.getValue("x"));



        assertEquals("x", ((Pil.Quad) pil.getTop()).ident);
    }

    @Test
    void testPopNoElement() {


        assertNull(pil.pop());

    }

    @Test
    void testPop() {
        pil.declareVar("x", 1, "integer");
        pil.declareVar("y", 2, "integer");
        Pil.Quad quad =pil.pop();
        assertEquals(new Pil.Quad("y", 2, "var", "integer"), quad);
        List<Pil.Quad> result = pil.getStackFromTopToBottom();


        assertEquals(1, result.size());
        assertNull(pil.getValue("y"));
        assertEquals(1, pil.getValue("x"));
    }

    @Test
    void testGettersReturnNullIfNotFound() {
        assertNull(pil.getValue("unknown"));
        assertNull(pil.getObjectType("unknown"));
        assertNull(pil.getDataType("unknown"));
    }
    /*
    test assignement coverage
     */
    @Test
    void testAssignValueToVariable() {
        pil.declareVar("x", 1, "integer");

        pil.assignValue("x", 10);
        assertEquals(10, pil.getValue("x"));
    }

    @Test
    void testAssignValueToConstant() {
        pil.declareCst("PI", 3.14, "integer");

        pil.assignValue("PI", 10);
        assertEquals(3.14, pil.getValue("PI"));
    }

    @Test
    void testAssignValueIdentifierNotFound() {
        pil.declareVar("x", 1, "integer");

        pil.assignValue("y", 10);
        assertNull(pil.getValue("y"));
        assertEquals(1, pil.getValue("x"));
    }

    @Test
    void testAssignValueRestoresStackOrder() {
        pil.declareVar("a", 1, "integer");
        pil.declareVar("b", 2, "integer");
        pil.declareVar("c", 3, "integer"); // top

        pil.assignValue("a", 10);

        List<Pil.Quad> stackList = pil.getStackFromTopToBottom();
        assertEquals("c", stackList.get(0).ident);
        assertEquals("b", stackList.get(1).ident);
        assertEquals("a", stackList.get(2).ident);
        assertEquals(10, stackList.get(2).value);
    }

    @Test
    void testAssignValueOnEmptyStack() {
        // pile vide
        pil.assignValue("x", 10);
        assertNull(pil.getValue("x"));
    }
    /*
    Get the data type
    Get the object type
     */
    @Test
    void testGetObjectTypeExistingIdentifier() {
        pil.declareVar("x", 1, "integer");
        pil.declareCst("PI", 3.14, "integer");

        assertEquals("var", pil.getObjectType("x"));
        assertEquals("cst", pil.getObjectType("PI"));
    }

    @Test
    void testGetObjectTypeNonExistingIdentifier() {
        pil.declareVar("x", 1, "integer");

        assertNull(pil.getObjectType("y"));
    }

    @Test
    void testGetObjectTypeEmptyStack() {
        assertNull(pil.getObjectType("x"));
    }

    @Test
    void testGetDataTypeExistingIdentifier() {
        pil.declareVar("x", 1, "integer");
        pil.declareVar("flag", true, "boolean");

        assertEquals("integer", pil.getDataType("x"));
        assertEquals("boolean", pil.getDataType("flag"));
    }

    @Test
    void testGetDataTypeNonExistingIdentifier() {
        pil.declareVar("x", 1, "integer");

        assertNull(pil.getDataType("y"));
    }

    @Test
    void testGetDataTypeEmptyStack() {
        assertNull(pil.getDataType("x"));
    }
    @Test
    void testPrintStack() {

        pil.declareVar("x", 1, "integer");
        pil.declareVar("y", 2, "integer");


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));


        pil.printStack();


        System.setOut(originalOut);


        String output = outputStream.toString();


        assertTrue(output.contains("<x, 1, var, integer>"));
        assertTrue(output.contains("<y, 2, var, integer>"));
        assertTrue(output.contains("--- Current Stack Content ---"));
        assertTrue(output.contains("------------------------------"));
    }

}


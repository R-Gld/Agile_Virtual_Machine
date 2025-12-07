package fr.ufrst.m1info.gl.groupe7.memoire.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TypeUtilsTest {

    @Test
    void testGetTypeFromString_boolean() {
        assertEquals(Type.BOOLEEN, TypeUtils.getTypeFromString("boolean"));
    }

    @Test
    void testGetTypeFromString_BOOLEEN() {
        assertEquals(Type.BOOLEEN, TypeUtils.getTypeFromString("BOOLEEN"));
    }

    @Test
    void testGetTypeFromString_BOOLEAN() {
        assertEquals(Type.BOOLEEN, TypeUtils.getTypeFromString("BOOLEAN"));
    }

    @Test
    void testGetTypeFromString_int() {
        assertEquals(Type.ENTIER, TypeUtils.getTypeFromString("int"));
    }

    @Test
    void testGetTypeFromString_integer() {
        assertEquals(Type.ENTIER, TypeUtils.getTypeFromString("integer"));
    }

    @Test
    void testGetTypeFromString_ENTIER() {
        assertEquals(Type.ENTIER, TypeUtils.getTypeFromString("ENTIER"));
    }

    @Test
    void testGetTypeFromString_void() {
        assertEquals(Type.VOID, TypeUtils.getTypeFromString("void"));
    }

    @Test
    void testGetTypeFromString_unknownType() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            TypeUtils.getTypeFromString("unknown");
        });
        assertEquals("Unknown type: unknown", exception.getMessage());
    }

    @Test
    void testGetTypeFromString_emptyString() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            TypeUtils.getTypeFromString("");
        });
        assertEquals("Unknown type: ", exception.getMessage());
    }
}

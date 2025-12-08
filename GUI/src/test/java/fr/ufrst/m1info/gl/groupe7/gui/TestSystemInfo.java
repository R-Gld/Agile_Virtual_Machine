package fr.ufrst.m1info.gl.groupe7.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe SystemInfo.
 */
class TestSystemInfo {

    @Test
    void testJavaVersionNotNull() {
        String version = SystemInfo.javaVersion();
        assertNotNull(version, "La version Java ne doit pas être null");
    }

    @Test
    void testJavaVersionNotEmpty() {
        String version = SystemInfo.javaVersion();
        assertFalse(version.isEmpty(), "La version Java ne doit pas être vide");
    }

    @Test
    void testJavaVersionMatchesSystemProperty() {
        String expected = System.getProperty("java.version");
        String actual = SystemInfo.javaVersion();
        assertEquals(expected, actual, "La version Java doit correspondre à la propriété système");
    }

    @Test
    void testJavafxVersionMatchesSystemProperty() {
        String expected = System.getProperty("javafx.version");
        String actual = SystemInfo.javafxVersion();
        assertEquals(expected, actual, "La version JavaFX doit correspondre à la propriété système");
    }

    @Test
    void testJavaVersionContainsDigit() {
        String version = SystemInfo.javaVersion();
        assertTrue(version.matches(".*\\d.*"), "La version Java doit contenir au moins un chiffre");
    }
}

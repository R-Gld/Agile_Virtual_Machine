package fr.ufrst.m1info.gl.groupe7.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe EditorPreferences (singleton).
 */
class TestEditorPreferences {

    private EditorPreferences prefs;

    @BeforeEach
    void setUp() {
        prefs = EditorPreferences.getInstance();
        // Reset aux valeurs par défaut
        prefs.setTabSize(4);
        prefs.setAutoClosePairs(true);
        prefs.setSmartIndentation(true);
    }

    @Test
    void testGetInstanceNotNull() {
        assertNotNull(EditorPreferences.getInstance(), "L'instance ne doit pas être null");
    }

    @Test
    void testSingletonReturnsSameInstance() {
        EditorPreferences instance1 = EditorPreferences.getInstance();
        EditorPreferences instance2 = EditorPreferences.getInstance();
        assertSame(instance1, instance2, "Le singleton doit retourner la même instance");
    }

    @Test
    void testDefaultTabSize() {
        assertEquals(4, prefs.getTabSize(), "La taille de tabulation par défaut doit être 4");
    }

    @Test
    void testSetTabSizeValid() {
        prefs.setTabSize(2);
        assertEquals(2, prefs.getTabSize(), "La taille de tabulation doit être mise à jour");
    }

    @Test
    void testSetTabSizeToOne() {
        prefs.setTabSize(1);
        assertEquals(1, prefs.getTabSize(), "La taille de tabulation minimale est 1");
    }

    @Test
    void testSetTabSizeLargeValue() {
        prefs.setTabSize(8);
        assertEquals(8, prefs.getTabSize(), "La taille de tabulation peut être 8");
    }

    @Test
    void testSetTabSizeZeroThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> prefs.setTabSize(0),
                "setTabSize(0) doit lancer IllegalArgumentException");
    }

    @Test
    void testSetTabSizeNegativeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> prefs.setTabSize(-1),
                "setTabSize(-1) doit lancer IllegalArgumentException");
    }

    @Test
    void testSetTabSizeNegativeLargeValue() {
        assertThrows(IllegalArgumentException.class, () -> prefs.setTabSize(-100),
                "setTabSize(-100) doit lancer IllegalArgumentException");
    }

    @Test
    void testDefaultAutoClosePairs() {
        assertTrue(prefs.isAutoClosePairs(), "AutoClosePairs doit être activé par défaut");
    }

    @Test
    void testSetAutoClosePairsFalse() {
        prefs.setAutoClosePairs(false);
        assertFalse(prefs.isAutoClosePairs(), "AutoClosePairs doit pouvoir être désactivé");
    }

    @Test
    void testSetAutoClosePairsTrue() {
        prefs.setAutoClosePairs(false);
        prefs.setAutoClosePairs(true);
        assertTrue(prefs.isAutoClosePairs(), "AutoClosePairs doit pouvoir être réactivé");
    }

    @Test
    void testDefaultSmartIndentation() {
        assertTrue(prefs.isSmartIndentation(), "SmartIndentation doit être activé par défaut");
    }

    @Test
    void testSetSmartIndentationFalse() {
        prefs.setSmartIndentation(false);
        assertFalse(prefs.isSmartIndentation(), "SmartIndentation doit pouvoir être désactivé");
    }

    @Test
    void testSetSmartIndentationTrue() {
        prefs.setSmartIndentation(false);
        prefs.setSmartIndentation(true);
        assertTrue(prefs.isSmartIndentation(), "SmartIndentation doit pouvoir être réactivé");
    }

    @Test
    void testMultiplePreferenceChanges() {
        prefs.setTabSize(2);
        prefs.setAutoClosePairs(false);
        prefs.setSmartIndentation(false);

        assertEquals(2, prefs.getTabSize());
        assertFalse(prefs.isAutoClosePairs());
        assertFalse(prefs.isSmartIndentation());
    }

    @Test
    void testPreferencesPersistAcrossCalls() {
        prefs.setTabSize(6);
        EditorPreferences newRef = EditorPreferences.getInstance();
        assertEquals(6, newRef.getTabSize(), "Les préférences doivent persister via le singleton");
    }
}

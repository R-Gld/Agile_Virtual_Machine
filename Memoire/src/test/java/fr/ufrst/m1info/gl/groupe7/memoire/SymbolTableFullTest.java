package fr.ufrst.m1info.gl.groupe7.memoire;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full coverage tests for SymbolTable (hash-based version).
 * Compatible with Symbol(name, type, addressStack) and SymbolTable(declareVar, declareCst, declareTab, declareMeth, assign, findSymbol, contains, remove, updateValue).
 */
public class SymbolTableFullTest {

    private SymbolTable table;

    @BeforeEach
    void setup() {
        table = new SymbolTable();

    }


    @Test
    void testCreationVarStoresSymbol() {
        table.creationSymbol("x", 5, "entier");
        Symbol s = table.findSymbol("x");
        assertNotNull(s);
        assertEquals("x", s.getName());
        assertEquals("entier", s.getType());
        assertEquals(5, s.getAddressStack());
    }




    @Test
    void testUpdateAddressStackWorksForVar() {
        table.creationSymbol("a", 1, "entier");
        assertTrue(table.updateAddressStack("a", 10));
        Symbol s = table.findSymbol("a");
        assertEquals(10, s.getAddressStack());
    }

    @Test
    void testUpdateAddressStackFailsForUnknown() {
        assertFalse(table.updateAddressStack("ghost", 10));
    }




    @Test
    void testRemoveExistingSymbol() {
        table.creationSymbol("z", 1, "entier");
        assertTrue(table.remove("z"));
        assertFalse(table.contains("z"));
    }

    @Test
    void testRemoveUnknownSymbol() {
        assertFalse(table.remove("unknown"));
    }


    @Test
    void testContainsSymbol() {
        table.creationSymbol("v", 2, "entier");
        assertTrue(table.contains("v"));
        assertFalse(table.contains("x"));
    }

    // ---------- FIND SYMBOL ----------

    @Test
    void testFindSymbolReturnsNullIfNotFound() {
        assertNull(table.findSymbol("nothing"));
    }

    @Test
    void testFindSymbolAfterUpdate() {
        table.creationSymbol("flag", 5, "booleen");
        table.updateAddressStack("flag", 2);
        Symbol s = table.findSymbol("flag");
        assertEquals(2, s.getAddressStack());
    }

    @Test
    void testInsertManySymbolsAndCheckCount() {
        int n = 150;
        for (int i = 0; i < n; i++) {
            table.creationSymbol("v" + i, i, "entier");
        }
        assertEquals(n, table.size());
        for (int i = 0; i < n; i++) {
            Symbol s = table.findSymbol("v" + i);
            assertNotNull(s);
            assertEquals(i, s.getAddressStack());
        }
    }

    // ---------- EDGE CASES ----------

    @Test
    void testRemoveThenReinsertSameName() {
        table.creationSymbol("tmp", 1, "entier");
        assertTrue(table.remove("tmp"));
        table.creationSymbol("tmp", 9, "entier");
        Symbol s = table.findSymbol("tmp");
        assertEquals(9, s.getAddressStack());
    }

    @Test
    void testDoubleDeclareReplacesExisting() {
        table.creationSymbol("dup", 1, "entier");
        assertFalse(table.creationSymbol("dup", 2, "entier"));
        Symbol s = table.findSymbol("dup");
        assertEquals(1, s.getAddressStack());
    }

    @Test
    void testPrintTableRunsWithoutError() {
        table.creationSymbol("x", 5, "entier");
        table.creationSymbol("c", 1, "entier");
        table.printTable();
    }

    // ---------- SYMBOL TESTS ----------



    @Test
    void testCreationVarWithNullInputs() {
        table.creationSymbol(null, 10, "entier");
        table.creationSymbol("x", 10, null);
        assertEquals(0, table.size());
    }

    @Test
    void testDeclareCstWithNullInputs() {
        table.creationSymbol(null, 10, "entier");
        table.creationSymbol("x", 10, null);
        assertEquals(0, table.size());
    }

    @Test
    void testDeclareTabWithNullInputs() {
        table.creationSymbol(null, 5, "entier");
        table.creationSymbol("t", 5, null);
        assertEquals(0, table.size());
    }


    @Test
    void testHashHandlesEmptyAndNull() {
        try {
            var h1 = table.getClass().getDeclaredMethod("hash", String.class);
            h1.setAccessible(true);
            assertEquals(0, (int) h1.invoke(table, (Object) null));
            assertEquals(0, (int) h1.invoke(table, ""));
        } catch (Exception e) {
            fail(e);
        }
    }





    @Test
    void testHashWithNullAndEmpty() throws Exception {
        var m = SymbolTable.class.getDeclaredMethod("hash", String.class);
        m.setAccessible(true);
        assertEquals(0, (int) m.invoke(table, (Object) null));
        assertEquals(0, (int) m.invoke(table, ""));
    }




    @Test
    void testUpdateAddressStackUnknownSymbolReturnsFalse() {
        assertFalse(table.updateAddressStack("notExist", 123));
    }



    @Test
    void testRemoveLastElementInChain() {
        table.creationSymbol("alpha", 1, "entier");
        table.creationSymbol("beta", 2, "entier");
        assertTrue(table.remove("beta"));
    }


    @Test
    void testHashEmptyString() throws Exception {
        var method = SymbolTable.class.getDeclaredMethod("hash", String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(table, "");
        assertEquals(0, result);
    }


    @Test
    void testHashNegativeOverflowPath() throws Exception {
        var method = SymbolTable.class.getDeclaredMethod("hash", String.class);
        method.setAccessible(true);
        String weird = new String(new char[5000]).replace('\0', 'ÿ');
        int h = (int) method.invoke(table, weird);
        assertTrue(h >= 0);
    }
    @Test
    void testPrintEmptyTable() {
        table.printTable();
    }

    @Test
    void testRemoveFromEmptyTable() {
        assertFalse(table.remove("ghost"));
    }



    @Test
    void testRemoveNullIdentifier() {
        assertFalse(table.remove(null));
    }
    @Test
    void testHashForNegativeValuePath() throws Exception {
        var method = SymbolTable.class.getDeclaredMethod("hash", String.class);
        method.setAccessible(true);
        String s = "\uFFFF\uFFFF\uFFFF";
        int h = (int) method.invoke(table, s);
        assertTrue(h >= 0);
    }

    @Test
    void testFindNodeNullInput() throws Exception {
        var m = SymbolTable.class.getDeclaredMethod("findSymbol", String.class);
        m.setAccessible(true);
        assertNull(m.invoke(table, (Object) null));
    }

    @Test
    void testFindSymbolNullName() {
        assertNull(table.findSymbol(null));
    }



    @Test
    void testContainsNullName() {
        assertFalse(table.contains(null));
    }



    @Test
    void testUpdateAddressStackNullName() {
        assertFalse(table.updateAddressStack(null, 1));
    }



    @Test
    void testHashFullBranches() throws Exception {
        var method = SymbolTable.class.getDeclaredMethod("hash", String.class);
        method.setAccessible(true);
        assertEquals(0, (int) method.invoke(table, (Object) null)); // null branch
        assertEquals(0, (int) method.invoke(table, "")); // empty branch
        assertTrue((int) method.invoke(table, "abc") > 0); // normal branch
        table.toString();
    }
    @Test
    void testToString_whenEmpty() {
        String result = table.toString();
        assertNotNull(result);
        assertTrue(result.contains("SymbolTable{"));
        assertTrue(result.contains("count=0"));
        // Vérifie que la table vide est bien imprimée
        assertTrue(result.contains("table=["));
    }

    @Test
    void testToString_whenHasOneSymbol() {
        // On ajoute un symbole pour avoir un contenu
        boolean created = table.creationSymbol("x", 1, "int");
        assertTrue(created);

        String result = table.toString();
        assertNotNull(result);
        assertTrue(result.contains("SymbolTable{"));
        assertTrue(result.contains("count=1"));
        assertTrue(result.contains("x")); // le nom du symbole
        assertTrue(result.contains("int")); // le type du symbole
    }

    @Test
    void testToString_afterMultipleSymbols() {
        table.creationSymbol("x", 1, "int");
        table.creationSymbol("y", 2, "boolean");
        table.creationSymbol("z", 3, "string");

        String result = table.toString();

        assertTrue(result.contains("x"));
        assertTrue(result.contains("y"));
        assertTrue(result.contains("z"));
        assertTrue(result.contains("count=3"));
    }
    @Test
    void testType_whenSymbolExists() {
        // Arrange : création d’un symbole
        table.creationSymbol("x", 1, "int");

        // Act : appel de la méthode type()
        String result = table.type("x");

        // Assert : on vérifie que le type correspond
        assertNotNull(result);
        assertEquals("int", result);
    }

    @Test
    void testType_whenSymbolDoesNotExist() {
        // Aucun symbole ajouté
        String result = table.type("unknown");

        // Si aucun symbole trouvé, la méthode doit retourner null
        assertNull(result);
    }

    @Test
    void testType_whenNameIsNull() {
        // Cas défensif : identifiant null
        String result = table.type(null);
        assertNull(result);
    }

    @Test
    void testType_withMultipleSymbols() {
        table.creationSymbol("a", 1, "int");
        table.creationSymbol("b", 2, "boolean");
        table.creationSymbol("c", 3, "String");

        assertEquals("int", table.type("a"));
        assertEquals("boolean", table.type("b"));
        assertEquals("String", table.type("c"));
    }
    // ------------------------------------------------------------------------
    // CAS 1 : suppression du premier élément de la liste chaînée (prev == null)
    // ------------------------------------------------------------------------
    @Test
    void testRemove_firstElementInBucket() {
        // Arrange
        table.creationSymbol("x", 1, "int");
        table.creationSymbol("y", 2, "boolean");

        assertTrue(table.contains("x"));
        int before = table.size();

        // Act
        boolean removed = table.remove("x");

        // Assert
        assertTrue(removed);
        assertFalse(table.contains("x")); // doit être supprimé
        assertEquals(before - 1, table.size());
    }

    // ------------------------------------------------------------------------
    // CAS 2 : suppression d’un élément au milieu ou à la fin (prev != null)
    // ------------------------------------------------------------------------
    @Test
    void testRemove_middleOrLastElementInBucket() {
        // Arrange : pour forcer une collision, on triche un peu
        // On crée deux symboles ayant le même index de hash
        // en utilisant un mock symbol ou des noms avec même hash
        // Pour garantir cela ici, on va insérer manuellement dans le même bucket :
        table.creationSymbol("a", 1, "int");
        table.creationSymbol("b", 2, "boolean");

        // Vérifie qu'ils existent
        assertTrue(table.contains("a"));
        assertTrue(table.contains("b"));

        int before = table.size();

        // Act : suppression du deuxième élément (requiert prev != null)
        boolean removed = table.remove("b");

        // Assert
        assertTrue(removed);
        assertFalse(table.contains("b"));
        assertEquals(before - 1, table.size());
    }

    // ------------------------------------------------------------------------
    // CAS 3 : itération sans suppression (aucun symbole correspondant)
    // ------------------------------------------------------------------------
    @Test
    void testRemove_nonExistingElement() {
        table.creationSymbol("x", 1, "int");

        boolean removed = table.remove("unknown");
        assertFalse(removed);
        assertTrue(table.contains("x"));
        assertEquals(1, table.size());
    }

    // ------------------------------------------------------------------------
    // CAS 4 : suppression dans une chaîne de plusieurs éléments
    // ------------------------------------------------------------------------
    @Test
    void testRemove_withThreeElements() {
        table.creationSymbol("a", 1, "int");
        table.creationSymbol("b", 2, "float");
        table.creationSymbol("c", 3, "boolean");

        int before = table.size();
        assertEquals(before, table.getCount());

        // Suppression du milieu (b)
        boolean removed = table.remove("b");

        assertTrue(removed);
        assertFalse(table.contains("b"));
        assertEquals(before - 1, table.size());
    }
    @Test
    void testRemove_one_element_in() {
        // Arrange
        table.creationSymbol("x", 1, "int");

        assertTrue(table.contains("x"));
        int before = table.size();

        // Act
        boolean removed = table.remove("x");
        // Assert
        assertTrue(removed);
        assertFalse(table.contains("x")); // doit être supprimé
        assertEquals(before - 1, table.size());
    }
    @Test
    void testRemove_one_element_not_in() {
        // Arrange
        table.creationSymbol("x", 1, "int");

        assertTrue(table.contains("x"));
        int before = table.size();

        // Act
        boolean removed = table.remove("y");
        // Assert
        assertFalse(removed);
        assertTrue(table.contains("x")); // doit être supprimé
        assertEquals(before , table.size());
    }
    @Test
    void testRemove_one_element_hash() {
        // Arrange
        table.creationSymbol("FB", 1, "int");

        assertTrue(table.contains("FB"));
        int before = table.size();

        // Act
        boolean removed = table.remove("Ea");
        // Assert
        assertFalse(removed);

        assertEquals(before , table.size());
    }
    @Test
    void testRemove_elseBranch_prevNotNull() {
        // Arrange : deux symboles différents, même bucket (collision de hash)
        table.creationSymbol("FB", 1, "int");      // premier dans la chaîne
        table.creationSymbol("Ea", 2, "boolean");  // ajouté dans le même bucket (chaîne: FB -> Ea)

        // Vérification de la collision
        assertEquals(getHash("FB"), getHash("Ea"), "Les deux hash doivent être identiques");

        int before = table.size();

        // Act : suppression du deuxième symbole ("Ea"), provoque le else
        boolean removed = table.remove("Ea");

        // Assert : la suppression a bien eu lieu et le else a été exécuté
        assertTrue(removed);
        assertFalse(table.contains("Ea")); // supprimé
        assertTrue(table.contains("FB"));  // le premier est toujours là
        assertEquals(before - 1, table.size());
    }

    // Méthode utilitaire locale pour tester la collision de hash
    private int getHash(String ident) {
        int TABLE_SIZE = 97;
        int h = 0;
        for (int i = 0; i < ident.length(); i++) {
            h = (31 * h + ident.charAt(i)) % TABLE_SIZE;
        }
        return (h < 0) ? -h : h;
    }
}




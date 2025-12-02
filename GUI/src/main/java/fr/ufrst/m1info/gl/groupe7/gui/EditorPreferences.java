package fr.ufrst.m1info.gl.groupe7.gui;

/**
 * Gestionnaire des préférences de l'éditeur.
 * <p>
 * Cette classe implémente un singleton exposant quelques réglages
 * d'édition (taille des tabulations, fermeture automatique des paires,
 * indentation intelligente). Les valeurs sont maintenues en mémoire et
 * accessibles globalement via {@link #getInstance()}.
 * </p>
 */
public class EditorPreferences {
    /** Instance unique du singleton. */
    private static EditorPreferences instance;

    /** Nombre d'espaces utilisés pour une tabulation. */
    private int tabSize = 4;
    /** Active la fermeture automatique des paires ((), {}, "", etc.). */
    private boolean autoClosePairs = true;
    /** Active l'indentation intelligente lors de la frappe. */
    private boolean smartIndentation = true;

    /**
     * Constructeur privé pour le pattern Singleton.
     */
    private EditorPreferences() {
    }

    /**
     * Retourne l'instance unique des préférences de l'éditeur.
     * Si aucune instance n'existe, elle est créée à la première invocation.
     *
     * @return l'instance unique d'EditorPreferences
     */
    public static EditorPreferences getInstance() {
        if (instance == null) {
            instance = new EditorPreferences();
        }
        return instance;
    }

    /**
     * Retourne la taille de tabulation (en nombre d'espaces).
     *
     * @return la taille de tabulation
     */
    public int getTabSize() {
        return tabSize;
    }

    /**
     * Définit la taille de tabulation (en nombre d'espaces).
     *
     * @param tabSize nouvelle taille de tabulation (doit être strictement positive)
     * @throws IllegalArgumentException si {@code tabSize} <= 0
     */
    public void setTabSize(int tabSize) {
        if (tabSize <= 0) {
            throw new IllegalArgumentException("tabSize must be > 0");
        }
        this.tabSize = tabSize;
    }

    /**
     * Indique si la fermeture automatique des paires est activée.
     *
     * @return {@code true} si activée, sinon {@code false}
     */
    public boolean isAutoClosePairs() {
        return autoClosePairs;
    }

    /**
     * Active ou désactive la fermeture automatique des paires.
     *
     * @param autoClosePairs {@code true} pour activer, {@code false} pour désactiver
     */
    public void setAutoClosePairs(boolean autoClosePairs) {
        this.autoClosePairs = autoClosePairs;
    }

    /**
     * Indique si l'indentation intelligente est activée.
     *
     * @return {@code true} si activée, sinon {@code false}
     */
    public boolean isSmartIndentation() {
        return smartIndentation;
    }

    /**
     * Active ou désactive l'indentation intelligente.
     *
     * @param smartIndentation {@code true} pour activer, {@code false} pour désactiver
     */
    public void setSmartIndentation(boolean smartIndentation) {
        this.smartIndentation = smartIndentation;
    }

}

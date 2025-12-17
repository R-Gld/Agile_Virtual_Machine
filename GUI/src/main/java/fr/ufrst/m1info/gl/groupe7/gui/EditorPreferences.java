package fr.ufrst.m1info.gl.groupe7.gui;

/**
 * Manager for editor preferences.
 * <p>
 * This class implements a singleton exposing some editing settings
 * (tab size, auto-closing of pairs, smart indentation). Values are
 * kept in memory and are globally accessible via {@link #getInstance()}.
 * </p>
 */
public class EditorPreferences {
    /** Singleton instance. */
    private static EditorPreferences instance;

    /** Number of spaces used for a tab. */
    private int tabSize = 4;
    /** Whether auto-closing of pairs ((), {}, "", etc.) is enabled. */
    private boolean autoClosePairs = true;
    /** Whether smart indentation while typing is enabled. */
    private boolean smartIndentation = true;

    /**
     * Private constructor for the Singleton pattern.
     */
    private EditorPreferences() {
    }

    /**
     * Returns the singleton instance of editor preferences.
     * If no instance exists, it is created on first invocation.
     *
     * @return the singleton instance of EditorPreferences
     */
    public static EditorPreferences getInstance() {
        if (instance == null) {
            instance = new EditorPreferences();
        }
        return instance;
    }

    /**
     * Returns the tab size (number of spaces).
     *
     * @return the tab size
     */
    public int getTabSize() {
        return tabSize;
    }

    /**
     * Sets the tab size (number of spaces).
     *
     * @param tabSize new tab size (must be strictly positive)
     * @throws IllegalArgumentException if {@code tabSize} <= 0
     */
    public void setTabSize(int tabSize) {
        if (tabSize <= 0) {
            throw new IllegalArgumentException("tabSize must be > 0");
        }
        this.tabSize = tabSize;
    }

    /**
     * Indicates whether auto-closing of pairs is enabled.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     */
    public boolean isAutoClosePairs() {
        return autoClosePairs;
    }

    /**
     * Enables or disables auto-closing of pairs.
     *
     * @param autoClosePairs {@code true} to enable, {@code false} to disable
     */
    public void setAutoClosePairs(boolean autoClosePairs) {
        this.autoClosePairs = autoClosePairs;
    }

    /**
     * Indicates whether smart indentation is enabled.
     *
     * @return {@code true} if enabled, otherwise {@code false}
     */
    public boolean isSmartIndentation() {
        return smartIndentation;
    }

    /**
     * Enables or disables smart indentation.
     *
     * @param smartIndentation {@code true} to enable, {@code false} to disable
     */
    public void setSmartIndentation(boolean smartIndentation) {
        this.smartIndentation = smartIndentation;
    }

}

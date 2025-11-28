package fr.ufrst.m1info.gl.groupe7.gui;

public class EditorPreferences {
    private static EditorPreferences instance;

    private int tabSize = 4;
    private boolean autoClosePairs = true;
    private boolean smartIndentation = true;

    private EditorPreferences() {
    }

    public static EditorPreferences getInstance() {
        if (instance == null) {
            instance = new EditorPreferences();
        }
        return instance;
    }

    public int getTabSize() {
        return tabSize;
    }

    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }

    public boolean isAutoClosePairs() {
        return autoClosePairs;
    }

    public void setAutoClosePairs(boolean autoClosePairs) {
        this.autoClosePairs = autoClosePairs;
    }

    public boolean isSmartIndentation() {
        return smartIndentation;
    }

    public void setSmartIndentation(boolean smartIndentation) {
        this.smartIndentation = smartIndentation;
    }

}

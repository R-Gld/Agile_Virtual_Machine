package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import java.util.function.IntFunction;

public class MyCodeArea extends AnchorPane {
    private final CodeArea codeArea;

    /**
     * Create a codeArea component with line number for javafx
     *
     * @param id the id of this component for javafx
     *  */
    public MyCodeArea(String id) {
        this(id, "");
    }

    /**
     * Create a codeArea component with line number for javafx
     *
     * @param id the id of this component for javafx
     * @param defaultValue A string to place in the codeArea
     *  */
    public MyCodeArea(String id, String defaultValue) {
        this.setId(id);

        this.getStylesheets().add("code_area.css");

        codeArea = new CodeArea(defaultValue);
        /* Used to detect code area with testfx */
        codeArea.setId(id + "_code_area");

        /* Permet d'avoir les numéros de ligne sur notre code area */
        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);

        /* Permet au bandeau de numéro de ligne de descendre en bas de la fenêtre
           si le nombre de ligne ne prend pas toute la fenêtre */
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox(numberFactory.apply(line));
            hbox.setSpacing(1);
            hbox.setAlignment(Pos.CENTER);
            if (line == 0){
                Rectangle rectangle = new Rectangle();
                rectangle.getStyleClass().add("lineno");
                rectangle.widthProperty().bind(hbox.widthProperty());
                rectangle.heightProperty().bind(codeArea.heightProperty());
                StackPane.setAlignment(rectangle, Pos.TOP_LEFT);
                return new StackPane(rectangle, hbox);
            }
            return new StackPane(hbox);
        };
        codeArea.setParagraphGraphicFactory(graphicFactory);

        /* Ajout de la barre de scroll,
           et mise en forme dans le composant parent pour prendre toute la place */
        VirtualizedScrollPane<CodeArea> scroll = new VirtualizedScrollPane<CodeArea>(codeArea);
        AnchorPane.setTopAnchor(scroll, 0d);
        AnchorPane.setBottomAnchor(scroll,0d);
        AnchorPane.setLeftAnchor(scroll,0d);
        AnchorPane.setRightAnchor(scroll,0d);
        this.getChildren().add(scroll);
    }

    /**
     * Function used to get the content of the code area
     * @return the code written
     */
    public String getText(){
        return codeArea.getText();
    }

    /**
     * Load text in code area
     * @param string text to load
     */
    public void loadText(String string) {
        codeArea.replaceText(string);
    }

    /**
     * Disable writting in the code area
     */
    public void disable() {
        codeArea.setEditable(false);
    }

    // ==== START ADDED ====
    /**
     * Déplace simplement le caret sur la ligne donnée (lineIndex),
     * ce qui permet de "suivre" la ligne courante pendant le debug.
     *
     * @param lineIndex index de la ligne (0-based)
     */
    public void highlightLine(int lineIndex) {
        if (lineIndex < 0) {
            return;
        }
        int paragraphCount = codeArea.getParagraphs().size();
        if (lineIndex >= paragraphCount) {
            return;
        }
        codeArea.moveTo(lineIndex, 0);
        codeArea.requestFollowCaret();
    }
    // ==== END ADDED ====

}

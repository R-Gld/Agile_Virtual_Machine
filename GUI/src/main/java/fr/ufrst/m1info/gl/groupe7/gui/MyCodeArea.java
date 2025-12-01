package fr.ufrst.m1info.gl.groupe7.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;

public class MyCodeArea extends AnchorPane {
    private final CodeArea codeArea;

    /* Stores all active breakpoints by line index */
    private final Set<Integer> breakpoints = new HashSet<>();

    /**
     * Create a codeArea component with line number for javafx
     *
     * @param id the id of this component for javafx
     */
    public MyCodeArea(String id) {
        this(id, "");
    }

    /**
     * Create a codeArea component with line number for javafx
     *
     * @param id the id of this component for javafx
     * @param defaultValue A string to place in the codeArea
     */
    public MyCodeArea(String id, String defaultValue) {
        this.setId(id);

        this.getStylesheets().add(getClass().getResource("/code_area.css").toExternalForm());

        codeArea = new CodeArea(defaultValue);
        codeArea.setId(id + "_code_area"); // for testfx

        /* Add line numbers */
        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);

        /* Keep line numbers aligned with content */
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox();

            /* Create breakpoint circle (initially hidden) */
            Circle bpCircle = new Circle(5);
            bpCircle.getStyleClass().add("breakpoint-node");
            bpCircle.setVisible(false);
            bpCircle.setManaged(true);   // always reserve space
            bpCircle.setStyle("-fx-fill: red;"); // fallback color in case CSS fails

            /* Clicking toggles breakpoint ON/OFF */
            bpCircle.setOnMouseClicked(e -> toggleBreakpoint(line, bpCircle));

            /* Number label next to breakpoint circle */
            Node number = numberFactory.apply(line);

            /* Clicking on the number also toggles breakpoint */
            number.setOnMouseClicked(e -> toggleBreakpoint(line, bpCircle));

            hbox.getChildren().addAll(number ,bpCircle);
            hbox.setSpacing(6);
            hbox.setAlignment(Pos.CENTER_LEFT);

            if (line == 0) {
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

        /* Add scroll pane */
        VirtualizedScrollPane<CodeArea> scroll = new VirtualizedScrollPane<>(codeArea);
        AnchorPane.setTopAnchor(scroll, 0d);
        AnchorPane.setBottomAnchor(scroll, 0d);
        AnchorPane.setLeftAnchor(scroll, 0d);
        AnchorPane.setRightAnchor(scroll, 0d);
        this.getChildren().add(scroll);
    }

    /**
     * Returns the set of all active breakpoint line indices.
     */
    public Set<Integer> getBreakpoints() {
        return breakpoints;
    }

    /**
     * Toggles the breakpoint on the given line.
     * If breakpoint is active → remove it and hide the circle.
     * If breakpoint is not active → add it and show the circle.
     */
    private void toggleBreakpoint(int line, Circle bpCircle) {
        if (breakpoints.contains(line)) {
            breakpoints.remove(line);
            bpCircle.setVisible(false);
        } else {
            breakpoints.add(line);
            bpCircle.setVisible(true);
        }
    }

    /**
     * Function used to get the content of the code area
     * @return the code written
     */
    public String getText() {
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
     * Disable writing in the code area
     */
    public void disable() {
        codeArea.setEditable(false);
    }

    // ==== START highlight ====
    /**
     * Highlights the specified line with the CSS class "current-line".
     * Used during debugging to indicate the current execution line.
     * @param lineIndex line index (0-based)
     */
    public void highlightLine(int lineIndex) {
        if (lineIndex < 0) return;
        Platform.runLater(() -> {
            int paragraphCount = codeArea.getParagraphs().size();
            if (lineIndex >= paragraphCount) return;

            for (int i = 0; i < paragraphCount; i++) {
                codeArea.setParagraphStyle(i, java.util.Collections.emptyList());
            }
            codeArea.setParagraphStyle(lineIndex, java.util.Collections.singletonList("current-line"));
            codeArea.showParagraphAtTop(lineIndex);
        });
    }

    // ==== END highlight ====
    // cos of the inner style it didn't work I have removed it and i think now it works for linux please check it
}

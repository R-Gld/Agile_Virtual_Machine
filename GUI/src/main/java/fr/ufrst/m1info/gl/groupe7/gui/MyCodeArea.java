package fr.ufrst.m1info.gl.groupe7.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class MyCodeArea extends AnchorPane {
    private static final String[] KEYWORDS = new String[] {
            "class", "final", "void", "main", "if", "else", "while", "return", "length"
    };
    private static final String[] TYPES = new String[] {
            "int", "boolean"
    };
    private static final String[] FUNCTIONS = new String[] {
            "write", "writeln"
    };
    private static final String[] BOOLEANS = new String[] {
            "true", "false"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String TYPE_PATTERN = "\\b(" + String.join("|", TYPES) + ")\\b";
    private static final String FUNCTION_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
    private static final String BOOLEAN_PATTERN = "\\b(" + String.join("|", BOOLEANS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<TYPE>" + TYPE_PATTERN + ")"
                    + "|(?<FUNCTION>" + FUNCTION_PATTERN + ")"
                    + "|(?<BOOLEAN>" + BOOLEAN_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")");

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpans<Collection<String>> syntaxHighlighting = computeSyntaxHighlighting(text);
        StyleSpans<Collection<String>> errorHighlighting = computeErrorHighlighting(text);
        return syntaxHighlighting.overlay(errorHighlighting, (style1, style2) -> {
            Collection<String> combined = new ArrayList<>(style1);
            combined.addAll(style2);
            return combined;
        });
    }

    private static StyleSpans<Collection<String>> computeSyntaxHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword"
                    : matcher.group("TYPE") != null ? "type"
                            : matcher.group("FUNCTION") != null ? "function"
                                    : matcher.group("BOOLEAN") != null ? "boolean"
                                            : matcher.group("PAREN") != null ? "paren"
                                                    : matcher.group("BRACE") != null ? "brace"
                                                            : matcher.group("BRACKET") != null ? "bracket"
                                                                    : matcher.group("SEMICOLON") != null ? "semicolon"
                                                                            : matcher.group("STRING") != null ? "string"
                                                                                    : matcher.group("COMMENT") != null
                                                                                            ? "comment"
                                                                                            : null;
            /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private static StyleSpans<Collection<String>> computeErrorHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);
        parser.removeErrorListeners();

        List<javafx.scene.control.IndexRange> errors = new ArrayList<>();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                    int charPositionInLine, String msg, RecognitionException e) {
                if (offendingSymbol instanceof Token) {
                    Token token = (Token) offendingSymbol;
                    int start = token.getStartIndex();
                    int stop = token.getStopIndex() + 1;
                    if (start >= 0 && stop > start) {
                        errors.add(new javafx.scene.control.IndexRange(start, stop));
                    }
                }
            }
        });

        try {
            parser.classe();
        } catch (Exception e) {
            // Ignore parser exceptions
        }

        int lastEnd = 0;
        for (javafx.scene.control.IndexRange error : errors) {
            if (error.getStart() > lastEnd) {
                spansBuilder.add(Collections.emptyList(), error.getStart() - lastEnd);
            }
            int length = error.getEnd() - error.getStart();
            if (length > 0) {
                spansBuilder.add(Collections.singleton("error"), length);
                lastEnd = error.getEnd();
            }
        }
        if (lastEnd < text.length() || (text.isEmpty() && errors.isEmpty())) {
            spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
        }

        return spansBuilder.create();
    }

    private final CodeArea codeArea;
    private ContextMenu autoCompletionPopup;

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
     * @param id           the id of this component for javafx
     * @param defaultValue A string to place in the codeArea
     */
    public MyCodeArea(String id, String defaultValue) {
        this.setId(id);

        this.getStylesheets().add(getClass().getResource("/code_area.css").toExternalForm());

        codeArea = new CodeArea(defaultValue);
        codeArea.setId(id + "_code_area"); // for testfx

        codeArea.multiPlainChanges()
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));

        /* Add line numbers */
        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);
        /* Keep line numbers aligned with content */
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox(numberFactory.apply(line));
            hbox.setSpacing(1);
            hbox.setAlignment(Pos.CENTER);
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

        // Auto-completion
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
                if (autoCompletionPopup != null && autoCompletionPopup.isShowing()) {
                    autoCompletionPopup.hide();
                } else {
                    showAutoCompletion();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.TAB) {
                int tabSize = EditorPreferences.getInstance().getTabSize();
                String spaces = " ".repeat(tabSize);
                codeArea.replaceSelection(spaces);
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER && EditorPreferences.getInstance().isSmartIndentation()) {
                handleSmartIndentation(event);
            }
        });

        // Auto-close pairs
        codeArea.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (EditorPreferences.getInstance().isAutoClosePairs()) {
                handleAutoClose(event);
            }
        });
    }

    private void handleSmartIndentation(KeyEvent event) {
        int currentParagraph = codeArea.getCurrentParagraph();

        // Get previous line content
        if (currentParagraph >= 0) {
            String lineText = codeArea.getText(currentParagraph);
            String indentation = "";
            Matcher matcher = Pattern.compile("^\\s*").matcher(lineText);
            if (matcher.find()) {
                indentation = matcher.group();
            }

            // Check if the line ends with {
            String trimmedLine = lineText.trim();
            if (trimmedLine.endsWith("{")) {
                int tabSize = EditorPreferences.getInstance().getTabSize();
                indentation += " ".repeat(tabSize);
            }

            codeArea.replaceSelection("\n" + indentation);
            event.consume();
        }
    }

    private void handleAutoClose(KeyEvent event) {
        String character = event.getCharacter();
        int caretPosition = codeArea.getCaretPosition();
        String text = codeArea.getText();

        switch (character) {
            case "(":
                codeArea.insertText(caretPosition, "()");
                codeArea.moveTo(caretPosition + 1);
                event.consume();
                break;
            case "{":
                codeArea.insertText(caretPosition, "{}");
                codeArea.moveTo(caretPosition + 1);
                event.consume();
                break;
            case "[":
                codeArea.insertText(caretPosition, "[]");
                codeArea.moveTo(caretPosition + 1);
                event.consume();
                break;
            case "\"":
                codeArea.insertText(caretPosition, "\"\"");
                codeArea.moveTo(caretPosition + 1);
                event.consume();
                break;
            case ")":
            case "}":
            case "]":
                if (caretPosition < text.length()
                        && text.substring(caretPosition, caretPosition + 1).equals(character)) {
                    codeArea.moveTo(caretPosition + 1);
                    event.consume();
                }
                break;
        }
    }

    private void showAutoCompletion() {
        if (autoCompletionPopup != null && autoCompletionPopup.isShowing()) {
            autoCompletionPopup.hide();
        }

        String text = codeArea.getText();
        int caretPosition = codeArea.getCaretPosition();

        // Find the word before the caret
        int start = caretPosition;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        String prefix = text.substring(start, caretPosition);

        List<String> suggestions = getSuggestions(prefix);

        if (suggestions.isEmpty()) {
            return;
        }

        final int finalStart = start;
        autoCompletionPopup = new ContextMenu();
        autoCompletionPopup.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
                autoCompletionPopup.hide();
                event.consume();
            }
        });
        for (String suggestion : suggestions) {
            MenuItem item = new MenuItem(suggestion);
            item.setOnAction(e -> {
                if (suggestion.equals("main")) {
                    codeArea.replaceText(finalStart, caretPosition, "void main() {\n    \n}");
                    codeArea.moveTo(finalStart + 16); // Move caret inside braces
                } else {
                    codeArea.replaceText(finalStart, caretPosition, suggestion);
                }
            });
            autoCompletionPopup.getItems().add(item);
        }

        Optional<Bounds> bounds = codeArea.getCaretBounds();
        if (bounds.isPresent()) {
            autoCompletionPopup.show(codeArea, bounds.get().getMaxX(), bounds.get().getMaxY());
        }
    }

    private List<String> getSuggestions(String prefix) {
        List<String> allSuggestions = new ArrayList<>();
        Collections.addAll(allSuggestions, KEYWORDS);
        Collections.addAll(allSuggestions, TYPES);
        Collections.addAll(allSuggestions, FUNCTIONS);
        Collections.addAll(allSuggestions, BOOLEANS);

        // Snippets
        allSuggestions.add("main"); // We will handle expansion in the action

        return allSuggestions.stream()
                .filter(s -> s.startsWith(prefix))
                .collect(Collectors.toList());
    }

    /**
     * Function used to get the content of the code area
     * 
     * @return the code written
     */
    public String getText() {
        return codeArea.getText();
    }

    /**
     * Load text in code area
     * 
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
     * 
     * @param lineIndex line index (0-based)
     */
    public void highlightLine(int lineIndex) {
        if (lineIndex < 0)
            return;
        Platform.runLater(() -> {
            int paragraphCount = codeArea.getParagraphs().size();
            if (lineIndex >= paragraphCount)
                return;

            for (int i = 0; i < paragraphCount; i++) {
                codeArea.setParagraphStyle(i, java.util.Collections.emptyList());
            }
            codeArea.setParagraphStyle(lineIndex, java.util.Collections.singletonList("current-line"));
            codeArea.showParagraphAtTop(lineIndex);
        });
    }

    // ==== END highlight ====
    // cos of the inner style it didn't work I have removed it and i think now it
    // works for linux please check it
}

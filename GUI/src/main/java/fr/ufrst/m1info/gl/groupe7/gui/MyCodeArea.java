package fr.ufrst.m1info.gl.groupe7.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javafx.event.Event;
import javafx.scene.control.Tooltip;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.fxmisc.richtext.model.TwoDimensional;

import java.util.HashSet;
import java.util.Set;


/**
 * Enhanced code editing area for JavaFX based on {@link CodeArea}.
 * <p>
 * Provides syntax highlighting, error highlighting
 * (via ANTLR for MiniJaja), line numbering, autocompletion,
 * auto-closing of pairs and smart indentation.
 * </p>
 */
public class MyCodeArea extends AnchorPane {
    /**
     * Languages supported by the code area.
     */
    public enum Language {
        /** MiniJaja (highlighting, errors, autocompletion). */
        MINIJAJA,
        /** JajaCode (raw display without syntax highlighting). */
        JAJACODE
    }

    /** Keywords supported for MiniJaja syntax highlighting. */
    private static final String[] KEYWORDS = new String[] {
            "class", "final", "void", "main", "if", "else", "while", "return", "length"
    };
    /** MiniJaja types. */
    private static final String[] TYPES = new String[] {
            "int", "boolean"
    };
    /** MiniJaja functions exposed for autocompletion. */
    private static final String[] FUNCTIONS = new String[] {
            "write", "writeln"
    };
    /** MiniJaja boolean literals. */
    private static final String[] BOOLEANS = new String[] {
            "true", "false"
    };

    /**
     * Regex patterns used for syntax highlighting.
     */
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String TYPE_PATTERN = "\\b(" + String.join("|", TYPES) + ")\\b";
    private static final String FUNCTION_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
    private static final String BOOLEAN_PATTERN = "\\b(" + String.join("|", BOOLEANS) + ")\\b";

    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[\\]]";
    private static final String SEMICOLON_PATTERN = ";";
    /** Strings and comments (supports multi-line). */
    private static final String STRING_PATTERN = "\\\"([^\\\"\\\\\\\\]|\\\\\\\\.)*\\\"";
    private static final String COMMENT_PATTERN = "//[^\\n]*" + "|" + "/\\\\*(.|\\\\R)*?\\\\*/";

    /** Global pattern to extract syntactic elements. */
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

    /** Langage courant de la zone de code. */
    private final Language language;
    private MiniJajaSymbolListener semanticListener;


    /**
     * Computes combined highlight styles (syntax + errors).
     *
     * @param text full content of the code area
     * @return style spans to apply to characters
     */
    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        // Perform semantic analysis first to get all semantic information
        runSemanticAnalysis(text);

        StyleSpans<Collection<String>> syntaxHighlighting = computeSyntaxHighlighting(text);
        StyleSpans<Collection<String>> errorHighlighting = computeErrorHighlighting(text);
        StyleSpans<Collection<String>> functionHighlighting = computeFunctionHighlighting();
        StyleSpans<Collection<String>> unusedVarHighlighting = computeUnusedVariableHighlighting();
        StyleSpans<Collection<String>> declAfterInstrHighlighting = computeDeclarationAfterInstructionHighlighting();
        StyleSpans<Collection<String>> declAfterErrorHighlighting = buildStyleSpansFromRanges(errorDeclaredAfterInstructionRanges, "error", codeArea.getLength());


        return syntaxHighlighting
                .overlay(errorHighlighting, this::mergeStyles)
                .overlay(functionHighlighting, this::mergeStyles)
                 .overlay(unusedVarHighlighting, this::mergeStyles)
                 .overlay(declAfterInstrHighlighting, this::mergeStyles)
                 .overlay(declAfterErrorHighlighting, this::mergeStyles);
    }

    private Collection<String> mergeStyles(Collection<String> style1, Collection<String> style2) {
        Collection<String> combined = new ArrayList<>(style1);
        combined.addAll(style2);
        return combined;
    }

    private void runSemanticAnalysis(String text) {
        if (language == Language.MINIJAJA) {
            MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(text));
            lexer.removeErrorListeners();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MiniJajaParser parser = new MiniJajaParser(tokens);
            parser.removeErrorListeners();

            org.antlr.v4.runtime.tree.ParseTree tree = parser.classe();
            semanticListener = new MiniJajaSymbolListener();
            org.antlr.v4.runtime.tree.ParseTreeWalker walker = new org.antlr.v4.runtime.tree.ParseTreeWalker();
            walker.walk(semanticListener, tree);
        }
    }


    /**
     * Performs semantic analysis to find user-defined function declarations and calls.
     *
     * @return StyleSpans for user-defined functions
     */
    private StyleSpans<Collection<String>> computeFunctionHighlighting() {
        if (language == Language.MINIJAJA && semanticListener != null) {
            return buildStyleSpansFromRanges(semanticListener.getFunctionStyleRanges(), "function", codeArea.getLength());
        }
        return new StyleSpansBuilder<Collection<String>>().add(Collections.emptyList(), codeArea.getLength()).create();
    }

      /**
     * Builds StyleSpans for unused variables based on the semantic analysis.
     *
     * @return StyleSpans for unused variables.
     */
    private StyleSpans<Collection<String>> computeUnusedVariableHighlighting() {
        if (language == Language.MINIJAJA && semanticListener != null) {
            return buildStyleSpansFromRanges(semanticListener.getUnusedVariableRanges(), "unused-variable", codeArea.getLength());
        }
        return new StyleSpansBuilder<Collection<String>>().add(Collections.emptyList(), codeArea.getLength()).create();
    }

    private StyleSpans<Collection<String>> computeDeclarationAfterInstructionHighlighting() {
        if (language == Language.MINIJAJA && semanticListener != null) {
            return buildStyleSpansFromRanges(semanticListener.getDeclarationAfterInstructionRanges(), "error", codeArea.getLength());
        }
        return new StyleSpansBuilder<Collection<String>>().add(Collections.emptyList(), codeArea.getLength()).create();
    }

    private StyleSpans<Collection<String>> buildStyleSpansFromRanges(List<javafx.scene.control.IndexRange> ranges, String styleClass, int textLength) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        ranges.sort(Comparator.comparingInt(javafx.scene.control.IndexRange::getStart));
        int lastEnd = 0;

        for (javafx.scene.control.IndexRange range : ranges) {
            if (range.getStart() > lastEnd) {
                spansBuilder.add(Collections.emptyList(), range.getStart() - lastEnd);
            }
            int length = range.getLength();
            if (length > 0) {
                spansBuilder.add(Collections.singleton(styleClass), length);
            }
            lastEnd = range.getEnd();
        }
        // Always add a final span (even if zero-length) to ensure the
        // StyleSpansBuilder has at least one entry before calling create().
        if (lastEnd < textLength || textLength == 0) {
            spansBuilder.add(Collections.emptyList(), textLength - lastEnd);
        }

        return spansBuilder.create();
    }

    /**
     * Computes syntax highlighting according to the active language.
     *
     * @param text text to apply highlighting on
     * @return style spans describing CSS classes to apply
     */
    private StyleSpans<Collection<String>> computeSyntaxHighlighting(String text) {
        if (language == Language.JAJACODE) {
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            spansBuilder.add(Collections.emptyList(), text.length());
            return spansBuilder.create();
        }

        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = getStyleClass(matcher, language);
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * Determines the CSS style class to apply based on the captured group.
     *
     * @param matcher the regex matcher containing captured groups
     * @param language the active language for highlighting
     * @return the corresponding CSS class or null if no group matches
     */
    private String getStyleClass(Matcher matcher, Language language) {
        List<String> groups = List.of("KEYWORD", "TYPE", "FUNCTION", "BOOLEAN",
                                        "PAREN", "BRACE", "BRACKET", "SEMICOLON",
                                        "STRING", "COMMENT");

        for (String group : groups) {
            if (group.equals("FUNCTION") && language != Language.MINIJAJA) {
                continue;
            }
            if (matcher.group(group) != null) {
                return group.toLowerCase();
            }
        }
        return null;
    }

    /**
     * Analyzes the text to locate error regions (MiniJaja) and
     * marks them with the CSS class "error".
     *
     * @param text text to analyze
     * @return style spans for error and neutral regions
     */
    private StyleSpans<Collection<String>> computeErrorHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        List<javafx.scene.control.IndexRange> errors = new ArrayList<>();
        final List<javafx.scene.control.IndexRange> declarationAfterInstructionErrorRanges = new ArrayList<>();

        if (language == Language.MINIJAJA) {
            MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(text));
            lexer.removeErrorListeners();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MiniJajaParser parser = new MiniJajaParser(tokens);
            parser.removeErrorListeners();

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
            } catch (Exception ignored) {

            }
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

        // classify some syntax errors as 'declaration after instruction' when they match a TYPE token
        if (language == Language.MINIJAJA && semanticListener != null) {
            for (javafx.scene.control.IndexRange e : errors) {
                String tokenText = text.substring(e.getStart(), Math.min(e.getEnd(), text.length()));
                boolean isTypeToken = false;
                for (String t : TYPES) { if (t.equals(tokenText)) { isTypeToken = true; break; } }
                if ("void".equals(tokenText)) isTypeToken = true;
                if (isTypeToken && semanticListener.isOffsetInScopeWithInstruction(e.getStart())) {
                    declarationAfterInstructionErrorRanges.add(e);
                }
            }
            this.errorDeclaredAfterInstructionRanges = declarationAfterInstructionErrorRanges;
        }

        return spansBuilder.create();
    }

    /** Underlying code area. */
    private final CodeArea codeArea;
    /** Popup for autocompletion. */
    private ContextMenu autoCompletionPopup;

    /* Stores all active breakpoints by line index */
    private final Set<Integer> breakpoints = new HashSet<>();

    // Tracks declaration-after-instruction ranges reported by syntax analysis
    private List<javafx.scene.control.IndexRange> errorDeclaredAfterInstructionRanges = new ArrayList<>();

    /**
     * Crée une zone de code MiniJaja avec identifiant donné.
     *
     * @param id identifiant de ce composant pour JavaFX
     */
    public MyCodeArea(String id) {
        this(id, "", Language.MINIJAJA);
    }

    /**
     * Crée une zone de code avec le langage précisé.
     *
     * @param id       identifiant de ce composant pour JavaFX
     * @param language langage de la zone de code
     */
    public MyCodeArea(String id, Language language) {
        this(id, "", language);
    }

    /**
     * Crée une zone de code MiniJaja avec une valeur par défaut.
     *
     * @param id           identifiant de ce composant pour JavaFX
     * @param defaultValue texte initial
     */
    public MyCodeArea(String id, String defaultValue) {
        this(id, defaultValue, Language.MINIJAJA);
    }

    /**
     * Crée une zone de code avec identifiant, texte initial et langage.
     * Initialise la coloration, la numérotation des lignes, le scroll,
     * l'auto-complétion, la fermeture des paires et l'indentation.
     *
     * @param id           identifiant de ce composant pour JavaFX
     * @param defaultValue texte initial
     * @param language     langage de la zone de code
     */
    public MyCodeArea(String id, String defaultValue, Language language) {
        this.language = language;
        this.setId(id);


        codeArea = new CodeArea(defaultValue);
        codeArea.setId(id + "_code_area"); // for testfx

        codeArea.multiPlainChanges()
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));

        /* Add line numbers */
        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);
        codeArea.setParagraphGraphicFactory(createParagraphGraphicFactory(numberFactory));

        /* Add scroll pane */
        VirtualizedScrollPane<CodeArea> scroll = new VirtualizedScrollPane<>(codeArea);
        AnchorPane.setTopAnchor(scroll, 0d);
        AnchorPane.setBottomAnchor(scroll, 0d);
        AnchorPane.setLeftAnchor(scroll, 0d);
        AnchorPane.setRightAnchor(scroll, 0d);
        this.getChildren().add(scroll);
        initCaretLineHighlight();

        Tooltip declTooltip = new Tooltip("Erreur : déclaration après instructions.");
        declTooltip.getStyleClass().add("decl-tooltip");
        final javafx.beans.property.ObjectProperty<javafx.scene.control.IndexRange> current = new javafx.beans.property.SimpleObjectProperty<>(null);

        // Show tooltip when hovering over the problematic type token
        codeArea.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_MOVED, e -> {
            if (semanticListener == null) {
                declTooltip.hide();
                current.set(null);
                return;
            }
            try {
                int pos = codeArea.hit(e.getX(), e.getY()).getInsertionIndex();
                javafx.scene.control.IndexRange found = null;
                // check semantic listener ranges
                for (javafx.scene.control.IndexRange r : semanticListener.getDeclarationAfterInstructionRanges()) {
                    if (r.getStart() <= pos && pos < r.getEnd()) {
                        found = r;
                        break;
                    }
                }
                // check syntax-derived ranges
                if (found == null) {
                    for (javafx.scene.control.IndexRange r : errorDeclaredAfterInstructionRanges) {
                        if (r.getStart() <= pos && pos < r.getEnd()) {
                            found = r;
                            break;
                        }
                    }
                }
                if (found != null) {
                    if (current.get() == null || !current.get().equals(found)) {
                        declTooltip.show(codeArea, e.getScreenX() + 10, e.getScreenY() + 10);
                        current.set(found);
                    }
                } else {
                    declTooltip.hide();
                    current.set(null);
                }
            } catch (Exception ignored) {
            }
        });

        // Auto-completion
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
                if (autoCompletionPopup != null && autoCompletionPopup.isShowing()) {
                    autoCompletionPopup.hide();
                } else {
                    showManualCompletion();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.TAB) {
                String text = codeArea.getText();
                int caretPosition = codeArea.getCaretPosition();

                int start = caretPosition;
                while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
                    start--;
                }
                String prefix = text.substring(start, caretPosition);

                if (!prefix.isEmpty()) {
                    List<String> suggestions = getSuggestions(prefix);
                    if (!suggestions.isEmpty()) {
                        String completion = suggestions.get(0);
                        codeArea.replaceText(start, caretPosition, completion);
                        if (autoCompletionPopup != null && autoCompletionPopup.isShowing()) {
                            autoCompletionPopup.hide();
                        }
                        event.consume();
                        return;
                    }
                }

                // If no completion, insert spaces.
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

        // Auto-show completion popup
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            // A simple heuristic to avoid showing popup on deletion.
            if (newText.length() > oldText.length()) {
                showAutoCompletion();
            } else {
                 if (autoCompletionPopup != null) {
                    autoCompletionPopup.hide();
                }
            }
        });
    }

    private IntFunction<Node> createParagraphGraphicFactory(IntFunction<Node> numberFactory) {
        return line -> {
            HBox hbox = new HBox();

            /* Create breakpoint circle (initially hidden) */
            Circle bpCircle = new Circle(5);
            bpCircle.getStyleClass().add("breakpoint-node");
            bpCircle.setManaged(true); // always reserve space

            // restore visibility from breakpoint set (after scroll)
            bpCircle.setVisible(breakpoints.contains(line));

            /* Clicking toggles breakpoint ON/OFF */
            bpCircle.setOnMouseClicked(e -> {
                toggleBreakpoint(line, bpCircle);
                e.consume(); // Prevent click from reaching code area
            });

            /* Number label next to breakpoint circle */
            Node number = numberFactory.apply(line);

            /* Clicking on the number also toggles breakpoint */
            number.setOnMouseClicked(e -> {
                toggleBreakpoint(line, bpCircle);
                e.consume(); // Prevent click from reaching code area
            });

            hbox.getChildren().addAll(number, bpCircle);
            hbox.setSpacing(0);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(new javafx.geometry.Insets(0, 0, 0, -20));

            // fix gutter width so it does not change when scrolling or line numbers grow
            hbox.setMinWidth(57);
            hbox.setPrefWidth(57);
            hbox.setMaxWidth(57);

            // Make entire gutter area clickable and add visual feedback
            hbox.setOnMouseClicked(e -> {
                toggleBreakpoint(line, bpCircle);
                e.consume(); // Prevent click from reaching code area
            });

            // Also consume mouse pressed and released to prevent any propagation
            hbox.setOnMousePressed(Event::consume);
            hbox.setOnMouseReleased(Event::consume);

            hbox.setCursor(javafx.scene.Cursor.HAND); // Visual feedback for clickability

            StackPane stack;

            if (line == 0) {
                Rectangle rectangle = new Rectangle();
                rectangle.getStyleClass().add("lineno");
                rectangle.widthProperty().bind(hbox.widthProperty());
                rectangle.heightProperty().bind(codeArea.heightProperty());
                StackPane.setAlignment(rectangle, Pos.TOP_LEFT);
                stack = new StackPane(rectangle, hbox);
            } else {
                stack = new StackPane(hbox);
            }

            // lock stack pane width to keep gutter width stable
            stack.setMinWidth(70);
            stack.setPrefWidth(70);
            stack.setMaxWidth(70);

            // Make the StackPane also clickable and consume all events
            stack.setOnMouseClicked(e -> {
                toggleBreakpoint(line, bpCircle);
                e.consume();
            });
            stack.setOnMousePressed(Event::consume);
            stack.setOnMouseReleased(Event::consume);
            stack.setCursor(javafx.scene.Cursor.HAND);

            return stack;
        };
    }


    private void showManualCompletion() {
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
            if (autoCompletionPopup != null) {
                autoCompletionPopup.hide();
            }
            return;
        }

        displaySuggestions(suggestions, start, caretPosition);
    }

    /**
     * Gère l'indentation intelligente à l'appui de Entrée.
     * Ajoute l'indentation de la ligne précédente et augmente après '{'.
     *
     * @param event événement clavier ENTER consommé
     */
    private void handleSmartIndentation(KeyEvent event) {
        int caretPosition = codeArea.getCaretPosition();
        String text = codeArea.getText();

        // Case 1: Enter is pressed between {}
        if (caretPosition > 0 && caretPosition < text.length() &&
            text.charAt(caretPosition - 1) == '{' && text.charAt(caretPosition) == '}') {

            int currentParagraph = codeArea.getCurrentParagraph();
            String lineText = codeArea.getText(currentParagraph);
            
            // Get base indentation of the current line
            String baseIndentation = "";
            Matcher matcher = Pattern.compile("^\\s*").matcher(lineText);
            if (matcher.find()) {
                baseIndentation = matcher.group();
            }
            
            int tabSize = EditorPreferences.getInstance().getTabSize();
            String indentedLine = baseIndentation + " ".repeat(tabSize);


            String toInsert = "\n" + indentedLine + "\n" + baseIndentation;
            

            codeArea.replaceSelection(toInsert);

            codeArea.moveTo(caretPosition + 1 + indentedLine.length());
            event.consume();
            return;
        }

        // Case 2: Original logic for line ending with {
        int currentParagraph = codeArea.getCurrentParagraph();
        if (currentParagraph >= 0) {
            String lineText = codeArea.getText(currentParagraph);
            String indentation = "";
            Matcher matcher = Pattern.compile("^\\s*").matcher(lineText);
            if (matcher.find()) {
                indentation = matcher.group();
            }

            String trimmedLine = lineText.substring(0, codeArea.getCaretColumn()).trim();
            if (trimmedLine.endsWith("{")) {
                int tabSize = EditorPreferences.getInstance().getTabSize();
                indentation += " ".repeat(tabSize);
            }

            codeArea.replaceSelection("\n" + indentation);
            event.consume();
        }
    }

    /**
     * Gère la fermeture automatique des paires: (), {}, [], "".
     * Saute la paire fermante si déjà présente.
     *
     * @param event événement clavier typé
     */
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

    /**
     * Affiche un menu d'auto-complétion près du caret en fonction du préfixe
     * courant. CTRL+Espace pour basculer.
     */
    private void showAutoCompletion() {
        String text = codeArea.getText();
        int caretPosition = codeArea.getCaretPosition();

        // Find the word before the caret
        int start = caretPosition;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        String prefix = text.substring(start, caretPosition);

        // For automatic completion, only trigger if there is a prefix.
        if (prefix.isEmpty()) {
            if (autoCompletionPopup != null) {
                autoCompletionPopup.hide();
            }
            return;
        }

        List<String> suggestions = getSuggestions(prefix);

        if (suggestions.isEmpty() || (suggestions.size() == 1 && suggestions.get(0).equals(prefix))) {
            if (autoCompletionPopup != null) {
                autoCompletionPopup.hide();
            }
            return;
        }

        displaySuggestions(suggestions, start, caretPosition);
    }

    private void displaySuggestions(List<String> suggestions, int start, int caretPosition) {
        final int finalStart = start;
        List<MenuItem> menuItems = new ArrayList<>();
        for (String suggestion : suggestions) {
            MenuItem item = new MenuItem(suggestion);
            item.setOnAction(e -> {
                codeArea.replaceText(finalStart, caretPosition, suggestion);
                int parenIndex = suggestion.indexOf('(');
                if (parenIndex >= 0) {
                    codeArea.moveTo(finalStart + parenIndex + 1);
                } else {
                    codeArea.moveTo(finalStart + suggestion.length());
                }
            });
            menuItems.add(item);
        }

        if (autoCompletionPopup == null) {
            autoCompletionPopup = new ContextMenu();
            autoCompletionPopup.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
                    autoCompletionPopup.hide();
                    event.consume();
                } else if (event.getCode() == KeyCode.TAB) {
                    if (!autoCompletionPopup.getItems().isEmpty()) {
                        // We need to find the prefix to replace, using the most current text
                        int currentCaret = codeArea.getCaretPosition();
                        int currentStart = currentCaret;
                        String currentText = codeArea.getText();
                        while (currentStart > 0 && Character.isJavaIdentifierPart(currentText.charAt(currentStart - 1))) {
                            currentStart--;
                        }
                        // Use the text from the first item
                        String suggestion = autoCompletionPopup.getItems().get(0).getText();
                        codeArea.replaceText(currentStart, currentCaret, suggestion);
                        int parenIndex = suggestion.indexOf('(');
                        if (parenIndex >= 0) {
                            codeArea.moveTo(currentStart + parenIndex + 1);
                        } else {
                            codeArea.moveTo(currentStart + suggestion.length());
                        }
                    }
                    autoCompletionPopup.hide();
                    event.consume();
                }
            });
        }

        autoCompletionPopup.getItems().setAll(menuItems);

        if (!autoCompletionPopup.isShowing()) {
            Optional<Bounds> bounds = codeArea.getCaretBounds();
            bounds.ifPresent(b -> autoCompletionPopup.show(codeArea, b.getMaxX(), b.getMaxY()));
        }
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
     * Retourne les suggestions filtrées par préfixe pour le langage actif.
     *
     * @param prefix préfixe tapé avant le caret
     * @return liste de suggestions correspondantes
     */
    private List<String> getSuggestions(String prefix) {
        // We'll build ordered suggestions: methods first, then scoped variables, then base keywords/types/functions
        java.util.List<String> result = new java.util.ArrayList<>();
        if (language == Language.MINIJAJA) {
            if (prefix != null) {
                String text = codeArea.getText();
                int caretPosition = codeArea.getCaretPosition();
                int start = caretPosition;
                while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
                    start--;
                }

                int idx = start - 1;
                while (idx >= 0 && Character.isWhitespace(text.charAt(idx))) idx--;
                int endPrev = idx;
                while (idx >= 0 && Character.isJavaIdentifierPart(text.charAt(idx))) idx--;
                int startPrev = idx + 1;
                String prevToken = (endPrev >= startPrev && startPrev >= 0) ? text.substring(startPrev, endPrev + 1) : "";

                boolean isTypeKeyword = false;
                for (String t : TYPES) {
                    if (t.equals(prevToken)) { isTypeKeyword = true; break; }
                }

                if ("void".equals(prevToken)) isTypeKeyword = true;

                if (isTypeKeyword) {
                    return result;
                }
            }

            // 1) methods
            if (semanticListener != null) {
                for (String mth : semanticListener.getDeclaredMethods()) {
                    String suggestion = mth + "()";
                    if (prefix == null || mth.startsWith(prefix)) {
                        result.add(suggestion);
                    }
                }
            }

            // 2) variables (scope-aware, ordered)
            boolean addVariables = false;
            if (semanticListener != null && prefix != null) {
                // Determine whether we are in a declaration context (after a type)
                String text = codeArea.getText();
                int caretPosition = codeArea.getCaretPosition();
                int start = caretPosition;
                while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
                    start--;
                }

                // Find the previous token (word) before the prefix
                int idx = start - 1;
                while (idx >= 0 && Character.isWhitespace(text.charAt(idx))) idx--;
                int endPrev = idx;
                while (idx >= 0 && Character.isJavaIdentifierPart(text.charAt(idx))) idx--;
                int startPrev = idx + 1;
                String prevToken = (endPrev >= startPrev && startPrev >= 0) ? text.substring(startPrev, endPrev + 1) : "";

                boolean isTypeKeyword = false;
                for (String t : TYPES) {
                    if (t.equals(prevToken)) { isTypeKeyword = true; break; }
                }
                addVariables = !isTypeKeyword;

                if (addVariables) {
                    for (String var : semanticListener.getVisibleVariablesOrdered(codeArea.getCaretPosition())) {
                        if (var.startsWith(prefix) && !result.contains(var)) {
                            result.add(var);
                        }
                    }
                }
            }

            // 3) base suggestions (keywords, types, functions, booleans)
            List<String> base = new ArrayList<>();
            Collections.addAll(base, KEYWORDS);
            Collections.addAll(base, TYPES);
            Collections.addAll(base, FUNCTIONS);
            Collections.addAll(base, BOOLEANS);

            for (String b : base) {
                String suggestion = b;
                boolean isFunc = false;
                for (String func : FUNCTIONS) {
                    if (func.equals(b)) { isFunc = true; break; }
                }
                if ("while".equals(b)) isFunc = true;
                if (isFunc) suggestion = b + "()";

                if ((prefix == null || b.startsWith(prefix)) && !result.contains(suggestion)) {
                    result.add(suggestion);
                }
            }
        }

        return result;
    }

    /**
     * Retourne le contenu de la zone de code.
     *
     * @return le code écrit
     */
    public String getText() {
        return codeArea.getText();
    }

    /**
     * Charge du texte dans la zone de code.
     *
     * @param string texte à charger
     */
    public void loadText(String string) {
        codeArea.replaceText(string);
    }

    /**
     * Désactive l'édition dans la zone de code.
     */
    public void disable() {
        codeArea.setEditable(false);
    }

    // START highlight
    /**
     * Met en évidence une ligne avec la classe CSS "current-line".
     * Utilisée durant le débogage pour indiquer la ligne d'exécution courante.
     *
     * @param lineIndex index de ligne (base 0)
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

    /* Highlight caret line */
    private void highlightCaretLine() {
        int caret = codeArea.getCaretPosition();
        int currentLine = codeArea.offsetToPosition(caret, TwoDimensional.Bias.Backward).getMajor();

        int paragraphCount = codeArea.getParagraphs().size();
        for (int i = 0; i < paragraphCount; i++) {
            codeArea.setParagraphStyle(i, Collections.emptyList());
        }

        if (currentLine >= 0 && currentLine < paragraphCount) {
            codeArea.setParagraphStyle(currentLine, Collections.singletonList("current-caret-line"));
        }
    }

    private void initCaretLineHighlight() {
        codeArea.caretPositionProperty().addListener((obs, oldV, newV) -> highlightCaretLine());
        codeArea.focusedProperty().addListener((obs, oldV, newV) -> highlightCaretLine());
        codeArea.textProperty().addListener((obs, oldV, newV) -> highlightCaretLine());
    }
}

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

import javafx.event.Event;
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
 * Zone d'édition de code enrichie pour JavaFX basée sur {@link CodeArea}.
 * <p>
 * Fournit la coloration syntaxique, la mise en évidence des erreurs
 * (via ANTLR pour MiniJaja), la numérotation des lignes, l'auto-complétion,
 * la fermeture automatique des paires et l'indentation intelligente.
 * </p>
 */
public class MyCodeArea extends AnchorPane {
    /**
     * Langages supportés par la zone de code.
     */
    public enum Language {
        /** MiniJaja (coloration, erreurs, auto-complétion). */
        MINIJAJA,
        /** JajaCode (affichage brut sans coloration syntaxique). */
        JAJACODE
    }

    /** Mots-clés pris en charge pour la coloration syntaxique MiniJaja. */
    private static final String[] KEYWORDS = new String[] {
            "class", "final", "void", "main", "if", "else", "while", "return", "length"
    };
    /** Types MiniJaja. */
    private static final String[] TYPES = new String[] {
            "int", "boolean"
    };
    /** Fonctions MiniJaja exposées pour l'auto-complétion. */
    private static final String[] FUNCTIONS = new String[] {
            "write", "writeln"
    };
    /** Littéraux booléens MiniJaja. */
    private static final String[] BOOLEANS = new String[] {
            "true", "false"
    };

    /**
     * Modèles de recherche pour la coloration syntaxique.
     */
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String TYPE_PATTERN = "\\b(" + String.join("|", TYPES) + ")\\b";
    private static final String FUNCTION_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
    private static final String BOOLEAN_PATTERN = "\\b(" + String.join("|", BOOLEANS) + ")\\b";

    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[\\]]";
    private static final String SEMICOLON_PATTERN = ";";
    /** Chaînes et commentaires (compatible multi-lignes). */
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    /** Pattern global pour extraire les éléments syntaxiques. */
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

    /**
     * Calcule les styles de surbrillance combinés (syntaxe + erreurs).
     *
     * @param text contenu complet de la zone de code
     * @return spans de style pour appliquer aux caractères
     */
    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpans<Collection<String>> syntaxHighlighting = computeSyntaxHighlighting(text);
        StyleSpans<Collection<String>> errorHighlighting = computeErrorHighlighting(text);
        StyleSpans<Collection<String>> semanticHighlighting = computeSemanticHighlighting(text); // pour higlight les fonctions

        return syntaxHighlighting
                .overlay(errorHighlighting, (style1, style2) -> {
                    Collection<String> combined = new ArrayList<>(style1);
                    combined.addAll(style2);
                    return combined;
                })
                .overlay(semanticHighlighting, (style1, style2) -> {
                    Collection<String> combined = new ArrayList<>(style1);
                    combined.addAll(style2);
                    return combined;
                });
    }

    /**
     * Performs semantic analysis to find user-defined function declarations and calls.
     *
     * @param text text to analyze
     * @return StyleSpans for user-defined functions
     */
    private StyleSpans<Collection<String>> computeSemanticHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        if (language == Language.MINIJAJA) {
            // Run the parser and listener
            MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(text));
            lexer.removeErrorListeners();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MiniJajaParser parser = new MiniJajaParser(tokens);
            parser.removeErrorListeners();

            org.antlr.v4.runtime.tree.ParseTree tree = parser.classe();
            MiniJajaSymbolListener listener = new MiniJajaSymbolListener();
            org.antlr.v4.runtime.tree.ParseTreeWalker walker = new org.antlr.v4.runtime.tree.ParseTreeWalker();
            walker.walk(listener, tree);

            // Get the ranges and build the StyleSpans
            List<javafx.scene.control.IndexRange> functionRanges = listener.getFunctionStyleRanges();
            int lastEnd = 0;
            for (javafx.scene.control.IndexRange range : functionRanges) {
                if (range.getStart() > lastEnd) {
                    spansBuilder.add(Collections.emptyList(), range.getStart() - lastEnd);
                }
                int length = range.getLength();
                if (length > 0) {
                    spansBuilder.add(Collections.singleton("function"), length);
                }
                lastEnd = range.getEnd();
            }
            if (lastEnd < text.length()) {
                spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
            }
        } else {
             spansBuilder.add(Collections.emptyList(), text.length());
        }

        return spansBuilder.create();
    }

    /**
     * Calcule la coloration syntaxique selon le langage.
     *
     * @param text texte sur lequel appliquer la coloration
     * @return spans de style décrivant les classes CSS à appliquer
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
            /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * Détermine la classe de style CSS à appliquer en fonction du groupe capturé.
     *
     * @param matcher le matcher regex contenant les groupes capturés
     * @param language le langage actif pour la coloration
     * @return la classe CSS correspondante ou null si aucun groupe ne correspond
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
     * Analyse le texte pour localiser les régions en erreur (MiniJaja) et
     * les marque avec la classe CSS "error".
     *
     * @param text texte à analyser
     * @return spans de style pour les erreurs et zones neutres
     */
    private StyleSpans<Collection<String>> computeErrorHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        List<javafx.scene.control.IndexRange> errors = new ArrayList<>();

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

        return spansBuilder.create();
    }

    /** Zone de code sous-jacente. */
    private final CodeArea codeArea;
    /** Popup pour l'auto-complétion. */
    private ContextMenu autoCompletionPopup;

    /* Stores all active breakpoints by line index */
    private final Set<Integer> breakpoints = new HashSet<>();

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

        /* Keep line numbers aligned with content */
        IntFunction<Node> graphicFactory = line -> {
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

        codeArea.setParagraphGraphicFactory(graphicFactory);

        /* Add scroll pane */
        VirtualizedScrollPane<CodeArea> scroll = new VirtualizedScrollPane<>(codeArea);
        AnchorPane.setTopAnchor(scroll, 0d);
        AnchorPane.setBottomAnchor(scroll, 0d);
        AnchorPane.setLeftAnchor(scroll, 0d);
        AnchorPane.setRightAnchor(scroll, 0d);
        this.getChildren().add(scroll);
        initCaretLineHighlight();

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
        List<String> allSuggestions = new ArrayList<>();
        if (language == Language.MINIJAJA) {
            Collections.addAll(allSuggestions, KEYWORDS);
            Collections.addAll(allSuggestions, TYPES);
            Collections.addAll(allSuggestions, FUNCTIONS);
            Collections.addAll(allSuggestions, BOOLEANS);
        }

        return allSuggestions.stream()
                .filter(s -> s.startsWith(prefix))
                .collect(Collectors.toList());
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

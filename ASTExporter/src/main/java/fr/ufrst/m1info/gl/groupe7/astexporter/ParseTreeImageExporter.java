package fr.ufrst.m1info.gl.groupe7.astexporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SyntaxErrorListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

/**
 * Utilitaire en ligne de commande pour parser une chaîne MiniJaja et exporter
 * l'arbre syntaxique en image PNG.
 * Usage:
 * java -cp <classpath>
 * fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ParseTreeImageExporter "class
 * A { }" arbre.png
 * java -cp <classpath>
 * fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ParseTreeImageExporter @chemin/fichier.mjj
 * arbre.png
 * Si le deuxième argument est omis, le fichier sera sauvegardé sous
 * parse_tree.png.
 */
public class ParseTreeImageExporter {

    private static final Logger logger = LoggerFactory.getLogger(ParseTreeImageExporter.class);

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int run(String[] args) {
        if (args.length < 1) {
            logger.error("Usage: ParseTreeImageExporter <code|minijaja|@fichier> [sortie.png]");
            return 1;
        }

        String inputArg = args[0];
        String outputFile = args.length >= 2 ? args[1] : "target/ast-images/parse_tree.png";

        String source;
        try {
            source = resolveSource(inputArg);
        } catch (IOException e) {
            logger.error("Erreur lecture source: {}", e.getMessage());
            return 2;
        }

        if (source.isBlank()) {
            logger.error("La source MiniJaja est vide.");
            return 3;
        }

        DiagnosticCollector collector = new DiagnosticCollector();
        SyntaxErrorListener sel = new SyntaxErrorListener(collector,
                inputArg.startsWith("@") ? inputArg.substring(1) : null);

        CharStream cs = CharStreams.fromString(source);
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        sel.register(lexer);
        sel.register(parser);

        ParseTree tree = parser.classe();

        if (collector.hasErrors()) {
            logger.error("Des erreurs de syntaxe ont été détectées: {}", collector.formatDiagnostics());
            logger.error("Aucune image générée (arrêt sur erreurs).");
            return 4;
        }

        try {
            // Tentative de construction et export de l'AST
            MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
            AstNode astRoot = visitor.visit(tree);
            exportAstImage(astRoot, Path.of(outputFile));
            logger.info("AST exporté dans: {}", outputFile);
        } catch (Exception e) {
            logger.error("Erreur lors de la construction de l'AST ({}). Export de l'arbre CST à la place.", e.getMessage());
            try {
                exportTreeImage(tree, Path.of(outputFile));
                logger.info("Arbre CST exporté dans: {}", outputFile);
            } catch (IOException ex) {
                logger.error("Erreur lors de l'export de l'image: {}", ex.getMessage());
                return 5;
            }
        }
        return 0;
    }

    private static String resolveSource(String arg) throws IOException {
        if (arg.startsWith("@")) {
            Path p = Path.of(arg.substring(1));
            if (!Files.exists(p)) {
                throw new IOException("Fichier introuvable: " + p);
            }
            return Files.readString(p, StandardCharsets.UTF_8);
        }
        return arg;
    }

    /**
     * Calcule la largeur et la hauteur nécessaires pour dessiner l'arbre.
     */
    private static Dimension computeTreeSize(Tree tree, Graphics2D g, int hGap, int vGap, Font font) {
        if (tree == null)
            return new Dimension(0, 0);
        FontRenderContext frc = g.getFontRenderContext();
        String label = sanitizeNode(tree);
        Rectangle2D bounds = font.getStringBounds(label, frc);
        int width = (int) bounds.getWidth() + 10; // padding
        int height = (int) bounds.getHeight() + 6;
        int childCount = tree.getChildCount();
        if (childCount == 0) {
            return new Dimension(width, height);
        }
        int totalChildrenWidth = 0;
        int maxChildHeight = 0;
        for (int i = 0; i < childCount; i++) {
            Dimension d = computeTreeSize(tree.getChild(i), g, hGap, vGap, font);
            totalChildrenWidth += d.width;
            if (i < childCount - 1)
                totalChildrenWidth += hGap;
            maxChildHeight = Math.max(maxChildHeight, d.height);
        }
        int totalWidth = Math.max(width, totalChildrenWidth);
        int totalHeight = height + vGap + maxChildHeight;
        return new Dimension(totalWidth, totalHeight);
    }

    private static void drawTree(Tree tree, Graphics2D g, int x, int y, int availableWidth, int hGap, int vGap,
            Font font) {
        if (tree == null)
            return;
        FontRenderContext frc = g.getFontRenderContext();
        String label = sanitizeNode(tree);
        Rectangle2D bounds = font.getStringBounds(label, frc);
        int nodeWidth = (int) bounds.getWidth() + 10;
        int nodeHeight = (int) bounds.getHeight() + 6;
        int nodeX = x + (availableWidth - nodeWidth) / 2;


        // Dessin du rectangle du noeud
        g.setColor(new Color(235, 242, 255));
        g.fillRoundRect(nodeX, y, nodeWidth, nodeHeight, 8, 8);
        g.setColor(new Color(60, 100, 160));
        g.drawRoundRect(nodeX, y, nodeWidth, nodeHeight, 8, 8);
        g.drawString(label, nodeX + 5, y + nodeHeight - 8);

        int childCount = tree.getChildCount();
        if (childCount == 0)
            return;

        // Calcul largeur totale des enfants
        int[] childWidths = new int[childCount];
        int totalChildrenWidth = 0;
        int maxChildHeight = 0;
        for (int i = 0; i < childCount; i++) {
            Dimension d = computeTreeSize(tree.getChild(i), g, hGap, vGap, font);
            childWidths[i] = d.width;
            totalChildrenWidth += d.width;
            if (i < childCount - 1)
                totalChildrenWidth += hGap;
            maxChildHeight = Math.max(maxChildHeight, d.height);
        }

        int childX = x + (availableWidth - totalChildrenWidth) / 2;
        int childY = y + nodeHeight + vGap;

        int centerParentX = nodeX + nodeWidth / 2;
        int parentBottomY = y + nodeHeight;

        for (int i = 0; i < childCount; i++) {
            Tree child = tree.getChild(i);
            int cw = childWidths[i];
            int childCenterX = childX + cw / 2;
            // Ligne vers l'enfant
            g.setColor(new Color(100, 120, 150));
            g.drawLine(centerParentX, parentBottomY, childCenterX, childY);
            drawTree(child, g, childX, childY, cw, hGap, vGap, font);
            childX += cw + hGap;
        }
    }

    private static String sanitizeNode(Tree t) {
        String s = t.toString();
        // Réduction du bruit ANTLR pour les tokens
        if (s.startsWith("[") && s.contains("]")) {
            // token format: [@TOKEN...] => garder type
            int spaceIdx = s.indexOf(' ');
            if (spaceIdx > 0) {
                return s.substring(1, spaceIdx);
            }
        }
        return s;
    }

    private static void exportTreeImage(ParseTree tree, Path output) throws IOException {
        // Font et gaps
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        BufferedImage tmp = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2tmp = tmp.createGraphics();
        g2tmp.setFont(font);
        Dimension size = computeTreeSize(tree, g2tmp, 25, 40, font);
        g2tmp.dispose();

        int width = Math.min(Math.max(size.width + 40, 200), 8000);
        int height = Math.min(Math.max(size.height + 40, 200), 8000);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setFont(font);

        drawTree(tree, g2, 20, 20, size.width, 25, 40, font);
        g2.dispose();

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        ImageIO.write(image, "png", output.toFile());
    }

    private static void exportAstImage(AstNode node, Path output) throws IOException {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        BufferedImage tmp = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2tmp = tmp.createGraphics();
        g2tmp.setFont(font);
        Dimension size = computeAstSize(node, g2tmp, 25, 40, font);
        g2tmp.dispose();

        int width = Math.min(Math.max(size.width + 40, 200), 8000);
        int height = Math.min(Math.max(size.height + 40, 200), 8000);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setFont(font);

        drawAst(node, g2, 20, 20, size.width, 25, 40, font);
        g2.dispose();

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        ImageIO.write(image, "png", output.toFile());
    }

    private static Dimension computeAstSize(AstNode node, Graphics2D g, int hGap, int vGap, Font font) {
        if (node == null)
            return new Dimension(0, 0);
        FontRenderContext frc = g.getFontRenderContext();
        String label = node.toString();
        Rectangle2D bounds = font.getStringBounds(label, frc);
        int width = (int) bounds.getWidth() + 10;
        int height = (int) bounds.getHeight() + 6;

        List<AstNode> children = getAstChildren(node);
        if (children.isEmpty()) {
            return new Dimension(width, height);
        }

        int totalChildrenWidth = 0;
        int maxChildHeight = 0;
        for (int i = 0; i < children.size(); i++) {
            Dimension d = computeAstSize(children.get(i), g, hGap, vGap, font);
            totalChildrenWidth += d.width;
            if (i < children.size() - 1)
                totalChildrenWidth += hGap;
            maxChildHeight = Math.max(maxChildHeight, d.height);
        }
        int totalWidth = Math.max(width, totalChildrenWidth);
        int totalHeight = height + vGap + maxChildHeight;
        return new Dimension(totalWidth, totalHeight);
    }

    private static void drawAst(AstNode node, Graphics2D g, int x, int y, int availableWidth, int hGap, int vGap,
            Font font) {
        if (node == null)
            return;
        FontRenderContext frc = g.getFontRenderContext();
        String label = node.toString();
        Rectangle2D bounds = font.getStringBounds(label, frc);
        int nodeWidth = (int) bounds.getWidth() + 10;
        int nodeHeight = (int) bounds.getHeight() + 6;
        int nodeX = x + (availableWidth - nodeWidth) / 2;


        g.setColor(new Color(235, 255, 242));
        g.fillRoundRect(nodeX, y, nodeWidth, nodeHeight, 8, 8);
        g.setColor(new Color(60, 160, 100));
        g.drawRoundRect(nodeX, y, nodeWidth, nodeHeight, 8, 8);
        g.setColor(Color.BLACK);
        g.drawString(label, nodeX + 5, y + nodeHeight - 8);

        List<AstNode> children = getAstChildren(node);
        if (children.isEmpty())
            return;

        int[] childWidths = new int[children.size()];
        int totalChildrenWidth = 0;
        for (int i = 0; i < children.size(); i++) {
            Dimension d = computeAstSize(children.get(i), g, hGap, vGap, font);
            childWidths[i] = d.width;
            totalChildrenWidth += d.width;
            if (i < children.size() - 1)
                totalChildrenWidth += hGap;
        }

        int childX = x + (availableWidth - totalChildrenWidth) / 2;
        int childY = y + nodeHeight + vGap;
        int centerParentX = nodeX + nodeWidth / 2;
        int parentBottomY = y + nodeHeight;

        for (int i = 0; i < children.size(); i++) {
            AstNode child = children.get(i);
            int cw = childWidths[i];
            int childCenterX = childX + cw / 2;
            g.setColor(new Color(100, 150, 120));
            g.drawLine(centerParentX, parentBottomY, childCenterX, childY);
            drawAst(child, g, childX, childY, cw, hGap, vGap, font);
            childX += cw + hGap;
        }
    }

    private static List<AstNode> getAstChildren(AstNode node) {
        List<AstNode> children = new ArrayList<>();
        Iterable<AstNode> iter = node.getChildren();
        if (iter != null) {
            for (AstNode child : iter) {
                children.add(child);
            }
        }
        if (children.isEmpty()) {
            Class<?> cls = node.getClass();
            while (cls != null && AstNode.class.isAssignableFrom(cls)) {
                for (Field f : cls.getDeclaredFields()) {
                    f.setAccessible(true);
                    try {
                        Object val = f.get(node);
                        if (val instanceof AstNode) {
                            children.add((AstNode) val);
                        } else if (val instanceof Iterable) {
                            for (Object item : (Iterable<?>) val) {
                                if (item instanceof AstNode) {
                                    children.add((AstNode) item);
                                }
                            }
                        } else if (val instanceof AstNode[]) {
                            children.addAll(Arrays.asList((AstNode[]) val));
                        }
                    } catch (IllegalAccessException ignored) {}
                }
                cls = cls.getSuperclass();
            }
        }
        return children;
    }
}

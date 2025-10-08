package fr.ufrst.m1info.gl.groupe7;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JajaCodeAnalyzer {
    public static void main(String[] args) {
        Path path = Paths.get(args[0]).toAbsolutePath().normalize();
        try (InputStream input = Files.newInputStream(path)) {
            CharStream charStream = CharStreams.fromStream(input, StandardCharsets.UTF_8);
            JajaCodeLexer lexer = new JajaCodeLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JajaCodeParser parser = new JajaCodeParser(tokens);

            ParseTree tree = parser.classe();

            ParseTreeWalker walker = new ParseTreeWalker();
            JajaCodeListener listener = new JajaCodeListener();
            walker.walk(listener, tree);

            System.out.println("Analyse terminée avec succès");
        } catch (NoSuchFileException e) {
            System.err.println("Fichier introuvable: " + e.getFile());
        } catch (IOException e) {
            System.err.println("Erreur d'E/S: " + e.getMessage());
        }

    }
}
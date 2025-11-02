package fr.ufrst.m1info.gl.groupe7.compiler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode.MiniJajaCompilerVisitor;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.Memoire.SymbolTable;
import fr.ufrst.m1info.gl.groupe7.Memoire.Stacks;

public class Main {

    /**
     * Main of the compiler.
     *
     * @param args the list of arguments.
     */
    public static void main(String[] args) {
        new Main(args);
    }

    private Main(String[] args) {
        Options options = getOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                printHelp(options, null, 0);
                return; // dead code because printHelp always System.exit
            }

            if (cmd.hasOption("f") && !cmd.hasOption("o")) {
                throw new ParseException("You can't use the -f option without the -o option");
            }
        } catch (ParseException e) {
            printHelp(options, e.getMessage(), 1);
            return; // dead code because printHelp always System.exit
        }

        // could be null if the options are not given.
        String inputFile = cmd.getOptionValue("i");
        String outputFilepath = cmd.getOptionValue("o");

        if (outputFilepath != null) { // -o option is given.
            boolean doesOutputFileExist = Files.exists(Path.of(outputFilepath));
            if (doesOutputFileExist && !cmd.hasOption("f")) {
                System.err.println(
                        "Output file already exists: '" + outputFilepath + "'. \nUse -f option to override it.");
                System.exit(1);
            }
        }

        try (InputStream inStream = (inputFile != null) ? new FileInputStream(inputFile) : System.in;
                OutputStream outStream = (outputFilepath != null) ? new FileOutputStream(outputFilepath) : System.out) {

            CharStream cs = CharStreams.fromStream(inStream);
            MiniJajaCompilerVisitor compiler = getMiniJajaCompilerVisitor(cs);
            outStream.write(compiler.getJajaCodeBuilder().toString().getBytes());
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Get the options of the compiler.
     *
     * @return the options.
     */
    private static Options getOptions() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Print help");
        Option output = new Option("o", "output", true, "Output file (if not present, uses stdout)");
        Option force = new Option("f", "force", false, "Force output, if the file exists, override it");
        Option input = new Option("i", "input", true, "Input file (if not present, uses stdin)");

        options.addOption(help);
        options.addOption(output);
        options.addOption(force);
        options.addOption(input);
        return options;
    }

    private MiniJajaCompilerVisitor getMiniJajaCompilerVisitor(CharStream cs) {
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser mjjparser = new MiniJajaParser(tokens);
        MiniJajaParser.ClasseContext parseTree = mjjparser.classe();

        SymbolTable symbolTable = new SymbolTable();
        Stacks stack = new Stacks();
        MiniJajaInterpreterVisitor miniJajaVisitor = new MiniJajaInterpreterVisitor(stack);
        ClasseNode ast = (ClasseNode) miniJajaVisitor.visit(parseTree);

        MiniJajaCompilerVisitor compiler = new MiniJajaCompilerVisitor(symbolTable);
        compiler.visit(ast);
        return compiler;
    }

    private void printHelp(Options options, String errMessage, int exitCode) {
        if (errMessage != null) {
            System.err.println("Error: " + errMessage);
            System.err.println();
        }

        String jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String jarName = jarPath.substring(jarPath.lastIndexOf('/') + 1);

        HelpFormatter helpFormatter = HelpFormatter.builder().get();
        String launchSyntax = "java -jar " + jarName;
        String header = "Compile a MiniJaja program into JajaCode";

        try {
            helpFormatter.printHelp(launchSyntax, header, options, "", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.exit(exitCode);
    }

}

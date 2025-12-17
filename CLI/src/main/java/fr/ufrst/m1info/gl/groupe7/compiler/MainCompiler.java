package fr.ufrst.m1info.gl.groupe7.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompiler;
import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MiniJajaCompilerVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Walker;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;

public class MainCompiler {

    private static final Logger logger = LoggerFactory.getLogger(MainCompiler.class);

    /**
     * Main of the compiler.
     *
     * @param args the list of arguments.
     */
    public static void main(String[] args) {
        new MainCompiler(args);
    }

    private MainCompiler(String[] args) {
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
        boolean isInterpretMode = cmd.hasOption("interpret");
        String debugOption = cmd.getOptionValue("debug");

        try (InputStream inStream = (inputFile != null) ? new FileInputStream(inputFile) : System.in) {
            if (isInterpretMode) {
                // Interpret mode
                interpretMiniJaja(inStream, debugOption);
            } else {
                // Compile mode
                if (outputFilepath != null) { // -o option is given.
                    boolean doesOutputFileExist = Files.exists(Path.of(outputFilepath));
                    if (doesOutputFileExist && !cmd.hasOption("f")) {
                        logger.error("Output file already exists: '{}'. \nUse -f option to override it.", outputFilepath);
                        System.exit(1);
                    }
                }
                
                try (OutputStream outStream = (outputFilepath != null) ? new FileOutputStream(outputFilepath) : System.out) {
                    MiniJajaCompilerVisitor compiler = MiniJajaCompiler.getMiniJajaCompilerVisitorFromStream(inStream);
                    outStream.write(compiler.getJajaCodeBuilder().toString().getBytes());
                }
            }
        } catch (IOException e) {
            logger.error("File error: {}", e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Interpret a MiniJaja program with optional debugging.
     *
     * @param inStream the input stream containing the MiniJaja program
     * @param debugOption the debug option (null for no debug, "step" for step-by-step, "b <line>" for breakpoint)
     */
    private void interpretMiniJaja(InputStream inStream, String debugOption) {
        try {
            // Read program from input stream
            String program = new String(inStream.readAllBytes());
            
            // Lexing and parsing
            CharStream cs = CharStreams.fromString(program);
            MiniJajaLexer lexer = new MiniJajaLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MiniJajaParser parser = new MiniJajaParser(tokens);
            
            // Build AST
            ParseTree tree = parser.classe();
            MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
            AstNode astRoot = visitor.visit(tree);
            
            if (astRoot == null) {
                logger.error("Failed to build AST");
                System.exit(1);
            }
            
            // Setup debug controller
            Debug debug = new Debug();
            if (debugOption != null) {
                if (debugOption.equalsIgnoreCase("step")) {
                    debug.setMode(Debug.Mode.STEP_BY_STEP);
                    logger.info("Debug mode: STEP_BY_STEP");
                } else if (debugOption.startsWith("b ")) {
                    try {
                        int lineNumber = Integer.parseInt(debugOption.substring(2).trim());
                        debug.setMode(Debug.Mode.BREAKPOINTS);
                        debug.addBreakPoint(lineNumber);
                        logger.info("Debug mode: BREAKPOINTS at line {}", lineNumber);
                    } catch (NumberFormatException e) {
                        logger.error("Invalid breakpoint line number: {}", debugOption);
                        System.exit(1);
                    }
                }
            }
            
            // Execute the AST
            Stacks stacks = new Stacks();
            Walker walker = new Walker(astRoot, stacks, debug, null);
            walker.walk();
            
            logger.info("Interpretation completed successfully");
            
        } catch (IOException e) {
            logger.error("IO error during interpretation: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Error during interpretation: {}", e.getMessage());
            e.printStackTrace();
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
        Option debug = new Option("debug", "debug", true, "Debug mode: 'step' for step-by-step, 'b <line>' for breakpoint at line");
        Option interpret = new Option("interpret", "interpret", false, "Interpret MiniJaja program instead of compiling");

        options.addOption(help);
        options.addOption(output);
        options.addOption(force);
        options.addOption(input);
        options.addOption(debug);
        options.addOption(interpret);
        return options;
    }

    private void printHelp(Options options, String errMessage, int exitCode) {
        if (errMessage != null) {
            logger.error("Error: {}", errMessage);
            logger.error("");
        }

        String jarPath = MainCompiler.class.getProtectionDomain().getCodeSource().getLocation().getPath();
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

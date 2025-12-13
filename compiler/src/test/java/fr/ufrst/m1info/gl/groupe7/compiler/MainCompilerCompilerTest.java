package fr.ufrst.m1info.gl.groupe7.compiler;

import org.jacoco.agent.rt.RT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MainCompilerCompilerTest {

    private static Path TEMP_DIR = null;

    private ProcessBuilder buildWithOption(String... options) { return buildWithOption(true, options); }

    /**
     * @param options the options to pass to the compiler.
     * @return a {@link ProcessBuilder} with the given options.
     */
    private ProcessBuilder buildWithOption(boolean redirectErrorStream, String... options) {
        List<String> preOptions = new ArrayList<>();
        preOptions.add("java");

        // Here, have to inject the jacoco agent inside the Process to be able to codecov theses tests.
        String agentPath;
        try {
            agentPath = Paths.get(
                    RT.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to resolve JaCoCo agent jar", e);
        }

        String destfile = Paths.get("target", "jacoco.exec").toString();

        String agentArg = "-javaagent:" + agentPath
                + "=destfile=" + destfile
                + ",append=true"
                + ",includes=fr/ufrst/m1info.gl.groupe7.*";

        preOptions.add(agentArg);

        preOptions.add("-cp");
        preOptions.add(System.getProperty("java.class.path"));
        preOptions.add("fr.ufrst.m1info.gl.groupe7.compiler.MainCompiler");
        preOptions.addAll(List.of(options));
        return new ProcessBuilder(preOptions)
                .redirectErrorStream(redirectErrorStream);
    }

    /**
     * @return a path inside the TEMP_DIR to an inexistant file.
     */
    private Path generateInexistantFile() {
        Random rdm = new Random();
        Path inexistantFile;
        do {
            inexistantFile = TEMP_DIR.resolve("file_" + Math.abs(rdm.nextInt()) + ".mjj");
        } while(inexistantFile.toFile().exists());
        return inexistantFile;
    }

    @BeforeEach
    void setup() throws IOException {
        TEMP_DIR = Files.createTempDirectory("compiler_test_");
        System.out.println("Temp dir created: " + TEMP_DIR);
    }

    @AfterEach
    void cleanup() throws IOException {
        if (Files.exists(TEMP_DIR)) {
            Files.walk(TEMP_DIR)
                    .sorted(Comparator.reverseOrder()) // Here we want the most depth first
                    .forEach(path -> path.toFile().delete());
        }
    }

    /**
     * Test the help option of the compiler
     * @throws InterruptedException if the thread executing the process is interrupted.
     * @throws IOException if an I/O error occurs while reading the output stream.
     */
    @Test
    void testHelpOption() throws InterruptedException, IOException {
        Process process = buildWithOption("--help")
                .start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        for (String s : new String[]{ "-h, --help", "-o, --output <arg>", "-f, --force", "-i, --input <arg>" }) {
            assertTrue(output.contains(s), "Output should contain " + s);
        }
        assertEquals(0, exitCode, "Exit code should be 0");
    }

    /**
     *
     * @throws InterruptedException if the thread executing the process is interrupted.
     * @throws IOException if an I/O error occurs while reading the output stream.
     */
    @Test
    void testMissingInputFile() throws InterruptedException, IOException {
        Process process = buildWithOption("--input", generateInexistantFile().toString())
                .start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        assertTrue(output.contains("File error:"), "Should return a file error");
        assertEquals(1, exitCode, "Sortie attendue: code 1");
    }

    @Test
    void testCompileMinimalProgramFromStdin() throws InterruptedException, IOException {
        String program = "class C { int x; main { x=5; } }";
        String expectedOutput = """
                                init
                                push(w)
                                new(x@global, int, var, 0)
                                push(5)
                                store(x@global)
                                push(0)
                                swap
                                pop
                                pop
                                jcstop
                                """;

        Process process = buildWithOption(false)
                .start();

        try (OutputStream os = process.getOutputStream()) {
            os.write(program.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).toLowerCase();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
        assertEquals(expectedOutput, output);
    }

    @Test
    void testInputWithout2ndArg() throws InterruptedException, IOException {
        Process process = buildWithOption("--input").start();
        int exitCode = process.waitFor();
        assertEquals(1, exitCode);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(output.contains("Error: Missing argument for option: i"));
    }

    @Test
    void testInputOutputFilesRelease1() throws IOException, URISyntaxException, InterruptedException {
        Path input = resourceAsPath("inputs/minimal_ok_release1.mjj");
        Path output = TEMP_DIR.resolve("test_out.jjc");
        Path goldenOutputPath = resourceAsPath("goldens/minimal_ok_release1.jjc");

        Process process = buildWithOption("--input", input.toString(),
                "--output", output.toString())
                .start();

        int exitCode = process.waitFor();
        String outputContent = Files.readString(output).replaceAll("\\r\\n", "\n");
        String goldenOutput = Files.readString(goldenOutputPath).replaceAll("\\r\\n", "\n");

        assertEquals(0, exitCode);
        assertEquals(goldenOutput, outputContent);
    }

    @Test
    void testOutputFileAlreadyExists() throws IOException, URISyntaxException, InterruptedException {
        File alreadyExistingFile = new File(generateInexistantFile().toUri());
        Path input = resourceAsPath("inputs/minimal_ok_release1.mjj");
        if(!alreadyExistingFile.createNewFile()) fail("Unable to create the file.");

        String contentOfAlreadyExistingFile = Files.readString(alreadyExistingFile.toPath());

        String[] args = {"--input", input.toString(), "--output", alreadyExistingFile.getAbsolutePath(), ""};

        // Without -f option
        Process process = buildWithOption(args).start();
        int exitCode = process.waitFor();
        assertEquals(1, exitCode);

        assertEquals(contentOfAlreadyExistingFile, Files.readString(alreadyExistingFile.toPath()));

        // With -f option
        args[args.length-1] = "-f";
        Process process2 = buildWithOption(args).start();
        int exitCode2 = process2.waitFor();
        assertEquals(0, exitCode2);

        Path goldenOutputPath = resourceAsPath("goldens/minimal_ok_release1.jjc");
        String goldenOutput = Files.readString(goldenOutputPath).replaceAll("\\r\\n", "\n");
        String actualOutput = Files.readString(alreadyExistingFile.toPath()).replaceAll("\\r\\n", "\n");
        assertEquals(goldenOutput, actualOutput);

        // With -f but without -o <arg>
        String[] args2 = { "--input", input.toString(), "-f" };
        Process process3 = buildWithOption(args2).start();
        int exitCode3 = process3.waitFor();
        assertEquals(1, exitCode3);
        String output = new String(process3.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(output.contains("Error: You can't use the -f option without the -o option"));
    }

    /**
     * @param path relative path to the resource
     * @return the absolute path of the resource
     * @throws URISyntaxException Shouldn't happen, since the url is given by the ContextClassLoader.
     */
    public static java.nio.file.Path resourceAsPath(String path) throws URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) throw new IllegalArgumentException("Resource not found: " + path);
        return java.nio.file.Path.of(url.toURI());
    }

}

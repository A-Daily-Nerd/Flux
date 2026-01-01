package flx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Compiler {
    private static final String VERSION = "0.1";

    public static void main(String[] args) {

        if (args.length < 1) {
            printUsageAndExit();
        }

        Path filePath = Path.of(args[0]);
        boolean emitJava = false;
        Path emitOutDir = null;
        boolean noRun = false;
        boolean noTypecheck = false;
        boolean quiet = false;

        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "--emit-java":
                    emitJava = true;
                    break;
                case "--emit-java-out":
                    if (i + 1 >= args.length) {
                        System.err.println("--emit-java-out requires a directory argument");
                        System.exit(1);
                    }
                    emitOutDir = Path.of(args[++i]);
                    emitJava = true;
                    break;
                case "--no-run":
                    noRun = true;
                    break;
                case "--no-typecheck":
                    noTypecheck = true;
                    break;
                case "--quiet":
                    quiet = true;
                    break;
                case "--help":
                    printUsageAndExit();
                    break;
                case "--version":
                    System.out.println("Flux Compiler " + VERSION);
                    System.exit(0);
                default:
                    System.err.println("Unknown option: " + a);
                    printUsageAndExit();
            }
        }

        if (!Files.exists(filePath)) {
            System.err.println("File not found: " + filePath);
            System.exit(1);
        }

        if (!filePath.toString().endsWith(".flx")) {
            System.err.println("Expected a .flx file");
            System.exit(1);
        }

        try {
            String source = Files.readString(filePath);

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            // tokens are produced by the lexer (debug printing removed)
            Parser parser = new Parser(tokens);
            List<Stmt> program = parser.parse();
            // run static type checking before execution
            if (!noTypecheck) {
                TypeChecker checker = new TypeChecker();
                checker.check(program);
            }

            if (emitJava) {
                try {
                    String src = Codegen.generateSource(program);
                    String name = filePath.getFileName().toString();
                    String base = name.endsWith(".flx") ? name.substring(0, name.length() - 4) : name;
                    Path out;
                    if (emitOutDir != null) {
                        out = emitOutDir.resolve(base + "-COMPILED.java");
                    } else {
                        Path parent = filePath.getParent();
                        out = (parent == null) ? Path.of(base + "-COMPILED.java") : parent.resolve(base + "-COMPILED.java");
                    }
                    Files.createDirectories(out.getParent());
                    Files.writeString(out, src);
                    if (!quiet) System.out.println("Wrote " + out.toString());
                    System.exit(0);
                } catch (IOException e) {
                    System.err.println("Failed to write generated Java: " + e.getMessage());
                    System.exit(1);
                }
            }

            if (noRun) {
                if (!quiet) System.out.println("No-run flag set; exiting after typecheck.");
                System.exit(0);
            }

            // compile to Java and run
            try {
                Codegen.compileAndRun(program);
            } catch (Exception e) {
                System.err.println("Compilation or execution failed:");
                e.printStackTrace();
                System.exit(1);
            }

        } catch (RuntimeException e) {
            System.err.println("Runtime error:");
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IO error:");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsageAndExit() {
        System.err.println("Usage: java flx.Compiler <file.flx> [options]");
        System.err.println("Options:");
        System.err.println("  --emit-java             Write generated Java next to the input file and exit");
        System.err.println("  --emit-java-out <dir>   Write generated Java into <dir> and exit");
        System.err.println("  --no-run                Do not compile or run generated Java (exit after typecheck)");
        System.err.println("  --no-typecheck          Skip static type checking");
        System.err.println("  --quiet                 Suppress informative output");
        System.err.println("  --help                  Show this help and exit");
        System.err.println("  --version               Show compiler version and exit");
        System.exit(1);
    }
}

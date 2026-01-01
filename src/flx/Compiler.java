package flx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Compiler {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage: java flx.Compiler <file.flx>");
            System.exit(1);
        }

        Path filePath = Path.of(args[0]);

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

            // debug: print tokens
            for (Token t : tokens) System.err.println(t);
            Parser parser = new Parser(tokens);
            List<Stmt> program = parser.parse();
            // run static type checking before execution
            TypeChecker checker = new TypeChecker();
            checker.check(program);

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
}

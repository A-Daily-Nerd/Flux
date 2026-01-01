package flx;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();

        while (true) {
            System.out.print("> ");
            String src = in.nextLine();

            try {
                Lexer lexer = new Lexer(src);
                Parser parser = new Parser(lexer.tokenize());
                for (Stmt s : parser.parse()) interpreter.execute(s);
            } catch (RuntimeException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

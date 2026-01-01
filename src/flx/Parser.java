package flx;

import java.util.*;

import flx.Stmt.ExprStmt;

public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) stmts.add(statement());
        return stmts;
    }

    private Stmt statement() {
        if (match(TokenType.FUN)) return functionDecl();
        if (match(TokenType.LET)) return varDecl();
        if (match(TokenType.PRINT)) return printStmt();
        if (match(TokenType.IF)) return ifStmt();
        if (match(TokenType.WHILE)) return whileStmt();
        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        if (match(TokenType.RETURN)) return returnStmt();
        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.EQUAL)) {
            return assignment();
        }

        return exprStmt();
    }

    private Stmt varDecl() {
        String name = consume(TokenType.IDENTIFIER).lexeme();
        consume(TokenType.EQUAL);
        Expr init = expression();
        consume(TokenType.SEMICOLON);
        return new Stmt.VarDecl(name, init);
    }

    private Stmt returnStmt() {
        Expr value = expression();
        consume(TokenType.SEMICOLON);
        return new Stmt.Return(value);
    }

    private Stmt functionDecl() {
        String name = consume(TokenType.IDENTIFIER).lexeme();
        consume(TokenType.LEFT_PAREN);
        List<String> params = new ArrayList<>();
        List<String> types = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                String pname = consume(TokenType.IDENTIFIER).lexeme();
                params.add(pname);
                String ptype = null;
                if (match(TokenType.COLON)) {
                    ptype = consume(TokenType.IDENTIFIER).lexeme();
                }
                types.add(ptype);
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN);
        consume(TokenType.LEFT_BRACE);
        List<Stmt> body = block();
        return new Stmt.Function(name, params, types, body);
    }

    private Stmt assignment() {
        Token name = consume(TokenType.IDENTIFIER);
        consume(TokenType.EQUAL);
        Expr value = expression();
        consume(TokenType.SEMICOLON);
        return new Stmt.Assign(name.lexeme(), value);
    }

    private Stmt printStmt() {
        consume(TokenType.LEFT_PAREN);
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN);
        consume(TokenType.SEMICOLON);
        return new Stmt.Print(expr);
    }

    private Stmt ifStmt() {
        consume(TokenType.LEFT_PAREN);
        Expr cond = expression();
        consume(TokenType.RIGHT_PAREN);
        Stmt thenB = statement();
        Stmt elseB = match(TokenType.ELSE) ? statement() : null;
        return new Stmt.If(cond, thenB, elseB);
    }

    private Stmt whileStmt() {
        consume(TokenType.LEFT_PAREN);
        Expr cond = expression();
        consume(TokenType.RIGHT_PAREN);
        return new Stmt.While(cond, statement());
    }

    private Stmt exprStmt() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON);
        return new Stmt.ExprStmt(expr);
    }


    private List<Stmt> block() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE)) stmts.add(statement());
        consume(TokenType.RIGHT_BRACE);
        return stmts;
    }

    private Expr expression() { return equality(); }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.EQUAL_EQUAL)) expr = new Expr.Binary(expr, prev(), comparison());
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.LESS)) expr = new Expr.Binary(expr, prev(), term());
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) expr = new Expr.Binary(expr, prev(), factor());
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH))
            expr = new Expr.Binary(expr, prev(), unary());
        return expr;
    }


    private Expr primary() {
        Expr expr;
        if (match(TokenType.INT)) expr = new Expr.Literal(Integer.parseInt(prev().lexeme()));
        else if (match(TokenType.DOUBLE)) expr = new Expr.Literal(Double.parseDouble(prev().lexeme()));
        else if (match(TokenType.STRING)) expr = new Expr.Literal(prev().lexeme());
        else if (match(TokenType.TRUE)) expr = new Expr.Literal(true);
        else if (match(TokenType.FALSE)) expr = new Expr.Literal(false);
        else if (match(TokenType.IDENTIFIER)) expr = new Expr.Variable(prev().lexeme());
        else if (match(TokenType.LEFT_PAREN)) {
            // detect cast syntax: (Type)expr
            if (check(TokenType.IDENTIFIER) && checkNext(TokenType.RIGHT_PAREN)) {
                String tname = advance().lexeme();
                consume(TokenType.RIGHT_PAREN);
                Expr inner = unary();
                expr = new Expr.Cast(tname, inner);
            } else {
                Expr e = expression();
                consume(TokenType.RIGHT_PAREN);
                expr = e;
            }
        } else {
            throw error("Expected expression");
        }

        // postfix parsing: allow trailing calls `( ... )` and indexing/slicing `[ ... ]`
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                List<Expr> args = new ArrayList<>();
                if (!check(TokenType.RIGHT_PAREN)) {
                    do {
                        args.add(expression());
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RIGHT_PAREN);
                expr = new Expr.Call(expr, args);
                continue;
            }

            if (match(TokenType.LEFT_BRACKET)) {
                // either index: [expr]
                // or slice: [start: end] where start or end may be omitted
                Expr startExpr = null;
                if (!check(TokenType.COLON) && !check(TokenType.RIGHT_BRACKET)) {
                    startExpr = expression();
                }
                if (match(TokenType.COLON)) {
                    Expr endExpr = null;
                    if (!check(TokenType.RIGHT_BRACKET)) endExpr = expression();
                    consume(TokenType.RIGHT_BRACKET);
                    expr = new Expr.Slice(expr, startExpr, endExpr);
                } else {
                    // index case
                    Expr idx = startExpr != null ? startExpr : expression();
                    consume(TokenType.RIGHT_BRACKET);
                    expr = new Expr.Index(expr, idx);
                }
                continue;
            }

            break;
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.MINUS)) {
            Token op = prev();
            Expr right = unary();
            return new Expr.Unary(op, right);
        }
        return primary();
    }

    private boolean match(TokenType... types) {
        for (TokenType t : types)
            if (check(t)) { advance(); return true; }
        return false;
    }

    private Token consume(TokenType t) {
        if (check(t)) return advance();
        throw error("Expected " + t);
    }

    private boolean check(TokenType t) { return peek().type() == t; }
    private Token advance() { return tokens.get(pos++); }
    private boolean isAtEnd() { return peek().type() == TokenType.EOF; }
    private Token peek() { return tokens.get(pos); }
    private Token prev() { return tokens.get(pos - 1); }

    private boolean checkNext(TokenType t) {
        if (pos + 1 >= tokens.size()) return false;
        return tokens.get(pos + 1).type() == t;
    }


    private RuntimeException error(String msg) {
        return new RuntimeException(msg + " at token " + peek());
    }
}

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokens = new ArrayList<>();

    private boolean not_eof() {
        return this.tokens.get(0).type != TokenType.EOF;
    }

    private Token at() {
        return this.tokens.get(0);
    }

    private Token eat() {
        Token prev = this.at();
        tokens.remove(0);
        return prev;
    }

    private void expect(TokenType type, String message) {
        Token prev = this.eat();
        if(prev == null || prev.type != type) {
            System.err.println("Parser error: " + message + " " + prev + " - Expecting: " + type);
            System.exit(0);
        }
    }

    public Program produceAst(String code) {
        Lexer lexer = new Lexer(code);
        this.tokens = (ArrayList<Token>) lexer.tokenize();
        Program program = new Program();
        program.body = new ArrayList<Stmt>();

        while(not_eof()) {
            program.body.add(this.parseStmt());
        }
        return program;
    }

    private Stmt parseStmt() {
        return this.parseExpr();
    }
    private Expr parseExpr() {
        return this.parseAdditiveExpr();
    }

    private Expr parseAdditiveExpr() {
        var left = this.parseMultiplicativeExpr();
        while(this.at().value.equals("+") || this.at().value.equals("-")) {
            var op = this.eat().value;
            var right = this.parseMultiplicativeExpr();
            var binExp = new BinaryExpr();
            binExp.left = left;
            binExp.right = right;
            binExp.op = op;
            left = binExp;
        }
        return left;
    }

    private Expr parseMultiplicativeExpr() {
        var left = this.parsePrimaryExpr();
        while(this.at().value.equals("*") || this.at().value.equals("/") || this.at().value.equals("%")) {
            var op = this.eat().value;
            var right = this.parsePrimaryExpr();
            var binExp = new BinaryExpr();
            binExp.left = left;
            binExp.right = right;
            binExp.op = op;
            left = binExp;
        }
        return left;
    }

    private Expr parsePrimaryExpr() {
        TokenType tk = this.at().type;

        switch (tk) {
            case Identifier -> {
                return new Identifier(this.eat().value);
            }
            case Number -> {
                return new NumericLiteral(Double.parseDouble(this.eat().value));
            }
            case OpenParen -> {
                this.eat();
                var value = this.parseExpr();
                this.expect(TokenType.CloseParen, "Unexpected token found inside the parenthesised expression");
                return value;
            }
            default -> {
                System.err.println("Unexpected token found during parsing! " + this.at());
                System.exit(0);
            }
        }
        return new BinaryExpr();
    }
}

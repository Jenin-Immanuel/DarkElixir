import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private boolean checkRelationalOperators(String op) {
        return op.equals("==") || op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=") || op.equals("!=");
    }

    public Program produceAst(String code) {
        Lexer lexer = new Lexer(code);
        this.tokens = (ArrayList<Token>) lexer.tokenize();
//        System.out.println(tokens);
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
        return this.parseMatchExpr();
    }

    private Expr parseMatchExpr() {
        // toAssigned value
        var left = this.parseTupleExpr();
        if(this.at().type == TokenType.Match) {
            // Go through the match operator
            this.eat();
            var right = this.parseMatchExpr(); // Result
            return new MatchExpr(left, right);
        }
        return left;
    }


    //
    private Expr parseTupleExpr() {
        // Not a tuple
        if(this.at().type != TokenType.OpenBrace)
            return parseRelationalExpr();

        // Eat the open brace
        this.eat();

        var newTuple = new Tuple();

        // Examples
        // { 1, 2, 3 }
        // { 1 }
        while(this.not_eof() && this.at().type != TokenType.CloseBrace) {
            var value = this.parseMatchExpr();
            newTuple.contents.add(value);
            if(this.at().type != TokenType.CloseBrace) {
                this.expect(TokenType.Comma, "Expected TokenType Comma, But got " + this.at());
            }
        }
        // Eat the close brace
        this.expect(TokenType.CloseBrace, "Expected trailing Closing Brackets. But got " + this.at());
        return newTuple;
    }

    private Expr parseRelationalExpr() {
        var left  = this.parseAdditiveExpr();
        while(this.checkRelationalOperators(this.at().value)) {
            var op = this.eat().value;
            var right = this.parseAdditiveExpr();
            var binExp = new BinaryExpr();
            binExp.left = left;
            binExp.right = right;
            binExp.op = op;
            left = binExp;
        }
        return left;
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
        var left = this.parseCallMemberExpr();
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

    private Expr parseCallMemberExpr() {
        var caller = this.parsePrimaryExpr();

        if(this.at().type == TokenType.OpenParen) {
            return this.parseCallExpr(caller);
        }
        return caller;
    }
    private Expr parseCallExpr(Expr caller) {
        CallExpr callExpr = new CallExpr(this.parseArguments(), caller);
        if(this.at().type == TokenType.OpenParen) {
            callExpr = (CallExpr) this.parseCallExpr(callExpr);
        }
        return callExpr;
    }

    private ArrayList<Expr> parseArguments() {

        // First expect OpenParen Token
        this.expect(TokenType.OpenParen, "Expected Open Parenthesis. Obtained " + this.at());
        var args = this.at().type == TokenType.CloseParen ? new ArrayList<Expr>() : parseArgumentsList();
        this.expect(TokenType.CloseParen, "Expected Closed Parenthesis. Obtained " + this.at());
        return args;
    }

    private ArrayList<Expr> parseArgumentsList() {
        ArrayList<Expr> args = new ArrayList<>();
        args.add(this.parseMatchExpr());
        while(this.at().type == TokenType.Comma) {
            this.eat();
            args.add(this.parseMatchExpr());
        }
        return args;
    }

    private StringLiteral parseStringLiteral(String val) {
        StringLiteral res = new StringLiteral(val);

        Pattern pattern = Pattern.compile("#\\{(.*?)}");
        Matcher matcher = pattern.matcher(val.toString());

        while(matcher.find()) {
            res.addInterpolatedValue(matcher.group(1));
        }
        return res;
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
            case Atom -> {
                // Removes the ':' from the string so that the value contains the actual value
                return new Atom(this.eat().value.substring(1));
            }
            case String -> {
                return parseStringLiteral(this.eat().value);
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

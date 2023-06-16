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

    private boolean checkLogicalOperators(String op) {
        return op.equals("and") || op.equals("or") || op.equals("not");
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
        switch(this.at().type) {
            case Keyword_If -> {
                return this.parseIfStatement();
            }
            case Keyword_Def -> {
                return this.parseFunctionDeclaration();
            }
            case Keyword_Return -> {
                return this.parseReturnStatement();
            }
            case Keyword_While -> {
                return this.parseWhileStatement();
            }
            default -> {
                return this.parseExpr();
            }
        }

    }

    private Stmt parseWhileStatement() {
        WhileStatement whileStatement = new WhileStatement();
        this.expect(TokenType.Keyword_While, "Expected keyword WHILE for while statement. Given " + this.at());
        whileStatement.condition = this.parseExpr();
        this.expect(TokenType.Keyword_Do, "Expected keyword DO. Given " + this.at());
        while(this.at().type != TokenType.Keyword_End && this.at().type != TokenType.EOF) {
            whileStatement.body.add(this.parseStmt());
        }
        this.expect(TokenType.Keyword_End,  "Expected keyword END at the end of while. Given " + this.at());
        return whileStatement;
    }

    private Stmt parseReturnStatement() {
        this.expect(TokenType.Keyword_Return, "Expected keyword RETURN for return statement. Given " +  this.at());
        return new ReturnStatement(this.parseExpr());
    }

    private Stmt parseFunctionDeclaration() {
        FunctionDeclaration fd = new FunctionDeclaration();
        this.expect(TokenType.Keyword_Def, "Expected keyword DEF for function declaration: Given " + this.at());
        fd.functionName = this.eat().value;
        fd.parameters = this.parseArguments();

        // Make sure all the parameters are Identifiers
        for(var parameter: fd.parameters) {
            if(parameter.getKind() != AstNode.Identifier) {
                System.err.println("Only Identifiers can be given as parameters. Given " + parameter.getKind());
            }
        }

        // Declare block
        this.expect(TokenType.Keyword_Do, "Expected keyword DO. Given " + this.at());
        while(this.at().type != TokenType.Keyword_End && this.at().type != TokenType.EOF) {
            fd.body.add(this.parseStmt());
        }
        this.expect(TokenType.Keyword_End, "Expected keyword END at the end of function declaration. Given " + this.at());

        return fd;
    }

    private Stmt parseIfStatement() {
        IfStatement ifStatement = new IfStatement();

        this.expect(TokenType.Keyword_If, "Expected keyword for conditional statements: IF");

        // Expecting a condition
        var condition = this.getExpressionInParenthesis();

        // Expect a block
        this.expect(TokenType.Keyword_Do, "Expected keyword: do");

        ArrayList<Stmt> block = new ArrayList<>();
        while(this.at().type != TokenType.Keyword_Elif && this.at().type != TokenType.Keyword_Else && this.at().type != TokenType.Keyword_End) {
            // Parse statements
            block.add(this.parseStmt());
        }
        ifStatement.clauses.add(new IfNode(condition, block, false));

        while(this.at().type != TokenType.Keyword_End) {
            if(this.at().type == TokenType.Keyword_Elif) {
                this.expect(TokenType.Keyword_Elif, "Expected keyword for conditional statements: ELIF");
                condition = this.getExpressionInParenthesis();
                this.expect(TokenType.Keyword_Do, "Expected keyword: do");
                block = new ArrayList<>();
                while(this.at().type != TokenType.Keyword_Elif && this.at().type != TokenType.Keyword_Else && this.at().type != TokenType.Keyword_End) {
                    // Parse statements
                    block.add(this.parseStmt());
                }
                ifStatement.clauses.add(new IfNode(condition, block, false));
            }
            else if(this.at().type == TokenType.Keyword_Else) {
                this.expect(TokenType.Keyword_Else, "Expected keyword for conditional statements: Else");
                block = new ArrayList<>();
                while(this.at().type != TokenType.Keyword_Elif && this.at().type != TokenType.Keyword_Else && this.at().type != TokenType.Keyword_End) {
                    block.add(this.parseStmt());
                }
                ifStatement.clauses.add(new IfNode(block, true));
                break;
            }
        }
        this.expect(TokenType.Keyword_End, "Expected keyword after the conditional statement: END");

        return ifStatement;
    }

    private Expr getExpressionInParenthesis() {
        this.expect(TokenType.OpenParen, "Expected token: (");
        var condition = this.parseExpr();
        this.expect(TokenType.CloseParen, "Expected token: )");
        return condition;
    }
    private Expr parseExpr() {
        return this.parseMatchExpr();
    }

    private Expr parseMatchExpr() {
        // toAssigned value
        var left = this.parseDataStructure();
        if(this.at().type == TokenType.Match) {
            // Go through the match operator
            this.eat();
            var right = this.parseMatchExpr(); // Result
            return new MatchExpr(left, right);
        }
        return left;
    }

    private Expr parseDataStructure() {
        switch(this.at().type) {
            case OpenBrace -> {
                return this.parseTupleExpr();
            }
            case OpenSquare -> {
                return this.parseListExpr();
            }
            default -> {
                return parseLogicalExpr();
            }
        }
    }
    private Expr parseTupleExpr() {
        // Not a tuple
        if(this.at().type != TokenType.OpenBrace)
            return parseLogicalExpr();

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

    private Expr parseListExpr() {
        // Eat the open brace
        this.expect(TokenType.OpenSquare, "Expected Open Square Bracket for list, But got " + this.at());

        var newList = new ListStructure();

        // Examples
        // [ 1, 2, 3 ]
        // [ 1 ]
        while(this.not_eof() && this.at().type != TokenType.CloseSquare) {
            var value = this.parseMatchExpr();
            newList.contents.add(value);
            if(this.at().type != TokenType.CloseSquare) {
                this.expect(TokenType.Comma, "Expected TokenType Comma, But got " + this.at());
            }
        }
        // Eat the close brace
        this.expect(TokenType.CloseSquare, "Expected trailing Closing Square Brackets. But got " + this.at());
        return newList;
    }

    private Expr parseLogicalExpr() {
        var left = this.parseRelationalExpr();
        while(this.checkLogicalOperators(this.at().value)) {
            var op = this.eat().value;
            var right = this.parseRelationalExpr();
            var binExp = new BinaryExpr();
            binExp.left = left;
            binExp.right = right;
            binExp.op = op;
            left = binExp;
        }
        return left;
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
        var caller = this.parseMemberExpr();

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

    private Expr parseMemberExpr() {
        var object = this.parseUnaryExpr();
        while(this.at().type == TokenType.Dot || this.at().type == TokenType.OpenSquare) {
            var op = this.eat();
            Expr property = new Expr() {
                @Override
                public AstNode getKind() {
                    return null;
                }
            };
            boolean computed = false;
            if(op.type == TokenType.Dot) {
                computed = false;
                property = this.parseUnaryExpr();
                if (property.getKind() != AstNode.Identifier) {
                    System.err.println("Cannot use dot operator without right hand side being a identifier");
                    System.exit(0);
                }
            }
            else if(op.type == TokenType.OpenSquare) {
                computed = true;
                property = this.parseExpr();
                this.expect(
                        TokenType.CloseSquare,
                        "Missing closing bracket in computed value."
                );
            }
            object = new MemberExpr(object, property, computed);
        }

        return object;
    }

    private Expr parseUnaryExpr() {
        if(this.at().type == TokenType.Minus) {
            this.eat();
            var val = this.at();
            this.expect(TokenType.Number, "Expected number after negative sign. But got " + this.at());
            return new NumericLiteral(Double.parseDouble(val.value) * -1);
        }
        return parsePrimaryExpr();
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
        Parser tempParser = new Parser();
        Pattern pattern = Pattern.compile("#\\{(.*?)}");
        Matcher matcher = pattern.matcher(val.toString());
        while(matcher.find()) {
            res.addInterpolatedString(matcher.group(1));
            var finalVal = tempParser.produceAst(matcher.group(1)).body;
            res.addInterpolatedValue(finalVal.isEmpty() ? new Identifier("null") : finalVal.get(0));

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

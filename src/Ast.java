import java.util.ArrayList;

enum AstNode {
    Program,
    NumericLiteral,
    StringLiteral,
    Identifier,
    BinaryExpr,
    MatchExpr,
    CallExpr,
    Atom,
    Tuple,
    IfStatement,
    FunctionDeclaration,
    ReturnStatement
}

abstract class Stmt {
    public AstNode kind;

    public abstract AstNode getKind();
}

class Program extends Stmt {
    public AstNode kind = AstNode.Program;
    public ArrayList<Stmt> body;

    @Override
    public String toString() {
        return "Program{" +
                "kind=" + kind +
                ", body=" + body +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.Program;
    }
}

abstract class Expr extends Stmt {

}

class MatchExpr extends Expr {
    public Expr toAssigned;
    public Expr value;

    public MatchExpr() {}

    public MatchExpr(Expr toAssigned, Expr value) {
        this.toAssigned = toAssigned;
        this.value = value;
    }

    @Override
    public String toString() {
        return "MatchExpr{" +
                "toAssigned=" + toAssigned +
                ", value=" + value +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.MatchExpr;
    }
}

class Identifier extends Expr {
    public AstNode kind = AstNode.Identifier;
    public String symbol;

    public Identifier(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "symbol='" + symbol + '\'' +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.Identifier;
    }
}

class NumericLiteral extends Expr {
    public AstNode kind = AstNode.NumericLiteral;
    public Double value;

    public NumericLiteral(Double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "NumericLiteral{" +
                "value=" + value +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.NumericLiteral;
    }
}

class BinaryExpr extends Expr {
    public AstNode kind = AstNode.BinaryExpr;
    public Expr left;
    public Expr right;
    public String op;

    @Override
    public String toString() {
        return "BinaryExpr{" +
                "kind=" + kind +
                ", left=" + left +
                ", right=" + right +
                ", op='" + op + '\'' +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.BinaryExpr;
    }
}

class CallExpr extends Expr {
    public ArrayList<Expr> args;
    public Expr caller;

    public CallExpr() {}

    public CallExpr(ArrayList<Expr> args, Expr caller) {
        this.args = args;
        this.caller = caller;
    }

    @Override
    public String toString() {
        return "CallExpr{" +
                "args=" + args +
                ", caller=" + caller +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.CallExpr;
    }
}

class Atom extends Expr {
    public String value;

    public Atom(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Atom{" +
                "value='" + value + '\'' +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.Atom;
    }
}

class StringLiteral extends Expr {

    public String value;
    private ArrayList<String> interpolatedString;
    private ArrayList<Stmt> interpolatedValues;

    public StringLiteral() {
        this.interpolatedValues = new ArrayList<>();
        this.interpolatedString = new ArrayList<>();
    }

    public StringLiteral(String value) {
        this.value = value;
        this.interpolatedValues = new ArrayList<>();
        this.interpolatedString = new ArrayList<>();
    }

    public void addInterpolatedString(String val) {
        this.interpolatedString.add(val);
    }

    public void addInterpolatedValue(Stmt expr) {
        this.interpolatedValues.add(expr);
    }


    public ArrayList<String> getInterpolatedString() {
        return interpolatedString;
    }

    public ArrayList<Stmt> getInterpolatedValues() {
        return interpolatedValues;
    }


    @Override
    public String toString() {
        return "StringLiteral{" +
                "value='" + value + '\'' +
                ", interpolatedString=" + interpolatedString +
                ", interpolatedValues=" + interpolatedValues +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.StringLiteral;
    }
}

class Tuple extends Expr {
    public ArrayList<Expr> contents;

    public Tuple() {
        this.contents = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "contents=" + contents +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.Tuple;
    }
}

class IfStatement extends Stmt {
    public ArrayList<IfNode> clauses;

    public IfStatement() {
        this.clauses = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "IfStatement{" +
                "clauses=" + clauses +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.IfStatement;
    }
}

class IfNode extends Stmt {
    public Expr condition;
    public ArrayList<Stmt> block;
    public Boolean isElse;

    public IfNode(Expr condition, ArrayList<Stmt> block, Boolean isElse) {
        this.condition = condition;
        this.block = block;
        this.isElse = isElse;
    }

    public IfNode(ArrayList<Stmt> block, Boolean isElse) {
        this.block = block;
        this.isElse = isElse;
    }

    public IfNode() {}

    @Override
    public String toString() {
        return "IfNode{" +
                "condition=" + condition +
                ", block=" + block +
                ", isElse=" + isElse +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.IfStatement;
    }
}

class ReturnStatement extends Stmt {
    public Expr returnValue;

    public ReturnStatement(Expr returnValue) {
        this.returnValue = returnValue;
    }

    public ReturnStatement() {}

    @Override
    public AstNode getKind() {
        return AstNode.ReturnStatement;
    }
}

class FunctionDeclaration extends Stmt {
    public String functionName;
    public ArrayList<Expr> parameters;
    public ArrayList<Stmt> body;

    public FunctionDeclaration(String functionName, ArrayList<Expr> parameters, ArrayList<Stmt> body) {
        this.functionName = functionName;
        this.parameters = parameters;
        this.body = body;
    }

    public FunctionDeclaration(String functionName) {
        this.functionName = functionName;
        this.parameters = new ArrayList<>();
        this.body = new ArrayList<>();
    }

    public FunctionDeclaration() {
        this.parameters = new ArrayList<>();
        this.body = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "FunctionDeclaration{" +
                "functionName='" + functionName + '\'' +
                ", parameters=" + parameters +
                ", body=" + body +
                '}';
    }

    @Override
    public AstNode getKind() {
        return AstNode.FunctionDeclaration;
    }
}

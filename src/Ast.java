import java.util.ArrayList;

enum AstNode {
    Program,
    NumericLiteral,
    Identifier,
    BinaryExpr,

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
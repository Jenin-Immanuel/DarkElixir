enum TokenType {
    Number,
    Identifier,
    Let,
    BinaryOperator,
    Match,
    OpenParen,
    CloseParen,
    Atom,
    Equals,
    LessThan,
    GreaterThan,
    LessThanOrEqual,
    GreaterThanOrEqual,
    NotEqual,
    Not,
    EOF
}

public class Token {
    public String value;
    public TokenType type;

    public Token(String value, TokenType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + value + '\'' +
                ", type=" + type +
                '}';
    }
}

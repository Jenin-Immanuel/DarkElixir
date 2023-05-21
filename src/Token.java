enum TokenType {
    Number,
    String,
    Identifier,
    Let,
    Minus,
    BinaryOperator,
    Match,
    OpenParen,
    CloseParen,
    OpenBrace,
    CloseBrace,
    Atom,
    Comma,
    Equals,
    LessThan,
    GreaterThan,
    LessThanOrEqual,
    GreaterThanOrEqual,
    NotEqual,
    Not,
    Keyword_If,
    Keyword_Else,
    Keyword_Do,
    Keyword_End,
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

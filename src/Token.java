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
    OpenSquare,
    CloseSquare,
    Atom,
    Comma,
    Equals,
    LessThan,
    GreaterThan,
    LessThanOrEqual,
    GreaterThanOrEqual,
    NotEqual,
    Not,
    Dot,
    Keyword_If,
    Keyword_Elif,
    Keyword_Else,
    Keyword_Do,
    Keyword_End,
    Keyword_And,
    Keyword_Or,
    Keyword_Not,
    Keyword_Def,
    Keyword_Return,
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Lexer {

    private String src;
    private Integer i;
    public static HashMap<String, TokenType> KEYWORDS;

    Lexer(String src) {
        this.src = src;
        this.i = 0;
    }


    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("let", TokenType.Let);
    }

    private boolean borderCheck() {
        return this.i < this.src.length();
    }


    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while(this.i < this.src.length()) {
            if(src.charAt(i) == '(' ) {
                tokens.add(new Token("(", TokenType.OpenParen));
            }
            else if(src.charAt(i) == ')') {
                tokens.add(new Token(")", TokenType.CloseParen));
            }
            else if(src.charAt(i) == '+' || src.charAt(i) == '-' || src.charAt(i) == '*' || src.charAt(i) == '/' || src.charAt(i) == '%') {
                tokens.add(new Token(Character.toString(src.charAt(i)), TokenType.BinaryOperator));
            }
            else if(src.charAt(i) == '<') {
                if(this.borderCheck() && src.charAt(i + 1) == '=') {
                    tokens.add(new Token("<=", TokenType.LessThanOrEqual));
                    i += 2;
                    continue;
                }
                tokens.add(new Token("<", TokenType.LessThan));
            }
            else if(src.charAt(i) == '>') {
                if(this.borderCheck() && src.charAt(i + 1) == '=') {
                    tokens.add(new Token(">=", TokenType.GreaterThanOrEqual));
                    i += 2;
                    continue;
                }
                tokens.add(new Token(">", TokenType.GreaterThan));

            }
            else if(src.charAt(i) == '=') {
                if(this.borderCheck() && src.charAt(i + 1) == '=') {
                    tokens.add(new Token("==", TokenType.Equals));
                    i += 2;
                    continue;
                }
                tokens.add(new Token("=", TokenType.Match));
            }
            else if(src.charAt(i) == ':') {
                StringBuilder atom = new StringBuilder(":");
                i++;
                if(Character.isLetter(src.charAt(i))) {
                    atom.append(src.charAt(i++));
                    while(this.borderCheck() && (Character.isDigit(src.charAt(i)) || Character.isLetter(src.charAt(i))  || src.charAt(i) == '_'))
                        atom.append(src.charAt(i++));
                }
                tokens.add(new Token(atom.toString(), TokenType.Atom));
                continue;
            }
            else {
                if(Character.isDigit(src.charAt(i))) {
                    // Parse Integer
                    StringBuilder num = new StringBuilder();
                    while(this.i < this.src.length() && Character.isDigit(src.charAt(i)))
                        num.append(src.charAt(i++));
                    tokens.add(new Token(num.toString(), TokenType.Number));
                }
                else if(Character.isLetter(src.charAt(i))) {
                    // Parse Identifier
                    StringBuilder ident = new StringBuilder();
                    ident.append(src.charAt(i++));
                    while(this.i < this.src.length() && (Character.isDigit(src.charAt(i)) || Character.isLetter(src.charAt(i))  || src.charAt(i) == '_'))
                        ident.append(src.charAt(i++));

                    tokens.add(new Token(ident.toString(), KEYWORDS.getOrDefault(ident.toString(), TokenType.Identifier)));
                }
                else if(Character.isSpaceChar(src.charAt(i))) {
                    i++;
                }
                else {
                    System.err.println("Unrecognized character: " + src.charAt(i));
                    System.exit(0);
                }
                continue;
            }
            i++;
        }
        tokens.add(new Token("EndOfFile", TokenType.EOF));
        return tokens;
    }

}

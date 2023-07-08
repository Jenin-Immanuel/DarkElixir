import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        KEYWORDS.put("if", TokenType.Keyword_If);
        KEYWORDS.put("elif", TokenType.Keyword_Elif);
        KEYWORDS.put("else", TokenType.Keyword_Else);
        KEYWORDS.put("do", TokenType.Keyword_Do);
        KEYWORDS.put("end", TokenType.Keyword_End);
        KEYWORDS.put("and", TokenType.Keyword_And);
        KEYWORDS.put("or", TokenType.Keyword_Or);
        KEYWORDS.put("not", TokenType.Keyword_Not);
        KEYWORDS.put("def", TokenType.Keyword_Def);
        KEYWORDS.put("return", TokenType.Keyword_Return);
        KEYWORDS.put("while", TokenType.Keyword_While);
        KEYWORDS.put("fn", TokenType.Keyword_Fn);
    }

    private boolean borderCheck() {
        return this.i < this.src.length();
    }

    private Token lexString() {
        StringBuilder val = new StringBuilder("\"");
        i++;
        while(this.borderCheck() && src.charAt(i) != '"') {
            val.append(src.charAt(i++));
//            if(src.charAt(i) == '#') {
//                if(src.charAt(i + 1) == '{') {
//                    val.append("#{");
//                    i += 2;
//                    while(this.borderCheck() && src.charAt(i) != '}')
//                        val.append(src.charAt(i++));
//                    val.append(src.charAt(i++));
//                    if(this.borderCheck() && src.charAt(i) == '"') break;
//                }
//            }

        }
        if(!borderCheck()) {
            System.err.println("Invalid String. Entered expected closing quotes.");
            System.exit(0);
        }
        if(src.charAt(i) == '"') val.append(src.charAt(i++));
        else {
            System.err.println("Invalid token found " + src.charAt(i));
            System.exit(0);
        }


        return new Token(val.toString(), TokenType.String);
    }


    public List<Token> tokenize() {

        // TODO deal with later
//        String regexString = "\"[^\"]*\"";

        List<Token> tokens = new ArrayList<>();
        while(this.i < this.src.length()) {
            if(src.charAt(i) == '(' ) {
                tokens.add(new Token("(", TokenType.OpenParen));
            }
            else if(src.charAt(i) == ')') {
                tokens.add(new Token(")", TokenType.CloseParen));
            }
            else if(src.charAt(i) == '{') {
                tokens.add(new Token("{", TokenType.OpenBrace));
            }
            else if(src.charAt(i) == '}') {
                tokens.add(new Token("}", TokenType.CloseBrace));
            }
            else if(src.charAt(i) == '[') {
                tokens.add(new Token("[", TokenType.OpenSquare));
            }
            else if(src.charAt(i) == ']') {
                tokens.add(new Token("]", TokenType.CloseSquare));
            }
            else if(src.charAt(i) == ',') {
                tokens.add(new Token(",", TokenType.Comma));
            }
            else if(src.charAt(i) == '.') {
                tokens.add(new Token(".", TokenType.Dot));
            }
            // Check for -ve numbers before checking for binary operators
            else if(src.charAt(i) == '+' || src.charAt(i) == '-' || src.charAt(i) == '*' || src.charAt(i) == '/' || src.charAt(i) == '%') {
                if (src.charAt(i) == '-' && (tokens.isEmpty() || tokens.get(tokens.size() - 1).type != TokenType.Number)) {
                    if(src.charAt(i + 1) == '>') {
                        i++;
                        tokens.add(new Token("->", TokenType.ArrowOperator));
                    } else {
                        tokens.add(new Token("-", TokenType.Minus));
                    }
                } else {
                    tokens.add(new Token(Character.toString(src.charAt(i)), TokenType.BinaryOperator));
                }
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
                if(this.borderCheck() && src.charAt(i + 1) == '>') {
                    tokens.add(new Token("=>", TokenType.MapOperator));
                    i += 2;
                    continue;
                }
                tokens.add(new Token("=", TokenType.Match));
            }
            else if(src.charAt(i) == '!') {
                if(this.borderCheck() && src.charAt(i + 1) == '=') {
                    tokens.add(new Token("!=", TokenType.NotEqual));
                    i += 2;
                    continue;
                }
                tokens.add(new Token("!", TokenType.Not));
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
            // FIXME
            else if(src.charAt(i) == '"') {
                tokens.add(this.lexString());
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
                else if(Character.isSpaceChar(src.charAt(i)) || (int) src.charAt(i) == 10) {
                    i++;
                }
                else {
                    System.err.println("Unrecognized character: " + (int) src.charAt(i));
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

import java.util.ArrayList;
import java.util.stream.Collectors;


public class Interpreter {
    static RuntimeValue evaluateProgram(Program program, Environment env) {
        RuntimeValue lastEvaluated = new RNullValue();
        for(Stmt stmt: program.body) {
            lastEvaluated = evaluate(stmt, env);
        }
        return lastEvaluated;
    }

    static RuntimeValue evaluateBinaryExpr(BinaryExpr binExp, Environment env) {
        var lhs = evaluate(binExp.left, env);
        var rhs = evaluate(binExp.right, env);


        if(lhs.getKind() == RuntimeValueType.Number && rhs.getKind() == RuntimeValueType.Number) {
            return evaluateNumericBinaryExpr(((RNumberValue) lhs).number, ((RNumberValue) rhs).number, binExp.op);
        }
        else if(lhs.getKind() == RuntimeValueType.String && rhs.getKind() == RuntimeValueType.String) {
            return evaluateStringBinaryExpr((RStringValue) lhs, (RStringValue)rhs, binExp.op);
        }
        else if(lhs.getKind() == RuntimeValueType.Atom && rhs.getKind() == RuntimeValueType.Atom) {
            return evaluateAtomComparison((RAtomValue) lhs,(RAtomValue) rhs, binExp.op);
        }
        return new RNullValue();
    }

    static RuntimeValue evaluateAtomComparison(RAtomValue lhs, RAtomValue rhs, String op) {
        // Currently only supports  '=='
        String leftAtomValue = lhs.value;
        String rightAtomValue = rhs.value;
        RBooleanValue result = new RBooleanValue(true);

        switch(op) {
            case "==" -> {
                result.value = leftAtomValue.equals(rightAtomValue);
            }
            case ">=" -> {
                result.value = leftAtomValue.compareTo(rightAtomValue) >= 0;
            }
            case ">" -> {
                result.value = leftAtomValue.compareTo(rightAtomValue) > 0;
            }
            case "<=" -> {
                result.value = leftAtomValue.compareTo(rightAtomValue) <= 0;
            }
            case "<" -> {
                result.value = leftAtomValue.compareTo(rightAtomValue) < 0;
            }
            case "!=" -> {
                result.value = leftAtomValue.compareTo(rightAtomValue) != 0;
            }
            default -> {
                return new RNullValue();
            }
        }
        return result;
    }
    static RuntimeValue evaluateNumericBinaryExpr(Double lhs, Double rhs, String op) {
        RNumberValue result =  new RNumberValue();
        switch (op) {
            case "+" -> result.number = lhs + rhs;
            case "-" -> result.number = lhs - rhs;
            case "*" -> result.number = lhs * rhs;
            case "/" -> result.number = lhs / rhs;
            case "%" -> result.number = lhs % rhs;
            case ">", "<", ">=", "<=", "==", "!=" -> {
                return evaluateNumericRelationalExpr(lhs, rhs, op);
            }
        }
        return result;
    }

    static RuntimeValue evaluateNumericRelationalExpr(Double lhs, Double rhs, String op) {
        RBooleanValue result = new RBooleanValue();
        switch (op) {
            case ">" -> result.value = lhs > rhs;
            case "<" -> result.value = lhs < rhs;
            case ">=" -> result.value = lhs >= rhs;
            case "<=" -> result.value = lhs <= rhs;
            case "==" -> result.value = lhs == rhs;
            case "!=" -> result.value = lhs != rhs;
        }
        return result;
    }

    static RStringValue evaluateStringBinaryExpr(RStringValue lhs, RStringValue rhs, String op) {
        RStringValue res = new RStringValue();

        // Will be extensible
        switch (op) {
            case "+" -> res.value = lhs.value + rhs.value;
            default -> {
                System.err.println("Invalid operator for strings. Given " + op);
                System.exit(0);
            }
        }
        return res;
    }

    static RuntimeValue evaluateIdentifier(Identifier astNode, Environment env) {
        // Gives the value tha variable holds
        return env.lookupVariable(astNode.symbol);
    }

    static RuntimeValue evaluateMatchExpr(MatchExpr matchExpr, Environment env) {


        if(matchExpr.toAssigned.getKind() != AstNode.Identifier && matchExpr.toAssigned.getKind() != AstNode.Tuple) {
            System.err.println("Invalid LHS of the Match expression " + matchExpr.toAssigned);
            System.exit(0);
        }

        // Implement tuple destructuring

        switch (matchExpr.toAssigned.getKind()) {
            case Identifier -> {
                var asIdentifier = (Identifier) matchExpr.toAssigned;

                // Check for constants

                if(asIdentifier.symbol.equals("true") || asIdentifier.symbol.equals("false") || asIdentifier.symbol.equals("null")) {
                    System.err.println("Invalid LHS of the Match expression. Expected Identifier got " + asIdentifier.symbol);
                    System.exit(0);
                }

                // Assign the new value, if it is present in the current environment
                if(env.containsVariable(asIdentifier.symbol))
                    return env.assignVariable(asIdentifier.symbol, evaluate(matchExpr.value, env));

                return env.declareVariable(asIdentifier.symbol, evaluate(matchExpr.value, env));
            }
            case Tuple -> {
                // Return the error before evaluating
                if(matchExpr.value.getKind() != AstNode.Identifier && matchExpr.value.getKind() != AstNode.Tuple) {
                    System.err.println("Match error. No match for the right hand value " + matchExpr.value);
                    System.exit(0);
                }

                // toAssigned should be evaluated later on

                var rhs = (RTupleValue) evaluate(matchExpr.value, env);
                var lhsContents = ((Tuple) matchExpr.toAssigned).contents;

                // The next element after the match operator should be a tuple or an identifier with a tuple
                if(rhs.getKind() != RuntimeValueType.Tuple) {
                    System.err.println("Match error. No match for the right hand value " + rhs.contents);
                    System.exit(0);
                }

                // Check whether both the tuples have the same size
                if(rhs.contents.size() != lhsContents.size()) {
                    System.err.println("Match error. No match for the right hand value " + rhs.contents);
                    System.exit(0);
                }

                // Check whether the functionality is pattern matching or multiple assignment
                if(lhsContents.stream().allMatch(n -> n.getKind() == AstNode.Identifier) && !lhsContents.isEmpty()) {
                    // Multiple assignments
                    for(int i = 0; i < lhsContents.size(); i++) {
                        Identifier variableLHS = (Identifier) lhsContents.get(i);
                        RuntimeValue variableRHS = rhs.contents.get(i);
                        env.declareVariable(variableLHS.symbol, variableRHS);
                    }

                }
                else {
                    for(int i = 0; i < lhsContents.size(); i++) {
                        if(evaluate(lhsContents.get(i), env).getKind() != rhs.contents.get(i).getKind()) {
                            System.err.println("Match error. No match for the right hand value " + rhs);
                            System.exit(0);
                        }
                    }
                }
                return rhs;

            }
            default -> {

            }
        }
        return new RNullValue();
    }

    static  RuntimeValue evaluateCallExpr(CallExpr expr, Environment env) {
        var args = expr.args.stream().map(arg -> evaluate(arg, env)).toList();
        var fn = evaluate(expr.caller, env);
        if(fn.getKind() != RuntimeValueType.NativeFunction) {
            System.err.println("Cannot call a value which is not a native function " + fn);
            System.exit(0);
        }
        return ((RNativeFunction) fn).call.call(new ArrayList<>(args), env);
    }

    static RuntimeValue evaluateTuple(Tuple tuple, Environment env) {
        RTupleValue newTuple = new RTupleValue();
        for(Expr content: tuple.contents) {
            newTuple.contents.add(evaluate(content, env));
        }
        return newTuple;
    }

    static RuntimeValue evaluateStringLiterals(StringLiteral string, Environment env) {
        RStringValue newString = new RStringValue(string.value);
        ArrayList<RuntimeValue> resulantValues = string.getInterpolatedValues().stream().map(arg -> evaluate(arg, env)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> interpolatedStrings = string.getInterpolatedString();

        if(resulantValues.size() == interpolatedStrings.size()) {

            for(int i = 0; i < resulantValues.size(); i++) {
                newString.value = newString.value.replace("#{"+ interpolatedStrings.get(i) + "}", resulantValues.get(i).toRawString());
            }
        }
        return newString;
    }

    static RuntimeValue evaluate(Stmt astNode, Environment env) {
        if(astNode.getKind() != null) {
            switch (astNode.getKind()) {
                case NumericLiteral -> {
                    var number = new RNumberValue();
                    NumericLiteral asNumber = (NumericLiteral) astNode;
                    number.number = asNumber.value;
                    return number;
                }
                case Program -> {
                    return evaluateProgram((Program) astNode, env);
                }
                case BinaryExpr -> {
                    return evaluateBinaryExpr((BinaryExpr) astNode, env);
                }
                case MatchExpr -> {
                    return evaluateMatchExpr((MatchExpr) astNode, env);
                }
                case CallExpr -> {
                    return evaluateCallExpr((CallExpr) astNode, env);
                }
                case Atom -> {
                    String atomValue = ((Atom) astNode).value;
                    return new RAtomValue(atomValue);
                }
                case StringLiteral -> {
//                    return new RStringValue(((StringLiteral)astNode).value);
                    return evaluateStringLiterals((StringLiteral) astNode, env);
                }
                case Identifier ->  {
                    return evaluateIdentifier((Identifier) astNode, env);
                }
                case Tuple -> {
                    return evaluateTuple((Tuple) astNode, env);
                }
                default -> {
                    System.err.println("This AST Node has not yet been setup for interpretation. " + astNode);
                    System.exit(0);
                }
            }
        }
        return new RNullValue();
    }
}

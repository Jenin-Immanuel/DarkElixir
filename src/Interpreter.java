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
        return new RNullValue();
    }
    static RNumberValue evaluateNumericBinaryExpr(Double lhs, Double rhs, String op) {
        RNumberValue result =  new RNumberValue();
        switch (op) {
            case "+" -> result.number = lhs + rhs;
            case "-" -> result.number = lhs - rhs;
            case "*" -> result.number = lhs * rhs;
            case "/" -> result.number = lhs / rhs;
            case "%" -> result.number = lhs % rhs;
        }
        return result;
    }

    static RuntimeValue evaluateIdentifier(Identifier astNode, Environment env) {
        // Gives the value tha variable holds
        return env.lookupVariable(astNode.symbol);
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
                case Identifier ->  {
                    return evaluateIdentifier((Identifier) astNode, env);
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

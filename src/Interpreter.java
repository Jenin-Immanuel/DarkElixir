public class Interpreter {
    static RuntimeValue evaluateProgram(Program program) {
        RuntimeValue lastEvaluated = new RNullValue();
        for(Stmt stmt: program.body) {
            lastEvaluated = evaluate(stmt);
        }
        return lastEvaluated;
    }

    static RuntimeValue evaluateBinaryExpr(BinaryExpr binExp) {
        var lhs = evaluate(binExp.left);
        var rhs = evaluate(binExp.right);

        if(lhs.getKind() == RuntimeValueType.Number && rhs.getKind() == RuntimeValueType.Number) {
            return evaluateNumericBinaryExpr(((RNumberValue) lhs).number, ((RNumberValue) rhs).number, binExp.op);
        }
        return new RNullValue();
    }
    static RNumberValue evaluateNumericBinaryExpr(Float lhs, Float rhs, String op) {
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

    static RuntimeValue evaluate(Stmt astNode) {
        if(astNode.getKind() != null) {
            switch (astNode.getKind()) {
                case NumericLiteral -> {
                    var number = new RNumberValue();
                    NumericLiteral asNumber = (NumericLiteral) astNode;
                    number.number = asNumber.value;
                    return number;
                }
                case Program -> {
                    return evaluateProgram((Program) astNode);
                }
                case BinaryExpr -> {

                    return evaluateBinaryExpr((BinaryExpr) astNode);
                }
                case NullLiteral -> {
                    return new RNullValue();
                }
                default -> {
                    System.out.println("This AST Node has not yet been setup for interpretation. " + astNode);
                    System.exit(0);
                }
            }
        }
        return new RNullValue();
    }
}

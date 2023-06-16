import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class Interpreter {
    static Stack<RuntimeValue> CallStack = new Stack<>();

    static Boolean equivalentBoolean(RuntimeValue val) {
        switch (val.getKind()) {
            case Boolean -> {
                return ((RBooleanValue) val).value;
            }
            case Null -> { return false; }
        }
        return true;
    }
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

        if(Objects.equals(binExp.op, "and") || Objects.equals(binExp.op, "or")) return evaluateLogicalExpr(lhs, rhs, binExp.op);

        HashMap<String, Supplier<RuntimeValue>> functionMap = new HashMap<>();
        functionMap.put("Number-Number", () -> evaluateNumericBinaryExpr(((RNumberValue) lhs), ((RNumberValue) rhs), binExp.op));
        functionMap.put("String-String", () -> evaluateStringBinaryExpr((RStringValue) lhs, (RStringValue) rhs, binExp.op));
        functionMap.put("Number-String", () -> evaluateNumberStringOps((RNumberValue) lhs, (RStringValue) rhs, binExp.op));
        functionMap.put("String-Number", () -> evaluateNumberStringOps((RStringValue) lhs, (RNumberValue) rhs, binExp.op));
        functionMap.put("Atom-Atom", () -> evaluateAtomComparison((RAtomValue) lhs, (RAtomValue) rhs, binExp.op));

        String key = lhs.getKind().toString() + "-" + rhs.getKind().toString();
        if (functionMap.containsKey(key)) return functionMap.get(key).get();

        return new RNullValue();
    }

    static RuntimeValue evaluateNumberStringOps(RStringValue lhs, RNumberValue rhs, String op) {
        RStringValue res = new RStringValue();
        switch (op) {
            case "+" -> res.value = lhs.toRawString() + rhs.number;
            default -> {
                System.err.println("Invalid Operation " + op + " on String and Number.");
                System.exit(0);
            }
        }
        return res;
    }

    static RuntimeValue evaluateNumberStringOps(RNumberValue lhs, RStringValue rhs, String op) {
        RStringValue res = new RStringValue();
        switch (op) {
            case "+" -> res.value = lhs.number + rhs.toRawString();
            default -> {
                System.err.println("Invalid Operation " + op + " on String and Number.");
                System.exit(0);
            }
        }

        return res;
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
    static RuntimeValue evaluateNumericBinaryExpr(RNumberValue lhs, RNumberValue rhs, String op) {
        RNumberValue result =  new RNumberValue();

        switch (op) {
            case "+" -> result.number = lhs.number + rhs.number;
            case "-" -> result.number = lhs.number - rhs.number;
            case "*" -> result.number = lhs.number * rhs.number;
            case "/" -> result.number = lhs.number / rhs.number;
            case "%" -> result.number = lhs.number % rhs.number;
            case ">", "<", ">=", "<=", "==", "!=" -> {
                return evaluateNumericRelationalExpr(lhs.number, rhs.number, op);
            }
            case "and", "or" -> {
                return evaluateLogicalExpr(lhs, rhs, op);
            }
        }
        return result;
    }

    static RuntimeValue evaluateLogicalExpr(RuntimeValue lhs, RuntimeValue rhs, String op) {
        RBooleanValue res = new RBooleanValue();
        var leftBool = equivalentBoolean(lhs);
        switch (op) {
            case "and" -> {
                if(leftBool)
                    return rhs;
                return lhs;
            }
            case "or" -> {
                if(leftBool)
                    return lhs;
                return rhs;
            }
            default -> {
                System.err.println("Invalid Operation for " + lhs.toRawString() + " and " + rhs.toRawString() + ". Given operation: " + op);
            }
        }
        return res;
    }
    static RuntimeValue evaluateNumericRelationalExpr(Double lhs, Double rhs, String op) {
        RBooleanValue result = new RBooleanValue();
        switch (op) {
            case ">" -> result.value = lhs > rhs;
            case "<" -> result.value = lhs < rhs;
            case ">=" -> result.value = lhs >= rhs;
            case "<=" -> result.value = lhs <= rhs;
            case "==" -> result.value = Objects.equals(lhs, rhs);
            case "!=" -> result.value = !Objects.equals(lhs, rhs);
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

//                if(asIdentifier.symbol.equals("true") || asIdentifier.symbol.equals("false") || asIdentifier.symbol.equals("null")) {
//                    System.err.println("Invalid LHS of the Match expression. Expected Identifier got " + asIdentifier.symbol);
//                    System.exit(0);
//                }

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

    static RuntimeValue evaluateReturnStatement(ReturnStatement rs, Environment env) {
        return evaluate(rs.returnValue, env);
    }

    static  RuntimeValue evaluateCallExpr(CallExpr expr, Environment env) {
        var args = expr.args.stream().map(arg -> evaluate(arg, env)).toList();
        var fn = evaluate(expr.caller, env);
        CallStack.push(fn);
        if(fn.getKind() == RuntimeValueType.NativeFunction) {
            var returnValue =  ((RNativeFunction) fn).call.call(new ArrayList<>(args), env);
            CallStack.pop();
            return returnValue;
        }
        if(fn.getKind() == RuntimeValueType.FunctionValue) {
            var fnValue = (RFunctionValue) fn;

            // Declare a new scope with current env as the parent
            var scope = new Environment(fnValue.declarationEnv);

            // Set the parameters as vars in the current scope
            for(int i = 0; i < fnValue.parameters.size(); i++) {
//                System.out.println(fnValue.parameters.get(i).toString() + " " + args.get(i));
                scope.declareVariable(((Identifier)fnValue.parameters.get(i)).symbol, args.get(i), false);
            }
            RuntimeValue result = new RNullValue();
            for(var stmt: fnValue.body) {
                if(stmt.getKind() == AstNode.ReturnStatement) {
                    var returnValue = evaluateReturnStatement((ReturnStatement) stmt, scope);
                    CallStack.pop();
                    return returnValue;
                }
                result = evaluate(stmt, scope);
            }
            CallStack.pop();
            return new RNullValue();
        }
        System.err.println("Cannot call a value which is not a native function " + fn);
        System.exit(0);
        return new RNullValue();
    }

    static RuntimeValue evaluateMemberExpr(MemberExpr memberExpr, Environment env) {

        var mod = evaluate(memberExpr.object, env);
        if(mod.getKind() != RuntimeValueType.Module) {
            throw new RuntimeException("Error: LHS of a member expression should be a module");
        }
        if(memberExpr.property.getKind() != AstNode.Identifier)  {
            throw new RuntimeException("Error: RHS of a member expression should be a function call of the module");
        }
        var lhs = (RModule) mod;
        var rhs = (Identifier) memberExpr.property;

        // Caller should be an identifier
//        if(rhs.caller.getKind() != AstNode.Identifier)  {
//            throw new RuntimeException("Error: The caller should be an Identifier");
//        }
        var caller = rhs.symbol;

        // Check if it is present
        if(lhs.functions.get(caller) != null) {
            return lhs.functions.get(caller);
        } else {
            throw new RuntimeException("InvalidIndexGiven: at function Tuple.at/2");
        }

    }

    static RuntimeValue evaluateTuple(Tuple tuple, Environment env) {
        RTupleValue newTuple = new RTupleValue();
        for(Expr content: tuple.contents) {
            newTuple.contents.add(evaluate(content, env));
        }
        return newTuple;
    }

    static RuntimeValue evaluateList(ListStructure list, Environment env) {
        RListValue newList = new RListValue();
        for(Expr content: list.contents) {
            newList.contents.add(evaluate(content, env));
        }
        return newList;
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

    // Evaluate body of statements
    static void evaluateBody(ArrayList<Stmt> body, Environment env) {
        RuntimeValue lastEvaluated = new RNullValue();
        var scope = new Environment(env);
        for(var stmt: body){
            lastEvaluated = evaluate(stmt, scope);
        }
    }

    // Evaluate If clause
    static Boolean evaluateIfClause(IfNode ifNode, Environment env) {
        if(ifNode.isElse) {
            evaluateBody(ifNode.block, env);
            return true;
        }
        var condition = evaluate(ifNode.condition, env);
        if(condition.getKind() == RuntimeValueType.Boolean){
            // If the condition is false
            if(!((RBooleanValue) condition).value) return false;
        }

        evaluateBody(ifNode.block, env);
        return true;
    }

    // Evaluating If statement
    static RuntimeValue evaluateIfStatement(IfStatement ifStatement, Environment env) {
        var clauses = ifStatement.clauses;
        for(IfNode clause: clauses) {
            Boolean isEvaluated = evaluateIfClause(clause, env);
            if(isEvaluated) break;
        }
        return new RBooleanValue(true);
    }

    static RuntimeValue evaluateFunctionValue(FunctionDeclaration fd, Environment env) {
        var fnValue = new RFunctionValue(fd.functionName, fd.parameters, fd.body, env);
        return env.declareVariable(fd.functionName, fnValue, false);
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
                case IfStatement -> {
                    return evaluateIfStatement((IfStatement) astNode, env);
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
                case MemberExpr -> {
                    return evaluateMemberExpr((MemberExpr) astNode, env);
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
                case List -> {
                    return evaluateList((ListStructure) astNode, env);
                }
                case FunctionDeclaration -> {
                    return evaluateFunctionValue((FunctionDeclaration) astNode, env);
                }
                case ReturnStatement -> {
                    if(CallStack.empty()) {
                        System.err.println("Top level return statements are not allowed.");
                        System.exit(0);
                    }
                    return evaluateReturnStatement((ReturnStatement) astNode, env);
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

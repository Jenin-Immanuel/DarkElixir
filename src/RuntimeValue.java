import java.util.ArrayList;
import java.util.HashMap;

enum RuntimeValueType {
    Null,
    Number,
    String,
    Boolean,
    Atom,
    Tuple,
    List,
    NativeFunction,
    Module,
    IfStatement,
    IfNode,
    FunctionValue,
    While
}

public abstract class RuntimeValue {
    public RuntimeValueType kind;

    public abstract RuntimeValueType getKind();

    public abstract String toRawString();

}

class RNullValue extends RuntimeValue {
    public RuntimeValueType kind = RuntimeValueType.Null;
    public String value = "null";

    @Override
    public String toString() {
        return "RNullValue{null}";
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.Null;
    }

    @Override
    public String toRawString() {
        return "";
    }
}

class RNumberValue extends RuntimeValue {
    public RuntimeValueType kind = RuntimeValueType.Number;
    public Double number;

    public RNumberValue() {}

    public RNumberValue(Double number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "RNumberValue{" + number + "}";
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.Number;
    }

    @Override
    public String toRawString() {
        if((number*10) % 10 == 0) {
            return String.valueOf(number.intValue());
        }
        return number.toString();
    }
}

class RStringValue extends RuntimeValue {
    public String value;


    public RStringValue() {}
    public RStringValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RStringValue{" +
                "value='" + value + '\'' +
                '}';
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.String;
    }

    @Override
    public String toRawString() {
        return value.replaceAll("\"", "");
    }
}

class RBooleanValue extends RuntimeValue {
    public Boolean value;

    public RBooleanValue() {}
    public RBooleanValue(Boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RBooleanValue{" + value + "}";
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.Boolean;
    }

    @Override
    public String toRawString() {
        return value.toString();
    }
}

class RAtomValue extends RuntimeValue {
    public String value;


    public RAtomValue(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return "RAtomValue{" + value + "}";
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.Atom;
    }

    @Override
    public String toRawString() {
        return ":" + value;
    }
}

class RTupleValue extends RuntimeValue {
    public ArrayList<RuntimeValue> contents;

    public RTupleValue() {
        this.contents = new ArrayList<>();
    }

    public RTupleValue(ArrayList<RuntimeValue> contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "RTupleValue{ " + contents + " }";
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.Tuple;
    }

    @Override
    public String toRawString() {
        StringBuilder val = new StringBuilder("{ ");
        for(var content: contents) {
            val.append(content.toRawString());
            val.append(" ");
        }
        val.append("}");
        return val.toString();
    }
}

class RListValue extends RuntimeValue {
    public ArrayList<RuntimeValue> contents;

    public RListValue() {
        this.contents = new ArrayList<>();
    }

    public RListValue(ArrayList<RuntimeValue> contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "RListValue{" +
                "contents=" + contents +
                '}';
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.List;
    }

    @Override
    public String toRawString() {
        StringBuilder val = new StringBuilder("[ ");
        for(var content: contents) {
            val.append(content.toRawString());
            val.append(" ");
        }
        val.append("]");
        return val.toString();
    }
}

@FunctionalInterface
interface FunctionCall {
    RuntimeValue call(ArrayList<RuntimeValue> args, Environment env);
}


class RNativeFunction extends RuntimeValue {

    public FunctionCall call;

    public RNativeFunction(FunctionCall call) {
        this.call = call;
    }

    public RNativeFunction() {}

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.NativeFunction;
    }

    @Override
    public String toRawString() {
        return "<native-fn-" + call.toString() + ">";
    }

    static RNativeFunction MAKE_NATIVE_FN(FunctionCall call) {
        return new RNativeFunction(call);
    }
}

class RModule extends RuntimeValue {
    public String moduleName;
    public HashMap<String, RNativeFunction> functions;

    public RModule() {}

    public RModule(String moduleName) {
        this.moduleName = moduleName;
        this.functions = new HashMap<>();
    }

    @Override
    public String toString() {
        return "RModule{" +
                "moduleName='" + moduleName + '\'' +
                ", functions=" + functions +
                '}';
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.Module;
    }

    @Override
    public String toRawString() {
        return "RModule{" +
                "moduleName='" + moduleName + '\'' +
                ", functions=" + functions +
                '}';
    }
}

class RFunctionValue extends RuntimeValue {
    public String functionName;
    public ArrayList<Expr> parameters;
    public ArrayList<Stmt> body;
    public Environment declarationEnv;

    public RFunctionValue(String functionName, ArrayList<Expr> parameters, ArrayList<Stmt> body, Environment declarationEnv) {
        this.functionName = functionName;
        this.parameters = parameters;
        this.body = body;
        this.declarationEnv = declarationEnv;
    }

    public RFunctionValue() {}

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.FunctionValue;
    }

    @Override
    public String toRawString() {
        return null;
    }
}

class RIfNode extends RuntimeValue {

    public Expr conditionResult;
    public ArrayList<Stmt> body;
    public RBooleanValue isElse;

    public RIfNode(Expr conditionResult, ArrayList<Stmt> body, RBooleanValue isElse) {
        this.conditionResult = conditionResult;
        this.body = body;
        this.isElse = isElse;
    }

    public RIfNode() {}

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.IfNode;
    }

    @Override
    public String toRawString() {
        return null;
    }
}

class RIfStatement extends RuntimeValue {

    public ArrayList<RIfNode> clauses;

    public RIfStatement() {}

    public RIfStatement(ArrayList<RIfNode> clauses) {
        this.clauses = clauses;
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.IfStatement;
    }

    @Override
    public String toRawString() {
        return null;
    }
}

class RWhile extends RuntimeValue {
    public Expr condition;
    public ArrayList<Stmt> body;

    public RWhile(Expr condition, ArrayList<Stmt> body) {
        this.condition = condition;
        this.body = body;
    }

    public RWhile(Expr condition) {
        this.condition = condition;
        this.body = new ArrayList<>();
    }

    public RWhile() {}

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.While;
    }

    @Override
    public String toRawString() {
        return null;
    }
}


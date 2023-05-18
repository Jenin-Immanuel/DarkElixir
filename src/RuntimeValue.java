import java.util.ArrayList;

enum RuntimeValueType {
    Null,
    Number,
    Boolean,
    Atom,
    Tuple,
    NativeFunction
}

public abstract class RuntimeValue {
    public RuntimeValueType kind;

    public abstract RuntimeValueType getKind();

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
}

class RBooleanValue extends RuntimeValue {
    public Boolean value;

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

    static RNativeFunction MAKE_NATIVE_FN(FunctionCall call) {
        return new RNativeFunction(call);
    }
}

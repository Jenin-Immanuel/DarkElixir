enum RuntimeValueType {
    Null,
    Number
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
    public Float number;

    @Override
    public String toString() {
        return "RNumberValue{" + number + "}";
    }

    @Override
    public RuntimeValueType getKind() {
        return RuntimeValueType.Number;
    }
}
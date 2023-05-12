enum RuntimeValueType {
    Null,
    Number,
    Boolean
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
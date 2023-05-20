import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

public class Environment {
    private Environment parent;
    private HashMap<String, RuntimeValue> variables;

    public Environment() {
        this.parent = null;
        this.variables = new HashMap<>();
    }

    public Environment(Environment parent) {
        this.parent = parent;
        this.variables = new HashMap<>();
    }

    public static Environment createGlobalEnvironment() {
        Environment env = new Environment();
        env.declareVariable("null", new RNullValue());
        env.declareVariable("true", new RBooleanValue(true));
        env.declareVariable("false", new RBooleanValue(false));

        env.declareVariable("print", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            StringBuilder string = new StringBuilder();
            for(var arg: args) {
                string.append(arg.toRawString()).append(" ");
            }
            System.out.println(string.toString().trim());
            return new RNullValue();
        }));

        env.declareVariable("date", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            System.out.println(Date.from(Instant.now()));
            return new RNullValue();
        }));

        return env;
    }

    public boolean containsVariable(String variableName) {
        var env = safeResolveEnvironment(variableName);
        return env != null;
    }

    // Same as resolve. But doesn't throw an error
    public Environment safeResolveEnvironment(String variableName) {
        if(this.variables.containsKey(variableName)) {
            return this;
        }
        if(this.parent == null) {
            return null;
        }
        return this.resolveEnvironment(variableName);
    }
    public RuntimeValue declareVariable(String variableName, RuntimeValue value) {
        if(this.variables.containsKey(variableName)) {
            this.assignVariable(variableName, value);
            return value;
//            System.err.println("Cannot declare a variable that already exists. Variable: " + variableName);
//            System.exit(0);
        }
        this.variables.put(variableName, value);
        return value;
    }

    public RuntimeValue assignVariable(String variableName, RuntimeValue value) {
        var env = this.resolveEnvironment(variableName);
        env.variables.put(variableName, value);
        return value;
    }

    public RuntimeValue lookupVariable(String variableName) {
        var env = this.resolveEnvironment(variableName);
        return env.variables.get(variableName);
    }

    public Environment resolveEnvironment(String variableName) {
        if(this.variables.containsKey(variableName)) {
            return this;
        }
        if(this.parent == null) {
            System.err.println("Cannot resolve variable " + variableName + " as it doesn't exist.");
            System.exit(0);
        }
        return this.resolveEnvironment(variableName);
    }
}

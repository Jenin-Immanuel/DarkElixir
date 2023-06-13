import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class Environment {
    private Environment parent;
    private HashMap<String, RuntimeValue> variables;

    private Set<String> constants;

    public Environment() {
        this.parent = null;
        this.variables = new HashMap<>();
        this.constants = new TreeSet<>();
    }

    public Environment(Environment parent) {
        this.parent = parent;
        this.variables = new HashMap<>();
        this.constants = new TreeSet<>();
    }

    public Environment getParent() {
        return parent;
    }

    public HashMap<String, RuntimeValue> getVariables() {
        return variables;
    }

    public Set<String> getConstants() {
        return constants;
    }

    public static Environment createGlobalEnvironment() {
        Environment env = new Environment();
        env.declareVariable("null", new RNullValue(), true);
        env.declareVariable("true", new RBooleanValue(true), true);
        env.declareVariable("false", new RBooleanValue(false), true);

        env.declareVariable("print", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            StringBuilder string = new StringBuilder();
            for(var arg: args) {
                string.append(arg.toRawString()).append(" ");
            }
            System.out.println(string.toString().trim());
            return new RNullValue();
        }), true);

        env.declareVariable("date", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            System.out.println(Date.from(Instant.now()));
            return new RNullValue();
        }), true);

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
        return this.parent.safeResolveEnvironment(variableName);
    }

    public RuntimeValue declareVariable(String variableName, RuntimeValue value) {
        return declareVariable(variableName, value,false);
    }

    public RuntimeValue declareVariable(String variableName, RuntimeValue value, Boolean constant) {
        if(this.variables.containsKey(variableName)) {
            this.assignVariable(variableName, value);
            return value;
//            System.err.println("Cannot declare a variable that already exists. Variable: " + variableName);
//            System.exit(0);
        }
        this.variables.put(variableName, value);
        if(constant) this.constants.add(variableName);

        return value;
    }

    public RuntimeValue assignVariable(String variableName, RuntimeValue value) {
        var env = this.resolveEnvironment(variableName);
        env.variables.put(variableName, value);
        if(env.constants.contains(variableName)) {
            System.err.println("Cannot reassign to variable " + variableName + " as it was declared as a constant");
            System.exit(0);
        }
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
        return this.parent.resolveEnvironment(variableName);
    }
}

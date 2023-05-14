import java.util.HashMap;

public class Environment {
    private Environment parent;
    private HashMap<String, RuntimeValue> variables;

    public Environment(Environment parent) {
        this.parent = parent;
        this.variables = new HashMap<>();
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
            System.err.println("Cannot declare a variable that already exists. Variable: " + variableName);
            System.exit(0);
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

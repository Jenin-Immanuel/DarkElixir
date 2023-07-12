import java.time.Instant;
import java.util.*;


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
                if(arg.getKind() == RuntimeValueType.String) {
                    string.append(((RStringValue) arg).withQuotes()).append(" ");
                }
                else
                    string.append(arg.toRawString()).append(" ");
            }
            System.out.println(string.toString().trim());
            return new RNullValue();
        }), true);

        env.declareVariable("date", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            System.out.println(Date.from(Instant.now()));
            return new RNullValue();
        }), true);


        // Modules baby
        Modules.declareAllModules(env);


        // is a function
        env.declareVariable("is_boolean", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            if(args.size() != 1) {
                System.err.println("Invalid Arguments: is_boolean/1 function accepts only one argument");
                System.exit(0);
            }
            return new RBooleanValue(args.get(0).getKind() == RuntimeValueType.Boolean);
        }));

        env.declareVariable("is_atom", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            if(args.size() != 1) {
                System.err.println("Invalid Arguments: is_atom/1 function accepts only one argument");
                System.exit(0);
            }
            return new RBooleanValue(args.get(0).getKind() == RuntimeValueType.Atom);
        }));

        env.declareVariable("is_number", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            if(args.size() != 1) {
                System.err.println("Invalid Arguments: is_number/1 function accepts only one argument");
                System.exit(0);
            }
            return new RBooleanValue(args.get(0).getKind() == RuntimeValueType.Number);
        }));

        env.declareVariable("is_tuple", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            if(args.size() != 1) {
                System.err.println("Invalid Arguments: is_tuple/1 function accepts only one argument");
                System.exit(0);
            }
            return new RBooleanValue(args.get(0).getKind() == RuntimeValueType.Tuple);
        }));

        env.declareVariable("is_list", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            if(args.size() != 1) {
                System.err.println("Invalid Arguments: is_list/1 function accepts only one argument");
                System.exit(0);
            }
            return new RBooleanValue(args.get(0).getKind() == RuntimeValueType.List);
        }));


        // len function
        env.declareVariable("len", RNativeFunction.MAKE_NATIVE_FN((args, scope) -> {
            if(args.size() != 1) {
                System.err.println("Invalid Arguments: len/1 function accepts only one argument");
                System.exit(0);
            }
            RuntimeValue arg = args.get(0);
            switch(arg.getKind()) {
                case Tuple -> {
                    return new RNumberValue((double) ((RTupleValue) arg).contents.size());
                }
                case List -> {
                    return new RNumberValue((double) ((RListValue) arg).contents.size());
                }
                case String -> {
                    return new RNumberValue((double) ((RStringValue) arg).value.length() - 2); // Remove the double quotes
                }

            }
            System.err.println("Invalid argument for len function, Given " + arg.getKind());
            System.exit(0);
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

public class Modules {
    static void declareTupleModule(Environment scope) {
        RModule module = new RModule("Tuple");

        // at/2
        module.functions.put("at", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            if(args.size() != 2) {
                System.err.println(module.moduleName + " at function accepts only two argument");
                System.exit(0);
            }

            if(args.get(0).getKind() != RuntimeValueType.Tuple) {
                System.err.println("Error");
                System.exit(0);
            }

            if(args.get(1).getKind() != RuntimeValueType.Number) {
                System.err.println("Error");
                System.exit(0);
            }


            var firstArg = (RTupleValue) args.get(0);
            var secondArg = (RNumberValue) args.get(1);

            if (firstArg == null || firstArg.contents == null) {
                throw new RuntimeException("Error: Invalid first argument");
            }

            int index = secondArg.number.intValue();
            if (index >= firstArg.contents.size()) {
                System.err.println("Error: Index out of bounds");
                System.exit(0);
            }

            return firstArg.contents.get(index);
        })));

        scope.declareVariable("Tuple", module, true);
    }

     static void declareAllModules(Environment env) {
        declareTupleModule(env);
    }
}

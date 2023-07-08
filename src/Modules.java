import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class Modules {

    static void expect(RuntimeValueType given, RuntimeValueType expected, String msg) {
        if(given != expected) {
            System.err.println(msg);
            System.exit(0);
        }
    }

    static void expectArgs(String functionName, Integer givenArgs, Integer requiredArgs, String argFormat) {
        if(!Objects.equals(givenArgs, requiredArgs)) {
            System.err.println("InvalidArguments: Argument format of " + functionName + " " + argFormat);
            System.exit(0);
        }
    }

    static void expectEnumerable(RuntimeValue given, String argFormat) {
        if(given.getKind() != RuntimeValueType.List && given.getKind() != RuntimeValueType.Map) {
            System.err.println(argFormat);
            System.exit(0);
        }
    }


    static void declareEnumModule(Environment scope) {
        RModule module = new RModule("Enum");

        // map/2
        module.functions.put("map", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Enum.map/2 (enumerable, fn)";
            if(args.size() != 2) {
                System.err.println(module.moduleName + " at function accepts only two argument");
                System.exit(0);
            }
            var firstArg = args.get(0);

            expectEnumerable(firstArg, argFormat);


            expect(args.get(1).getKind(), RuntimeValueType.AnonymousFn, argFormat);

//            var secondArg = (RAnonymousFn) args.get(1);
            switch (firstArg.getKind()) {
                case List -> {
                    var fnValue = (RAnonymousFn) args.get(1);
                    // args should be only 1
                    if(fnValue.parameters.size() != 1) {
                        System.err.println("The map function should have only one arg for list");
                        System.exit(0);
                    }
                    var innerScope = new Environment(fnValue.declarationEnv);

                    var list = (RListValue) firstArg;
                    RListValue res = new RListValue();
                    for(var e: list.contents) {
                        innerScope.declareVariable(((Identifier)fnValue.parameters.get(0)).symbol, e, false);
                        res.contents.add(Interpreter.evaluate(fnValue.returnExpr, innerScope));
                    }
                    return res;
                }
                case Map -> {
                    var fnValue = (RAnonymousFn) args.get(1);
                    // args should be only 1
                    if(fnValue.parameters.size() != 2) {
                        System.err.println("The map function should have two args for maps");
                        System.exit(0);
                    }
                    var innerScope = new Environment(fnValue.declarationEnv);

                    var map = (RMapStructure) firstArg;
                    RMapStructure res = new RMapStructure();

                }
            }


            return new RNullValue();
        })));

        // sum

        module.functions.put("sum", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            expectArgs("sum", args.size(), 1, "sum (list)");

            expect(args.get(0).getKind(), RuntimeValueType.List, module.moduleName + " at function accepts only two argument");
            var list = (RListValue) args.get(0);

            Double sum = (double) 0;
            for(var e: list.contents) {
                if(e.getKind() !=  RuntimeValueType.Number) {
                    System.err.println("The array in Enum.sum should contain only numbers");
                    System.exit(0);
                }

                RNumberValue n = (RNumberValue) e;
                sum += n.number;
            }

            return new RNumberValue(sum);
        })));

        // sort/1
        // sort/2

        scope.declareVariable("Enum", module, true);
    }
    static void declareTupleModule(Environment scope) {
        RModule module = new RModule("Tuple");

        // at/2
        module.functions.put("at", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Tuple.at/2 (tuple, index)";
            if(args.size() != 2) {
                System.err.println(module.moduleName + " at function accepts only two argument");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.Tuple, argFormat);
            expect(args.get(1).getKind(), RuntimeValueType.Number, argFormat);


            var firstArg = (RTupleValue) args.get(0);
            var secondArg = (RNumberValue) args.get(1);


            return firstArg.contents.get(secondArg.number.intValue());
        })));

        // append/2
        module.functions.put("append", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argError = "InvalidArguments: Argument Format of Tuple.append/2 (tuple, element)";
            if(args.size() != 2) {
                System.err.println(module.moduleName + " at function accepts only two argument");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.Tuple, argError);
            var firstArg = (RTupleValue) args.get(0);
            firstArg.contents.add(args.get(1));
            return firstArg;
        })));

        // delete_at/2
        module.functions.put("delete_at", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argError = "InvalidArguments: Argument Format of Tuple.delete_at/2 (tuple, index)";
            if(args.size() != 2) {
                System.err.println(module.moduleName + " at function accepts only two argument");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.Tuple, argError);
            expect(args.get(1).getKind(), RuntimeValueType.Number, argError);


            var firstArg = (RTupleValue) args.get(0);
            var secondArg = (RNumberValue) args.get(1);
            firstArg.contents.remove(secondArg.number.intValue());
            return firstArg;
        })));

        // insert_at/3
        module.functions.put("insert_at", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argError = "InvalidArguments: Argument Format of Tuple.insert_at/3 (tuple, index, value)";
            if(args.size() != 3) {
                System.err.println(module.moduleName + " at function accepts only two argument");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.Tuple, argError);
            expect(args.get(1).getKind(), RuntimeValueType.Number, argError);


            var firstArg = (RTupleValue) args.get(0);
            var secondArg = (RNumberValue) args.get(1);

            if(secondArg.number >= firstArg.contents.size()) {
                System.err.println("Invalid Argument:Size of the tuple is smaller than the given index. Tuple.insert_at/3");
            }
            System.out.println(secondArg.number.intValue() + " " + firstArg.contents.size());
            firstArg.contents.add(secondArg.number.intValue(), args.get(2));
            return firstArg;
        })));

        // to_list/1
        module.functions.put("to_list", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argError = "InvalidArguments: Argument Format of Tuple.to_list/1 (tuple)";

            if(args.size() != 1) {
                System.err.println(module.moduleName + " to_list function accepts only one argument");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.Tuple, argError);
            var firstArg = (RTupleValue) args.get(0);

            RListValue list = new RListValue();
            list.contents.addAll(firstArg.contents);
            return list;
        })));


        scope.declareVariable("Tuple", module, true);
    }
     static void declareAllModules(Environment env) {
        declareTupleModule(env);
        declareEnumModule(env);
    }
}

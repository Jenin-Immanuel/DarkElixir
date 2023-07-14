import java.util.*;
import java.util.stream.Collectors;

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
        if(given.getKind() == RuntimeValueType.List || given.getKind() == RuntimeValueType.Map)
            return;
        System.err.println(argFormat);
        System.exit(0);
    }

    static void expectFunction(RuntimeValue given, String argFormat) {
        if(given.getKind() == RuntimeValueType.AnonymousFn || given.getKind() == RuntimeValueType.FunctionValue)
            return;
        System.err.println(argFormat);
        System.exit(0);
    }

    static void safeError(String msg) {
        System.err.println(msg);
        System.exit(0);
    }


    static void declareMapModule(Environment scope) {
        RModule module = new RModule("Map");

        // delete (map, key)
        // Returns: A map with the particular key removed

        module.functions.put("delete", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Map.delete/2 (map, key)";
            expectArgs("Map.delete", args.size(), 2, "(map, key)");
            expect(args.get(0).getKind(), RuntimeValueType.Map, argFormat);

            // 2nd arg can be of any value

            RMapStructure map = (RMapStructure) args.get(0);

            if(!map.map.containsKey(args.get(1))) {
                safeError("IndexError: Map.delete() Map does not contain the given key");
            }
            map.map.remove(args.get(1));

            return map;
        })));

        // fetch (map, key)
        // Returns : The value of the particular key in the map or null if it is not present

        module.functions.put("fetch", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Map.fetch/2 (map, key)";
            expectArgs("Map.fetch", args.size(), 2, "(map, key)");
            expect(args.get(0).getKind(), RuntimeValueType.Map, argFormat);

            RMapStructure map = (RMapStructure) args.get(0);

            if(!map.map.containsKey(args.get(1))) {
                return new RNullValue();
            }

            return map.map.get(args.get(1));
        })));

        // replace(map, key, value)
        // Returns: A new map with the updated value

        module.functions.put("replace", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Map.replace/3 (map, key, value)";
            expectArgs("Map.replace", args.size(), 3, "(map, key, value)");
            expect(args.get(0).getKind(), RuntimeValueType.Map, argFormat);

            RMapStructure map = (RMapStructure) args.get(0);
            if(!map.map.containsKey(args.get(1))) {
                safeError("IndexError: Map.replace() The given key is not present in the map");
            }

            map.map.replace(args.get(1), args.get(2));
            return map;
        })));

        // to_list(map)

        module.functions.put("to_list", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Map.to_list/1 (map)";
            expectArgs("Map.to_list", args.size(), 1, "(map)");
            expect(args.get(0).getKind(), RuntimeValueType.Map, argFormat);

            RMapStructure map = (RMapStructure) args.get(0);
            RListValue res = new RListValue();

            for(Map.Entry<RuntimeValue, RuntimeValue> entry : map.map.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                RTupleValue ans = new RTupleValue();
                ans.contents.add(key);
                ans.contents.add(value);
                res.contents.add(ans);
            }


            return res;
        })));



        scope.declareVariable("Map", module, true);

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
                    for(Map.Entry<RuntimeValue, RuntimeValue> entry: map.map.entrySet()) {
                        var key =  entry.getKey();
                        var value = entry.getValue();
                        innerScope.declareVariable(((Identifier)fnValue.parameters.get(0)).symbol, key, false);
                        innerScope.declareVariable(((Identifier)fnValue.parameters.get(1)).symbol, value, false);
                        var result = Interpreter.evaluate(fnValue.returnExpr, innerScope);
                        if(result.getKind() != RuntimeValueType.Tuple)
                            safeError("Enum.map The function should return a tuple of two elements for map operations");
                        var t = (RTupleValue) result;
                        if(t.contents.size() != 2)
                            safeError("Enum.map The function should return a tuple of two elements for map operations");
                        res.map.put(t.contents.get(0), t.contents.get(1));
                    }
                    return res;
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
        module.functions.put("sort", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Enum.sort/1 (enumerable)";
            expectArgs("Enum.sort", args.size(), 1, "(enumerable)");
            expect(args.get(0).getKind(), RuntimeValueType.List, argFormat);

            RListValue list = (RListValue) args.get(0);
            RListValue newList = new RListValue();

            // Precedence
            // Number, Atom, Tuple, List, Map, String
            int size = 0;
            for(int i = 0; i < 5; i++) {
                if(newList.contents.size() == list.contents.size()) break;

                // Get the required list
                switch (i) {
                    case 0 -> {
                        // Number
                        // Get all the numbers from the given list
                        ArrayList<RNumberValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.Number)
                                .map(e -> (RNumberValue) e)
                                .sorted(Comparator.comparing(o -> o.number))
                                .collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                    case 1 -> {
                        // Atom
                        // Get all the atoms from the list
                        ArrayList<RAtomValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.Atom)
                                .map(e -> (RAtomValue) e)
                                .sorted(Comparator.comparing(o -> o.value))
                                .collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                    case 2 -> {
                        // Tuples
                        // Get all tuples
                        ArrayList<RTupleValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.Tuple)
                                .map(e -> (RTupleValue) e)
                                .sorted(Comparator.comparing(o -> o.contents.size())).
                                collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                    case 3 -> {
                        // List
                        ArrayList<RListValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.List)
                                .map(e -> (RListValue) e)
                                .sorted(Comparator.comparing(o -> o.contents.size())).
                                collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                    case 4 -> {
                        // String
                        ArrayList<RStringValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.String)
                                .map(e -> (RStringValue) e)
                                .sorted(Comparator.comparing(o -> o.value))
                                .collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                }
            }

            return newList;
        })));

        // reverse/1
        module.functions.put("reverse", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Enum.reverse/1 (enumerable)";
            expectArgs("Enum.reverse", args.size(), 1, "(enumerable)");
            expect(args.get(0).getKind(), RuntimeValueType.List, argFormat);

            RListValue list = (RListValue) args.get(0);
            RListValue newList = new RListValue();

            // Precedence
            // Number, Atom, Tuple, List, Map, String
            for(int i = 4; i >= 0; i--) {
                if(newList.contents.size() == list.contents.size()) break;

                // Get the required list
                switch (i) {
                    case 0 -> {
                        // Number
                        // Get all the numbers from the given list
                        ArrayList<RNumberValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.Number)
                                .map(e -> (RNumberValue) e)
                                .sorted(Comparator.comparing(o -> ((RNumberValue) o).number).reversed())
                                .collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                    case 1 -> {
                        // Atom
                        // Get all the atoms from the list
                        ArrayList<RAtomValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.Atom)
                                .map(e -> (RAtomValue) e)
                                .sorted(Comparator.comparing(o -> ((RAtomValue) o).value).reversed())
                                .collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                    case 2 -> {
                        // Tuples
                        // Get all tuples
                        ArrayList<RTupleValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.Tuple)
                                .map(e -> (RTupleValue) e)
                                .sorted(Comparator.comparing(o -> ((RTupleValue) o).contents.size()).reversed()).
                                collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                    case 3 -> {
                        // List
                        ArrayList<RListValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.List)
                                .map(e -> (RListValue) e)
                                .sorted(Comparator.comparing(o -> ((RListValue) o).contents.size()).reversed())
                                .collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                    case 4 -> {
                        // String
                        ArrayList<RStringValue> t = list.contents.stream()
                                .filter(e -> e.getKind() == RuntimeValueType.String)
                                .map(e -> (RStringValue) e)
                                .sorted(Comparator.comparing(o -> ((RStringValue) o).value))
                                .collect(Collectors.toCollection(ArrayList::new));
                        newList.contents.addAll(t);
                    }
                }
            }

            return newList;
        })));

        // count
        module.functions.put("count", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Enum.count/1 (enumerable)";
            expectArgs("Enum.reverse", args.size(), 1, "(enumerable)");
            expectEnumerable(args.get(0), argFormat);

            switch(args.get(0).getKind()) {
                case List -> {
                    return new RNumberValue((double) ((RListValue) args.get(0)).contents.size());
                }
                case Map -> {
                    return new RNumberValue((double) ((RMapStructure) args.get(0)).map.size());
                }
            }
            return new RNullValue();
        })));

        // each/2

        module.functions.put("each", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Enum.each/2 (enumerable, fn)";
            expectArgs("Enum.each", args.size(), 2, "(enumerable, fn)");
            expectEnumerable(args.get(0), argFormat);
            expectFunction(args.get(1), argFormat);


            var e = args.get(0);
            var f = args.get(1);
            switch (e.getKind()) {
                case List -> {
                    var list = (RListValue) e;
                    switch (f.getKind()) {
                        case AnonymousFn -> {
                            var fn = (RAnonymousFn) f;
                            var innerScope = new Environment(fn.declarationEnv);
                            list.contents.forEach(element -> {
                                innerScope.declareVariable(((Identifier)fn.parameters.get(0)).symbol, element, false);
                                Interpreter.evaluate(fn.returnExpr, innerScope);
                            });
                        }
                        case FunctionValue -> {
                            var fn = (RFunctionValue) f;
                            if(fn.parameters.size() != 1)
                                safeError("Enum.each The given function should have only one argument for lists");
                            var innerScope = new Environment(fn.declarationEnv);
                            list.contents.forEach(element -> {
                                innerScope.declareVariable(((Identifier)fn.parameters.get(0)).symbol, element, false);
                                fn.body.forEach(stmt -> {
                                    var res = Interpreter.evaluate(stmt, innerScope);
                                    if(res.getKind() == RuntimeValueType.Break || res.getKind() == RuntimeValueType.Continue)
                                        safeError("Top level break/continue statements are not allowed");
                                });
                            });
                        }
                    }


                }
                case Map -> {
                    var map = (RMapStructure) e;
                    switch (f.getKind()) {
                        case AnonymousFn ->  {
                            var fn = (RAnonymousFn) f;
                            if(fn.parameters.size() != 2)
                                safeError("Enum.each The given function should have two arguments for maps");
                            var innerScope = new Environment(fn.declarationEnv);
                            map.map.forEach((key, value) -> {
                                innerScope.declareVariable(((Identifier)fn.parameters.get(0)).symbol, key, false);
                                innerScope.declareVariable(((Identifier)fn.parameters.get(1)).symbol, value, false);
                                Interpreter.evaluate(fn.returnExpr, innerScope);
                            });
                        }
                        case FunctionValue -> {
                            var fn = (RFunctionValue) f;
                            if(fn.parameters.size() != 2)
                                safeError("Enum.each The given function should have two arguments for maps");
                            var innerScope = new Environment(fn.declarationEnv);
                            map.map.forEach((key, value) -> {
                                innerScope.declareVariable(((Identifier)fn.parameters.get(0)).symbol, key, false);
                                innerScope.declareVariable(((Identifier)fn.parameters.get(1)).symbol, value, false);
                                fn.body.forEach(stmt -> {
                                    var res = Interpreter.evaluate(stmt, innerScope);
                                    if(res.getKind() == RuntimeValueType.Break || res.getKind() == RuntimeValueType.Continue)
                                        safeError("Top level break/continue statements are not allowed");
                                });
                            });
                        }
                    }
                }
            }

            return new RNullValue();
        })));

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
                System.err.println(module.moduleName + " append function accepts only two argument");
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
                System.err.println(module.moduleName + " insert_at function accepts only two argument");
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

    static void declareListModule(Environment scope) {
        RModule module = new RModule("List");

        module.functions.put("at", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of List.at/2 (list, index)";
            if(args.size() != 2) {
                System.err.println(module.moduleName + " at function accepts only two argument");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.List, argFormat);
            expect(args.get(1).getKind(), RuntimeValueType.Number, argFormat);


            var firstArg = (RListValue) args.get(0);
            var secondArg = (RNumberValue) args.get(1);


            return firstArg.contents.get(secondArg.number.intValue());
        })));

        module.functions.put("append", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argError = "InvalidArguments: Argument Format of List.append/2 (list, element)";
            if(args.size() != 2) {
                System.err.println(module.moduleName + " append function accepts only two argument");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.List, argError);
            var firstArg = (RListValue) args.get(0);
            firstArg.contents.add(args.get(1));
            return firstArg;
        })));

        module.functions.put("delete_at", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argError = "InvalidArguments: Argument Format of List.delete_at/2 (list, index)";
            if(args.size() != 2) {
                System.err.println(module.moduleName + " delete_at function accepts only two arguments");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.List, argError);
            expect(args.get(1).getKind(), RuntimeValueType.Number, argError);


            var firstArg = (RListValue) args.get(0);
            var secondArg = (RNumberValue) args.get(1);
            firstArg.contents.remove(secondArg.number.intValue());
            return firstArg;
        })));

        module.functions.put("insert_at", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argError = "InvalidArguments: Argument Format of List.insert_at/3 (list, index, value)";
            if(args.size() != 3) {
                System.err.println(module.moduleName + " insert_at function accepts three arguments");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.List, argError);
            expect(args.get(1).getKind(), RuntimeValueType.Number, argError);


            var firstArg = (RListValue) args.get(0);
            var secondArg = (RNumberValue) args.get(1);

            if(secondArg.number >= firstArg.contents.size()) {
                System.err.println("Invalid Argument:Size of the list is smaller than the given index. List.insert_at/3");
            }
            System.out.println(secondArg.number.intValue() + " " + firstArg.contents.size());
            firstArg.contents.add(secondArg.number.intValue(), args.get(2));
            return firstArg;
        })));

        module.functions.put("pop", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argError = "InvalidArguments: Argument Format of List.pop (list)";
            if(args.size() != 1) {
                System.err.println(module.moduleName + " pop function accepts only one argument");
                System.exit(0);
            }

            expect(args.get(0).getKind(), RuntimeValueType.List, argError);

            var firstArg = (RListValue) args.get(0);
            if(firstArg.contents.size() == 0) {
                System.err.println("List.pop: Index Error while pop");
                System.exit(0);
            }

            RTupleValue returnValue = new RTupleValue();
            returnValue.contents.add(firstArg.contents.get(firstArg.contents.size() - 1));
            firstArg.contents.remove(firstArg.contents.size() - 1);
            returnValue.contents.add(firstArg);

            return returnValue;
        })));

        scope.declareVariable("List", module, true);
    }

    static void declareStringModule(Environment scope) {
        RModule module = new RModule("String");

        // length
        module.functions.put("length", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of String.length/1 (string)";
            expectArgs("String.length", args.size(), 1, "(string)");
            expect(args.get(0).getKind(), RuntimeValueType.String, argFormat);
            RStringValue stringValue = (RStringValue) args.get(0);


            return new RNumberValue((double) stringValue.value.length() - 2);
        })));

        // split
        module.functions.put("split", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of String.split/2 (string, separator)";
            expectArgs("String.split", args.size(), 2, "(string, separator)");
            expect(args.get(0).getKind(), RuntimeValueType.String, argFormat);
            expect(args.get(1).getKind(), RuntimeValueType.String, argFormat);
            RStringValue stringValue = (RStringValue) args.get(0);
            RStringValue delimiterValue = (RStringValue) args.get(1);
            RListValue list = new RListValue();
            var l = stringValue.value.split(delimiterValue.toRawString());
            for(var e: l)
                list.contents.add(new RStringValue("\"" + e.replace("\"", "") + "\""));

            return list;
        })));

        // to_number
        module.functions.put("to_number", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of String.to_number/1 (string)";
            expectArgs("String.to_number", args.size(), 1, "(string)");
            expect(args.get(0).getKind(), RuntimeValueType.String, argFormat);
            var arg = (RStringValue) args.get(0);
            double res = 0;
            try {
                res = Double.parseDouble(arg.toRawString());
            } catch(Exception e) {
                System.out.println("Expecting a string with number for input() function");
                System.exit(0);
            }
            return new RNumberValue(res);
        })));

        scope.declareVariable("String", module, true);
    }

    static void declareNumberModule(Environment scope) {
        RModule module = new RModule("Number");

        module.functions.put("pow", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            // validation
            String argFormat = "InvalidArguments: Argument Format of Number.pow/2 (base, exponent)";
            expectArgs("Number.pow", args.size(), 2, "(base, exponent)");
            expect(args.get(0).getKind(), RuntimeValueType.Number, argFormat);
            expect(args.get(1).getKind(), RuntimeValueType.Number, argFormat);
            var n1 = (RNumberValue) args.get(0);
            var n2 = (RNumberValue) args.get(1);
            return new RNumberValue(Math.pow(n1.number, n2.number));
        })));

        module.functions.put("floor_div", RNativeFunction.MAKE_NATIVE_FN(((args, env) -> {
            String argFormat = "InvalidArguments: Argument Format of Number.floor_div/2 (dividend, divisor)";
            expectArgs("Number.floor_div", args.size(), 2, "(dividend, divisor)");
            expect(args.get(0).getKind(), RuntimeValueType.Number, argFormat);
            expect(args.get(1).getKind(), RuntimeValueType.Number, argFormat);
            var n1 = (RNumberValue) args.get(0);
            var n2 = (RNumberValue) args.get(1);
            return new RNumberValue(Math.floor(n1.number / n2.number));
        })));
        scope.declareVariable("Number", module, true);
    }
     static void declareAllModules(Environment env) {
        declareTupleModule(env);
        declareEnumModule(env);
        declareListModule(env);
        declareMapModule(env);
        declareStringModule(env);
        declareNumberModule(env);
    }
}

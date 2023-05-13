import java.util.Scanner;
public class Main {
    static void repl() {
        Scanner sc = new Scanner(System.in);
        Parser parser = new Parser();

        // The environment scope for the global variables
        Environment env = new Environment(null);

        env.declareVariable("x", new RNumberValue(100D));
        env.declareVariable("null", new RNullValue());
//        env.declareVariable("true", new RAtomValue("true"));
//        env.declareVariable("false", new RAtomValue("false"));
        env.declareVariable("true", new RBooleanValue(true));
        env.declareVariable("false", new RBooleanValue(false));
        while(true) {
            System.out.print("user> ");
            String prompt = sc.nextLine();
            if(prompt.equals("exit") || prompt.equals("e")) {
                System.out.println("Bye Bye...");
                break;
            }


            Program program = parser.produceAst(prompt);
            var result = Interpreter.evaluateProgram(program, env);
            System.out.println(result);

        }
    }
    public static void main(String[] args) {
        repl();
    }
}

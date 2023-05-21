import java.util.Scanner;
public class Main {
    static void repl() {
        Scanner sc = new Scanner(System.in);
        Parser parser = new Parser();

        // The environment scope for the global variables
        Environment env = Environment.createGlobalEnvironment();
        int line = 1;
        while(true) {
            System.out.print("dex(" + line + ")> ");
            String prompt = sc.nextLine();
            if(prompt.equals("exit") || prompt.equals("e")) {
                System.out.println("Bye Bye...");
                break;
            }


            Program program = parser.produceAst(prompt);
            var result = Interpreter.evaluateProgram(program, env);


            // Don't print if it is a null character
            if(result.getKind() != RuntimeValueType.Null) {
                System.out.println(result);
            }
            line++;
        }
    }
    public static void main(String[] args) {
        repl();
    }
}

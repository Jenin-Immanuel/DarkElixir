import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static void repl() {
        Scanner sc = new Scanner(System.in);
        while(true) {
            System.out.print("user> ");
            String prompt = sc.nextLine();
            if(prompt.equals("exit") || prompt.equals("e")) {
                System.out.println("Bye Bye...");
                break;
            }
//            System.out.println(prompt);
            Parser parser = new Parser();
            Program program = parser.produceAst(prompt);
            var result = Interpreter.evaluateProgram(program);
            System.out.println(result);

        }
    }
    public static void main(String[] args) {
        repl();
    }
}

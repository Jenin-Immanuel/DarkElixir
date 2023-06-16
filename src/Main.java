import java.io.*;
import java.util.Scanner;
public class Main {

    static void run(String filePath) {
        StringBuilder sourceCode = new StringBuilder();
        try {
            File Obj = new File(filePath);
            Scanner Reader = new Scanner(Obj);
            while (Reader.hasNextLine()) {
                if(sourceCode.isEmpty()) {
                    sourceCode.append(Reader.nextLine());
                }
                else {
                    sourceCode.append("\n").append(Reader.nextLine());
                }
            }
            Reader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An error has occurred.");
            e.printStackTrace();
        }

        Parser parser = new Parser();

        // The environment scope for the global variables
        Environment env = Environment.createGlobalEnvironment();

        Program program = parser.produceAst(sourceCode.toString());
//        System.out.println(program.body);
        var result = Interpreter.evaluateProgram(program, env);
    }
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
        run("test.dx");
    }
}

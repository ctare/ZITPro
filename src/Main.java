import newlang4.*;
import newlangdef.Program;

/**
 * Created by ctare on 2018/01/08.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        LexicalAnalyzer lex = new LexicalAnalyzerImpl("resource/input");
        Environment env = new Environment(lex);

        Node program;
        while(true) {
            program = NodeUtil.isMatch(Program.class, env, null);
            if(program == null || !program.parse()) {
                break;
            }
        }

        System.out.println("-----");
        System.out.println(GlobalScope.scopes.get(GlobalScope.main).get("a").getIValue());
    }
}

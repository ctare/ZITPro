package newlang4;

import newlangdef.Program;

import java.io.FileInputStream;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		LexicalAnalyzer lex;
		Environment		env;
		Node			program;

		System.out.println("basic parser");
		lex = new LexicalAnalyzerImpl("resource/input");
        env = new Environment(lex);

        while(true) {
            program = NodeUtil.isMatch(Program.class, env, null);
            if(program == null || !program.parse()) {
                break;
            }
        }
	}

}

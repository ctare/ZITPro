package newlang4;

import newlangdef.Expr;
import newlangdef.Program;
import newlangdef.StmtList;

import java.io.FileInputStream;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		FileInputStream fin = null;
		LexicalAnalyzer lex;
		LexicalUnit		first;
		Environment		env;
		Node			program;

		System.out.println("basic parser");
		lex = new LexicalAnalyzerImpl("resource/input");
        env = new Environment(lex);
//		first = lex.get();

        System.out.println("+++ Stmt List +++");
        System.out.println(StmtList.firstSet);
        System.out.println(StmtList.children);
        System.out.println("+++ Expr +++");
        System.out.println(Expr.firstSet);
        System.out.println(Expr.children);

        System.out.println("- - - start parse - - -");

//        while(true) {
//            LexicalUnit lexicalUnit = lex.get();
//            if(lexicalUnit != null && lexicalUnit.getType() != LexicalType.END) {
//                System.out.println(lexicalUnit);
//            } else break;
//        }
        while(true) {
            program = NodeUtil.isMatch(Program.class, env, null);
            if(program == null || !program.parse()) {
                break;
            }
        }


//		if (program != null && program.parse()) {
//			System.out.println(program);
//			System.out.println("value = " + program.getValue());
//		}
//		else System.out.println("syntax error");
	}

}

package newlangdef;


import newlang4.*;

import java.lang.reflect.Field;

/**
 * Created by ctare on 2017/12/05.
 */
@NodeUtil.Define(type = NodeType.PROGRAM, firstSet = "f")
public class Program extends Node {
    public final static NodeUtil.FirstSet f = new NodeUtil.FirstSet() {
        @Override
        public boolean contains(LexicalUnit lexicalUnit) {
            return true;
        }
    };

    @Override
    public boolean parse() throws Exception {
        Environment env = getEnv();
        LexicalUnit lexicalUnit = env.getInput().get();
        env.getInput().unget(lexicalUnit);

        Node stmtList = NodeUtil.isMatch(StmtList.class, env, lexicalUnit);
        boolean result = stmtList != null && stmtList.parse();
        if(stmtList instanceof NodeUtil.HasSyntaxTree) {
            NodeUtil.SyntaxTree tree = ((NodeUtil.HasSyntaxTree) stmtList).getSyntaxTree();
            System.out.println(tree);
            System.out.println(tree.getValue());
        }
        return result;
    }

    private Environment getEnv() throws NoSuchFieldException, IllegalAccessException {
        Field f = Node.class.getDeclaredField("env");
        f.setAccessible(true);
        return (Environment) f.get(this);
    }
}

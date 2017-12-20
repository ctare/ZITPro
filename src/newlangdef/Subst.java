package newlangdef;

import newlang4.*;

/**
 * Created by ctare on 2017/12/19.
 */
@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.STMT_LIST)
public class Subst extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(LexicalType.NAME);
    public final static NodeUtil.Children children = new NodeUtil.Children<Subst>()
            .or(LexicalType.NAME, LexicalType.EQ, Expr.class);
}

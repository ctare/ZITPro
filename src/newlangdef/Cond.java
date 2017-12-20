package newlangdef;

import newlang4.LexicalType;
import newlang4.Node;
import newlang4.NodeType;
import newlang4.NodeUtil;

/**
 * Created by ctare on 2017/12/20.
 */
@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.COND)
public class Cond extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(Expr.firstSet);
    public final static NodeUtil.Children children = new NodeUtil.Children<CallFunc>()
            .or(Expr.class, LexicalType.EQ, Expr.class)
            .or(Expr.class, LexicalType.GT, Expr.class)
            .or(Expr.class, LexicalType.LT, Expr.class)
            .or(Expr.class, LexicalType.GE, Expr.class)
            .or(Expr.class, LexicalType.LE, Expr.class)
            .or(Expr.class, LexicalType.NE, Expr.class);
}

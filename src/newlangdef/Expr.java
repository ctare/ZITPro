package newlangdef;

import newlang4.LexicalType;
import newlang4.Node;
import newlang4.NodeType;
import newlang4.NodeUtil;

/**
 * Created by ctare on 2017/12/19.
 */
@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.EXPR)
public class Expr extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(LexicalType.SUB, LexicalType.LP, LexicalType.NAME, LexicalType.INTVAL, LexicalType.DOUBLEVAL, LexicalType.LITERAL /* call func */);
    public final static NodeUtil.Children children = new NodeUtil.Children<Expr>()
            .or(Expr.class, LexicalType.ADD, Expr.class)
            .or(Expr.class, LexicalType.SUB, Expr.class)
            .or(Expr.class, LexicalType.MUL, Expr.class)
            .or(Expr.class, LexicalType.DIV, Expr.class)
            .or(LexicalType.NAME)
            .or(LexicalType.INTVAL)
            .or(LexicalType.DOUBLEVAL)
            .or(LexicalType.LITERAL)
            .or(LexicalType.SUB, Expr.class)
            .or(LexicalType.LP, Expr.class, LexicalType.RP);
}


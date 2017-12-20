package newlangdef;

import newlang4.LexicalType;
import newlang4.Node;
import newlang4.NodeType;
import newlang4.NodeUtil;

/**
 * Created by ctare on 2017/12/19.
 */
@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.EXPR_LIST)
public class ExprList extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(Expr.firstSet);
    public final static NodeUtil.Children children = new NodeUtil.Children<ExprList>()
            .or(Expr.class)
            .or(ExprList.class, LexicalType.COMMA, Expr.class);
}

package newlangdef;

import newlang4.LexicalType;
import newlang4.Node;
import newlang4.NodeType;
import newlang4.NodeUtil;

/**
 * Created by ctare on 2017/12/07.
 */

@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.STMT)
public class Stmt extends Node {
    public static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(LexicalType.END).merge(CallFunc.firstSet).merge(Subst.firstSet);

    public static NodeUtil.Children children = new NodeUtil.Children<Stmt>()
            .or(Subst.class)
            .or(LexicalType.END)
            .or(CallFunc.class);
}

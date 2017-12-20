package newlangdef;

/**
 * Created by ctare on 2017/12/20.
 */

import newlang4.LexicalType;
import newlang4.Node;
import newlang4.NodeType;
import newlang4.NodeUtil;

@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.STMT_LIST)
public class ValueStmtTop extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(LexicalType.NAME);
    public final static NodeUtil.Children children = new NodeUtil.Children<ValueStmtTop>()
            .or(LexicalType.NAME)
            .or(LexicalType.LP, ValueStmtTop.class, LexicalType.RP);
}

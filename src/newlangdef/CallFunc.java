package newlangdef;

import newlang4.*;

/**
 * Created by ctare on 2017/12/19.
 */
@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.FUNCTION_CALL)
public class CallFunc extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(LexicalType.NAME);
    public final static NodeUtil.Children children = new NodeUtil.Children<CallFunc>()
            .or(LexicalType.NAME, LexicalType.LP, ExprList.class, LexicalType.RP);
}

package newlangdef;

import newlang4.*;

/**
 * Created by ctare on 2017/12/15.
 */
@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.BLOCK)
public class Block extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(LexicalType.DO, LexicalType.WHILE);
    public final static NodeUtil.Children children = new NodeUtil.Children<StmtList>()
            .or(LexicalType.DO, LexicalType.WHILE, /* cond */ LexicalType.NL, StmtList.class, LexicalType.LOOP, LexicalType.NL);
}

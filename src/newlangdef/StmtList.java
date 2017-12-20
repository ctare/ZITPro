package newlangdef;

import newlang4.LexicalType;
import newlang4.Node;
import newlang4.NodeType;
import newlang4.NodeUtil;

/**
 * Created by ctare on 2017/12/05.
 */
@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.STMT_LIST)
public class StmtList extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(Stmt.firstSet).merge(Block.firstSet);
    public final static NodeUtil.Children children = new NodeUtil.Children<StmtList>()
            .or(Stmt.class)
            .or(StmtList.class, LexicalType.NL, Stmt.class)
            .or(StmtList.class, LexicalType.NL)
            .or(Block.class)
            .or(Block.class, LexicalType.NL);
}

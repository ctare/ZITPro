package newlangdef;

import newlang4.*;

/**
 * Created by ctare on 2017/12/19.
 */
@NodeUtil.SimpleParse
@NodeUtil.Define(type = NodeType.EXPR)
public class Expr extends Node {
    public final static NodeUtil.FirstSet firstSet = new NodeUtil.FirstSet(
            LexicalType.SUB,
            LexicalType.LP,
            LexicalType.NAME,
            LexicalType.INTVAL,
            LexicalType.DOUBLEVAL,
            LexicalType.LITERAL)
            .merge(CallFunc.firstSet);
    public final static NodeUtil.Children children = new NodeUtil.Children<Expr>()
            .or(Expr.class, LexicalType.ADD, Expr.class)
            .or(Expr.class, LexicalType.SUB, Expr.class).f(tree -> {
                // TODO: 2018/01/06 引き算 switchしたくない
                System.out.println("sub");
                System.out.println(tree.get(0).getClass());
                System.out.println(tree.get(0).get(0).getClass());
                System.out.println(tree.get(0).getValue().getType());
                System.out.println(tree.get(0).getValue().getIValue());
                System.out.println(tree.get(2).getValue().getType());
                // TODO: 2018/01/08 数値 - 数値で仮対応
                return new ValueImpl(tree.get(0).getValue().getIValue() - tree.get(2).getValue().getIValue(), ValueType.INTEGER);
            })
            .or(Expr.class, LexicalType.MUL, Expr.class)
            .or(Expr.class, LexicalType.DIV, Expr.class)
            .or(LexicalType.SUB, Expr.class)
            .or(LexicalType.LP, Expr.class, LexicalType.RP)
            .or(LexicalType.NAME).f(tree -> tree.get(0).getValue())
            .or(LexicalType.INTVAL).f(tree -> tree.get(0).getValue())
            .or(LexicalType.DOUBLEVAL).f(tree -> tree.get(0).getValue())
            .or(LexicalType.LITERAL).f(tree -> tree.get(0).getValue())
            .or(CallFunc.class);
}


package newlang4;

public class Variable extends Node {
	String var_name;
	Value v;

	/** Creates a new instance of variable */
	public Variable(String name) {
		var_name = name;
	}
	public Variable(LexicalUnit u) {
		var_name = u.getValue().getSValue();
	}

	public static Node isMatch(Environment my_env, LexicalUnit first) {
		if (first.getType() == LexicalType.NAME) {
			Variable v;
//			String s = first.getValue().getSValue();
//			v = my_env.getVariable(s);
//			return v;
			return new Variable(first.getValue().getSValue());
		}
		return null;
	}

	public void setValue(Value my_v) {
		v = my_v;
	}

	public Value getValue() {
		return v;
	}

}

package newlang4;

import java.util.Hashtable;

public class Environment {
	   LexicalAnalyzer input;
	   Hashtable var_table;
	    
	    public Environment(LexicalAnalyzer my_input) {
	        input = my_input;
	        var_table = new Hashtable();
	    }
	    
	    public LexicalAnalyzer getInput() {
	        return input;
	    }	    
}

package newlang4;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ctare on 2018/01/08.
 */
public class GlobalScope extends Scope{
    public static final String main = "__main__";
    public static final HashMap<String, GlobalScope> scopes = new HashMap<>();
    static {
        scopes.put(main, new GlobalScope());
    }
}

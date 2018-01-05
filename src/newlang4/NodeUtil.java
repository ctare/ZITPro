package newlang4;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ctare on 2017/12/05.
 */
public final class NodeUtil {
    public static Node isMatch(Class<? extends Node> cls, Environment my_env, LexicalUnit first){
        Define define = cls.getAnnotation(Define.class);
        if(define == null) {
            return null;
        }
        try {
            Field field = cls.getDeclaredField(define.firstSet());
            FirstSet firstSet = (FirstSet) field.get(null);

            NodeType nodeType = define.type();

            if(firstSet.contains(first)) {
                Node instance = cls.newInstance();
                instance.env = my_env;
                instance.type = nodeType;
                if(cls.getAnnotation(SimpleParse.class) != null) {
                    instance = new SimpleParseDecorator(instance);
                }
                return instance;
            }
            return null;
        } catch (NoSuchFieldException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class FirstSet {
        private Set<LexicalType> firstSet = new HashSet<>();
        public FirstSet(LexicalType... types) {
            add(types);
        }

        public FirstSet(FirstSet another) {
            merge(another);
        }

        public void add(LexicalType... types) {
            Collections.addAll(firstSet, types);
        }

        public boolean contains(LexicalUnit lexicalUnit) {
            return firstSet.contains(lexicalUnit.type);
        }

        public FirstSet merge(FirstSet another) {
            firstSet.addAll(another.firstSet);
            return this;
        }

        @Override
        public String toString() {
            return firstSet.toString();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Define {
        NodeType type();
        String firstSet() default "firstSet";
        String children() default "children";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SimpleParse {
    }

    public interface HasSyntaxTree {
        SyntaxTree getSyntaxTree();
    }

    private static class SimpleParseDecorator extends Node implements HasSyntaxTree{
        private Node node;
        private SyntaxTree tree;
        private SimpleParseDecorator(Node node) {
            this.node = node;
        }

        private static boolean parseSub(Children.Rule target, Node node, SyntaxTree tree) throws Exception {
            boolean result = true;
            for (Child child : target) {
                LexicalUnit lexicalUnit = node.env.getInput().get();

                result = false;
                if(child.cls != null) {
                    Node body = NodeUtil.isMatch(child.cls, node.env, lexicalUnit);
                    if (body != null) {
                        node.env.getInput().unget(lexicalUnit);
                        result = body.parse();
                        if(body instanceof HasSyntaxTree) {
                            tree.add(((HasSyntaxTree) body).getSyntaxTree());
                        }

                        if(!result) {
                            break;
                        }
                    } else {
                        tree.add(new TerminalSymbol(target, lexicalUnit));
                    }
                } else {
                    result = child.lexicalType.equals(lexicalUnit.type);
                    tree.add(new TerminalSymbol(target, lexicalUnit));
                }

                if(!result) {
                    break;
                }
            }
            return result;
        }

        private boolean recParse(SyntaxTree mainTree, Set<Children.Rule> rules) throws Exception {
            for (Children.Rule rule : rules) {
                SyntaxTree tmp_tree2 = new SyntaxTree(mainTree.rule);
                boolean result_rec = parseSub(rule, node, tmp_tree2);

                if(result_rec) {
                    SyntaxTree tmp = new SyntaxTree(rule);
                    tmp.add(mainTree);
                    tmp.addAll(tmp_tree2);

                    recParse(tmp, rules);

                    this.tree = tmp;
                    return true;
                } else {
                    tmp_tree2.unget(node.env);
                }
            }
            return false;
        }

        @Override
        public boolean parse() throws Exception{
            Define define = node.getClass().getAnnotation(Define.class);
            Field field = node.getClass().getField(define.children());
            Children<?> children = (Children<?>)field.get(null);

            // not recursions parse
            for (Children.NotRecursiveRule notRecursiveRule : children.not_recursions) {
                SyntaxTree tmp_tree1 = new SyntaxTree(notRecursiveRule);
                boolean result = parseSub(notRecursiveRule, node, tmp_tree1);

                if(result) {
                    // recursions parse
                    if(recParse(tmp_tree1, children.recursions)) {
                        return true;
                    }

                    this.tree = tmp_tree1;
                    return true;
                } else {
                    tmp_tree1.unget(node.env);
                }
            }

//            if (lexicalUnit.getType() == LexicalType.END) {
//                super.type = NodeType.END;
//                return true;
//            }

            return false;
        }

        @Override
        public SyntaxTree getSyntaxTree() {
            return this.tree;
        }
    }

    public static class SyntaxTree extends ArrayList<SyntaxTree> {
        private Children.Rule rule;
        SyntaxTree(Children.Rule rule) {
            this.rule = rule;
        }

        void unget(Environment env) throws Exception {
            Collections.reverse(this);
            for (SyntaxTree syntaxTree : this) {
                if(syntaxTree != null) {
                    syntaxTree.unget(env);
                }
            }
        }

        public Value getValue() {
            return rule.getValue(this);
        }
    }

    public static class TerminalSymbol extends SyntaxTree {
        private LexicalUnit terminal;
        public TerminalSymbol(Children.Rule rule, LexicalUnit terminal) {
            super(rule);
            this.terminal = terminal;
        }

        public LexicalUnit getTerminal() {
            return terminal;
        }

        @Override
        void unget(Environment env) throws Exception {
            env.getInput().unget(terminal);
        }

        @Override
        public boolean add(SyntaxTree tree) {
            throw new RuntimeException("can't add tree to terminal");
        }

        @Override
        public String toString() {
            return terminal.toString();
        }

        public Value getValue() {
            return terminal.getValue();
        }
    }

    public static class Child {
        private Class<? extends Node> cls;
        private LexicalType lexicalType;

        public Child(Class<? extends Node> cls) {
            this.cls = cls;
        }

        public Child(LexicalType lexicalType) {
            this.lexicalType = lexicalType;
        }

        public boolean match(Class<? extends Node> cls) {
            return this.cls != null && this.cls.equals(cls);
        }

        public boolean match(LexicalType lexicalType) {
            return this.lexicalType != null && this.lexicalType.equals(lexicalType);
        }

        @Override
        public String toString() {
            return lexicalType == null ? cls.toString() : lexicalType.toString();
        }
    }

    public static class Children<T extends Node> {
        private final Set<NotRecursiveRule> not_recursions = new TreeSet<>((o1, o2) -> o1.length() < o2.length() ? 1 : -1);
        private final Set<Rule> recursions = new TreeSet<>((o1, o2) -> o1.length() < o2.length() ? 1: -1);
        private final Class<? extends Node> parent;
        private Rule now;

        @SuppressWarnings("unchecked")
        public Children(T... dummy) {
             parent = (Class<? extends Node>) dummy.getClass().getComponentType();
        }

        public final Children<T> or(Child... children) {
            if(children[0].match(parent)) {
                Rule rule = new Rule(children);
                recursions.add(rule);
                now = rule;
            } else {
                NotRecursiveRule rule = new NotRecursiveRule(children);
                not_recursions.add(rule);
                now = rule;
            }
            return this;
        }

        public final Children<T> or(Object... childrenWithConvert) {
            return or(convert(childrenWithConvert));
        }

        public final Children<T> f(GetValueFunction getValueFunction) {
            now.getValueFunction = getValueFunction;
            return this;
        }

        @SuppressWarnings("unchecked")
        public static Child[] convert(Object... children){
            List<Child> c = Stream.of(children).map(e -> {
                if(e instanceof LexicalType) {
                    return new Child((LexicalType) e);
                }
                Class cls = (Class) e;
                if(cls.getGenericSuperclass() == Node.class) {
                    return new Child((Class<? extends Node>) e);
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            Child[] result = new Child[c.size()];
            return c.toArray(result);
        }

        public interface GetValueFunction {
            Value getValue(SyntaxTree tree);
        }

        private static class Rule implements Iterator<Child>, Iterable<Child>{
            List<Child> children = new ArrayList<>();
            Iterator<Child> iterator;
            GetValueFunction getValueFunction = e -> null;

            private Rule(Child... children) {
                Collections.addAll(this.children, children);
            }

            @Override
            public Iterator<Child> iterator() {
                this.iterator = children.iterator();
                if(this.iterator.hasNext()) {
                    this.iterator.next();
                }
                return this.iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Child next() {
                return iterator.next();
            }

            public int length(){
                return children.size();
            }

            @Override
            public String toString() {
                return children.toString();
            }

            public Value getValue(SyntaxTree tree) {
                return getValueFunction.getValue(tree);
            }
        }

        private static class NotRecursiveRule extends Rule {
            private NotRecursiveRule(Child... children) {
                super(children);
            }

            @Override
            public Iterator<Child> iterator() {
                this.iterator = children.iterator();
                return this.iterator;
            }
        }

        @Override
        public String toString() {

            return "=== recursions ===\n" +
                    recursions.stream().map(Object::toString).collect(Collectors.joining("\n")) +
                    "\n" +
                    "=== not recursions ===\n" +
                    not_recursions.stream().map(Object::toString).collect(Collectors.joining("\n")) +
                    "\n";
        }
    }
}

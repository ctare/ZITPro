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
        boolean value() default true;
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
//                System.out.println("===vvv");
//                System.out.print(lexicalUnit);
//                System.out.print("     ->    ");
//                System.out.println(node.getClass());
//                System.out.println("===^^^");

                result = false;
                Node body;
//                System.out.println("" + child.lexicalType + " ;; " + child.cls);
                if(child.cls != null) {
                    body = NodeUtil.isMatch(child.cls, node.env, lexicalUnit);
                    if(body == null) {
                        tree.add(new TerminalSymbol(target, lexicalUnit));
                    }
                } else {
                    body = null;
                    result = child.lexicalType.equals(lexicalUnit.type);
                    tree.add(new TerminalSymbol(target, lexicalUnit));
                }

                if (body != null) {
                    node.env.getInput().unget(lexicalUnit);
                    result = body.parse();
                    if(body instanceof HasSyntaxTree) {
                        tree.add(((HasSyntaxTree) body).getSyntaxTree());
                    }
                    if(!result) {
                        break;
                    }
                }
                // TODO: 2017/12/15 delete
                if(result) {
//                    if(child.lexicalType != null) System.out.println("" + child.lexicalType + ", " + lexicalUnit.type + " => " + result);
//                    else System.out.println("" + child.cls + ", " + lexicalUnit.type + " => " + result);
                } else {
                    break;
                }
            }
//            System.out.println("DEQUE!! " + deque + " => " + result);
            return result;
        }

        private boolean recParse(SyntaxTree mainTree, Set<Children.Rule> rules) throws Exception {
            for (Children.Rule rule : rules) {
                SyntaxTree tmp_tree2 = new SyntaxTree(rule);
//                        System.out.println("----rec>>>>  " + recursion);
                boolean result_rec = parseSub(rule, node, tmp_tree2);

                if(result_rec) {

                    mainTree.add(tmp_tree2);
                    recParse(mainTree, rules);

                    this.tree = mainTree;
                    // TODO: 2017/12/15 delete
//                            System.out.print("rec!! node parse: " + this.node.getClass() + " @@@ ");
//                            System.out.println(deque_rec);
                    return true;
                } else {
//                            System.out.println("false... rec " + tmp_tree2);
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
//                System.out.println("====non rec>>>>  " + notRecursiveRule);
                boolean result = parseSub(notRecursiveRule, node, tmp_tree1);

                if(result) {
                    // recursions parse
                    if(recParse(tmp_tree1, children.recursions)) {
                        return true;
                    }

                    // TODO: 2017/12/15 delete
//                    System.out.print("not rec!! node parse: " + this.node.getClass() + " @@@ ");
//                    System.out.println(deque);
                    this.tree = tmp_tree1;
                    return true;
                } else {
//                    System.out.println("false... non rec " + tmp_tree1);
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

        @SuppressWarnings("unchecked")
        public Children(T... dummy) {
             parent = (Class<? extends Node>) dummy.getClass().getComponentType();
        }

        public final Children<T> or(Child... children) {
            if(children[0].match(parent)) {
                recursions.add(new Rule(children));
            } else {
                not_recursions.add(new NotRecursiveRule(children));
            }
            return this;
        }

        public final Children<T> or(Object... childrenWithConvert) {
            return or(convert(childrenWithConvert));
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

        private static class Rule implements Iterator<Child>, Iterable<Child>{
            List<Child> children = new ArrayList<>();
            Iterator<Child> iterator;

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

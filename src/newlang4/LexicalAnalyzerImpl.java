package newlang4;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ctare on 2017/09/26.
 */
public class LexicalAnalyzerImpl implements LexicalAnalyzer {
    private Deque<LexicalUnit> back = new ArrayDeque<>();
    private PushbackReader in;
    private String all;
    private LinkedHashMap<LexicalType, Def> patternMap = new LinkedHashMap<LexicalType, Def>(){{
        put(LexicalType.INTVAL, new PatternDefWithValueType(Pattern.compile("^([0-9]+)"), ValueType.INTEGER, Integer::parseInt));
        put(LexicalType.DOUBLEVAL, new PatternDefWithValueType(Pattern.compile("^([0-9]+(\\.[0-9]+)?)"), ValueType.DOUBLE, Double::parseDouble));
        put(LexicalType.NAME, new PatternNameDef(Pattern.compile("^([a-zA-Z_]+[0-9_]*)")));
        put(LexicalType.ADD, new PatternDef(Pattern.compile("^(\\+)")));
        put(LexicalType.SUB, new PatternDef(Pattern.compile("^(-)")));
        put(LexicalType.MUL, new PatternDef(Pattern.compile("^(\\*)")));
        put(LexicalType.DIV, new PatternDef(Pattern.compile("^(/)")));
        put(LexicalType.COMMA, new PatternDef(Pattern.compile("^(,)")));
        put(LexicalType.DOT, new PatternDef(Pattern.compile("^(\\.)")));
        put(LexicalType.LE, new PatternDef(Pattern.compile("^(<=|=<)")));
        put(LexicalType.GE, new PatternDef(Pattern.compile("^(>=|=>)")));
        put(LexicalType.GE, new PatternDef(Pattern.compile("^(<>)")));
        put(LexicalType.EQ, new PatternDef(Pattern.compile("^(=)")));
        put(LexicalType.LT, new PatternDef(Pattern.compile("^(<)")));
        put(LexicalType.GT, new PatternDef(Pattern.compile("^(>)")));
        put(LexicalType.LP, new PatternDef(Pattern.compile("^(\\()")));
        put(LexicalType.RP, new PatternDef(Pattern.compile("^(\\))")));
        put(LexicalType.LITERAL, new PatternDefWithValueType(Pattern.compile("^\"(.*)\""), ValueType.STRING, value -> value));
        put(LexicalType.NL, new PatternDef(Pattern.compile("^(\n)")));
    }};

    private Pattern[] ignore = {
            Pattern.compile("^([ \t])"),
    };

    public LexicalAnalyzerImpl(String fileName) throws FileNotFoundException {
        this.in = new PushbackReader(new FileReader(fileName));
        StringBuilder all_s = new StringBuilder();
        while(true) {
            int c = -1;
            try {
                c = this.in.read();
            } catch (IOException e) {
                // ignored
            }
            if(c == -1) break;
            all_s.append((char) c);
        }

        all = all_s.toString();
    }

    @Override
    public LexicalUnit get() throws Exception{
        if(!back.isEmpty()) {
//            System.out.println("get:: " + back);
            return back.pop();
        }
        while(true) {
            boolean break_flg = true;
            for (Map.Entry<LexicalType, Def> lexicalTypePatternEntry : patternMap.entrySet()) {
                Return ret = lexicalTypePatternEntry.getValue().match(lexicalTypePatternEntry.getKey(), all);
                if(ret != null) {
                    all = ret.result;
                    return ret.lexicalUnit;
                }
            }

            for (Pattern pattern : ignore) {
                Matcher matcher = pattern.matcher(all);
                if(matcher.find()){
                    all = all.substring(matcher.end());
                    break_flg = false;
                }
            }
            if(break_flg) break;
        }
        return new LexicalUnit(LexicalType.END);
    }

    @Override
    public boolean expect(LexicalType type) throws Exception {
        return false;
    }

    @Override
    public void unget(LexicalUnit token) throws Exception {
        back.push(token);
    }

    private interface Def {
        Return match(LexicalType type, String target);
    }

    private static class Return {
        private String result;
        private String value;
        private LexicalUnit lexicalUnit;

        Return(String result, String value, LexicalUnit lexicalUnit) {
            this.result = result;
            this.value = value;
            this.lexicalUnit = lexicalUnit;
        }
    }

    private static class PatternDef implements Def {
        private Pattern pattern;

        PatternDef(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public Return match(LexicalType type, String target) {
            Matcher matcher = pattern.matcher(target);
            if(matcher.find()) {
                target = target.substring(matcher.end());
                String value = matcher.group(1);
                return new Return(target, value, new LexicalUnit(type, createValue(value)));
            }
            return null;
        }

        Value createValue(String value) {
            return null;
        }
    }

    private static class PatternNameDef extends PatternDef {
        private LexicalType[] types = new LexicalType[]{
                LexicalType.IF,
                LexicalType.THEN,
                LexicalType.ELSE,
                LexicalType.ELSEIF,
                LexicalType.ENDIF,
                LexicalType.FOR,
                LexicalType.FORALL,
                LexicalType.NEXT,
                LexicalType.FUNC,
                LexicalType.DIM,
                LexicalType.AS,
                LexicalType.END,
                LexicalType.WHILE,
                LexicalType.DO,
                LexicalType.UNTIL,
                LexicalType.LOOP,
                LexicalType.TO,
                LexicalType.WEND,
                LexicalType.EOF,
        };

        PatternNameDef(Pattern pattern) {
            super(pattern);
        }

        @Override
        public Return match(LexicalType type, String target) {
            Return ret = super.match(type, target);
            if(ret == null) return null;
            for (LexicalType lexicalType : types) {
                if(lexicalType.name().equals(ret.value)) {
                    ret.lexicalUnit.type = lexicalType;
                    break;
                }
            }
            return ret;
        }

        @Override
        Value createValue(String value) {
            return new ValueImpl(value, ValueType.STRING);
        }
    }

    private static class PatternDefWithValueType extends PatternDef {
        private ValueType type;
        private Cast cast;

        PatternDefWithValueType(Pattern pattern, ValueType type, Cast cast) {
            super(pattern);
            this.type = type;
            this.cast = cast;
        }

        @Override
        Value createValue(String value) {
            return new ValueImpl(cast.exec(value), type);
        }

        @FunctionalInterface
        interface Cast{
            Object exec(String value);
        }
    }
}

package newlang3;

public interface LexicalAnalyzer {
    public LexicalUnit get() throws Exception;
    public boolean expect(LexicalType type) throws Exception;
    public void unget(LexicalUnit token) throws Exception;    
}

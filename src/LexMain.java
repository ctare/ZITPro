import newlang4.*;

import java.io.FileNotFoundException;

/**
 * Created by ctare on 2017/09/26.
 */
public class LexMain {
    // 字句解析確認用
    public static void main(String[] args) throws FileNotFoundException {
        String fileName = "resource/input";
        LexicalAnalyzerImpl lexicalAnalyzer = new LexicalAnalyzerImpl(fileName);
        while(true) {
            try {
                LexicalUnit unit = lexicalAnalyzer.get();
                System.out.println(unit);
                if (unit.getType() == LexicalType.END) break;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}

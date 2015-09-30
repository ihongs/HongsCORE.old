package app.hongs.dl.lucene.tokenizer;

import java.util.Map;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/**
 * 分词器工厂
 * @author Hongs
 */
public class HongsTokenizerFactory extends TokenizerFactory {

    public HongsTokenizerFactory(Map<String, String> args) {
        super(args);
    }
    
    @Override
    public Tokenizer create(AttributeFactory af) {
        return new HongsTokenizer();
    }
    
}

package net.hongs.search;

import app.hongs.HongsException;
import app.hongs.annotaion.Cmdlet;
import java.io.IOException;
import java.io.StringReader;
import net.paoding.analysis.analyzer.PaodingAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * 搜索模型
 * @author Hongs
 */
@Cmdlet("search")
public class SearchCmdlet {

    @Cmdlet("test")
    public static void test(String[] args) throws IOException, HongsException {
        StringReader      sr = new StringReader(args[0]);
        Analyzer          ar;
        TokenStream       ts;
        CharTermAttribute ta;
        long t;
        
        System.out.println("Standard Analyer:");
        ar = new StandardAnalyzer(Version.LUCENE_46);
        t  = System.currentTimeMillis();
        ts = ar.tokenStream("", sr);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            System.out.print(ta.toString()+"\t");
        }
        ts.end ( );
        t = System.currentTimeMillis() - t;
        System.out.println(" : "+t);
        
        sr.reset();
        
        System.out.println("Chinese Analyzer:");
        ar = new ChineseAnalyzer( );
        t  = System.currentTimeMillis();
        ts = ar.tokenStream("", sr);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            System.out.print(ta.toString()+"\t");
        }
        ts.end ( );
        t = System.currentTimeMillis() - t;
        System.out.println(" : "+t);
        
        sr.reset();
        
        System.out.println("CJK Analyzer:");
        ar = new CJKAnalyzer(Version.LUCENE_46);
        t  = System.currentTimeMillis();
        ts = ar.tokenStream("", sr);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            int c = ta.toString().hashCode();
            System.out.print(ta.toString()+"\t");
        }
        ts.end ( );
        t = System.currentTimeMillis() - t;
        System.out.println(" : "+t);
        
        sr.reset();
        
        System.out.println("Smartcn Analyzer:");
        ar = new SmartChineseAnalyzer(Version.LUCENE_46, true);
        t  = System.currentTimeMillis();
        ts = ar.tokenStream("", sr);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            System.out.print(ta.toString()+"\t");
        }
        ts.end ( );
        t = System.currentTimeMillis() - t;
        System.out.println(" : "+t);
        
        sr.reset();
        
        System.out.println("Paoding Analyzer:");
        ar = new PaodingAnalyzer( );
        t  = System.currentTimeMillis();
        ts = ar.tokenStream("", sr);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            System.out.print(ta.toString()+"\t");
        }
        ts.end ( );
        t = System.currentTimeMillis() - t;
        System.out.println(" : "+t);
    }
    
}

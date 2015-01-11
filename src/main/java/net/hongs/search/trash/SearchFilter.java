package net.hongs.search.trash;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import app.hongs.util.Text;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import net.paoding.analysis.analyzer.PaodingAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * 搜索过滤器
 * @author Hongs
 */
public class SearchFilter implements Filter {

    Analyzer paodingAnalyzer;
    Analyzer chineseAnalyzer;

    public void init(FilterConfig fc) throws ServletException {
        paodingAnalyzer = new PaodingAnalyzer();
        chineseAnalyzer = new ChineseAnalyzer();
    }

    public void destroy() {
        paodingAnalyzer.close();
        chineseAnalyzer.close();
    }

    public void doFilter(ServletRequest sr, ServletResponse sp, FilterChain fc) throws IOException, ServletException {
        Core core = Core.getInstance();
        ActionHelper helper = core.get(ActionHelper.class);
        try {
            Map rd = helper.getRequestData();
            doFilter(rd);
        } catch (HongsException ex) {
            throw new ServletException(ex);
        }

        fc.doFilter(sr, sp);
    }

    public void doFilter(Map rd) throws HongsException, IOException {
        checkWdParam(rd);
        checkArticle(rd);
        checkKeyword(rd);
        checkSubject(rd);
        checkRelated(rd);
    }

    private void checkWdParam(Map rd) throws HongsException, IOException {
        String wd = (String) rd.get("wd");
        if (null == wd) return;

        Set<String>       ss = new LinkedHashSet();
        TokenStream       ts;
        CharTermAttribute ta;

        long t = System.currentTimeMillis();
        
        ts = paodingAnalyzer.tokenStream("", new StringReader(wd));
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            ss.add(getHid(ta.toString()));
        }
        ts.end ( );
        ts.close();

        ts = chineseAnalyzer.tokenStream("", new StringReader(wd));
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            ss.add(getHid(ta.toString()));
        }
        ts.end ( );
        ts.close();

        t = System.currentTimeMillis() - t;
        rd.put("kt", t );

        rd.put("kd", ss);
    }

    private void checkArticle(Map rd) throws HongsException, IOException {
        String name = (String) rd.get("name");
        String note = (String) rd.get("note");
        if (name == null || note == null) return ;

        Map<String, Integer> ss = new HashMap();
        TokenStream          ts;
        CharTermAttribute    ta;
        String               wd = name + " " + note;
        int                  wt = 0;

        ts = paodingAnalyzer.tokenStream("", new StringReader(wd));
        //oa = ts.addAttribute(OffsetAttribute.class);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            String s = ta.toString();
            if (!ss.containsKey(s)) {
                ss.put(s, wt ++ );
            }
        }
        ts.end ( );
        ts.close();

        ts = chineseAnalyzer.tokenStream("", new StringReader(wd));
        //oa = ts.addAttribute(OffsetAttribute.class);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            String s = ta.toString();
            if (!ss.containsKey(s)) {
                ss.put(s, wt ++);
            }
        }
        ts.end ( );
        ts.close();

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> et : ss.entrySet()) {
            String s = et.getKey();
            int  w = et.getValue();
            w = (int) Math.floor((double) (wt - w - 1) / wt * 100);
            sb.append(s+"^"+w+"\r\n");
        }

        rd.put("keyword_idx", sb.toString());
    }

    private void checkKeyword(Map rd) throws HongsException {
        String ki = (String) rd.get("keyword_idx");
        if (ki == null) return;

        Pattern pe = Pattern.compile("\\^(\\d+)$");

        Map<String, Integer> kw = new HashMap();
        String[] ks = ki.split("(\\r|\\n)");
        for (String k : ks) {
            k = k.trim  ( );
            if (k.length( ) == 0) {
                continue;
            }
            Matcher m = pe.matcher(k);
            int     w = 0;
            if (m.find()) {
                w = Integer.parseInt(m.group(1));
                k = k.substring ( 0, m.start( ));
            }
            kw.put(k, w);
        }

        Table ktab = DB.getInstance("search").getTable("keyword");
        List<Map> data = new ArrayList();

        FetchCase caze = new FetchCase()
            .where (".name IN (?)", kw.keySet())
            .select(".id, .name");
        List<Map> list = ktab.fetchMore( caze );
        for (Map  info : list) {
             Object nm = info.get("name");
             Object id = info.get("id");
             Object wt = kw.remove(nm);

            info.put("keyword_id", id);
            info.put("weight", wt);
            info.remove("name");
            info.remove("id");
            data.add(info);
        }

        for (String nm : kw.keySet( )) {
             String id = getHid ( nm );
             int    wt = kw.get ( nm );

            Map info = new HashMap(  );
            info.put("name", nm);
            info.put("id", id);
            ktab.insert(info);

            info.put("keyword_id", id);
            info.put("weight", wt);
            info.remove("name");
            info.remove("id");
            data.add(info);
        }

        rd.put("article_keyword", data);
    }

    private void checkSubject(Map rd) throws HongsException {
        String ki = (String) rd.get("subject_idx");
        if (ki == null) return;

        Pattern pe = Pattern.compile("\\^(\\d+)$");

        Map<String, Integer> kw = new HashMap();
        String[] ks = ki.split("(\\r|\\n)");
        for (String k : ks) {
            k = k.trim  ( );
            if (k.length( ) == 0) {
                continue;
            }
            Matcher m = pe.matcher(k);
            int     w = 0;
            if (m.find()) {
                w = Integer.parseInt(m.group(1));
                k = k.substring ( 0, m.start( ));
            }
            kw.put(k, w);
        }

        Table ktab = DB.getInstance("search").getTable("subject");
        List<Map> data = new ArrayList();

        FetchCase caze = new FetchCase()
            .where (".name IN (?)", kw.keySet())
            .select(".id, .name");
        List<Map> list = ktab.fetchMore( caze );
        for (Map  info : list) {
             Object nm = info.get("name");
             Object id = info.get("id");
             Object wt = kw.remove(nm);

            info.put("keyword_id", id);
            info.put("weight", wt);
            info.remove("name");
            info.remove("id");
            data.add(info);
        }

        rd.put("article_subject", data);
    }

    private void checkRelated(Map rd) throws HongsException {
        String ki = (String) rd.get("related_idx");
        if (ki == null) return;

        Pattern pe = Pattern.compile("\\^(\\d+)$");

        Map<String, Integer> kw = new HashMap();
        String[] ks = ki.split("(\\r|\\n)");
        for (String k : ks) {
            k = k.trim  ( );
            if (k.length( ) == 0) {
                continue;
            }
            Matcher m = pe.matcher(k);
            int     w = 0;
            if (m.find()) {
                w = Integer.parseInt(m.group(1));
                k = k.substring ( 0, m.start( ));
            }
            kw.put(k, w);
        }

        List<Map> data = new  ArrayList();

        for (String id : kw.keySet( )) {
             int    wt = kw.get ( id );

            Map info = new HashMap(  );
            info.put("related_id", id);
            info.put("weight", wt);
            data.add(info);
        }

        rd.put("article_related", data);
    }

    public static String getHid(String wd) {
        int     i = wd.hashCode( );
        if (i < 0) i = Math.abs(i);
        byte[]  x = wd.getBytes( );
        byte    b = x[(x.length-1)%2];
        return String.format("%x%02x", i, b);
    }

}

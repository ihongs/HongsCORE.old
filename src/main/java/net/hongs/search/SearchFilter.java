package net.hongs.search;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import static app.hongs.action.ActionDriver.REQCORE;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import app.hongs.util.Util;
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

    public void doFilter(ServletRequest sr, ServletResponse sp, FilterChain fc) throws IOException, ServletException {
        Core core = (Core) sr.getAttribute(REQCORE);
        ActionHelper helper = core.get(ActionHelper.class);
        try {
            checkWdParam(helper);
            checkArticle(helper);
            checkKeyword(helper);
            checkSubject(helper);
            checkRelated(helper);
        } catch (HongsException ex) {
            throw new ServletException(ex);
        }

        fc.doFilter(sr, sp);
    }

    public void destroy() {
    }

    private void checkWdParam(ActionHelper helper) throws IOException, HongsException {
        String wd = helper.getParameter("wd");
        if (null == wd) return;

        StringReader      sr = new StringReader (wd);
        Set<String>       ss = new LinkedHashSet(  );
        TokenStream       ts;
        CharTermAttribute ta;

        ts = paodingAnalyzer.tokenStream("", sr);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            int c = ta.toString( ).hashCode();
            if (c < 0) {
                c = Math.abs(c);
                ss.add("0" + Util.to36Hex(c));
            } else {
                ss.add(/****/Util.to36Hex(c));
            }
        }
        ts.end ( );

        sr.reset();

        ts = chineseAnalyzer.tokenStream("", sr);
        ta = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            int c = ta.toString( ).hashCode();
            if (c < 0) {
                c = Math.abs(c);
                ss.add("0" + Util.to36Hex(c));
            } else {
                ss.add(/****/Util.to36Hex(c));
            }
        }
        ts.end ( );

        helper.getRequestData().put("kd", ss);
    }

    private void checkArticle(ActionHelper helper) throws HongsException {
        String caption = helper.getParameter("caption");
        String article = helper.getParameter("article");
        if (caption == null || article == null) return ;


    }

    private void checkKeyword(ActionHelper helper) throws HongsException {
        String ki = helper.getParameter("keyword_idx");
        if (ki == null) return;

        Pattern pe = Pattern.compile("\\^\\d+$");

        Map<String, Integer> kw = new HashMap();
        String[] ks = ki.split("(\\r|\\n)");
        for (String k : ks) {
            k = k.trim  ( );
            if (k.length( ) == 0) {
                continue;
            }
            Matcher m = pe.matcher(k);
            int w = m.find() ? Integer.parseInt(m.group(0)) : 0;
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

        helper.getRequestData().put("article_keyword", data);
        
        Map x = helper.getRequestData();
        
        int i = 0;
    }

    private void checkSubject(ActionHelper helper) throws HongsException {
        String ki = helper.getParameter("subject_idx");
        if (ki == null) return;

        Pattern pe = Pattern.compile("\\^\\d+$");

        Map<String, Integer> kw = new HashMap();
        String[] ks = ki.split("(\\r|\\n)");
        for (String k : ks) {
            k = k.trim  ( );
            if (k.length( ) == 0) {
                continue;
            }
            Matcher m = pe.matcher(k);
            int w = m.find() ? Integer.parseInt(m.group(0)) : 0;
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

        helper.getRequestData().put("article_subject", data);
    }

    private void checkRelated(ActionHelper helper) throws HongsException {
        String ki = helper.getParameter("related_idx");
        if (ki == null) return;

        Pattern pe = Pattern.compile("\\^\\d+$");

        Map<String, Integer> kw = new HashMap();
        String[] ks = ki.split("(\\r|\\n)");
        for (String k : ks) {
            k = k.trim  ( );
            if (k.length( ) == 0) {
                continue;
            }
            Matcher m = pe.matcher(k);
            int w = m.find() ? Integer.parseInt(m.group(0)) : 0;
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

        helper.getRequestData().put("article_related", data);
    }

    private String getHid(String wd) {
        int c = wd.hashCode();
        if (c < 0) {
            c = Math.abs( c );
            return "0" + Util.to36Hex(c);
        } else {
            return /****/Util.to36Hex(c);
        }
    }

}

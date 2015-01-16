package net.hongs.search;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.Formset;
import app.hongs.util.Data;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import app.hongs.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 搜索器
 * @author Hongs
 */
public class Reader {

    private IndexReader   reader;
    private IndexSearcher finder;
    private Map           ssform;

    private int rowsPerPage;
    private int lnksPerPage;

    public Reader() throws HongsException {
        try {
            Map map = new HashMap();
            map.put("BASE_PATH", Core.VARS_PATH);
            map.put("VARS_PATH", Core.VARS_PATH);

            String    din = CoreConfig.getInstance( ).getProperty("core.search.location", "${VARS_PATH}/search");
            Directory dir = FSDirectory.open(new File(Text.inject(din, map)));

            reader = DirectoryReader.open(dir);
            finder = new IndexSearcher(reader);
            ssform = Formset.getInstance("search").getForm("_search");
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }

        CoreConfig conf = CoreConfig.getInstance();
        rowsPerPage = conf.getProperty("fore.rows.per.page", 20);
        lnksPerPage = conf.getProperty("fore.lnks.per.page", 10);
    }

    public Map getInfo(String id) throws HongsException {
        Map resp = new HashMap();
        Map info;
        try {
                Query  q    = new TermQuery(new Term("id", id));
              TopDocs  docs = finder.search(q, 10);
            ScoreDoc[] hits = docs.scoreDocs;
            if  ( 0 == hits.length ) {
                info = new HashMap();
            } else {
                Document doc = finder.doc(hits[0].doc);
                info = doc2Map(doc );
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
        resp.put("info", info);
        return resp;
    }

    public Map getList(Map rd) throws HongsException {
        Map  resp = new HashMap();
        Map  page = new HashMap();
        List list = new ArrayList();
        resp.put("page", page);
        resp.put("list", list);

        //** 计算分页 **/

        int rn = Synt.declare(rd.get("rn"), 0);
        if (rn == 0) rn = rowsPerPage;
        int ln = Synt.declare(rd.get("ln"), 0);
        if (ln == 0) ln = lnksPerPage;
        int pn = Synt.declare(rd.get("pn"), 0);
        if (pn == 0) pn = 1 ;
        int xn = rn*(pn - 1);

        int minPn = ln - (pn % ln);
        int maxPn = ln + minPn;
        int minRn = rn * (pn - 1 );
        int maxRn = rn + minRn;

        try {
            Query q = getFind(rd);
            Sort  s = getSort(rd);

            TopDocs docz;
            if (s != null) {
                docz = finder.search(q, rn * maxPn, s);
            } else {
                docz = finder.search(q, rn * maxPn);
            }

            if (maxRn > docz.totalHits) {
                maxRn = docz.totalHits;
            }

            ScoreDoc[] docs = docz.scoreDocs;
            for (int i = minRn; i < maxRn; i ++) {
                Document doc = reader.document(docs[i].doc);
                list.add(doc2Map(doc));
            }

            //** 核实分页 **/

            page.put("rowscount", docs.length);
            page.put("pagecount", (int) Math.ceil((double) docs.length / rn));
            if (docs.length ==  0) {
                page.put("err", 1);
                page.put("next", false);
            } else
            if (list.size() ==  0) {
                page.put("err", 2);
                page.put("next", false);
            } else
            {
                // 是否有下一组分页
                docz = finder.searchAfter(docs[maxRn - 1], q, 1);
                page.put("next", docz.totalHits > 0);
                page.put("err", 0);
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }

        return resp;
    }

    public Map getCnts(Map rd) throws HongsException {
        Map  resp = new HashMap();
        Map  cnts = new HashMap();
        resp.put("cnts", cnts);

        Set<String> cnt2 = Synt.declare(rd.get("cnts"), new HashSet());
        Map<String, Map<String, Integer>> cntz = new HashMap();
        Map<String, Map<String, Integer>> cntx = new HashMap();
        for(String   x : cnt2) {
            String[] a = x.split( ":", 2 );
            if (!ssform.containsKey(a[0])) {
                throw new HongsException(HongsException.COMMON, "Field "+a[0]+" not exists");
            }
            if (a.length > 1) {
                cntz.get(a[0]).put(a[1], 0);
            } else
            if (! cntx.containsKey(a[0] ) ) {
                cntx.put(a[0], new HashMap());
            }
        }

        try {
            Query q = getFind(rd);

            TopDocs docz = finder.search(q, 500);
            while ( docz.totalHits > 0) {
                ScoreDoc[] docs = docz.scoreDocs;
                for(ScoreDoc d  : docs) {
                    Document doc = reader.document(d.doc);

                    for(Map.Entry<String, Map<String, Integer>> et : cntz.entrySet()) {
                        String k = et.getKey();
                        Map<String, Integer> cntc = et.getValue();
                        String[] vals = doc.getValues(k);

                        for ( String val : vals ) {
                            if (cntc.containsKey(val)) {
                                cntc.put(val , cntc.get(val) + 1);
                            }
                        }
                    }

                    for(Map.Entry<String, Map<String, Integer>> et : cntx.entrySet()) {
                        String k = et.getKey();
                        Map<String, Integer> cntc = et.getValue();
                        String[] vals = doc.getValues(k);

                        for ( String val : vals ) {
                            if (cntc.containsKey(val)) {
                                cntc.put(val , cntc.get(val) + 1);
                            } else {
                                cntc.put(val , 1);
                            }
                        }
                    }
                }

                if (docs.length > 0) {
                    docz = finder.searchAfter(docs[docs.length - 1], q, 500);
                } else {
                    break;
                }
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }

        cnts.putAll(cntx);
        cnts.putAll(cntz);

        return resp;
    }

    private Sort  getSort(Map rd) throws HongsException {
        Set<String> ob = Synt.declare(rd.get("ob"), Set.class);
        List<SortField>  of = new ArrayList();
        if (ob != null)
        for (String fn : ob) {
            boolean rv = false;
            if (fn.startsWith("-") ) {
                fn = fn.substring(1);
                rv = true;
            }

            Map fm = (Map ) ssform.get(fn);
            if (fm == null) {
                continue;
            }

            SortField.Type st;
            if ("number".equals(fm.get("__type__"))) {
                Object nt = fm.get("type");
                if ("int".equals(nt)) {
                    st = SortField.Type.INT;
                } else
                if ("long".equals(nt)) {
                    st = SortField.Type.LONG;
                } else
                if ("float".equals(nt)) {
                    st = SortField.Type.FLOAT;
                } else
                if ("double".equals(nt)) {
                    st = SortField.Type.DOUBLE;
                } else {
                    continue;
                }
            } else {
                    st = SortField.Type.STRING;
            }

            of.add( new SortField(fn, st, rv));
        }

        return of.isEmpty() ? null : new Sort(of.toArray(new SortField[0]));
    }

    private Query getFind(Map rd) throws HongsException {
        BooleanQuery query = new BooleanQuery();
        int i = 0;

        for (Object o : rd.entrySet( )) {
            Map.Entry e = (Map.Entry) o;
            String k = (String) e.getKey();
            Object v = e.getValue();

            Map    m = (Map) ssform.get(k);
            if (null == m) {
                continue;
            }

            // 存储类型
            String g = Synt.declare(m.get("field_class"), "");
            if ("stored".equals(g)) {
                continue;
            }

            // 字段类型
            String t = Synt.declare(m.get("__type__"), "");
            if ("number".equals(t)) {
                // 数字类型
                String l = Synt.declare(m.get("type"), "");
                if ("int".equals(l)) {
                    addQuery(query, k, v, new AddIntQuery());
                } else
                if ("long".equals(l)) {
                    addQuery(query, k, v, new AddLongQuery());
                } else
                if ("float".equals(l)) {
                    addQuery(query, k, v, new AddFloatQuery());
                } else
                {
                    addQuery(query, k, v, new AddDoubleQuery());
                }
            } else
            {
                if ("text".equals(g)) {
                    addQuery(query, k, v, new AddTextQuery());
                } else
                {
                    addQuery(query, k, v, new AddStringQuery());
                }
            }

            i ++;
        }

        return i > 0 ? query : new MatchAllDocsQuery();
    }

    private Map doc2Map(Document doc) throws HongsException {
        Map map = new HashMap();
        for(Object o : ssform.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String k = (String) e.getKey();
            Map    m = (Map ) e.getValue();
            int    r = Dict.getValue(m,  0  , "__repeated__");
            String t = Dict.getValue(m,  "" , "__type__");
            String g = Dict.getValue(m,  "" , "field_class");
           boolean s = Dict.getValue(m, true, "field_store");
            if (!s) {
                continue;
            }
            IndexableField[] fs = doc.getFields(k);
            if (  "json".equals(g)) {
                if (r > 0) {
                    if (fs.length > 0) {
                        for (IndexableField f : fs) {
                            Dict.put(map, Data.toObject(f.stringValue()), k, null);
                        }
                    } else
                    {
                        map.put(k, new ArrayList());
                    }
                } else {
                    if (fs.length > 0) {
                        map.put(k, Data.toObject(fs[0].stringValue()));
                    } else
                    {
                        map.put(k, new HashMap());
                    }
                }
            } else
            if ("number".equals(t)) {
                if (r > 0) {
                    if (fs.length > 0) {
                        for (IndexableField f : fs) {
                            Dict.put(map, f.numericValue(), k, null);
                        }
                    } else
                    {
                        map.put(k, new ArrayList());
                    }
                } else {
                    if (fs.length > 0) {
                        map.put(k, fs[0].numericValue());
                    } else
                    {
                        map.put(k,  0);
                    }
                }
            } else
            {
                if (r > 0) {
                    if (fs.length > 0) {
                        for (IndexableField f : fs) {
                            Dict.put(map, f.stringValue(), k, null);
                        }
                    } else
                    {
                        map.put(k, new ArrayList());
                    }
                } else {
                    if (fs.length > 0) {
                        map.put(k, fs[0].stringValue());
                    } else
                    {
                        map.put(k, "");
                    }
                }
            }
        }

        return map;
    }

    private void addQuery(BooleanQuery qs, String k, Object v, AddQuery q) {
        Map m;
        if (v instanceof Map) {
            m = (Map) v ;
        } else {
            if (null==v || "".equals(v)) {
                return;
            }
            m = new HashMap();
            if (v instanceof Collection) {
                m.put("-in", v);
            } else {
                m.put("-eq", v);
            }
        }

        if (m.containsKey("-eq")) {
            Object n = m.get("-eq");
            qs.add(q.add(k, n), Occur.MUST);
            //qs.add(NumericRangeQuery.newIntRange(k, n, n, true, true), Occur.MUST);
        }
        if (m.containsKey("-ne")) {
            Object n = m.get("-eq");
            qs.add(q.add(k, n), Occur.MUST_NOT);
            //qs.add(NumericRangeQuery.newIntRange(k, n, n, true, true), Occur.MUST_NOT);
        }
        if (m.containsKey("-or")) {
            int n = Synt.declare(m.get("-eq"), 0);
            qs.add(q.add(k, n), Occur.SHOULD);
            //qs.add(NumericRangeQuery.newIntRange(k, n, n, true, true), Occur.SHOULD);
        }
        if (m.containsKey("-in")) {
            BooleanQuery qz = new BooleanQuery();
            Set a = Synt.declare(m.get("-in"), new HashSet());
            for(Object x : a) {
                int n = Synt.declare(x, 0);
                qs.add(q.add(k, n), Occur.SHOULD);
                //qz.add(NumericRangeQuery.newIntRange(k, n, n, true, true), Occur.SHOULD);
            }
            qs.add(qz, Occur.MUST);
        }
        if (m.containsKey("-ei")) {
            Set a = Synt.declare(m.get("-oi"), new HashSet());
            for(Object x : a) {
                int n = Synt.declare(x, 0);
                qs.add(q.add(k, n), Occur.MUST);
                //qs.add(NumericRangeQuery.newIntRange(k, n, n, true, true), Occur.MUST);
            }
        }
        if (m.containsKey("-ni")) {
            Set a = Synt.declare(m.get("-ni"), new HashSet());
            for(Object x : a) {
                int n = Synt.declare(x, 0);
                qs.add(q.add(k, n), Occur.MUST_NOT);
                //qs.add(NumericRangeQuery.newIntRange(k, n, n, true, true), Occur.MUST_NOT);
            }
        }
        if (m.containsKey("-oi")) {
            Set a = Synt.declare(m.get("-oi"), new HashSet());
            for(Object x : a) {
                int n = Synt.declare(x, 0);
                qs.add(q.add(k, n), Occur.SHOULD);
                //qs.add(NumericRangeQuery.newIntRange(k, n, n, true, true), Occur.SHOULD);
            }
        }
        if (m.containsKey("-gt") || m.containsKey("-ge")
        ||  m.containsKey("-lt") || m.containsKey("-le")) {
            Object  n, x;
            boolean l, r;

            if (m.containsKey("-gt")) {
                n = m.get("-gt"); l = false;
            } else
            if (m.containsKey("-ge")) {
                n = m.get("-ge"); l = true;
            } else
            {
                n = null; l = true;
            }

            if (m.containsKey("-lt")) {
                x = m.get("-lt"); r = false;
            } else
            if (m.containsKey("-le")) {
                x = m.get("-le"); r = true;
            } else
            {
                x = null; r = true;
            }

            if (n != null || x != null) {
                qs.add(q.add(k, n, x, l, r), Occur.MUST);
            }
        }
    }

    private static interface AddQuery {
        public Query add(String k, Object v);
        public Query add(String k, Object n, Object x, boolean l, boolean r);
    }

    private static class AddIntQuery implements AddQuery {
        public Query add(String k, Object v) {
            int n2 = Synt.declare(v, Integer.class);
            return NumericRangeQuery.newIntRange(k, n2, n2, true, true);
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            int n2 = Synt.declare(n, Integer.class);
            int x2 = Synt.declare(x, Integer.class);
            return NumericRangeQuery.newIntRange(k, n2, x2, l, r);
        }
    }

    private static class AddLongQuery implements AddQuery {
        public Query add(String k, Object v) {
            long n2 = Synt.declare(v, Long.class);
            return NumericRangeQuery.newLongRange(k, n2, n2, true, true);
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            long n2 = Synt.declare(n, Long.class);
            long x2 = Synt.declare(x, Long.class);
            return NumericRangeQuery.newLongRange(k, n2, x2, l, r);
        }
    }

    private static class AddFloatQuery implements AddQuery {
        public Query add(String k, Object v) {
            float n2 = Synt.declare(v, Float.class);
            return NumericRangeQuery.newFloatRange(k, n2, n2, true, true);
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            float n2 = Synt.declare(n, Float.class);
            float x2 = Synt.declare(x, Float.class);
            return NumericRangeQuery.newFloatRange(k, n2, x2, l, r);
        }
    }

    private static class AddDoubleQuery implements AddQuery {
        public Query add(String k, Object v) {
            double n2 = Synt.declare(v, Double.class);
            return NumericRangeQuery.newDoubleRange(k, n2, n2, true, true);
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            double n2 = Synt.declare(n, Double.class);
            double x2 = Synt.declare(x, Double.class);
            return NumericRangeQuery.newDoubleRange(k, n2, x2, l, r);
        }
    }

    private static class AddStringQuery implements AddQuery {
        public Query add(String k, Object v) {
            String n2 = v.toString();
            return new TermQuery(new Term(k, n2));
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            String n2 = n.toString();
            String x2 = x.toString();
            return TermRangeQuery.newStringRange(k, n2, x2, l, r);
        }
    }

    private static class AddTextQuery implements AddQuery {
        public Query add(String k, Object v) {
            String n2 = v.toString();
            //return new TermQuery(new Term(k, n2));
            try {
                String    anc = CoreConfig.getInstance( ).getProperty("core.search.analyzer", "org.apache.lucene.analysis.cn.ChineseAnalyzer");
                Analyzer  ana = ( Analyzer )  Class.forName( anc ).newInstance( );
                Query q = new QueryParser(Version.LUCENE_CURRENT, k, ana).parse(n2);System.out.println(q);
                return q;
            } catch (ClassNotFoundException ex) {
                throw new HongsError(HongsError.COMMON, ex);
            } catch (InstantiationException ex) {
                throw new HongsError(HongsError.COMMON, ex);
            } catch (IllegalAccessException ex) {
                throw new HongsError(HongsError.COMMON, ex);
            } catch (ParseException ex) {
                throw new HongsError(HongsError.COMMON, ex);
            }
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            String n2 = n.toString();
            String x2 = x.toString();
            return TermRangeQuery.newStringRange(k, n2, x2, l, r);
        }
    }

    public void close() throws HongsException {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

}

package net.hongs.search.record;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.action.StructConfig;
import app.hongs.util.Data;
import app.hongs.util.Dict;
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
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
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

    public Reader() throws HongsException {
        try {
            Map map = new HashMap();
            map.put("BASE_PATH", Core.VARS_PATH);
            map.put("VARS_PATH", Core.VARS_PATH);

            String    din = CoreConfig.getInstance( ).getProperty("core.search.location", "${VARS_PATH}/search");
            Directory dir = FSDirectory.open(new File(Text.inject(din, map)));

            reader = DirectoryReader.open(dir);
            finder = new IndexSearcher(reader);
            ssform = StructConfig.getInstance( "search" ).getForm("_search" );
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public Map getInfo(String id) throws HongsException {
        try {
                Query  q    = new TermQuery(new Term("id", id));
              TopDocs  docs = finder.search(q, 10);
            ScoreDoc[] hits = docs.scoreDocs;
            if  ( 0 == hits.length ) {
                return new HashMap();
            }
            Document doc = finder.doc(hits[0].doc);
            return doc2Map(doc);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public Map getList(Map rd) throws HongsException {
        Map  resp = new HashMap();
        Map  page = new HashMap();
        List list = new ArrayList();
        resp.put("page", page);
        resp.put("list", list);

        int rn = Dict.conv4Def(rd.get("rn"), 0);
        if (rn == 0) rn = 20;
        int un = Dict.conv4Def(rd.get("un"), 0);
        if (un == 0) un = 10;
        int pn = Dict.conv4Def(rd.get("pn"), 0);
        if (pn == 0) pn = 1 ;
        int xn = rn*(pn - 1);

        int minPn = un - (pn % un);
        int maxPn = un + minPn;
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

            // 总条目数和分页数
            page.put("rowscount", docs.length);
            page.put("pagecount", Math.ceil((double) docs.length / 20));
            if (docs.length == 0) {
                page.put("err",1);
            } else
            if (docs.length < xn) {
                page.put("err",2);
            }

            // 是否有下一组分页
            docz = finder.searchAfter(docs[maxRn - 1], q, 1);
            if (docz.totalHits  >  0 ) {
                page.put("next", true);
            } else {
                page.put("next",false);
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

        Set<String> cnt2 = Dict.conv4Def(rd.get("cnts"), new HashSet());
        Map<String, Map<String, Integer>> cntz = new HashMap();
        for(String   x : cnt2) {
            String[] a = x.split( ":", 2 );
            if (!ssform.containsKey(a[0])) {
                throw new HongsException(HongsException.COMMON, "Field "+a[0]+" not exists");
            }
            if (cntz.containsKey(a[0])) {
                cntz.put(a[0], new HashMap());
            }
            if (a.length > 2) {
                cntz.get(a[0]).put( a[1], 0 );
            }
        }

        try {
            Query q = getFind(rd);
            TopDocs docz = finder.search(q, 100);

            while ( docz.totalHits > 0) {
                ScoreDoc[] docs = docz.scoreDocs;
                for (ScoreDoc d : docs) {
                    Document doc = reader.document(d.doc);
                    for (Map.Entry<String, Map<String, Integer>> et : cntz.entrySet()) {
                        String k = et.getKey();
                        Map<String, Integer> cntc = et.getValue();
                        String[] vals = doc.getValues(k);

                        if (!cntc.isEmpty()) {
                            for ( String val : vals ) {
                                cntc.put(val , cntc.get(val) + 1);
                            }
                        } else {
                            for ( String val : vals ) {
                                cntc.put(val , cntc.containsKey(val) ? cntc.get(val) + 1 : 1);
                            }
                        }
                    }
                }

                docz = finder.searchAfter(docz.scoreDocs[docz.scoreDocs.length - 1], q, 100);
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }

        return resp;
    }

    private Query getFind(Map rd) throws HongsException {
        List<String> flds = new ArrayList();
        List<String> vals = new ArrayList();
        List<Occur > occs = new ArrayList();

        for (Object o : rd.entrySet( )) {
            Map.Entry e = (Map.Entry) o;
            String k = (String) e.getKey();
            Object v = e.getValue();

            if (!ssform.containsKey(k)) {
                continue;
            }

            if (v instanceof Map) {
                for (Object o2 : ((Map) v).entrySet()) {
                    Map.Entry e2 = (Map.Entry) o2;
                    String k2 = (String) e2.getKey();
                    Object v2 = e2.getValue();

                    Set<String> v3;
                    if (v2 instanceof Collection) {
                        v3 = new HashSet((Collection) v2);
                    } else if (v2 instanceof Map) {
                        v3 = new HashSet(((Map) v2).values());
                    } else {
                        v3 = new HashSet(); v3.add(v2.toString());
                    }

                    Occur c4;
                    if ("-eq".equals(k2)) {
                        c4 = Occur.MUST;
                    } else
                    if ("-ne".equals(k2)) {
                        c4 = Occur.MUST_NOT;
                    } else
                    if ("-or".equals(k2)) {
                        c4 = Occur.SHOULD;
                    } else {
                        continue;
                    }

                    for (String v4 : v3) {
                        flds.add(k );
                        vals.add(v4);
                        occs.add(c4);
                    }
                }
            } else {
                flds.add(k);
                vals.add(v.toString());
                occs.add(Occur.MUST);
            }
        }

        //if (flds.isEmpty()) return null;

        try {
            String    anc = CoreConfig.getInstance( ).getProperty("core.search.analyzer", "org.apache.lucene.analysis.cn.ChineseAnalyzer");
            Analyzer  ana = ( Analyzer )  Class.forName( anc ).newInstance( );

            return MultiFieldQueryParser.parse(Version.LATEST,
                   vals.toArray(new String[0]),
                   flds.toArray(new String[0]),
                   occs.toArray(new Occur [0]),
                   ana);
        } catch (ParseException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (ClassNotFoundException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (InstantiationException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (IllegalAccessException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    private Sort  getSort(Map rd) throws HongsException {
        Set<String> ob = Dict.conv2Cls(rd.get("ob"), Set.class);
        List<SortField>  of = new ArrayList();
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
            if ("number".equals(fm.get("_type"))) {
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

    private Map doc2Map(Document doc) throws HongsException {
        Map map = new HashMap();
        for(Object o : ssform.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String k = (String) e.getKey();
            Map    m = (Map ) e.getValue();
            int    r = Dict.getP4Def(m,0, "_repeated");
            IndexableField[] fs = doc.getFields(k);
            if (  "json".equals(m.get("field_genre"))) {
                if (r > 0) {
                    for (IndexableField f : fs) {
                        Dict.setPoint(map, Data.toObject(f.stringValue()), k, null);
                    }
                } else {
                    map.put(k, Data.toObject(fs[0].stringValue()));
                }
            } else
            if ("number".equals(m.get("type"))) {
                if (r > 0) {
                    for (IndexableField f : fs) {
                        Dict.setPoint(map, f.numericValue(), k, null);
                    }
                } else {
                    map.put(k, fs[0].numericValue());
                }
            } else
            {
                if (r > 0) {
                    for (IndexableField f : fs) {
                        Dict.setPoint(map, f.stringValue(), k, null);
                    }
                } else {
                    map.put(k, fs[0].stringValue());
                }
            }
        }

        return map;
    }

    public void close() throws HongsException {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

}

package app.hongs.dh.lucene;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.FormSet;
import app.hongs.dh.IRecord;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
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
 * Lucene 数据模型
 * @author Hongs
 */
public class LuceneRecord implements IRecord, Core.Destroy {

    private final String  datapath;
    private final String  analyzer;
    private final Map     fields;

    private IndexWriter   writer = null;
    private IndexReader   reader = null;
    private IndexSearcher finder = null;

    public String   idCol = "id";
    public String   wdCol = "wd";
    public String[] dispCols = new String[] {"name"};

    public LuceneRecord(Map items, String datapath, String analyzer) {
        if (datapath == null) {
            datapath = CoreConfig.getInstance().getProperty("core.dh.lucene.datapath", "${VARS_PATH}/lucene") + "/test";
        } else
        if (! new File(datapath).isAbsolute( )) {
            datapath = CoreConfig.getInstance().getProperty("core.dh.lucene.datapath", "${VARS_PATH}/lucene") + "/" + datapath;
        }
        if (analyzer == null) {
            analyzer = CoreConfig.getInstance().getProperty("core.dh.lucene.analyzer", "org.apache.lucene.analysis.cn.ChineseAnalyzer");
        }

        Map ti = new HashMap();
        ti.put("BASE_PATH", Core.VARS_PATH);
        ti.put("VARS_PATH", Core.VARS_PATH);
        datapath = Text.inject(datapath,ti);

        this.fields = items;
        this.datapath = datapath;
        this.analyzer = analyzer;
    }

    public LuceneRecord(String module, String entity) throws HongsException {
        this(FormSet.getInstance(module).getForm(entity), module + "/" + entity, null);
    }

    public Map retrieve(Map rd) throws HongsException {
        initReader();

        Object id = rd.get (idCol); // 指定单个 id 则走 get
        if (id != null && !(id instanceof Collection) && !(id instanceof Map)) {
            String jd = id.toString();
            Map  data = new HashMap();
            Map  info = get ( jd );
            data.put("info", info);
            return data;
        }

        int pn = Synt.declare(rd.get("pn"), 1); // 当前页码
        if (pn == 0) { // 明确指定页码为 0 则不分页
            Map  data = new HashMap();
            List list = getAll(rd);
            data.put("list", list);
            return data;
        }

        //** 计算分页 **/
        CoreConfig conf = CoreConfig.getInstance();
        int rn = Synt.declare(rd.get("rn"), 0); // 每页行数
        if (rn == 0) rn = conf.getProperty("fore.rows.per.page", 20);
        int ln = Synt.declare(rd.get("ln"), 0); // 分链接数
        if (ln == 0) ln = conf.getProperty("fore.lnks.per.page", 10);
        int minPn = pn - (pn % ln);
        int maxPn = ln + minPn;
        int minRn = rn * (pn - 1 );
        int maxRn = rn + minRn;

        Map  resp = new HashMap();
        Map  page = new HashMap();
        List list = new ArrayList();
        resp.put("page", page);
        resp.put("list", list);

        try {
            Query q = getQuery(rd);
            Sort  s = getSort (rd);

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
            if (list.isEmpty( )  ) {
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

    public Map counts(Map rd) throws HongsException {
        initReader();

        Map  resp = new HashMap();
        Map  cnts = new HashMap();
        resp.put("info", cnts);

        Set<String> cnt2 = Synt.declare(rd.get("count"), new HashSet());
        Map<String, Map<String, Integer>> cuntz = new HashMap();
        Map<String, Map<String, Integer>> cuntx = new HashMap();

        for(String   x : cnt2) {
            String[] a = x.split(":", 2);
            if (!fields.containsKey(a[0])) {
                throw new HongsException(HongsException.COMMON, "Field "+a[0]+" not exists");
            }
            if (a.length > 1) {
                cuntz.get(a[0]).put( a[1], 0 );
            } else
            if (!cuntx.containsKey(a[0])) {
                cuntx.put(a[0], new HashMap());
            } else
            {
                for(Object o : fields.entrySet()) {
                    Map.Entry e = (Map.Entry) o;
                    String k = (String) e.getKey();
                    Map    m = (Map ) e.getValue();

                    // id,wd 都不统计
                    if (idCol.equals(k) || wdCol.equals(k)) {
                        continue;
                    }

                    // 未存储的不统计
                   boolean s = Synt.declare(m.get("field_store"), true);
                    if ( ! s ) {
                        continue;
                    }

                    // text 和 json 的不统计
                    String g = Synt.declare(m.get("field_class"),  "" );
                    if ("text".equals(g) || "json".equals(g)) {
                        continue;
                    }

                    cuntx.put(k, new HashMap());
                }
            }
        }

        try {
            Query q = getQuery(rd);

            TopDocs docz = finder.search(q, 500);
            while ( docz.totalHits > 0) {
                ScoreDoc[] docs = docz.scoreDocs;
                for(ScoreDoc d  : docs) {
                    Document doc = reader.document(d.doc);

                    for(Map.Entry<String, Map<String, Integer>> et : cuntz.entrySet()) {
                        String k = et.getKey();
                        Map<String, Integer> cntc = et.getValue();
                        String[] vals = doc.getValues(k);

                        for ( String val : vals ) {
                            if (cntc.containsKey(val)) {
                                cntc.put(val , cntc.get(val) + 1);
                            }
                        }
                    }

                    for(Map.Entry<String, Map<String, Integer>> et : cuntx.entrySet()) {
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
                    docz=finder.searchAfter(docs[docs.length - 1], q, 500);
                } else {
                    break;
                }
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }

        cnts.putAll(cuntx);
        cnts.putAll(cuntz);

        return resp;
    }

    public String[] create(Map rd) throws HongsException {
        initWriter();

        add(rd);

        String[] resp = new String[dispCols.length  +  1];
                 resp[0] = (String) rd.get(idCol);
        for (int i = 0; i < dispCols.length; i++) {
            resp[i + 1] = Synt.declare(rd.get(dispCols[i]), String.class);
        }
        return resp;
    }

    public int update(Map rd) throws HongsException {
        initWriter();

        Set<String> ids = Synt.declare(rd.get(idCol), new HashSet());
        for (String id  : ids) {
            put(id, rd);
        }
        return ids.size();
    }

    public int upsert(Map rd) throws HongsException {
        initWriter();

        Set<String> ids = Synt.declare(rd.get(idCol), new HashSet());
        for (String id  : ids) {
            set(id, rd);
        }
        return ids.size();
    }

    public int delete(Map rd) throws HongsException {
        initWriter();

        Set<String> ids = Synt.declare(rd.get(idCol), new HashSet());
        for (String id  : ids) {
            del(id);
        }
        return ids.size();
    }

    /**
     * 添加文档
     * @param rd
     * @throws HongsException
     */
    public void add(Map rd) throws HongsException {
        try {
            rd.put(idCol, Core.getUniqueId());
            Document doc = map2doc(rd);
            writer.addDocument(doc);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    /**
     * 修改文档(局部更新)
     * 单独使用前需要执行 initWriter
     * @param id
     * @param rd
     * @throws HongsException
     */
    public void put(String id, Map rd) throws HongsException {
        Map od  = get(id);
        if (od == null || od.isEmpty()) {
            throw new HongsException(HongsException.COMMON, "Can not found document for '"+id+"'");
        } else {
            // 可以只对文档局部做更新
            Dict.putAll(od, rd);
            rd  = od ;
        }

        set(id  , rd);
    }

    /**
     * 设置文档(全量更新)
     * 单独使用前需要执行 initWriter
     * @param id
     * @param rd
     * @throws HongsException
     */
    public void set(String id, Map rd) throws HongsException {
        try {
            Document doc = map2doc(rd);
            writer.updateDocument(new Term(idCol, id), doc);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    /**
     * 单独使用前需要执行 initWriter
     * @param id
     * @throws HongsException
     */
    public void del(String id) throws HongsException {
        try {
            writer.deleteDocuments(new Term(idCol, id));
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    /**
     * 单独使用前需要执行 initReader
     * @param id
     * @return
     * @throws HongsException
     */
    public Map get(String id) throws HongsException {
        try {
                Query  q    = new TermQuery(new Term(idCol, id));
              TopDocs  docs = finder.search(q, 1);
            ScoreDoc[] hits = docs.scoreDocs;
            if  ( 0 != hits.length ) {
                Document doc = finder.doc(hits[0].doc);
                return doc2Map(doc );
            } else {
                return new HashMap();
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    /**
     * 单独使用前需要执行 initReader
     * @param rd
     * @return
     */
    public List getAll(Map rd) throws HongsException {
        List list = new ArrayList();

        try {
            Query q = getQuery(rd);
            Sort  s = getSort (rd);

            TopDocs  docz;
            if (s != null) {
                docz = finder.search(q, 100, s);
            } else {
                docz = finder.search(q, 100);
            }

            while ( docz.totalHits > 0) {
                ScoreDoc[] docs = docz.scoreDocs;
                for(ScoreDoc d  : docs) {
                    Document doc = reader.document(d.doc);
                    list.add(doc2Map(doc));
                }

                if (docs.length  ==  0) {
                    break;
                }

                if (s != null) {
                    docz = finder.searchAfter(docs[docs.length - 1], q, 100, s);
                } else {
                    docz = finder.searchAfter(docs[docs.length - 1], q, 100);
                }
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }

        return list;
    }

    public Map doc2Map(Document doc) throws HongsException {
        Map map = new HashMap();
        for(Object o : fields.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String k = (String) e.getKey();
            Map    m = (Map ) e.getValue();

            int    r = Synt.declare(m.get("repeated"), 0);
            String t = Synt.declare(m.get("__type__"),"");
            String g = Synt.declare(m.get("lucene-field"),  "" );
           boolean s = Synt.declare(m.get("lucene-store"), true);
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

    private void add2Doc(Document doc, Field.Store s, String g, String k, Object v) throws HongsException {
        if (  "json".equals(g)) {
            if (  "".equals(v)) {
                v = "{}";
            } else
            if (!(v instanceof String)) {
                v = Data.toString( v );
            }
            doc.add(new StoredField(k, (String) v));
        } else
        if (v instanceof String) {
            if ("stored".equals(g)) {
                doc.add(new StoredField(k, Synt.declare(v, String.class)));
            } else
            if (  "text".equals(g)) {
                doc.add(new   TextField(k, Synt.declare(v, String.class), s));
            } else
            {
                doc.add(new StringField(k, Synt.declare(v, String.class), s));
            }
        } else
        if (v instanceof Integer) {
            doc.add(new IntField(k, Synt.declare(v, Integer.class), s));
        } else
        if (v instanceof String) {
            doc.add(new LongField(k, Synt.declare(v, Long.class), s));
        } else
        if (v instanceof Float) {
            doc.add(new FloatField(k, Synt.declare(v, Float.class), s));
        } else
        if (v instanceof Double) {
            doc.add(new DoubleField(k, Synt.declare(v, Double.class), s));
        } else
        {
            throw new HongsException(HongsException.COMMON, "Not support type '"+v.getClass().getName()+"'");
        }
    }

    public Document map2doc(Map rd) throws HongsException {
        Document doc = new Document();

        for(Object o : rd.entrySet()) {
            Map.Entry e = (Map.Entry)o;
            String k = e.getKey(  ).toString();
            Object v = e.getValue();

            String g = Dict.getValue(fields, String.class, k, "field_class");
            Field.Store s  =  Dict.getValue(fields, true , k, "field_store")
                           ?  Field.Store.YES : Field.Store.NO;

            if (!(v instanceof Collection)) {
                add2Doc(doc, s, g, k, v);
            } else
            for ( Object x : ( Collection ) v) {
                add2Doc(doc, s, g, k, x);
            }
        }

        return doc;
    }

    public Sort getSort(Map rd) throws HongsException {
        Set<String> ob = Synt.declare(rd.get("ob"), new HashSet());
        List<SortField> of = new ArrayList();

        for (String fn: ob) {
            boolean rv;
            if (fn.startsWith("-") ) {
                fn = fn.substring(1);
                rv = true ;
            } else {
                rv = false;
            }

            Map fm = (Map ) fields.get(fn);
            if (fm == null) {
                continue;
            }

            SortField.Type st;
            if ("number".equals(fm.get("__type__"))) {
                Object   nt  =  fm.get(  "type"  );
                if ("int".equals(nt)) {
                    st = SortField.Type.INT;
                } else
                if ("long".equals(nt)) {
                    st = SortField.Type.LONG;
                } else
                if ("float".equals(nt)) {
                    st = SortField.Type.FLOAT;
                } else
                {
                    st = SortField.Type.DOUBLE;
                }
            } else {
                    st = SortField.Type.STRING;
            }

            of.add( new SortField(fn, st, rv));
        }

        return of.isEmpty() ? null : new Sort(of.toArray(new SortField[0]));
    }

    public Query getQuery(Map rd) throws HongsException {
        BooleanQuery query = new BooleanQuery();

        int i = 0;
        for(Object o : rd.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String k = (String) e.getKey();
            Object v = e.getValue(  );

            Map    m = (Map) fields.get("k");
            if (null == m) {
                continue;
            }

            // 存储类型
            String g = Synt.declare(m.get("lucene-field"), "");
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

    private void addQuery(BooleanQuery qs, String k, Object v, AddQuery q) {
        Map m;
        if (v instanceof Map) {
            m = (Map) v;
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
            qs.add(q.add(k, n), BooleanClause.Occur.MUST);
        }

        if (m.containsKey("-ne")) {
            Object n = m.get("-ne");
            qs.add(q.add(k, n), BooleanClause.Occur.MUST_NOT);
        }

        if (m.containsKey("-or")) {
            int n = Synt.declare(m.get("-or"), 0);
            qs.add(q.add(k, n), BooleanClause.Occur.SHOULD);
        }

        if (m.containsKey("-in")) { // In
            BooleanQuery qz = new BooleanQuery();
            Set a = Synt.declare(m.get("-in"), new HashSet());
            for(Object x : a) {
                int n = Synt.declare(x, 0);
                qs.add(q.add(k, n), BooleanClause.Occur.SHOULD);
            }
            qs.add(qz, BooleanClause.Occur.MUST);
        }

        if (m.containsKey("-ai")) { // All In
            Set a = Synt.declare(m.get("-oi"), new HashSet());
            for(Object x : a) {
                int n = Synt.declare(x, 0);
                qs.add(q.add(k, n), BooleanClause.Occur.MUST);
            }
        }

        if (m.containsKey("-ni")) { // Not In
            Set a = Synt.declare(m.get("-ni"), new HashSet());
            for(Object x : a) {
                int n = Synt.declare(x, 0);
                qs.add(q.add(k, n), BooleanClause.Occur.MUST_NOT);
            }
        }

        if (m.containsKey("-oi")) { // Or In
            Set a = Synt.declare(m.get("-oi"), new HashSet());
            for(Object x : a) {
                int n = Synt.declare(x, 0);
                qs.add(q.add(k, n), BooleanClause.Occur.SHOULD);
            }
        }

        //** 区间查询 **/

        Integer n, x;
        boolean l, r;

        if (m.containsKey("-gt")) {
            n = Synt.declare(m.get("-gt"), 0); l = false;
        } else
        if (m.containsKey("-ge")) {
            n = Synt.declare(m.get("-ge"), 0); l = true;
        } else
        {
            n = null; l = true;
        }

        if (m.containsKey("-lt")) {
            x = Synt.declare(m.get("-lt"), 0); r = false;
        } else
        if (m.containsKey("-le")) {
            x = Synt.declare(m.get("-le"), 0); r = true;
        } else
        {
            x = null; r = true;
        }

        if (n != null || x != null) {
            qs.add(q.add(k, n, x, l, r), BooleanClause.Occur.MUST);
        }
    }

    public void initWriter() {
        if (writer != null) {
            return;
        }
        try {
            Analyzer  ana = (Analyzer)  Class.forName(analyzer).newInstance();
            Directory dir = FSDirectory.open(new File(datapath));

            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_CURRENT, ana);
            iwc.setOpenMode( IndexWriterConfig.OpenMode.CREATE );

            writer = new IndexWriter(dir, iwc);
        } catch (IOException ex) {
            Logger.getLogger(LuceneRecord.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LuceneRecord.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(LuceneRecord.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LuceneRecord.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initReader() throws HongsException {
        if (reader != null) {
            return;
        }
        try {
            Directory dir = FSDirectory.open(new File(datapath));

            reader = DirectoryReader.open(dir);
            finder = new IndexSearcher(reader);
        } catch (IOException ex) {
            Logger.getLogger(LuceneRecord.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void destroy() throws HongsException {
        try {
            if (writer != null) {
                writer.close( );
                writer  = null;
            }
            if (reader != null) {
                reader.close( );
                reader  = null;
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
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
            try {
                String x = v.toString( );
                String   anc = CoreConfig.getInstance().getProperty("core.search.analyzer", "org.apache.lucene.analysis.cn.ChineseAnalyzer");
                Analyzer ana = (Analyzer) Class.forName(anc).newInstance();
                Query  q = new QueryParser(Version.LUCENE_CURRENT, k, ana).parse(x);
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

}

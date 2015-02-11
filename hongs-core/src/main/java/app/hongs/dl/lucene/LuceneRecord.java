package app.hongs.dl.lucene;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.FormSet;
import app.hongs.dl.IRecord;
import app.hongs.util.Data;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import app.hongs.util.Text;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import static org.apache.lucene.util.Version.LUCENE_CURRENT;

/**
 * Lucene 记录模型
 * @author Hongs
 */
public class LuceneRecord implements IRecord, Core.Destroy {

    protected final Map     fields;
    protected final String  datapath;
    protected final String  analyzer;

    protected IndexWriter   writer = null;
    protected IndexReader   reader = null;
    protected IndexSearcher finder = null;

    public String   idCol = "id";
    public String   wdCol = "wd";
    public String[] dispCols = new String[] {"name"};

    public LuceneRecord(Map fields, String datapath, String analyzer) {
        if (datapath == null) {
            datapath = CoreConfig.getInstance().getProperty("core.lucene.datapath", "${VARS_PATH}/lucene") + "/test";
        } else
        if (! new File(datapath).isAbsolute( )) {
            datapath = CoreConfig.getInstance().getProperty("core.lucene.datapath", "${VARS_PATH}/lucene") + "/" + datapath;
        }
        if (analyzer == null) {
            analyzer = CoreConfig.getInstance().getProperty("core.lucene.analyzer", "org.apache.lucene.analysis.standard.StandardAnalyzer");
        }

        Map ti = new HashMap();
        ti.put("BASE_PATH", Core.VARS_PATH);
        ti.put("VARS_PATH", Core.VARS_PATH);
        datapath = Text.inject(datapath,ti);

        this.fields = fields;
        this.datapath = datapath;
        this.analyzer = analyzer;
    }

    public LuceneRecord(String conf, String form) throws HongsException {
        this(FormSet.getInstance(conf).getForm(form), conf + "/" + form, null);
    }

    public Map retrieve(Map rd) throws HongsException {
        initReader();

        // 指定单个 id 则走 get
        Object id = rd.get (idCol);
        if (id != null && !(id instanceof Collection) && !(id instanceof Map)) {
            String jd = id.toString();
            Map  data = new HashMap();
            Map  info = get ( jd );
            data.put("info", info);
            return data;
        }

        // 明确指定页码为 0 则走 getAll
        int pn = Synt.declare(rd.get("pn"), 1); // 页码
        if (pn == 0) {
            Map  data = new HashMap();
            List list = getAll(rd);
            data.put("list", list);
            return data;
        }

        // 计算分页
        CoreConfig conf = CoreConfig.getInstance();
        int rn = Synt.declare(rd.get("rn"), 0); // 每页行数
        if (rn == 0) rn = conf.getProperty("fore.rows.per.page.N", 20);
        int ln = Synt.declare(rd.get("ln"), 0); // 分链接数
        if (ln == 0) ln = conf.getProperty("fore.lnks.per.page.N", 5 );
        int limit = (int) (Math.ceil((double) pn / ln) * ln * rn + 1 );
        int minRn = (pn - 1) * rn;
        int maxRn =  rn +   minRn;

        int rc, pc;
        List list = new ArrayList();
        try {
            Query q = getFind(rd);
            Sort  s = getSort (rd);

            if (1 == (1 & Core.DEBUG)) {
                CoreLogger.debug("...\r\n\tQuery: "+q.toString()+"\r\n\tSort : "+s.toString());
            }

            TopDocs docz = finder.search(q, limit, s);

            if (maxRn > docz.totalHits) {
                maxRn = docz.totalHits;
            }

            ScoreDoc[] docs = docz.scoreDocs;
            for (int i = minRn; i < maxRn; i ++) {
                Document doc = reader.document(docs[i].doc);
                list.add(doc2Map(doc));
            }

            rc = docs.length;
            pc = (int) Math.ceil((double) rc / rn);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }

        // 记录分页
        Map  resp = new HashMap();
        Map  page = new HashMap();
        resp.put("list", list);
        resp.put("page", page);
        page.put("page", pn);
        page.put("rows", rn);
        page.put("lnks", ln);
        page.put("rowscount", rc);
        page.put("pagecount", pc);
        page.put("uncertain", true);
        if (rc == 0) {
            page.put("err", 1);
        } else
        if (list.isEmpty()) {
            page.put("err", 2);
        }

        return resp;
    }

    public String[] create(Map rd) throws HongsException {
        initWriter();

        String[] resp;
        resp = new String[dispCols.length  +  1];
        resp[0] = add(rd);
        for (int i = 0; i<dispCols.length; i ++) {
            resp[i + 1] = Synt.declare(rd.get(dispCols[i]), String.class);
        }

        commit();
        return resp;
    }

    public int update(Map rd) throws HongsException {
        initWriter();

        Set<String> ids = Synt.declare(rd.get(idCol), new HashSet());
        for (String id  : ids) {
            put(id, rd);
        }

        commit();
        return ids.size();
    }

    public int upsert(Map rd) throws HongsException {
        initWriter();

        Set<String> ids = Synt.declare(rd.get(idCol), new HashSet());
        for (String id  : ids) {
            set(id, rd);
        }

        commit();
        return ids.size();
    }

    public int delete(Map rd) throws HongsException {
        initWriter();

        Set<String> ids = Synt.declare(rd.get(idCol), new HashSet());
        for (String id  : ids) {
            del(id);
        }

        commit();
        return ids.size();
    }

    /**
     * 添加文档
     * 单独使用前需要执行 initWriter, commit
     * @param rd
     * @return ID
     * @throws HongsException
     */
    public String add(Map rd) throws HongsException {
        if (rd.containsKey(idCol)) {
            throw new HongsException(HongsException.COMMON, "Id can not set in add");
        }
        String id = Core.getUniqueId();
        rd.put(idCol , id );
        addDoc(map2Doc(rd));
        return id;
    }

    /**
     * 修改文档(局部更新)
     * 单独使用前需要执行 initWriter, commit
     * @param id
     * @param rd
     * @throws HongsException
     */
    public void put(String id, Map rd) throws HongsException {
        Document doc = getDoc(id);
        if (doc == null) {
            throw new HongsException(HongsException.COMMON, "Can not found document for '"+id+"'");
        }
        docAdd(doc, rd);
        setDoc(id, doc);
    }

    /**
     * 设置文档(全量更新)
     * 单独使用前需要执行 initWriter, commit
     * @param id
     * @param rd
     * @throws HongsException
     */
    public void set(String id, Map rd) throws HongsException {
        setDoc(id, map2Doc(rd));
    }

    /**
     * 单独使用前需要执行 initWriter, commit
     * @param id
     * @throws HongsException
     */
    public void del(String id) throws HongsException {
        delDoc(id);
    }

    /**
     * 单独使用前需要执行 initReader
     * @param id
     * @return
     * @throws HongsException
     */
    public Map get(String id) throws HongsException {
        Document doc = getDoc(id);
        if (doc != null) {
            return doc2Map(doc );
        } else {
            return new HashMap();
        }
    }

    /**
     * 单独使用前需要执行 initReader
     * @param rd
     * @return
     * @throws HongsException
     */
    public List getAll(Map rd) throws HongsException {
        List list = new ArrayList();

        try {
            Query q = getFind(rd);
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

    public void addDoc(Document doc) throws HongsException {
        try {
            writer.addDocument(doc);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public void setDoc(String id, Document doc) throws HongsException {
        try {
            writer.updateDocument(new Term(idCol, id), doc);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public void delDoc(String id) throws HongsException {
        try {
            writer.deleteDocuments(new Term(idCol, id));
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public Document getDoc(String id) throws HongsException {
        try {
                Query  q    = new TermQuery(new Term(idCol, id));
              TopDocs  docs = finder.search(q, 1);
            ScoreDoc[] hits = docs.scoreDocs;
            if  ( 0 != hits.length ) {
                return finder.doc(hits[0].doc);
            } else {
                return null;
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public Query getFind(Map rd) throws HongsException {
        BooleanQuery query = new BooleanQuery();

        for(Object o : rd.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            Object fv = e.getValue( );
            String fn = (String)e.getKey();

            // 字段是否存在
            Map fm = (Map ) fields.get(fn);
            if (fm == null) {
                continue;
            }

            // 存储类型
            String ft = Synt.declare(fm.get("lucene-field"), "");
            if ("stored".equals(ft)) {
                continue;
            }

            // 值类型
            if ("number".equals(fm.get("__type__"))) {
                // 数字类型
                Object   nt  =  fm.get(  "type"  );
                if ("int".equals(nt)) {
                    qryAdd(query, fn, fv, new AddIntQuery());
                } else
                if ("long".equals(nt)) {
                    qryAdd(query, fn, fv, new AddLongQuery());
                } else
                if ("float".equals(nt)) {
                    qryAdd(query, fn, fv, new AddFloatQuery());
                } else
                {
                    qryAdd(query, fn, fv, new AddDoubleQuery());
                }
            } else
            {
                if ("text".equals(ft)) {
                    AddTextQuery q = new AddTextQuery();
                                 q.ana( getAna() );
                    qryAdd(query, fn, fv, q);
                } else
                {
                    qryAdd(query, fn, fv, new AddStringQuery());
                }
            }
        }

        return query.clauses().size() > 0 ? query : new MatchAllDocsQuery();
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

        of.add(SortField.FIELD_SCORE);
        of.add(SortField.FIELD_DOC  );

        return new Sort(of.toArray(new SortField[0]));
    }

    public Document map2Doc(Map rd) throws HongsException {
        Document doc = new Document();
        docAdd(doc, rd);
        return doc;
    }

    public Map doc2Map(Document doc) throws HongsException {
        Map map = new HashMap();
        for(Object o : fields.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String k = (String) e.getKey();
            Map    m = (Map ) e.getValue();

           boolean s = Synt.declare(m.get("lucene-store"), true); if (!s) continue;
            String g = Synt.declare(m.get("lucene-field"), ""  );
           boolean r = Synt.declare(m.get("__repeated__"),false);
            String t = Synt.declare(m.get("__type__"),"");

            IndexableField[] fs = doc.getFields(k);
            if (  "json".equals(g)) {
                if (r) {
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
                if (r) {
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
                if (r) {
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

    public void initWriter() throws HongsException {
        if (writer != null) {
            return;
        }
        try {
            File      dio = new File(datapath);
//          if (! dio.exists( ) ) dio.mkdirs();
            Directory dir = FSDirectory.open(dio);

            Analyzer  ana = getAna();
            IndexWriterConfig iwc = new IndexWriterConfig(LUCENE_CURRENT, ana);
//          iwc.setOpenMode ( IndexWriterConfig.OpenMode.CREATE_OR_APPEND/**/);

            writer = new IndexWriter(dir, iwc);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public void initReader() throws HongsException {
        if (reader != null) {
            return;
        }
        try {
            File      dio = new File(datapath);
            if (! dio.exists( ) ) initWriter();
            Directory dir = FSDirectory.open(dio);

            reader = DirectoryReader.open(dir);
            finder = new IndexSearcher(reader);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
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

    public void commit() throws HongsException {
        try {
            writer.commit();
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public void rollback() throws HongsException {
        try {
            writer.rollback();
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    private Analyzer getAna() throws HongsException {
        try {
            return (Analyzer) Class.forName(analyzer).getConstructor(LUCENE_CURRENT.getClass()).newInstance(LUCENE_CURRENT);
        } catch ( IllegalArgumentException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (InvocationTargetException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (NoSuchMethodException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (SecurityException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (InstantiationException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (IllegalAccessException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (ClassNotFoundException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    private void docAdd(Document doc, Map rd) throws HongsException {
        for(Object o : rd.entrySet()) {
            Map.Entry e = (Map.Entry)o;
            String k = e.getKey(  ).toString();
            Object v = e.getValue();

            String g = Dict.getValue(fields, String.class, k, "lucene-field");
            Field.Store s  =  Dict.getValue( fields, true, k, "lucene-store")
                           ?  Field.Store.YES : Field.Store.NO;

            if (v instanceof Collection) {
                for (Object x : (Collection) v) {
                    this.docAdd(doc, k, x, g, s);
                }
            } else
            if (v instanceof Object[ ] ) {
                for (Object x : (Object[ ] ) v) {
                    this.docAdd(doc, k, x, g, s);
                }
            } else
            {
                /**/this.docAdd(doc, k, v, g, s);
            }
        }
    }

    private void docAdd(Document doc, String k, Object v, String g, Field.Store s) throws HongsException {
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
        if (v instanceof Long) {
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

    private void qryAdd(BooleanQuery qry, String k, Object v, AddXxxQuery q) {
        Map m;
        if (v instanceof Map) {
            m = new HashMap();
            m.putAll((Map) v);
        } else {
            if (null==v || "".equals(v)) {
                return ;
            }
            m = new HashMap();
            if (v instanceof Collection) {
                Collection c = (Collection) v;
                    c.remove("");
                if (c.isEmpty()) {
                    return;
                }
                m.put("-in", c);
            } else
            {
                m.put("-eq", v);
            }
        }

        float w = 1F;

        if (m.containsKey("-wt")) {
            Object n = m.remove("-wt");
            q.bst( Synt.declare(n, w));
        }

        if (m.containsKey("-eq")) {
            Object n = m.remove("-eq");
            qry.add(q.add(k, n), BooleanClause.Occur.MUST);
        }

        if (m.containsKey("-ne")) {
            Object n = m.remove("-ne");
            qry.add(q.add(k, n), BooleanClause.Occur.MUST_NOT);
        }

        if (m.containsKey("-or")) {
            Object n = m.remove("-or");
            qry.add(q.add(k, n), BooleanClause.Occur.SHOULD);
        }

        if (m.containsKey("-in")) { // In
            BooleanQuery qay = new BooleanQuery();
            Set a = Synt.declare(m.remove("-in"), new HashSet());
            for(Object x : a) {
                qay.add(q.add(k, x), BooleanClause.Occur.SHOULD);
            }
            qry.add(qay, BooleanClause.Occur.MUST);
        }

        if (m.containsKey("-ai")) { // All In
            Set a = Synt.declare(m.remove("-ai"), new HashSet());
            for(Object x : a) {
                qry.add(q.add(k, x), BooleanClause.Occur.MUST);
            }
        }

        if (m.containsKey("-ni")) { // Not In
            Set a = Synt.declare(m.remove("-ni"), new HashSet());
            for(Object x : a) {
                qry.add(q.add(k, x), BooleanClause.Occur.MUST_NOT);
            }
        }

        if (m.containsKey("-oi")) { // Or In
            Set a = Synt.declare(m.remove("-oi"), new HashSet());
            for(Object x : a) {
                qry.add(q.add(k, x), BooleanClause.Occur.SHOULD);
            }
        }

        //** 区间查询 **/

        Integer n, x;
        boolean l, r;

        if (m.containsKey("-gt")) {
            n = Synt.declare(m.remove("-gt"), 0); l = false;
        } else
        if (m.containsKey("-ge")) {
            n = Synt.declare(m.remove("-ge"), 0); l = true;
        } else
        {
            n = null; l = true;
        }

        if (m.containsKey("-lt")) {
            x = Synt.declare(m.remove("-lt"), 0); r = false;
        } else
        if (m.containsKey("-le")) {
            x = Synt.declare(m.remove("-le"), 0); r = true;
        } else
        {
            x = null; r = true;
        }

        if (n != null || x != null) {
            qry.add(q.add(k, n, x, l, r), BooleanClause.Occur.MUST);
        }

        // 其他 IN
        if (!m.isEmpty()) {
            Set s = new HashSet();
            s.addAll(m.values( ));
            qryAdd(qry, k, s, q );
        }
    }

    private static interface AddXxxQuery {
        public void  bst(float  w);
        public Query add(String k, Object v);
        public Query add(String k, Object n, Object x, boolean l, boolean r);
    }

    private static class AddIntQuery implements AddXxxQuery {
        private Float w = null;
        public void  bst(float  w) {
            this.w  = w;
        }
        public Query add(String k, Object v) {
            Integer n2 = Synt.declare(v, Integer.class);
            Query   q2 = NumericRangeQuery.newIntRange(k, n2, n2, true, true);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            Integer n2 = Synt.declare(n, Integer.class);
            Integer x2 = Synt.declare(x, Integer.class);
            Query   q2 = NumericRangeQuery.newIntRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    private static class AddLongQuery implements AddXxxQuery {
        private Float w = null;
        public void  bst(float  w) {
            this.w  = w;
        }
        public Query add(String k, Object v) {
            Long    n2 = Synt.declare(v, Long.class);
            Query   q2 = NumericRangeQuery.newLongRange(k, n2, n2, true, true);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            Long    n2 = Synt.declare(n, Long.class);
            Long    x2 = Synt.declare(x, Long.class);
            Query   q2 = NumericRangeQuery.newLongRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    private static class AddFloatQuery implements AddXxxQuery {
        private Float w = null;
        public void  bst(float  w) {
            this.w  = w;
        }
        public Query add(String k, Object v) {
            Float   n2 = Synt.declare(v, Float.class);
            Query   q2 = NumericRangeQuery.newFloatRange(k, n2, n2, true, true);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            Float   n2 = Synt.declare(n, Float.class);
            Float   x2 = Synt.declare(x, Float.class);
            Query   q2 = NumericRangeQuery.newFloatRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    private static class AddDoubleQuery implements AddXxxQuery {
        private Float w = null;
        public void  bst(float  w) {
            this.w  = w;
        }
        public Query add(String k, Object v) {
            Double  n2 = Synt.declare(v, Double.class);
            Query   q2 = NumericRangeQuery.newDoubleRange(k, n2, n2, true, true);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            Double  n2 = Synt.declare(n, Double.class);
            Double  x2 = Synt.declare(x, Double.class);
            Query   q2 = NumericRangeQuery.newDoubleRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    private static class AddStringQuery implements AddXxxQuery {
        private Float w = null;
        public void  bst(float  w) {
            this.w  = w;
        }
        public Query add(String k, Object v) {
            String  n2 = v.toString();

            int p = n2.indexOf('^');
            Float x = null;
            if (p > 0) {
                x  = Float.parseFloat(n2.substring(p + 1));
                n2 = n2.substring(0, p);
            }

            Query   q2 = new TermQuery(new Term(k, n2));
            if (w != null) q2.setBoost(w);
            if (x != null) q2.setBoost(x);
            return  q2;
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            String  n2 = n.toString();
            String  x2 = x.toString();
            Query   q2 = TermRangeQuery.newStringRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    private static class AddTextQuery implements AddXxxQuery {
        private Analyzer a = null;
        private Float    w = null;
        public void  ana(Analyzer a) {
            this.a  = a;
        }
        public void  bst(  float  w) {
            this.w  = w;
        }
        public Query add(String k, Object v) {
            try {
                Query   q2 = new QueryParser(LUCENE_CURRENT, k, a).parse(QueryParser.escape(v.toString()));
                if (w != null) q2.setBoost(w);
                return  q2;
            } catch (ParseException ex) {
                throw new HongsError(HongsError.COMMON, ex);
            }
        }
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            String  n2 = n.toString();
            String  x2 = x.toString();
            Query   q2 = TermRangeQuery.newStringRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

}

package app.hongs.dl.lucene;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.CoreLogger;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.FormSet;
import app.hongs.dl.IRecord;
import app.hongs.dl.ITrnsct;
import app.hongs.util.Data;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import app.hongs.util.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * Lucene 记录模型
 * @author Hongs
 */
public class LuceneRecord implements IRecord, ITrnsct, Core.Destroy {

    protected final String  dataPath;
    protected final Map<String, Map   > fields;
    protected final Map<String, String> talias;

    protected IndexWriter   writer = null;
    protected IndexReader   reader = null;
    protected IndexSearcher finder = null;

    public boolean IN_TRNSCT_MODE = false;
    public boolean IN_OBJECT_MODE = false;

    public String   idCol = "id";
    public String[] findCols = new String[] { "wd" };
    public String[] dispCols = new String[] {"name"};

    public LuceneRecord(String path, final Map<String, Map> fields, final Map<String, String> talias)
    throws HongsException {
        CoreConfig conf = CoreConfig.getInstance();
        if (path == null) {
            path = conf.getProperty(
                    "core.lucene.datapath",
                    "${VARS_PATH}/lucene") + "/test" ;
        }
        if (! new File(path).isAbsolute()) {
            path = conf.getProperty(
                    "core.lucene.datapath",
                    "${VARS_PATH}/lucene") + "/"+path;
        }

        Map m = new HashMap();
        m.put("CORE_PATH", Core.CORE_PATH);
        m.put("CONF_PATH", Core.CONF_PATH);
        m.put("VARS_PATH", Core.VARS_PATH);
        m.put("TMPS_PATH", Core.TMPS_PATH);
        path = Text.inject(path, m);

        this.dataPath = path;

        this.fields = fields != null ? fields : FormSet.getInstance("default").getForm(  "test"   );
        this.talias = talias != null ? talias : FormSet.getInstance("default").getEnum("__types__");

        this.IN_TRNSCT_MODE = Synt.declare(Core.getInstance().got("__IN_TRNSCT_MODE__"), conf.getProperty("core.in.trnsct.mode", false));
        this.IN_OBJECT_MODE = Synt.declare(Core.getInstance().got("__IN_OBJECT_MODE__"), conf.getProperty("core.in.object.mode", false));
    }

    public LuceneRecord(String conf, String form)
    throws HongsException {
        this(conf+"/"+form,
            FormSet.getInstance(  conf   ).getForm(   form    ) ,
            FormSet.getInstance("default").getEnum("__types__"));
    }

    /**
     * 获取数据

 以下参数为特殊参数:
 id   ID
 wd   搜索
 ob   排序
 sf   字段
 pn   页码
 rn   每页行数
 ln   分页数量
 or   多组"或"关系条件
 ar   串联多组关系条件
 请注意尽量避免将其作为字段名(id,wd除外)
     *
     * @param rd
     * @return
     * @throws HongsException
     */
    @Override
    public Map retrieve(Map rd) throws HongsException {
        // 指定单个 id 则走 getOne
        Object id = rd.get (idCol);
        if (id != null && !(id instanceof Collection) && !(id instanceof Map)) {
            Map  data = new HashMap();
            Map  info = getOne(rd);
            data.put("info", info);
            return data;
        }

        // 获取行数, 默认依从配置
        int rn;
        if (rd.containsKey("rn")) {
            rn = Synt.declare(rd.get("rn"), 0);
        } else {
            rn = CoreConfig.getInstance().getProperty("fore.rows.per.page", 10);
        }

        // 指定行数 0, 则走 getAll
        if (rn == 0) {
            Map  data = new HashMap();
            List list = getAll(rd);
            data.put("list", list);
            return data;
        }

        // 获取链数, 默认依从配置
        int ln;
        if (rd.containsKey("ln")) {
            ln = Synt.declare(rd.get("ln"), 0);
        } else {
            ln = CoreConfig.getInstance().getProperty("fore.lnks.per.page", 10);
        }

        // 获取页码, 依此计算分页
        if (ln < 1) ln = 1; // 链数不得少于 1
        int pn = Synt.declare(rd.get("pn"), 1);
        int minPn = pn - (ln / 2 );
        if (minPn < 1)   minPn = 1;
        int maxPn = ln + minPn - 1;
        int limit = rn * maxPn + 1;
        int minRn = rn * (pn - 1 );
        int maxRn = rn + minRn;

        // 查询列表
        List list = new ArrayList();
        int rc, pc;
        initial( );
        try {
            Query q = getQuery(rd);
            Sort  s = getSort (rd);
                      ignFlds (rd);

            if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG)) {
                CoreLogger.debug("LuceneRecord.retrieve: "+q.toString()+" Sort: "+s.toString()+" Page: "+pn+" Rows: "+rn);
            }

            TopDocs tops = finder.search(q, limit, s);

            if (maxRn > tops.totalHits) {
                maxRn = tops.totalHits;
            }

            ScoreDoc[] scos = tops.scoreDocs;
            ScoreDoc   sco  ;
            Document   doc  ;
            for (int i = minRn; i < maxRn; i++) {
                sco =  scos[i];
                doc = reader.document(sco.doc );
                list.add(/**/doc2Map (doc)/**/);
            }

            rc = scos.length;
            pc = (int) Math.ceil((double)rc / rn);
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
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
        page.put("uncertain", pc > maxPn);
        if (rc == 0) {
            page.put("err", 1);
        } else
        if (list.isEmpty()) {
            page.put("err", 2);
        }

        return resp;
    }

    /**
     * 创建记录
     * @param rd
     * @return ID,名称等(由dispCols指定)
     * @throws HongsException
     */
    @Override
    public Map create(Map rd) throws HongsException {
        Map sd = new HashMap();
        sd.put("id" , add(rd));
        for(String fn : dispCols) {
            sd.put(fn, rd.get(fn));
        }
        return rd;
    }

    /**
     * 更新记录
     * @param rd
     * @return
     * @throws HongsException
     */
    @Override
    public int update(Map rd) throws HongsException {
        Set<String> ids = Synt.declare(rd.get(idCol), new HashSet());
        for(String  id  : ids) {
            put(id, rd );
        }
        return ids.size();
    }

    /**
     * 删除记录
     * @param rd
     * @return
     * @throws HongsException
     */
    @Override
    public int delete(Map rd) throws HongsException {
        Set<String> ids = Synt.declare(rd.get(idCol), new HashSet());
        for(String  id  : ids) {
            del(id);
        }
        return ids.size();
    }

    /**
     * 获取单个文档
     * @param rd
     * @return
     * @throws HongsException
     */
    public Map getOne(Map rd) throws HongsException {
        initial();
        try {
            Query q = getQuery(rd);
            Sort  s = getSort (rd);
                      ignFlds (rd);

            if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG)) {
                CoreLogger.debug("LuceneRecord.getOne: "+q.toString()+" Sort: "+s.toString());
            }

            TopDocs  tops;
            if (s != null) {
                tops = finder.search(q, 1, s);
            } else {
                tops = finder.search(q, 1);
            }

            if (tops.totalHits > 0) {
                ScoreDoc[] scos  = tops.scoreDocs;
                ScoreDoc   sco   = scos[ 0 ] /**/;
                Document   doc   = reader.document(sco.doc);
                return doc2Map(doc);
            }

            return new HashMap(   );
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        }
    }

    /**
     * 获取全部文档
     * @param rd
     * @return
     * @throws HongsException
     */
    public List getAll(Map rd) throws HongsException {
        initial();
        try {
            Query q = getQuery(rd);
            Sort  s = getSort (rd);
                      ignFlds (rd);

            if (0 < Core.DEBUG && 8 != (8 & Core.DEBUG)) {
                CoreLogger.debug("LuceneRecord.getAll: "+q.toString()+" Sort: "+s.toString());
            }

            int n  = 1000;
            TopDocs  tops;
            if (s != null) {
                tops = finder.search(q, n, s);
            } else {
                tops = finder.search(q, n);
            }

            List list = new ArrayList();

            while(tops.totalHits > 0) {
                ScoreDoc[] scos  = tops.scoreDocs;
                ScoreDoc   sco   = null;
                Document   doc   ;
                for(int i = 0; i < scos.length; i ++) {
                    sco = scos[i];
                    doc = reader.document(sco.doc );
                    list.add(/**/doc2Map (doc)/**/);
                }

                if (n != scos.length) {
                    break;
                }

                if (s != null) {
                    tops = finder.searchAfter(sco, q, n, s);
                } else {
                    tops = finder.searchAfter(sco, q, n);
                }
            }

            return  list;
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        }
    }

    /**
     * 添加文档
     * @param rd
     * @return ID
     * @throws HongsException
     */
    public String add(Map rd) throws HongsException {
        String id = Synt.declare(rd.get(idCol), String.class);
        if (id != null && id.length() != 0) {
            throw HongsException.common("Id can not set in add");
        }
        id = Core.getUniqueId();
        rd.put(idCol , id );
        addDoc(map2Doc(rd));
        return id;
    }

    /**
     * 修改文档(局部更新)
     * @param id
     * @param rd
     * @throws HongsException
     */
    public void put(String id, Map rd) throws HongsException {
        if (id == null || id.length() == 0) {
            throw HongsException.common("Id must be set in put");
        }
        Document doc = getDoc(id);
        if (doc == null) {
            throw HongsException.common("Doc#"+id+" not exists");
        } else {
            /**
             * 实际运行中发现
             * 直接往取出的 doc 里设置属性, 会造成旧值的索引丢失
             * 故只好转换成 map 再重新设置, 这样才能确保索引完整
             * 但那些 Store=NO 的数据将无法设置
             */
            ignFlds(new HashMap());
            Map  md = doc2Map(doc);
            md.putAll(rd);
            rd = md;
        }
        rd.put(idCol, id);
        docAdd(doc, rd);
        setDoc(id, doc);
    }

    /**
     * 设置文档(无则添加)
     * @param id
     * @param rd
     * @throws HongsException
     */
    public void set(String id, Map rd) throws HongsException {
        if (id == null || id.length() == 0) {
            throw HongsException.common("Id must be set in put");
        }
        Document doc = getDoc(id);
        if (doc == null) {
            doc =  new Document();
        } else {
            /**
             * 实际运行中发现
             * 直接往取出的 doc 里设置属性, 会造成旧值的索引丢失
             * 故只好转换成 map 再重新设置, 这样才能确保索引完整
             * 但那些 Store=NO 的数据将无法设置
             */
            ignFlds(new HashMap());
            Map  md = doc2Map(doc);
            md.putAll(rd);
            rd = md;
        }
        rd.put(idCol, id);
        docAdd(doc, rd);
        setDoc(id, doc);
    }

    /**
     * 删除文档(delDoc 的别名)
     * @param id
     * @throws HongsException
     */
    public void del(String id) throws HongsException {
        delDoc(id);
    }

    /**
     * 获取文档信息
     * @param id
     * @return
     * @throws HongsException
     */
    public Map get(String id) throws HongsException {
        Document doc = getDoc(id);
        if (doc != null) {
           ignFlds(new HashMap());
            return doc2Map( doc );
        } else {
            return new HashMap( );
        }
    }

    public void addDoc(Document doc) throws HongsException {
        connect();
        try {
            writer.addDocument(doc);
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        }
        if (!IN_TRNSCT_MODE) {
            commit();
        }
    }

    public void setDoc(String id, Document doc) throws HongsException {
        connect();
        try {
            writer.updateDocument(new Term(idCol, id), doc);
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        }
        if (!IN_TRNSCT_MODE) {
            commit();
        }
    }

    public void delDoc(String id) throws HongsException {
        connect();
        try {
            writer.deleteDocuments(new Term(idCol, id));
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        }
        if (!IN_TRNSCT_MODE) {
            commit();
        }
    }

    public Document getDoc(String id) throws HongsException {
        initial();
        try {
                Query  q    = new TermQuery(new Term(idCol, id));
              TopDocs  docs = finder.search(q, 1);
            ScoreDoc[] hits =   docs.scoreDocs;
            if  ( 0 != hits.length ) {
                return finder.doc(hits[0].doc);
            } else {
                return null;
            }
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        }
    }

    public Document map2Doc(Map map) throws HongsException {
        Document doc = new Document();
        docAdd(doc, map);
        return doc;
    }

    public Map doc2Map(Document doc) {
        Map map = new LinkedHashMap();
        mapAdd(map, doc);
        return map;
    }

    public void initial() throws HongsException {
        if (reader != null) {
            return;
        }
        try {
            // 索引目录不存在则先写入一个并删除
            if (!(new File(dataPath)).exists()) {
                connect();
                del(add(new HashMap( )));
                commit( );
            }

            Path p = Paths.get(dataPath);
            Directory dir = FSDirectory.open(p);

            reader = DirectoryReader.open (dir);
            finder = new IndexSearcher (reader);
        } catch (IOException x) {
            throw HongsException.common(null,x);
        }
    }

    public void connect() throws HongsException {
        if (writer != null) {
            return;
        }
        try {
            IndexWriterConfig iwc = new IndexWriterConfig(getAnalyzer());
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            Path d = Paths.get(dataPath);
            Directory dir = FSDirectory.open(d);

            writer = new IndexWriter(dir , iwc);
        } catch (IOException x) {
            throw HongsException.common(null,x);
        }
    }

    @Override
    public void destroy() throws HongsException {
        try {
            if (writer != null) {
                // 默认退出时提交
                if (IN_TRNSCT_MODE) {
                    try {
                        commit();
                    } catch (HongsException he) {
                        rolbak();
                        throw he;
                    }
                }

                writer.maybeMerge();
                writer.close( );
                writer  = null ;
            }

            if (reader != null) {
                reader.close( );
                reader  = null ;
            }
        } catch (IOException x) {
            throw HongsException.common(null,x);
        }
    }

    @Override
    public void trnsct() {
        IN_TRNSCT_MODE = true;
    }

    @Override
    public void commit() throws HongsException {
        if (writer == null) {
            return;
        }
        try {
            writer.commit(  );
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        }
        IN_TRNSCT_MODE = Synt.declare(Core.getInstance().got( "__IN_TRNSCT_MODE__" ), false);
    }

    @Override
    public void rolbak() throws HongsException {
        if (writer == null) {
            return;
        }
        try {
            writer.rollback();
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        }
        IN_TRNSCT_MODE = Synt.declare(Core.getInstance().got( "__IN_TRNSCT_MODE__" ), false);
    }

    /**
     * 写入分析器
     * @return
     * @throws HongsException
     */
    protected Analyzer getAnalyzer() throws HongsException {
        Map<String , Analyzer> az = new HashMap( );
        Analyzer ad = new StandardAnalyzer();
        for(Object ot : fields.entrySet( ) ) {
            Map.Entry et = (Map.Entry) ot;
            String fn = (String) et.getKey();
            Map    fc = (Map ) et.getValue();
            String t = getFtype(fc);
            if ("search".equals(t )
            ||    "text".equals(t)) {
                az.put(fn, getAnalyzer(fc, false));
            }
        }
        return new PerFieldAnalyzerWrapper(ad, az);
    }

    /**
     * 查询分析器
     * @param fn 字段名
     * @return
     * @throws HongsException
     */
    protected Analyzer getAnalyzer(String fn) throws HongsException {
        Map fc = (Map) fields.get(fn);
        return getAnalyzer(fc, true );
    }

    /**
     * 构建分析器
     * @param fc 字段配置
     * @param iq 是否查询
     * @return
     * @throws HongsException
     */
    protected Analyzer getAnalyzer(Map fc, boolean iq) throws HongsException {
        try {
            CustomAnalyzer.Builder cb = CustomAnalyzer.builder();
            String kn, an, ac; Map oc;

            // 分词器
            an = Synt.declare(fc.get("lucene-tokenizer"), "");
            if (!"".equals(an)) {
                int p  = an.indexOf('{');
                if (p != -1) {
                    ac = an.substring(p);
                    an = an.substring(0, p - 1).trim( );
                    oc = Synt.declare(Data.toObject(ac), Map.class);
                    cb.withTokenizer(an, oc);
                } else {
                    cb.withTokenizer(an/**/);
                }
            } else {
                cb.withTokenizer("Standard");
            }

            // 过滤器
            for(Object ot2 : fc.entrySet()) {
                Map.Entry et2 = (Map.Entry) ot2;
                kn = (String) et2.getKey();
                if (iq) {
                    if (kn.startsWith("lucene-find-filter")) {
                        an = (String) et2.getValue();
                        an = an.trim();
                        if ("".equals(an)) {
                            continue;
                        }
                        int p  = an.indexOf('{');
                        if (p != -1) {
                            ac = an.substring(p);
                            an = an.substring(0, p - 1).trim( );
                            oc = Synt.declare(Data.toObject(ac), Map.class);
                            cb.addCharFilter(an, oc);
                        } else {
                            cb.addCharFilter(an/**/);
                        }
                    } else
                    if (kn.startsWith("lucene-query-filter")) {
                        an = (String) et2.getValue();
                        an = an.trim();
                        if ("".equals(an)) {
                            continue;
                        }
                        int p  = an.indexOf('{');
                        if (p != -1) {
                            ac = an.substring(p);
                            an = an.substring(0, p - 1).trim( );
                            oc = Synt.declare(Data.toObject(ac), Map.class);
                            cb.addTokenFilter(an, oc);
                        } else {
                            cb.addTokenFilter(an/**/);
                        }
                    }
                } else {
                    if (kn.startsWith("lucene-char-filter")) {
                        an = (String) et2.getValue();
                        an = an.trim();
                        if ("".equals(an)) {
                            continue;
                        }
                        int p  = an.indexOf('{');
                        if (p != -1) {
                            ac = an.substring(p);
                            an = an.substring(0, p - 1).trim();
                            oc = Synt.declare(Data.toObject(ac), Map.class);
                            cb.addCharFilter(an, oc);
                        } else {
                            cb.addCharFilter(an/**/);
                        }
                    } else
                    if (kn.startsWith("lucene-token-filter")) {
                        an = (String) et2.getValue();
                        an = an.trim();
                        if ("".equals(an)) {
                            continue;
                        }
                        int p  = an.indexOf('{');
                        if (p != -1) {
                            ac = an.substring(p);
                            an = an.substring(0, p - 1).trim();
                            oc = Synt.declare(Data.toObject(ac), Map.class);
                            cb.addTokenFilter(an, oc);
                        } else {
                            cb.addTokenFilter(an/**/);
                        }
                    }
                }
            }

            return cb.build();
        } catch (IOException ex) {
            throw HongsException.common(null, ex);
        } catch ( IllegalArgumentException ex) {
            throw HongsException.common(null, ex);
        }
    }

    /**
     * 获取字段类型
     * 支持的类型有
     * int
     * long
     * float
     * double
     * string
     * text
     * json
     * @param fc 字段配置
     * @return
     */
    protected String getFtype(Map fc) {
        String t = Synt.declare(fc.get("lucene-fieldtype"), String.class);

        // 如果未指定 lucene-fieldtype 则用 field-type 替代
        if (t == null) {
            t = (String) fc.get("__type__");

            if ("stored".equals(t) || "json".equals(t)) {
                return t;
            }
            if ("search".equals(t) || "find".equals(t)) {
                return "text";
            }

            t = Synt.declare(talias.get(t) , t);
            if ("number".equals(t)) {
                t = Synt.declare(fc.get("type"), "double");
            } else
            if (  "date".equals(t)) {
                Object x = fc.get("type");
                if ("microtime".equals(x) || "timestamp".equals(x)) {
                    t = "long";
                }
            }
        }

        return t;
    }

    protected Query getQuery(Map rd) throws HongsException {
        BooleanQuery query = new BooleanQuery();

        for(Object o : rd.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            Object fv = e.getValue( );
            String fn = (String) e.getKey();

            if ("wd".equals(fn) || "ob".equals(fn) || "sf".equals(fn)
            ||  "or".equals(fn) || "ar".equals(fn) || "xr".equals(fn)) {
                continue;
            }

            Map m = (Map ) fields.get( fn );
            if (m == null) {
                continue;
            }

            AddQuery aq;
            String t = getFtype(m);
            if (   "int".equals(t)) {
                aq = new IntQuery();
            } else
            if (  "long".equals(t)) {
                aq = new LongQuery();
            } else
            if ( "float".equals(t)) {
                aq = new FloatQuery();
            } else
            if ("double".equals(t)) {
                aq = new DoubleQuery();
            } else
            if ("string".equals(t)) {
                aq = new StringQuery();
            } else
            if (  "text".equals(t)) {
                aq = new SearchQuery();
            } else
            {
                continue;
            }

            qryAdd(query, fn, fv, aq);
        }

        // 关键词
        if (rd.containsKey("wd")) {
            Object fv = rd.get("wd");
            if(fv instanceof Map) {
                Map fm = (Map) fv;
                for(String fk : findCols) {
                if (fm.containsKey( fk )) {
                    qryAdd(query, fk, fm.get(fk), new SearchQuery());
                }
                }
            } else {
                for(String fk : findCols) {
                    qryAdd(query, fk, fv/*WORD*/, new SearchQuery());
                }
            }
        }

        // 或条件
        if (rd.containsKey("or")) {
            BooleanQuery qay = new BooleanQuery();
            Set<Map> set = Synt.declare(rd.get("or"), Set.class);
            for(Map  map : set) {
                qay.add(getQuery(map), BooleanClause.Occur.SHOULD);
            }
            query.add(qay, BooleanClause.Occur.MUST);
        }

        // 附条件
        if (rd.containsKey("xr")) {
            Set<Map> set = Synt.declare(rd.get("xr"), Set.class);
            for(Map  map : set) {
                query.add(getQuery(map), BooleanClause.Occur.SHOULD);
            }
        }

        // 并条件
        if (rd.containsKey("ar")) {
            Set<Map> set = Synt.declare(rd.get("ar"), Set.class);
            for(Map  map : set) {
                query.add(getQuery(map), BooleanClause.Occur.MUST  );
            }
        }

        // 没有条件则查询全部
        if ( query.clauses( ).isEmpty( ) ) {
            return new MatchAllDocsQuery();
        }

        // 有条件无排序则按相关度排序
        if(!rd.containsKey("ob")) {
            rd.put("ob", "-");
        }

        return query;
    }

    protected Sort getSort(Map rd) throws HongsException {
        Set<String> ob;
        Object xb = rd.get("ob" );
        if (xb instanceof String)
        {
          ob = new LinkedHashSet(Arrays.asList(((String)xb).split("[, \\+]")));
        } else
        if (xb instanceof Collection)
        {
          ob = new LinkedHashSet((Collection)xb);
        } else
        {
          ob = new LinkedHashSet();
        }

        List<SortField> of = new ArrayList();

        for (String fn: ob) {
            // 相关
            if (fn.equals/**/("-") ) {
                of.add(SortField.FIELD_SCORE);
                continue;
            }

            // 逆序
            boolean rv;
            if (fn.startsWith("-") ) {
                fn = fn.substring(1);
                rv = true ;
            } else {
                rv = false;
            }

            Map m = (Map ) fields.get(fn );
            if (m == null) {
                continue;
            }
            if (!Synt.declare(m.get("sortable"), false)) {
                continue;
            }

            SortField.Type st;
            String t = getFtype(m);
            if (   "int".equals(t)
            ||    "long".equals(t)) {
                st = SortField.Type.LONG;
            } else
            if ( "float".equals(t)) {
                st = SortField.Type.FLOAT;
            } else
            if ("double".equals(t)) {
                st = SortField.Type.DOUBLE;
            } else
            if ("string".equals(t)) {
                st = SortField.Type.STRING;
            } else
            {
                continue;
            }

            /**
             * 因 Lucene 5 必须使用 DocValues 字段才能排序
             * 在构建 Document 时, 单独建立了一个 "." 打头的排序字段
             */
            of.add( new SortField("."+fn, st, rv));
        }

        if (of.isEmpty()) {
            of.add(SortField.FIELD_DOC);
        }

        return new Sort(of.toArray(new SortField[0]));
    }

    protected void ignFlds(Map rd) {
        Set<String> fs;
        Object fz = rd.get("sf" );
        if (fz instanceof String)
        {
          fs = new LinkedHashSet(Arrays.asList(((String)fz).split("[, \\+]")));
        } else
        if (fz instanceof Collection)
        {
          fs = new LinkedHashSet((Collection)fz);
        } else
        {
          fs = new LinkedHashSet();
        }

        if (!fs.isEmpty()) {
            Set<String> cf = new HashSet();
            Set<String> sf = new HashSet();
            for (String fn : fs) {
                if (fn.startsWith("-")) {
                    fn= fn.substring(1);
                    cf.add(fn);
                } else {
                    sf.add(fn);
                }
            }
            if (!sf.isEmpty()) {
                cf.addAll(fields.keySet());
                cf.removeAll(sf);
            }

            for(Map.Entry<String, Map> me : fields.entrySet()) {
                Map fc = me.getValue();
                String f = me.getKey();
                fc.put("-ignore-", cf.contains(f));
            }
        } else {
            for(Map.Entry<String, Map> me : fields.entrySet()) {
                Map fc = me.getValue();
                fc.remove("-ignore-" );
            }
        }
    }

    protected void mapAdd(Map map, Document doc) {
        for(Object o : fields.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String k = (String)e.getKey();
            Map    m = (Map )e.getValue();

            if (Synt.declare(m.get("invisble"), false)
            ||  Synt.declare(m.get("-ignore-"), false)) {
                continue;
            }

            String  t = getFtype(m);
            boolean r = Synt.declare(m.get("__repeated__"), false);
            IndexableField[] fs = doc.getFields(k);

            if (  "json".equals(t)) {
                if (r) {
                    if (fs.length > 0) {
                        for(IndexableField f : fs) {
                            Dict.put(map, Data.toObject(f.stringValue()), k, null);
                        }
                    } else {
                        map.put(k, new ArrayList());
                    }
                } else {
                    if (fs.length > 0) {
                        map.put(k, Data.toObject(fs[0].stringValue()));
                    } else {
                        map.put(k, new HashMap( ) );
                    }
                }
            } else
            if ((  "int".equals(t)
            ||    "long".equals(t)
            ||   "float".equals(t)
            ||  "double".equals(t)
            ||  "number".equals(t))
            &&  IN_OBJECT_MODE) {
                if (r) {
                    if (fs.length > 0) {
                        for(IndexableField f : fs) {
                            Dict.put(map , f.numericValue(), k, null);
                        }
                    } else {
                        map.put(k, new ArrayList());
                    }
                } else {
                    if (fs.length > 0) {
                        map.put(k, fs[0].numericValue());
                    } else {
                        map.put(k, 0 );
                    }
                }
            } else
            {
                if (r) {
                    if (fs.length > 0) {
                        for(IndexableField f : fs) {
                            Dict.put(map , f.stringValue( ), k, null);
                        }
                    } else {
                        map.put(k, new ArrayList());
                    }
                } else {
                    if (fs.length > 0) {
                        map.put(k, fs[0].stringValue( ));
                    } else {
                        map.put(k, "");
                    }
                }
            }
        }
    }

    protected void docAdd(Document doc, Map map) {
        for(Object o : fields.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            Map    m = (Map ) e.getValue();
            String k = (String) e.getKey();
            Object v = Dict.getParam(map, k);

            if (null == v) {
                continue;
            }

            String  t = getFtype(m);
            boolean u = !Synt.declare(m.get("invisble"), false);
            boolean s =  Synt.declare(m.get("sortable"), false);
            boolean r = Synt.declare(m.get("__repeated__"), false);

            doc.removeFields(k);
            if (r && v instanceof Collection) {
                for (Object x : ( Collection) v) {
                    this.docAdd(doc, k, x, t, u, s, true );
                }
            } else
            if (r && v instanceof Object[ ] ) {
                for (Object x : ( Object[ ] ) v) {
                    this.docAdd(doc, k, x, t, u, s, true );
                }
            } else
            if (r) {
                Set a = Synt.declare(v, Set.class);
                for (Object x : a) {
                    this.docAdd(doc, k, x, t, u, s, true );
                }
            } else
            {
                /**/this.docAdd(doc, k, v, t, u, s, false);
            }
        }
    }

    protected void docAdd(Document doc, String k, Object v, String t, boolean u, boolean s, boolean r) {
        if (   "int".equals(t)) {
            doc.add(new    IntField(k, Synt.declare(v, 0 ), u ? Field.Store.YES : Field.Store.NO));
        } else
        if (  "long".equals(t)) {
            doc.add(new   LongField(k, Synt.declare(v, 0L), u ? Field.Store.YES : Field.Store.NO));
        } else
        if ( "float".equals(t)) {
            doc.add(new  FloatField(k, Synt.declare(v, 0.0F), u ? Field.Store.YES : Field.Store.NO));
        } else
        if ("double".equals(t)) {
            doc.add(new DoubleField(k, Synt.declare(v, 0.0D), u ? Field.Store.YES : Field.Store.NO));
        } else
        if ("string".equals(t)) {
            doc.add(new StringField(k, Synt.declare(v, ""), u ? Field.Store.YES : Field.Store.NO));
        } else
        if (  "text".equals(t)) {
            doc.add(new   TextField(k, Synt.declare(v, ""), u ? Field.Store.YES : Field.Store.NO));
        } else
        if (  "json".equals(t)) {
            if (v == null || "".equals(v)) {
                v = "{}";
            } else
            if (! ( v instanceof String )) {
                v = Data.toString(v);
            }
            doc.add(new StoredField(k, ( String ) v));
        } else
        {
            doc.add(new StoredField(k, v.toString()));
        }

        /**
         * 针对 Lucene 5 的排序
         */
        if (s) {
            if (   "int".equals(t)
            ||    "long".equals(t)) {
                doc.add(new NumericDocValuesField("."+k, Synt.declare(v, 0L)));
            } else
            if ( "float".equals(t)) {
                doc.add(new   FloatDocValuesField("."+k, Synt.declare(v, 0.0F)));
            } else
            if ("double".equals(t)) {
                doc.add(new  DoubleDocValuesField("."+k, Synt.declare(v, 0.0D)));
            } else
            if (r) {
                doc.add(new SortedSetDocValuesField("."+k, new BytesRef(Synt.declare(v, ""))));
            } else
            {
                doc.add(new    SortedDocValuesField("."+k, new BytesRef(Synt.declare(v, ""))));
            }
        }
    }

    protected void qryAdd(BooleanQuery qry, String k, Object v, AddQuery q)
    throws HongsException {
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

        // 对 text 类型指定分词器
        if (q instanceof SearchQuery) {
            ((SearchQuery) q).ana(getAnalyzer(k));
        }

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

        Object  n, x;
        boolean l, r;

        if (m.containsKey("-gt")) {
            n = m.remove ("-gt"); l = false;
        } else
        if (m.containsKey("-ge")) {
            n = m.remove ("-ge"); l = true;
        } else
        {
            n = null; l = true;
        }

        if (m.containsKey("-lt")) {
            x = m.remove ("-lt"); r = false;
        } else
        if (m.containsKey("-le")) {
            x = m.remove ("-le"); r = true;
        } else
        {
            x = null; r = true;
        }

        if (n != null || x != null) {
            qry.add(q.add(k, n, x, l, r), BooleanClause.Occur.MUST);
        }

        //** 其他查询 **/

        if (!m.isEmpty()) {
            Set s = new HashSet();
            s.addAll(m.values( ));
            qryAdd(qry, k, s, q );
        }
    }

    protected static interface AddQuery {
        public void  bst(float  w);
        public Query add(String k, Object v);
        public Query add(String k, Object n, Object x, boolean l, boolean r);
    }

    protected static class IntQuery implements AddQuery {
        private Float w = null;
        @Override
        public void  bst(float  w) {
            this.w  = w;
        }
        @Override
        public Query add(String k, Object v) {
            Integer n2 = Synt.declare(v, Integer.class);
            Query   q2 = NumericRangeQuery.newIntRange(k, n2, n2, true, true);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        @Override
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            Integer n2 = Synt.declare(n, Integer.class);
            Integer x2 = Synt.declare(x, Integer.class);
            Query   q2 = NumericRangeQuery.newIntRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    protected static class LongQuery implements AddQuery {
        private Float w = null;
        @Override
        public void  bst(float  w) {
            this.w  = w;
        }
        @Override
        public Query add(String k, Object v) {
            Long    n2 = Synt.declare(v, Long.class);
            Query   q2 = NumericRangeQuery.newLongRange(k, n2, n2, true, true);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        @Override
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            Long    n2 = Synt.declare(n, Long.class);
            Long    x2 = Synt.declare(x, Long.class);
            Query   q2 = NumericRangeQuery.newLongRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    protected static class FloatQuery implements AddQuery {
        private Float w = null;
        @Override
        public void  bst(float  w) {
            this.w  = w;
        }
        @Override
        public Query add(String k, Object v) {
            Float   n2 = Synt.declare(v, Float.class);
            Query   q2 = NumericRangeQuery.newFloatRange(k, n2, n2, true, true);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        @Override
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            Float   n2 = Synt.declare(n, Float.class);
            Float   x2 = Synt.declare(x, Float.class);
            Query   q2 = NumericRangeQuery.newFloatRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    protected static class DoubleQuery implements AddQuery {
        private Float w = null;
        @Override
        public void  bst(float  w) {
            this.w  = w;
        }
        @Override
        public Query add(String k, Object v) {
            Double  n2 = Synt.declare(v, Double.class);
            Query   q2 = NumericRangeQuery.newDoubleRange(k, n2, n2, true, true);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        @Override
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            Double  n2 = Synt.declare(n, Double.class);
            Double  x2 = Synt.declare(x, Double.class);
            Query   q2 = NumericRangeQuery.newDoubleRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    protected static class StringQuery implements AddQuery {
        private Float w = null;
        @Override
        public void  bst(float  w) {
            this.w  = w;
        }
        @Override
        public Query add(String k, Object v) {
            Query   q2 = new TermQuery(new Term(k, v.toString()));
            if (w != null) q2.setBoost(w);
            return  q2;
        }
        @Override
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            String  n2 = n.toString();
            String  x2 = x.toString();
            Query   q2 = TermRangeQuery.newStringRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

    protected static class SearchQuery implements AddQuery {
        private Analyzer a = null;
        private Float    w = null;
        public void  ana(Analyzer a) {
            this.a  = a;
        }
        @Override
        public void  bst(  float  w) {
            this.w  = w;
        }
        @Override
        public Query add(String k, Object v) {
            try {
                Query  q2 = new QueryParser(k, a).parse(QueryParser.escape(v.toString()));
                if (w  != null) q2.setBoost(   w);
                return q2;
            } catch (ParseException ex) {
                throw HongsError.common(null, ex);
            }
        }
        @Override
        public Query add(String k, Object n, Object x, boolean l, boolean r) {
            String  n2 = n.toString();
            String  x2 = x.toString();
            Query   q2 = TermRangeQuery.newStringRange(k, n2, x2, l, r);
            if (w != null) q2.setBoost(w);
            return  q2;
        }
    }

}

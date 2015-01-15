package net.hongs.search;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.action.SourceConfig;
import app.hongs.action.VerifyHelper;
import app.hongs.util.Data;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import app.hongs.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 索引器
 * @author Hongs
 */
public class Writer {

    private IndexWriter   writer;
    private VerifyHelper  verify = null;
    private Map           vitems = null;

    public Writer() throws HongsException {
        try {
            Map map = new HashMap();
            map.put("BASE_PATH", Core.VARS_PATH);
            map.put("VARS_PATH", Core.VARS_PATH);

            String    din = CoreConfig.getInstance( ).getProperty("core.search.location", "${VARS_PATH}/search");
            Directory dir = FSDirectory.open(new File(Text.inject(din, map)));

            String    anc = CoreConfig.getInstance( ).getProperty("core.search.analyzer", "org.apache.lucene.analysis.cn.ChineseAnalyzer");
            Analyzer  ana = ( Analyzer )  Class.forName( anc ).newInstance( );

            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_CURRENT, ana);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            writer = new IndexWriter(dir, iwc);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (ClassNotFoundException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (InstantiationException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        } catch (IllegalAccessException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public void delete(String id) throws HongsException {
        try {
            writer.deleteDocuments(new Term("id", id));
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public void update(Map rd) throws HongsException {
        Document doc = new Document();

        String id = Dict.getValue(rd, "", "id");
        if ("".equals( id )) {
            id = Core.getUniqueId();
            rd.put("id", id);
        }

        if (verify == null ) {
            verify =  new VerifyHelper();
            verify.addRulesByUnit("search", "_search");
            vitems = SourceConfig.getInstance("search").getItems("_search");
        }

        rd = verify.verify(rd , true);
        //app.hongs.util.Data.dumps(rd);
        for(Object o : rd.entrySet()) {
            Map.Entry e = (Map.Entry)o;
            String k = e.getKey().toString();
            Object v = e.getValue();

            String g = Dict.getValue(vitems, String.class, k, "field_class");
            Field.Store s  =  Dict.getValue(vitems, true , k, "field_store")
                ? Field.Store.YES : Field.Store.NO;

            if (!(v instanceof Collection)) {
                docadd(doc, s, g, k, v);
            } else
            for ( Object x : ( Collection ) v) {
                docadd(doc, s, g, k, x);
            }
        }System.out.println(doc);

        try {
            writer.updateDocument (new Term("id", id), doc);
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    private void docadd(Document doc, Field.Store s, String g, String k, Object v) throws HongsException {
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

    public void commit() throws HongsException {
        try {
            writer.commit();
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public void close() throws HongsException {
        try {
            writer.close();
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

}

package app.hongs.serv.module;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.dl.lucene.LuceneRecord;
import app.hongs.util.Synt;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Document;

/**
 * 数据存储模型
 * @author Hongs
 */
public class Data extends LuceneRecord {

    public Data(String conf, String form) throws HongsException {
        super(conf, form);
    }

    /**
     * 添加文档
     * @param rd
     * @return ID
     * @throws HongsException
     */
    @Override
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
    @Override
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
        
        change(id,  rd);
    }

    /**
     * 设置文档(无则添加)
     * @param id
     * @param rd
     * @throws HongsException
     */
    public void set(String id, Map rd) throws HongsException {
        if (id == null && id.length() == 0) {
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
        
        change(id,  rd);
    }

    private void change(String id, Map rd) throws HongsException {
        StringBuilder nm = new StringBuilder();
        for (String fn : dispCols) {
            nm.append(rd.get(fn).toString()).append(' ');
        }
        
        Map dd = new HashMap();
        for(Map.Entry<String, Map> me : fields.entrySet()) {
            String fn = me.getKey();
            dd.put(fn , rd.get(fn));
        }
        
        Map nd = new HashMap();
        nd.put("id", id);
        nd.put("name", nm.toString().trim());
        nd.put("data", app.hongs.util.Data.toString(dd));
        
        Model model = DB.getInstance("module").getModel("data");
        model.create(rd);
    }
    
}

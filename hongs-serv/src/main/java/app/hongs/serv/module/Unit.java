package app.hongs.serv.module;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Mtree;
import app.hongs.db.Table;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 单元模型
 * @author Hongs
 */
public class Unit extends Mtree {
    
    public Unit() throws HongsException {
        this(DB.getInstance("module").getTable("unit"));
    }

    public Unit(Table table)
    throws HongsException {
        super(table);
    }

    /**
     * 添加/修改记录
     *
     * @param rd
     * @return 记录ID
     * @throws app.hongs.HongsException
     */
    public String save(Map rd) throws HongsException {
        String id = (String) rd.get(this.table.primaryKey);
        if (id == null || id.length() == 0) {
            id = this.add(rd);
        } else {
            this.put(rd , id);
        }

        // 建立菜单配置
        String name = (String) rd.get("name");
        if (name != null && !"".equals(name)) {
            updateOrCreateMenuSet( id, name );
        }

        return id;
    }

    public void updateOrCreateMenuSet(String id, String name) throws HongsException {
        List<Map> rows;
        
        // 1. 找出首层
        rows = this.table.fetchCase()
            .select("id,name")
            .where ("pid='0'")
            .all();
        Set<String> unitIds = new HashSet();
        for (Map  row  : rows) {
            unitIds.add(row.get("id").toString());
        }
        
        rows = this.db.getTable("form").fetchCase()
            .select("id,unit_id")
            .where ("unit_id IN (?)", unitIds)
            .all();
        Map<String, Set<String>> unitMap = new LinkedHashMap();
        for (Map  row  : rows) {
            String formId = row.get(/**/ "id").toString();
            String unitId = row.get("unit_id").toString();
            Set formIds;
            if (unitMap.containsKey(unitId)) {
                formIds=unitMap.get(unitId);
            } else {
                formIds=new LinkedHashSet();
            }
            formIds.add(formId);
        }
        
        // 1. 查找首层单元 id
        
        // 2. 构建 document 将首层单元加入 menu
        
        // 3. 查找首层下的全部 form 并 include 其 menu 配置
        
        // 4. 
        
        
         Document docm = makeDocument();

        Element root = docm.createElement("root");
        docm.appendChild ( root );

        Element menu;
        
        menu = docm.createElement("menu");
        root.appendChild ( menu );
        menu.setAttribute("disp", name);
        menu.setAttribute("href", "common/menu/cell.act?m=hongs/module/unit/"+id);
        
        List<String> pids = this.getParentIds(id);
        
        FetchCase fc = new FetchCase();
        fc.select("id,name")
          .where ("unit_id = ?", id);
        List<Map> fl = this.db.getTable("form").fetchMore(fc);
        
        
        
        saveDocument(id+".menu.xml", docm);
    }
    
    private Document makeDocument() throws HongsException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder        builder = factory.newDocumentBuilder();
            return  builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw HongsException.common(null, e);
        }
    }
    
    private void saveDocument(String name, Document docm) throws HongsException {
        File file = new File(Core.CONF_PATH+"/hongs/module/unit/"+name);
        if (!file.getParentFile().exists()) {
             file.getParentFile().mkdirs();
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer tr = tf.newTransformer();
            DOMSource   ds = new DOMSource(docm);
            tr.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            tr.setOutputProperty(OutputKeys.INDENT  , "yes"  );

            PrintWriter  pw = new PrintWriter(new FileOutputStream(file));
            StreamResult sr = new StreamResult(pw);
            tr.transform(ds, sr);
        } catch (TransformerConfigurationException e) {
            throw HongsException.common(null, e);
        } catch (IllegalArgumentException e) {
            throw HongsException.common(null, e);
        } catch (TransformerException  e) {
            throw HongsException.common(null, e);
        } catch (FileNotFoundException e) {
            throw HongsException.common(null, e);
        }
    }

}

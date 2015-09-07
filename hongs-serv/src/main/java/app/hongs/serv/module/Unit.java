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
import java.util.List;
import java.util.Map;
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
 *
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

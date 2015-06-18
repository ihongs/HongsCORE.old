package app.hongs.serv.module;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.db.Table;
import app.hongs.util.Data;
import app.hongs.util.Synt;

import java.util.Map;
import java.util.List;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

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
public class Form extends Model {

    public Form() throws HongsException {
        this(DB.getInstance("module").getTable("form"));
    }

    public Form(Table table)
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
      
      // 建立表单配置
      String ud = (String) rd.get("unit_id");
      String cf = (String) rd.get("conf"   );
      if (ud != null && !"".equals(ud) && cf != null && !"".equals(cf)) {
        List fs = Synt.declare(Data.toObject(cf), List.class);
        updateOrCreateFormSet ( ud, id, fs );
      }
      
      return id;
    }

    public void updateOrCreateFormSet(String ud, String id, List<Map> fields) throws HongsException {
        Document docm;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder        builder = factory.newDocumentBuilder();
            docm = builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw HongsException.common(null, e);
        }

        Element root = docm.createElement("root");
        docm.appendChild(root);
        
        Element form = docm.createElement("form");
        form.setAttribute("name", id);
        root.appendChild (form);
        
        for (Map field : fields) {
            Element item = docm.createElement("field");
            String s;
            s = (String) field.get("__disp__");
            item.setAttribute("disp", s);
            s = (String) field.get("__name__");
            item.setAttribute("name", s);
            s = (String) field.get("__type__");
            item.setAttribute("type", s);
            s = Synt.declare(field.get("__required__"), "");
            item.setAttribute("type", s);
            s = Synt.declare(field.get("__repeated__"), "");
            item.setAttribute("type", s);
            for (Object   ot : field.entrySet( )) {
                Map.Entry et = (Map.Entry) ot;
                String k = (String) et.getKey(  );
                String v = (String) et.getValue();
                if (k.startsWith("__")) {
                    continue;
                }
                Element para = docm.createElement("param");
                para.setAttribute("name", k);
                para.setNodeValue(v);
            }
        }

        File file = new File(Core.CONF_PATH+"/hongs/module/auto/"+ud+"/"+id+".form.xml");
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

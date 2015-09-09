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
import java.util.ArrayList;

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

        // 建立菜单配置
        String name = (String) rd.get("name");
        if (name != null && !"".equals(name)) {
            updateOrCreateMenuSet( id, name );
        }

        // 建立表单配置
        String conf = (String) rd.get("conf");
        if (conf != null && !"".equals(conf)) {
            List flds = Synt.declare(Data.toObject(conf), List.class);
            updateOrCreateFormSet( id, flds );
        }
        
        return id;
    }

    public void updateOrCreateMenuSet(String id, String name) throws HongsException {
        Document docm = makeDocument();

        Element root = docm.createElement("root");
        docm.appendChild ( root );

        Element menu = docm.createElement("menu");
        root.appendChild ( menu );
        menu.setAttribute("disp", name);
        menu.setAttribute("href", "hongs/module/data/#"+id);
        menu.setAttribute("page", "hongs/module/data/" +id+"/main.html");
        
        Element role, actn, depn;
        
        // 查看
        
        role = docm.createElement("menu");
        menu.appendChild ( role );
        role.setAttribute("name", "hongs/module/data/" +id+"/retrieve");
        role.setAttribute("disp", "查看"+name);
        
        actn = docm.createElement("action");
        role.appendChild ( actn );
        actn.setNodeValue("hongs/module/data/"+id+"/retrieve.act");
        
        // 添加
        
        role = docm.createElement("menu");
        menu.appendChild ( role );
        role.setAttribute("name", "hongs/module/data/" +id+"/create");
        role.setAttribute("disp", "添加"+name);
        
        actn = docm.createElement("action");
        role.appendChild ( actn );
        actn.setNodeValue("hongs/module/data/"+id+"/create.act");
        
        depn = docm.createElement("depend");
        role.appendChild ( depn );
        depn.setNodeValue("hongs/module/data/"+id+"/retrieve");
        
        // 修改
        
        role = docm.createElement("menu");
        menu.appendChild ( role );
        role.setAttribute("name", "hongs/module/data/" +id+"/update");
        role.setAttribute("disp", "修改"+name);
        
        actn = docm.createElement("action");
        role.appendChild ( actn );
        actn.setNodeValue("hongs/module/data/"+id+"/update.act");
        
        depn = docm.createElement("depend");
        role.appendChild ( depn );
        depn.setNodeValue("hongs/module/data/"+id+"/retrieve");
        
        // 删除
        
        role = docm.createElement("menu");
        menu.appendChild ( role );
        role.setAttribute("name", "hongs/module/data/" +id+"/delete");
        role.setAttribute("disp", "删除"+name);
        
        actn = docm.createElement("action");
        role.appendChild ( actn );
        actn.setNodeValue("hongs/module/data/"+id+"/delete.act");
        
        depn = docm.createElement("depend");
        role.appendChild ( depn );
        depn.setNodeValue("hongs/module/data/"+id+"/retrieve");
        
        // 保存
        
        saveDocument(id+".form.xml", docm);
    }
    
    public void updateOrCreateFormSet(String id, List<Map> conf) throws HongsException {
        Document docm = makeDocument();

        Element root = docm.createElement("root");
        docm.appendChild ( root );

        Element form = docm.createElement("form");
        root.appendChild ( form );
        form.setAttribute("name", id);

        for (  Map  fiel : conf ) {
            Element item = docm.createElement("field");
            form.appendChild ( item );
            String s;
            s = (String) fiel.get("__disp__");
            item.setAttribute("disp", s);
            s = (String) fiel.get("__name__");
            item.setAttribute("name", s);
            s = (String) fiel.get("__type__");
            item.setAttribute("type", s);
            s = Synt.declare(fiel.get("__required__"), "");
            item.setAttribute("type", s);
            s = Synt.declare(fiel.get("__repeated__"), "");
            item.setAttribute("type", s);

            for (Object   ot : fiel.entrySet( )) {
                Map.Entry et = (Map.Entry) ot;
                String k = (String) et.getKey(  );
                String v = (String) et.getValue();

                // 忽略基础参数
                if (k.startsWith("__")) {
                    continue;
                }

                // 构建枚举列表
                if (k.equals("datalist")) {
                    Element anum = docm.createElement("enum");
                    root.appendChild ( anum );
                    anum.setAttribute("name", k );
                    Object x = Data.toObject( v );
                    List de = new ArrayList(/**/);
                    List dl = Synt.declare(x, de);
                    for (Object o : dl) {
                        List di = Synt.declare(o, de);
                        Element valu = docm.createElement("value");
                        anum.appendChild ( valu );
                        valu.setAttribute("code", Synt.declare(di.get(0), ""));
                        valu.setNodeValue(/*txt*/ Synt.declare(di.get(1), ""));
                    }
                    continue;
                }

                // 识别搜索类型
                if (k.equals("findable")) {
                    Element para = docm.createElement("param");
                    item.appendChild ( para );
                    para.setAttribute("name", "lucene-fieldtype");
                    para.setNodeValue(/*val*/ "search" /*fiel*/);
                }

                Element para = docm.createElement("param");
                item.appendChild ( para );
                para.setAttribute("name", k);
                para.setNodeValue(/*val*/ v);
            }
        }

        saveDocument(id+".form.xml", docm);
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
        File file = new File(Core.CONF_PATH+"/hongs/module/data/"+name);
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